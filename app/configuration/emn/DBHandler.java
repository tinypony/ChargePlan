package configuration.emn;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import model.BusStop;
import model.BusTrip;
import model.ScheduleStop;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.mongodb.BasicDBObject;


public class DBHandler extends DefaultHandler {
	private Map<String, BusStop> stops;
	private ArrayList<BusTrip> buses;

	private Map<String, BasicDBObject> validityDays;
	private List<String> allowedModes = Arrays.asList("1", "3", "4", "5", "25");

	private String deliveryStart = "";
	private Stack<String> elementStack = new Stack<String>();
	private Stack<Object> objectStack = new Stack<Object>();
	

	public DBHandler(String route, String company) {
		this.stops = new HashMap<String, BusStop>();
		this.buses = new ArrayList<BusTrip>();
		this.validityDays = new HashMap<String, BasicDBObject>();
	}

	public List<BusTrip> getBuses() {
		return this.buses;
	}

	public Map<String, BusStop> getStops() {
		return this.stops;
	}

	public boolean isReal(Attributes atts) {
		String type = atts.getValue("type");
		String isVirtual = atts.getValue("isVirtual");


		if (type == null && isVirtual == null) {
			return true;
		}

		return (type != null && "0".equals(type)) || (isVirtual != null && "false".equals(isVirtual));
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		String qNameLow = qName.toLowerCase();
		this.elementStack.push(qName);

		if("footnote".equalsIgnoreCase(qName)) {
			BasicDBObject footnote = new BasicDBObject();
			footnote.append("vector", attributes.getValue("Vector"));
			
			if(attributes.getValue("Firstdate") != null) {
				footnote.append("firstDate", attributes.getValue("Firstdate"));
			} else {
				footnote.append("firstDate", deliveryStart);
			}
			
			validityDays.put(attributes.getValue("FootnoteId"), footnote);
			
		} else if("delivery".equalsIgnoreCase(qName)) {
			deliveryStart = attributes.getValue("Firstday");
			
		} else if ("station".equals(qName.toLowerCase())) {
			
			if (isReal(attributes) && attributes.getValue("X") != null && attributes.getValue("Y") != null) {
				BusStop stop = new BusStop(attributes);
				this.objectStack.push(stop);
				this.stops.put(stop.getStopId(), stop);
			}
			
		} else if ("service".equals(qName.toLowerCase())) {
			
			BusTrip bus = new BusTrip();
			bus.setServiceID(attributes.getValue("ServiceId"));
			this.objectStack.push(bus);
			
		} else if ("servicenbr".equals(qNameLow)) {
			String routeNbr = attributes.getValue("Variant");
			String serviceNbr = attributes.getValue("ServiceNbr");
			String companyId = attributes.getValue("CompanyId");
			
			
			if (routeNbr != null) {
				BusTrip bus = (BusTrip) this.objectStack.peek();
				bus.setCompany(companyId);
				bus.setRoute(routeNbr);
				bus.setRouteId(serviceNbr);
			}
			
		} else if ("stop".equals(qNameLow)) {
			BusTrip bus = (BusTrip) this.objectStack.peek();
			
			try {
				ScheduleStop stop = new ScheduleStop(attributes, this.getStops());
				bus.addStop(stop);
			} catch(Exception e) {
				System.out.println("Error parsing stop");
			}
			
		} else if ("servicetrnsmode".equals(qNameLow)) {
			BusTrip bus = (BusTrip) this.objectStack.peek();
			bus.setTrnsmode(attributes.getValue("TrnsmodeId"));
			
		} else if("servicevalidity".equalsIgnoreCase(qName)) {
			BusTrip bus = (BusTrip) objectStack.peek();
			String footnoteid = attributes.getValue("FootnoteId");
			bus.setFootnoteId (footnoteid);
			BasicDBObject validity = validityDays.get(footnoteid);
			bus.setFirstDate(validity.getString("firstDate"));
			bus.setVector(validity.getString("vector"));
		}
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		this.elementStack.pop();
		String qNameLow = qName.toLowerCase();
		
		if ("service".equals(qNameLow)) {
			BusTrip bus = (BusTrip) this.objectStack.pop();
			
			if(filter(bus)) {
				this.buses.add(bus);
			}
		}
	}
	
	public Map<String, BasicDBObject> getValidityDays() {
		return this.validityDays;
	}

	private boolean filter(BusTrip bus) {
		return allowedModes.contains(bus.getTrnsmode()); //bus.getRoute().equals(this.route) && this.company.equals(bus.getCompany());
	}
}