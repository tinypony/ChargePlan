package utils;

import java.net.UnknownHostException;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import play.Logger;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class MongoUtils {
	private static boolean configured = false;
	private static MongoClient mongoClient;
	private static DB db;
	private static String dbName;
	private static Datastore datastore;
	
	private static final String DB_NAME_KEY = "mongo.db.name";
	
	static {
		configure();
	}
	
	public static void configure() {
		if(configured) {
			return;
		}
		
		try {
			String configuredDbName = play.Configuration.root().getString(DB_NAME_KEY);
			Logger.info("Found "+configuredDbName);
			 
			setDBName(configuredDbName);
			mongoClient = new MongoClient( "localhost" );
			db = getClient().getDB( dbName );			
			datastore = new Morphia().createDatastore(mongoClient, dbName);
			configured = true;
		} catch(UnknownHostException e) {
			throw new RuntimeException("Cannot proceed without DB. Abort",  e);
		}
	}
	
	public static void setDBName(String dbName) {
		MongoUtils.dbName = dbName;
	}
	
	public static MongoClient getClient() throws UnknownHostException {
		if(mongoClient == null) {
			mongoClient = new MongoClient( "localhost" );
		}
		
		return mongoClient;
	}
	
	public static DB getDB() throws UnknownHostException {
	    return getDB(MongoUtils.dbName);
	}
	
	public static DB getDB(String dbName) throws UnknownHostException {
		if(db == null) {
			db = getClient().getDB( dbName );
		}		
		return db;
	}
	
	public static Datastore ds() {
		return datastore;
	}
}
