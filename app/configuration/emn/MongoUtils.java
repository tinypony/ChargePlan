package configuration.emn;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class MongoUtils {
	private static MongoClient mongoClient;
	private static DB db;
	private static String dbName;
	
	public static void setDBName(String dbName) {
		MongoUtils.dbName = dbName;
	}
	
	public static DB getDB() throws UnknownHostException {
	    return getDB(MongoUtils.dbName);
	}
	
	public static DB getDB(String dbName) throws UnknownHostException {
		if(mongoClient == null) {
			mongoClient = new MongoClient( "localhost" );
		}
		
		if(db == null) {
			db = mongoClient.getDB( dbName );
		}
		
		return db;
	}
}
