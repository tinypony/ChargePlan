package actors.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import model.ClientConfig;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;

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
		//	this.retrieveRouteLength();
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

			System.out.println("file unzip : " + newFile.getAbsoluteFile());

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

	public void retrieveRouteLength() throws IOException, InterruptedException {
		MongoUtils.setDBName(databaseName);
		DBCollection coll = MongoUtils.getDB().getCollection("trips");
		
		Cursor tripsCurs = coll.find();

		float i = 0;

		while (tripsCurs.hasNext()) {
			DBObject trip = tripsCurs.next();

			// Skip already processed routes
			if (trip.get("tripLength") == null
					|| (Integer) trip.get("tripLength") == 0) {

				int lengthInMeters;

				try {
					lengthInMeters = DistanceRetriever.getRouteLength(trip);
				} catch (IllegalStateException e) { // results from exceeding
													// google api request quota,
													// try to use hashed
													// distances instead
					continue;
				}

				BasicDBObject update = new BasicDBObject("$set",
						new BasicDBObject("tripLength", lengthInMeters));

				WriteResult result = coll.update(trip, update, false, false);
			}

			i += 1.0;
			System.out.print("Done " + i + "/" + coll.count() + "("
					+ (i / coll.count()) * 100 + "%)            \r");
		}
	}

	public List<String> getDistinctRoutes() throws UnknownHostException {
		DBCollection coll = MongoUtils.getDB().getCollection("trips");
		return coll.distinct("serviceNbr");
	}

	public DBObject getBus(String route) throws UnknownHostException {
		DBCollection coll = MongoUtils.getDB().getCollection("trips");
		BasicDBObject query = new BasicDBObject();
		query.append("serviceNbr", route);
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
