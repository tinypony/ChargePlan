package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

import configuration.emn.MongoUtils;
import configuration.emn.RuterGTFSHandler;
import configuration.emn.route.DistanceRetriever;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;

public class ScheduleImport extends Controller {

	private static String databaseName = "ruter";
	private static String OUTPUT_FOLDER = "./tmp";

	public static Result uploadGtfs() throws IOException {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart archive = body.getFile("gtfs-archive");

		if (archive != null) {
			String fileName = archive.getFilename();
			String contentType = archive.getContentType();
			
			File file = archive.getFile();
			String gtfsFolder = unzipArchive(file);
			importGtfs(gtfsFolder);
			return ok("Uploaded");
		} else {
			flash("error", "Missing file");
			return badRequest("Could not upload file.");
		}
	}

	public static String unzipArchive(File file) throws IOException {
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

	public static void importGtfs(String path) throws IOException {
		RuterGTFSHandler ruterHandler = new RuterGTFSHandler(path);
		ruterHandler.parseAndDump();
	}

	public void retrieveRouteLength() throws IOException, InterruptedException {
		MongoUtils.setDBName(databaseName);
		DBCollection coll = MongoUtils.getDB().getCollection("trips");
		Cursor tripsCurs = coll.find();

		float i = 0;

		while (tripsCurs.hasNext()) {
			DBObject bus = tripsCurs.next();

			// Skip already processed routes
			if (bus.get("tripLength") == null
					|| (Integer) bus.get("tripLength") == 0) {

				int lengthInMeters;

				try {
					lengthInMeters = DistanceRetriever.getRouteLength(bus);
				} catch (IllegalStateException e) { // results from exceeding
													// google api request quota,
													// try to use hashed
													// distances instead
					continue;
				}

				BasicDBObject update = new BasicDBObject("$set",
						new BasicDBObject("tripLength", lengthInMeters));

				WriteResult result = coll.update(bus, update, false, false);
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
}
