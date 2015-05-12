import model.ClientConfig;

import org.emn.plan.model.PlanningProject;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import play.*;
import utils.MongoUtils;

public class Global extends GlobalSettings {

    public void onStart(Application app) {
        MongoUtils.configure();
        Datastore ds = MongoUtils.ds();
        Query<ClientConfig> query = ds.find(ClientConfig.class);
        ClientConfig config = query.get();
        
        if(config == null) {
        	config = new ClientConfig();
        	ds.save(config);
        }
        
        //Automatically create test project to work with
   
        	if(MongoUtils.ds().createQuery(PlanningProject.class).asList().size() == 0){
        		PlanningProject project = new PlanningProject();
        		project.setName("TestProject");
        		project.setLocation("Oslo, Norway");
        		MongoUtils.ds().save(project);
        		config.setRecentProject(project.getId().toHexString());
        		ds.save(config);
        	} else {
        		Logger.info("Found existing projects");
        	}
        
    }

    public void onStop(Application app) {
        Logger.info("Application shutdown...");
    }

}