import model.ClientConfig;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import play.*;
import utils.JPAUtils;
import utils.MongoUtils;

public class Global extends GlobalSettings {

    public void onStart(Application app) {
        Logger.info("Application has started");
        MongoUtils.configure();
        JPAUtils.init();
        Datastore ds = MongoUtils.ds();
        Query<ClientConfig> query = ds.find(ClientConfig.class);
        ClientConfig config = query.get();
        
        if(config == null) {
        	Logger.info("Creating config");
        	config = new ClientConfig();
        	ds.save(config);
        }
        
    }

    public void onStop(Application app) {
        Logger.info("Application shutdown...");
    }

}