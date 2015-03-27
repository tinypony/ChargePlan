import play.*;
import utils.MongoUtils;

public class Global extends GlobalSettings {

    public void onStart(Application app) {
        Logger.info("Application has started");
        MongoUtils.configure();
    }

    public void onStop(Application app) {
        Logger.info("Application shutdown...");
    }

}