package model;
import org.onebusaway.gtfs.model.Stop;
import org.xml.sax.Attributes;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

@Embedded
@Entity
public class BusStop {
	private String stopId;
    private String name;
    private String x;
    private String y;
	private boolean isFirst;
	private boolean isLast;
	
	public BusStop(){
		
	}
    
    public BusStop(Attributes atts) {
        String name = atts.getValue("Name");
        String id = atts.getValue("StationId");
        String x = atts.getValue("X");
        String y = atts.getValue("Y");
        
        if(name != null) {
            this.setName(name);
        }
        
        if(id !=null ) {
            this.setStopId(id);
        } else {
        	throw new IllegalArgumentException("Id canot be null");
        }
        
        if(x !=null) {
            this.setX(x);
        }
        
        if(y !=null) {
            this.setY(y);
        }
    }
    
    public BusStop(Stop stop) {
    	this.setStopId(stop.getId().getId());
    	this.setName(stop.getName());
    	this.setY(""+stop.getLat());
    	this.setX(""+stop.getLon());
    }
    
    public BusStop(String id, String name, String x, String y) {
        this.setStopId(id);
        this.setName(name);
        this.setX(x);
        this.setY(y);
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String id) {
        this.stopId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

	public boolean isFirst() {
		return isFirst;
	}

	public void setFirst(boolean isFirst) {
		this.isFirst = isFirst;
	}

	public boolean isLast() {
		return isLast;
	}

	public void setLast(boolean isLast) {
		this.isLast = isLast;
	}
}
