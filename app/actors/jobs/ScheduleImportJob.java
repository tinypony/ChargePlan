package actors.jobs;

import static org.mongodb.morphia.aggregation.Group.first;
import static org.mongodb.morphia.aggregation.Group.grouping;
import static org.mongodb.morphia.aggregation.Group.id;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import model.ClientConfig;
import model.dataset.BusRoute;
import model.dataset.BusStop;
import model.dataset.BusTrip;
import model.dataset.BusTripGroup;
import model.dataset.DayStat;
import model.dataset.ScheduleStop;
import model.dataset.Waypoint;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.aggregation.Accumulator;
import org.mongodb.morphia.aggregation.AggregationPipeline;
import org.mongodb.morphia.aggregation.Group;
import org.mongodb.morphia.aggregation.Projection;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.EntityCache;
import org.mongodb.morphia.query.MorphiaIterator;
import org.mongodb.morphia.query.Query;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

import configuration.emn.RuterGTFSHandler;
import configuration.emn.route.DistanceRetriever;
import dto.jobstate.ScheduleImportJobState;
import dto.message.StartImportMessage;
import play.Logger;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import utils.MongoUtils;


public class ScheduleImportJob extends UntypedActor {

	private static String databaseName = "ruter";
	private static String OUTPUT_FOLDER = "./tmp";

	private ScheduleImportJobState state;
	private MultipartFormData body;

	public static Props props(final ScheduleImportJobState job,
			final MultipartFormData body) {
		return Props.create(new Creator<ScheduleImportJob>() {
			private static final long serialVersionUID = 1L;

			@Override
			public ScheduleImportJob create() throws Exception {
				return new ScheduleImportJob(job, body);
			}
		});
	}

	public ScheduleImportJob(ScheduleImportJobState job, MultipartFormData body) {
		this.state = job;
		this.body = body;
	}

	// @Override
	public void runJob() throws IOException, InterruptedException {
		this.startHistory();
		this.state.uploaded(getSelf());

		FilePart archive = body.getFile("gtfs-archive");

		if (archive != null) {

			File file = archive.getFile();
			String gtfsFolder = this.unzipArchive(file);

			this.state.unzipped(getSelf());

			this.importGtfs(gtfsFolder, this.state);
			this.resolveLengths(this.state);
			this.augmentRoutes();
			this.state.distancesResolved(getSelf());
			this.saveHistory(true);
			getContext().stop(getSelf());
		} else {
			this.saveHistory(false);
		}
	}
	
	private void startHistory() {
		Datastore ds = MongoUtils.ds();
        Query<ClientConfig> query = ds.find(ClientConfig.class);
        ClientConfig config = query.get();
        config.getSchedules().setInProgress(true);
        ds.save(config);
	}

	
	private void saveHistory(boolean isSuccess) {
		Datastore ds = MongoUtils.ds();
        Query<ClientConfig> query = ds.find(ClientConfig.class);
        ClientConfig config = query.get();
        config.getSchedules().setImported(isSuccess);
        config.getSchedules().setDate(new Date());
        config.isSufficient();
        ds.save(config);
	}

	public String unzipArchive(File file) throws IOException {
		String outputFolder = OUTPUT_FOLDER;
		byte[] buffer = new byte[1024];

		File folder = new File(outputFolder);
		if (!folder.exists()) {
			folder.mkdir();
		}

		// get the zip file content
		ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
		// get the zipped file list entry
		ZipEntry ze = zis.getNextEntry();

		while (ze != null) {

			String fileName = ze.getName();
			File newFile = new File(outputFolder + File.separator + fileName);

			//System.out.println("file unzip : " + newFile.getAbsoluteFile());

			// create all non exists folders
			// else you will hit FileNotFoundException for compressed folder
			new File(newFile.getParent()).mkdirs();

			FileOutputStream fos = new FileOutputStream(newFile);

			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}

			fos.close();
			ze = zis.getNextEntry();
		}

		zis.closeEntry();
		zis.close();

		return OUTPUT_FOLDER;
	}

	public void importGtfs(String path, ScheduleImportJobState job)
			throws IOException {
		RuterGTFSHandler ruterHandler = new RuterGTFSHandler(path);

		Logger.info("Parsing data");
		ruterHandler.parseInput();
		job.read(getSelf());
		
		Logger.info("Data has been parsed");
		ruterHandler.dumpData();
		job.imported(getSelf());
		Logger.info("Data has been imported");
	}
	
	public void resolveLengths(ScheduleImportJobState state2) throws IOException, InterruptedException {
		MongoUtils.setDBName(databaseName);
		Datastore ds = MongoUtils.ds();
		DBCollection coll = ds.getCollection(BusTrip.class);
		AggregationPipeline<BusTrip, BusTripGroup> ap = ds.createAggregation(BusTrip.class);
		
		List<Group> idGroup = id(grouping("routeId"), 
				grouping("direction"), grouping("numOfStops"));

		ap.group(idGroup, grouping("id", first("id")), 
				grouping("routeId", first("routeId")), 
				grouping("direction", first("direction")),
				grouping("stops", first("stops")));
		
		MorphiaIterator<BusTripGroup, BusTripGroup> iterator = ap.aggregate(BusTripGroup.class);
		
		List<BusTripGroup> distinctTrips = Lists.newArrayList(iterator.iterator());
		Iterator<BusTripGroup> it = distinctTrips.iterator();
		int i = 0;
		
		while(it.hasNext()) {
			BusTripGroup oneTrip = it.next();
			int lengthInMeters;

			try {
				List<ScheduleStop> stops = oneTrip.getStops();
				lengthInMeters = DistanceRetriever.getRouteLength(stops);
			//	Logger.info("Route "+ oneTrip.getRouteId() +" length " + lengthInMeters);
			} catch (IllegalStateException e) { // results from exceeding
												// google api request quota,
												// try to use hashed
												// distances instead
				continue;
			}

			BasicDBObject query = new BasicDBObject("routeId", oneTrip.getRouteId());
			query.put("direction", oneTrip.getDirection());
			query.put("stops", new BasicDBObject("$size", oneTrip.getStops().size()));
			
			BasicDBObject update = new BasicDBObject("$set",
					new BasicDBObject("tripLength", lengthInMeters));

			WriteResult result = coll.update(query, update, false, true);
			i ++;
			
			float progress = (i / (float) distinctTrips.size()) * 100;
			
			state2.setStateProgress(progress);
			state2.publishChange(getSelf());
			
			System.out.print("Done " + i + "/" + distinctTrips.size() + "("
					+ progress + "%)            \r");
		}
	}

	public void augmentRoutes() {
		Datastore ds = MongoUtils.ds();
		
		Query<BusRoute> q = ds.createQuery(BusRoute.class);
		List<BusRoute> routes =  q.asList();
		
		for(BusRoute r: routes) {
			HashMap<String, DayStat> stats = new HashMap<String, DayStat>();
			
			Query<BusTrip> qr = ds.createQuery(BusTrip.class);
			List<BusTrip> trips = qr.field("routeId").equal(r.getRouteId()).asList();
			r.setWaypoints(this.getWaypoints(trips.get(0).getStops()));
			
			for(BusTrip trip: trips) {
				for(String tripDate: trip.getDates()) {
					DayStat st = stats.get(tripDate);
					if(st == null) {
						st = new DayStat();
						st.setDate(tripDate);
						stats.put(tripDate, st);
					}
					
					st.incrementDepartures();
					st.incrementTotalDistance(trip.getTripLength());
				}
			}
			List<DayStat> list = new ArrayList<DayStat>();
			list.addAll(stats.values());
			r.setStats(list);
		}
		ds.save(routes);
	}
	
	private List<Waypoint> getWaypoints(List<ScheduleStop> stops) {
		Collections.sort(stops);
		
		List<Waypoint> waypoints = Lists.transform(stops, new Function<ScheduleStop, Waypoint>(){
			public Waypoint apply(ScheduleStop s) {
				return new Waypoint(s.getOrder(), s.getStop().getStopId());
			}
		});
		
		return waypoints;
	}

	public List<String> getDistinctRoutes() throws UnknownHostException {
		DBCollection coll = MongoUtils.getDB().getCollection("trips");
		return coll.distinct("routeId");
	}

	public DBObject getBus(String route) throws UnknownHostException {
		DBCollection coll = MongoUtils.getDB().getCollection("trips");
		BasicDBObject query = new BasicDBObject();
		query.append("routeId", route);
		DBObject bus = coll.findOne(query);
		return bus;
	}

	public ScheduleImportJobState getState() {
		return state;
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof StartImportMessage) {
			this.runJob();
		}

	}

}
