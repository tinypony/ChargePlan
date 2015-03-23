package models;

import com.mongodb.BasicDBObject;

public interface Mongoable {
	public BasicDBObject toMongoObj();
}
