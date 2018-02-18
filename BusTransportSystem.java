import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//@Invariant({hour>=0, hour<=23})
class Time implements Runnable {
	static float time = 0;
	float hour = 0;
	
	//@Requires({"hour!=null"})
	//@Ensures({"hour==time/5"})
	public float getHour() {
		hour = time / 5;
		return hour;
	}

	//@Requires({"time!=null"})
	public Time() {
		Thread timeThread = new Thread();
		timeThread.start();
	}

	@Override
	//@Ensures({"time>=0", "time<=120"})
	public void run() {
		while (true) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (time == 120) {
				centralControlSystem.displayFareCollected();
				time = 0;
			} else {
				time = time + 1;
			}
		}
	}
}

//@Invariant({"Stop != null"})
class Route {
	String name;
	
	//@Requires({"Stops.size()>0"}
	public Route(Stops[] s, String name) {
		this.name = name;
		for (int i = 0; i < s.length; i++) {
			this.routes.add(s[i]);
		}
	}

	ArrayList<Stops> routes = new ArrayList<Stops>();
}

/*
 * class DB { public DB(Driver d, Schedule sc){ this.driver = d; this.schedule =
 * sc; } Driver driver; Schedule schedule;
 * 
 * }
 */
class Driver {
	public Driver(String name) {
		this.drivername = name;
	}

	String drivername;
	
	public static void PlyadditionalBus(Route request, double time)
	{
		centralControlSystem.AddScheduleForOverCrowdRequest(request,time);
	}
}
//@Invariant({"route!=null","start!=null"})
class Schedule {
	//@Requires({"route!=null","start >=0","start<=23"})
	public Schedule(Route route, double start,String type) {
		this.route = route;
		this.starttime = start;
		this.type=type;
	}

	Route route;
	double starttime;
	String type;
}

class scheduleManager implements Runnable {
	Time time;
	static Schedule[] schedule;
	centralControlSystem ccs;

	//@Requires({"time>=0", "time<=120", "s.length>=0"})
	public scheduleManager(Schedule[] s, Time t) {
		schedule = s;
		time = t;
	}

	public static void UpdateSchedule(Schedule[] s) {
		schedule = s;
	}
	
	public void run() {
		
		System.out.println("In run of schedule manager");
		bus b;
		Driver d;
		ArrayList<Thread> trips = new ArrayList<Thread>();
		while (true) {
			for (int i = 0; i < schedule.length; i++) {
				 //System.out.println("Schedule: "+schedule[i].starttime+" time.gethour" + time.getHour());
				// "+time.getHour());
				if (schedule[i].starttime == time.getHour()) {
					System.out.println("Scheduled Trip For Bus :" + (i+1));
					b = centralControlSystem.getBus();
					if (b == null) {
						System.out.println("No buses available");
						continue;
					}
					d = centralControlSystem.getDriver();
					if (d == null) {
						System.out.println("No drivers available");
						continue;
					}
					startTrip trip = new startTrip(schedule[i], b, d);
					trips.add(new Thread (trip, "trip"));
					System.out.println("Starting trip");
					trips.get(trips.size()-1).start();
			}
				
		}
			 try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

class startTrip implements Runnable {
	bus b1;
	Driver d1;
	Schedule s1;

	//@Requires({"b1!=null","d1!=null","s1!=null"})
	//@Ensures({"b1.route == s1.route"})
	public startTrip(Schedule s, bus b, Driver d) {
		b1 = b;
		d1 = d;
		s1 = s;
		b1.updateRoute(s1.route);
	}
	
	
	@Override
	public void run() {
		System.out.println(
				"Route Size:" + s1.route.routes.size() + "  Bus operates From " + s1.route.routes.get(0).name + " to " + s1.route.routes.get(1).name);
		for (int i = 0; i < s1.route.routes.size(); i++) {
			System.out.println("Bus: " + b1.licensePlateNo + " being driven by: " + d1.drivername + " is at stop: "
					+ b1.returnLocation().name + " next stop: " + (i+1));
			b1.updateLocation(s1.route.routes.get(i));

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(s1.type=="Additional")
		{
			centralControlSystem.RemoveBusSchedule(s1);
		}
		b1.updateTripCount();
		centralControlSystem.fareCollected += b1.fc.tripFare;
		centralControlSystem.addVehicleToAvailableList(b1.licensePlateNo);
		centralControlSystem.addDriverToAvailableList(d1);
		
	}
}

//@Invariant({"name!= null","crowdStatus!=null"})
class Stops {
	String name;
	String crowdStatus;

	//@Requires{"name=["Ellicott Tunnel", "Maynard","Lee Loop", "Goodyear","South Campus","Garage","Flint Loop","Governors"]", "crowdStatus=["Normal","Crowded"]"}
	public Stops(String name) {
		this.name = name;
		this.crowdStatus = "Normal";
	}
}

interface vehicles {
	public int getTripCount();

	public void resetTripCount();
}

interface employees {
	public void checkMaintenanceStatus();

	public void returnVehicleForService(Integer vehicle_number);
}

//@Invariant({"licensePlateNo!=null","currentLocation!=null"})
class bus implements vehicles {
	public Route route = null;
	int busTime = 0;
	Stops currentLocation = centralControlSystem.stops[8];

	public bus(Integer bus_number) {
		// TODO Auto-generated method stub
		// this.database = centralControlSystem.BusInfo(bus_number);
		licensePlateNo = bus_number;
		this.tripCount = centralControlSystem.returnTripCount(bus_number);
	}

	public void updateRoute(Route r) {
		this.route = r;
	}

	//@Ensures({tripCount=old(tripCount)+1})
	public void updateTripCount() {
		tripCount += 1;

	}

	public void updateLocation(Stops stops) {
		currentLocation = stops;
	}

	public Stops returnLocation() {
		return (currentLocation);
	}

	@Override
	public int getTripCount() {
		// TODO Auto-generated method stub
		return this.tripCount;
	}

	@Override
	public void resetTripCount() {
		// TODO Auto-generated method stub
		this.tripCount = 0;

	}

	int licensePlateNo;
	int tripCount;
	// DB database;
	fareCollector fc = new fareCollector();
}

class van implements vehicles {

	int licensePlateNo;
	int tripCount;
	fareCollector fc = new fareCollector();

	@Override
	public int getTripCount() {
		// TODO Auto-generated method stub
		return tripCount;
	}

	@Override
	public void resetTripCount() {
		// TODO Auto-generated method stub
		this.tripCount = 0;
	}

	public van(Integer van_number) {
		// TODO Auto-generated method stub
		// this.database = centralControlSystem.BusInfo(bus_number);
		licensePlateNo = van_number;
		this.tripCount = centralControlSystem.returnTripCount(van_number);
	}

}

class fareCollector {
	commuters c;
	int tripFare;

	public void requestToBoardVehicle(commuters commuters, bus b) {
		// TODO Auto-generated method stub
		c = commuters;
		c.boardedAt = b.currentLocation;
	}
	

	//@Requires{"tripFare>old(tripfare)"}
	public void requestToDepartVehicle(commuters commuters, bus b) {
		// TODO Auto-generated method stub
		c.departedAt = b.currentLocation;
		tripFare += (b.route.routes.indexOf(c.departedAt) - b.route.routes.indexOf(c.boardedAt)) * 2;
	}

}

class maintenanceStaff implements employees {

	@Override
	public void checkMaintenanceStatus() {

		Integer number_of_buses = 5;
		bus[] temp = centralControlSystem.returnBusObject();
		for (int i = 0; i < number_of_buses; i++) {
			if (temp[i].getTripCount() < maintainance_Threshold) {
				//System.out.println("Maintenance not needed");
			} else {
				System.out.println(temp[i].getTripCount() +" Maintenance needed For Bus: " + (i+1));
				centralControlSystem.removeVehicleFromAvailableList(i);
				SendVehicletoGarage(i);
			}
		}
	}

	@Override
	public void returnVehicleForService(Integer vehicle_number) {
		centralControlSystem.addVehicleToAvailableList(vehicle_number);
		bus[] temp = centralControlSystem.returnBusObject();
		temp[vehicle_number].resetTripCount();
	}

	public void SendVehicletoGarage(Integer vehicle_number) {

		// Assume vehicle is in garage for repair for 3 seconds
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}

		// Send Vehicle back to field
		returnVehicleForService(vehicle_number);
	}

	Integer maintainance_Threshold = 10;
}

class drivers implements employees {

	@Override
	public void checkMaintenanceStatus() {
		// TODO Auto-generated method stub
	}

	@Override
	public void returnVehicleForService(Integer vehicle_number) {
		// TODO Auto-generated method stub

	}

}

class request{
	static HashMap<commuters, Stops> commuterList= new HashMap<commuters,Stops>();
	static HashMap<Stops, Integer> stopsList = new HashMap<Stops, Integer>();

	public request(commuters c1, Stops s1) {
	commuterList.put(c1,s1);
	if(stopsList.get(s1)!=null)
	stopsList.put(s1, stopsList.get(s1)+1);
	else
	stopsList.put(s1, 1);
	}

	public static int getRequestCount(Stops s1) {
	return stopsList.get(s1);
	}
	
}

class vanRequestService {
	int threshold=2;
	ArrayList<request> requests = new ArrayList<request>(); 

	public void addRequest(commuters commuters, Stops s1) {
	requests.add(new request(commuters, s1));
	if(request.getRequestCount(s1)>=threshold){
	{
	Set<commuters> commuters1 = request.commuterList.keySet();
	Iterator<commuters> iter = commuters1.iterator();
	while(iter.hasNext()){
	iter.next().displayNotifications("Van is being deployed for stop: "+s1.name);
	}
	request.stopsList.remove(s1);
	request.commuterList.remove(commuters, s1);
	}
	}
	

}
}

class commuters {
	int id;
	public Stops boardedAt = null;
	public Stops departedAt = null;
	String commuterDisplay;

	public commuters(int id) {
		this.id = id;
	}

	public void registerCommuter() {
		centralControlSystem.registerCommuter(this);
	}

	public void retrieveRouteMap(String sourceStop, String destStop) {
		commuterDisplay = centralControlSystem.retrieveRouteMap(sourceStop, destStop);
		this.displayNotifications(commuterDisplay);
	}

	public void retrieveFare(String sourceStop, String destStop) {
		commuterDisplay = centralControlSystem.retrieveFare(sourceStop, destStop);
		this.displayNotifications(commuterDisplay);
	}

	public void retrieveSchedule(String sourceStop) {
		commuterDisplay = centralControlSystem.retrieveSchedule(sourceStop);
		this.displayNotifications(commuterDisplay);
	}

	public void retrieveRealTimeLocation() {
		commuterDisplay = centralControlSystem.retrieveRealTimeLocations();
		this.displayNotifications(commuterDisplay);
	}

	public void requestToBoardVehicle(bus b) {
		b.fc.requestToBoardVehicle(this, b);
	}

	public void requestToDepartVehicle(bus b) {
		b.fc.requestToDepartVehicle(this, b);
	}

	public void displayNotifications(String commuterDisplay) {
		System.out.println(commuterDisplay);
	}
	
	public void requestVan(String s) {
	centralControlSystem.requestVan(this, s);
	
	}
}

//@Invariant({"number_of_routes>=0","number_of_vehicle>=0","number_of_Drivers>=0", "numberOfSchedules>=0"})
class centralControlSystem {
	public static int fareCollected;
	static bus[] buses;
	static Stops[] stops = new Stops[8];
	static commuters[] commuter = new commuters[5];
	static Integer number_of_routes = 4;
	static Route[] route = new Route[number_of_routes];
	static Integer number_of_vehicle = 5;
	static Integer number_of_Drivers = 5;
	static Time time = new Time();
	static int numberOfSchedules=16;
	static vanRequestService vanRequestService = new vanRequestService();


	// static DB[] vehicle_DB = new DB[number_of_vehicle];
	static Driver[] drivers = new Driver[number_of_Drivers];
	static Set<Driver> availableDrivers;
	static Schedule[] schedule = new Schedule[numberOfSchedules];
	static int[] tripcount = { 13, 2, 6, 33, 11 };

	// static ArrayList <Integer> availableBuses = new ArrayList <Integer>();
	static Set<Integer> availableBuses;
	static ArrayList<commuters> commuter_database = new ArrayList<commuters>();

	//@Requires({"number_of_routes>=0","number_of_vehicle>=0","number_of_Drivers>=0", "numberOfSchedules>=0"})
	public centralControlSystem() throws InterruptedException {
		System.out.println("bleh");
		availableDrivers = new HashSet<>();

		drivers[0] = new Driver("Tom");
		drivers[1] = new Driver("Ding");
		drivers[2] = new Driver("Mat");
		drivers[3] = new Driver("Bryan");
		drivers[4] = new Driver("Kelly");

		availableDrivers.add(drivers[0]);
		availableDrivers.add(drivers[1]);
		availableDrivers.add(drivers[2]);
		availableDrivers.add(drivers[3]);
		availableDrivers.add(drivers[4]);

		commuter[0] = new commuters(50208400);
		commuter[1] = new commuters(50208401);
		commuter[2] = new commuters(50208402);
		commuter[3] = new commuters(50208403);
		commuter[4] = new commuters(50208404);

		commuter_database.add(commuter[0]);
		commuter_database.add(commuter[1]);
		commuter_database.add(commuter[2]);
		commuter_database.add(commuter[3]);
		commuter_database.add(commuter[4]);

		Integer number_of_stops = 9;
		stops = new Stops[number_of_stops];

		stops[0] = new Stops("Ellicott Tunnel");
		stops[1] = new Stops("Lee Loop");
		stops[2] = new Stops("Governors");
		stops[3] = new Stops("Flint Loop");
		stops[4] = new Stops("Centre For Tomorrow");
		stops[5] = new Stops("Maynard");
		stops[6] = new Stops("Goodyear");
		stops[7] = new Stops("South Campus");
		stops[8] = new Stops("Garage");

		route[0] = new Route(new Stops[] { stops[3], stops[6], stops[7] }, "Yellow Line");
		route[1] = new Route(new Stops[] { stops[7], stops[6], stops[3] }, "Green Line");
		route[2] = new Route(new Stops[] { stops[0], stops[1], stops[2], stops[3], stops[4], stops[5], stops[7] },
				"South Campus");
		route[3] = new Route(new Stops[] { stops[7], stops[5], stops[4], stops[3], stops[2], stops[1], stops[0] },
				"North Campus");

		schedule[0] = new Schedule(route[0], 1,"normal");
		schedule[1] = new Schedule(route[1], 8,"normal");
		schedule[2] = new Schedule(route[0], 11,"normal");
		schedule[3] = new Schedule(route[1], 13,"normal");
		schedule[4] = new Schedule(route[0], 15,"normal");
		schedule[5] = new Schedule(route[1], 17,"normal");
		schedule[6] = new Schedule(route[0], 19,"normal");

		schedule[7] = new Schedule(route[2], 8,"normal");
		schedule[8] = new Schedule(route[3], 10,"normal");
		schedule[9] = new Schedule(route[2], 12,"normal");
		schedule[10] = new Schedule(route[3], 14,"normal");
		schedule[11] = new Schedule(route[2], 16,"normal");
		schedule[12] = new Schedule(route[3], 18,"normal");
		schedule[13] = new Schedule(route[2], 20,"normal");
		schedule[14] = new Schedule(route[3], 22,"normal");

		Integer number_of_buses = 5;
		buses = new bus[number_of_buses];
		availableBuses = new HashSet<>();
		for (int i = 0; i < number_of_buses; i++) {
			buses[i] = new bus(i);
			availableBuses.add(i);
		}

		//UC5
		drivers[0].PlyadditionalBus(route[0], 1.0);
		/* Not needed
		 * vehicle_DB[0] = new DB(drivers[0],schedule[0]); vehicle_DB[1] = new
		 * DB(drivers[1],schedule[1]); vehicle_DB[2] = new
		 * DB(drivers[2],schedule[2]); vehicle_DB[3] = new
		 * DB(drivers[3],schedule[3]); vehicle_DB[4] = new
		 * DB(drivers[4],schedule[4]);
		 */
		
		
		/* To be debugged
		 * for(int i=0; i<15; i++) { for(int
		 * j=0;j<schedule[i].route.routes.size();j++)
		 * System.out.println(schedule[i].route.routes.get(j).name); }
		 * Thread.sleep(10000000);
		 */

		  Thread timeThread = new Thread(time, "timeThread");
		  timeThread.start(); 
		  scheduleManager scheduleManager = new scheduleManager(schedule, time); 
		  Thread scheduleManagerThread = new Thread(scheduleManager,"scheduleManagerThread");
		  scheduleManagerThread.start();
		 
	}

	public static void requestVan(commuters commuters, String s) {
	Stops s1=null;
	for(int i=0; i<stops.length;i++)
			{

			if(stops[i].name==s)
			s1=stops[i];	
			}
			vanRequestService.addRequest(commuters, s1);
	}
		
	public static String retrieveSchedule(String sourceStop) {

		String commuterDisplay = "";
		for (int i = 0; i < schedule.length; i++) {
			for (int j = 0; j < schedule[i].route.routes.size(); j++) {
				if (schedule[i].route.routes.get(j).name == sourceStop)
					commuterDisplay = commuterDisplay + " Bus for route" + schedule[i].route.name + " will arrive on "
							+ sourceStop + " at " + (schedule[i].starttime + (j * 2) / 10) + "\n";
			}
		}
		return commuterDisplay;
	}
	
	public static Route returnRoute(int number)
	{
		return route[number];
	}

	public static void AddScheduleForOverCrowdRequest(Route request, double time) {
		
		
		schedule[15] = new Schedule(request, time,"Additional");
		bus b;
		Driver d;
		
		b = centralControlSystem.getBus();
		d = centralControlSystem.getDriver();
		if (b==null || d == null) {
			System.out.println("No Avaialable Resources");
			return;
		}

		scheduleManager.UpdateSchedule(schedule);
		
		String commuterDisplay = "New Buses added at route " + schedule[15].route.routes.get(0).name + " at "+
				schedule[15].starttime;
		
		//UC7
		for(int i=0;i<5;i++)
		{
			commuter[i].displayNotifications(commuterDisplay);
		}
	}

	public static void RemoveBusSchedule(Schedule to_delete) {
		to_delete=null;
		scheduleManager.UpdateSchedule(schedule);
	}
	
	public static String retrieveRealTimeLocations() {

		String commuterDisplay = "";
		for (int i = 0; i < buses.length; i++) {
			commuterDisplay = commuterDisplay + " Bus number: " + buses[i].licensePlateNo + " is at "
					+ buses[i].currentLocation.name + "\n";
		}
		return commuterDisplay;
	}

	public static Driver getDriver() {
		if (availableDrivers != null) {
			Iterator<Driver> iter = availableDrivers.iterator();
			if (iter.hasNext()) {
				Driver driver = iter.next();
				removeDriverFromAvailableList(driver);
				return driver;
			}
		}
		return null;
	}

	public static void removeDriverFromAvailableList(Driver d) {
		if (availableDrivers != null)
			availableDrivers.remove(d);
	}

	public static void addDriverToAvailableList(Driver d) {
		availableDrivers.add(d);
	}

	public static bus getBus() {
		if (availableBuses != null) {
			Iterator<Integer> iter = availableBuses.iterator();
			if (iter.hasNext()) {
				int vehicleNumber = iter.next();
				removeVehicleFromAvailableList(vehicleNumber);
				return buses[vehicleNumber];
			}
		}
		return null;
	}

	public static void displayFareCollected() {
		System.out.println("Central Control System: The total fare collected for the day is: " + fareCollected);
		
		// UC3
		maintenanceStaff employee = new maintenanceStaff();
		employee.checkMaintenanceStatus();
	}
	

	
	public static int returnTripCount(Integer vehice_number) {
		return (tripcount[vehice_number]);
	}

	/* 
	 * public static DB BusInfo(Integer vehice_number) { return
	 * (vehicle_DB[vehice_number]); }
	 */

	public static bus[] returnBusObject() {
		return buses;
	}

	public static boolean removeVehicleFromAvailableList(int vehicle_number) {
		// System.out.print(availableBuses.size());
		if (availableBuses != null) {
			availableBuses.remove(vehicle_number);
		}

		return true;
	}

	public static boolean addVehicleToAvailableList(int vehicle_number) {
		// System.out.print(availableBuses.size());
		availableBuses.add(vehicle_number);

		return true;

	}

	public static String retrieveRouteMap(String sourceStop, String destStop) {
		int best_route = -1;
		int source = -1, dest = -1, dist = Integer.MAX_VALUE, least_distance = Integer.MAX_VALUE, source_value = -1,
				dest_value = -1;
		String routeMap = "";
		for (int i = 0; i < number_of_routes; i++) {
			for (int j = 0; j < route[i].routes.size(); j++) {
				if (route[i].routes.get(j).name == sourceStop)
					source = j;
				if (route[i].routes.get(j).name == destStop)
					dest = j;
			}
			if (dist != -1 && source != -1)
				dist = (dest > source) ? (dest - source) : (source - dest);
			System.out.println(dist);
			if (dist < least_distance && dist != Integer.MAX_VALUE) {
				source_value = source;
				dest_value = dest;
				least_distance = dist;
				best_route = i;
			}
		}
		for (int i = source_value; i <= dest_value; i++)
			routeMap = routeMap + ", " + route[best_route].routes.get(i).name;
		String commuterDisplay = "The best route is: " + route[best_route].name + " with Stops: " + routeMap;
		return commuterDisplay;
	}

	public static String retrieveFare(String sourceStop, String destStop) {
		int best_route = -1;
		int source = -1, dest = -1, dist = Integer.MAX_VALUE, least_distance = Integer.MAX_VALUE, source_value = -1,
				dest_value = -1;
		String routeMap = "";
		for (int i = 0; i < number_of_routes; i++) {
			for (int j = 0; j < route[i].routes.size(); j++) {
				if (route[i].routes.get(j).name == sourceStop)
					source = j;
				if (route[i].routes.get(j).name == destStop)
					dest = j;
			}
			if (dist != -1 && source != -1)
				dist = (dest > source) ? (dest - source) : (source - dest);
			System.out.println(dist);
			if (dist < least_distance && dist != Integer.MAX_VALUE) {
				source_value = source;
				dest_value = dest;
				least_distance = dist;
				best_route = i;
			}
		}
		float fare = 2 * least_distance;
		String commuterDisplay = "The fare for this trip will be $" + fare;
		return commuterDisplay;
	}

	public static void registerCommuter(commuters c) {
		commuter_database.add(c);
	}

}

public class BusTransportSystem {
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		new centralControlSystem();
		commuters c1 = new commuters(50208476);
		commuters c2 = new commuters(50208476);

		// UC1
		c1.registerCommuter();

		// UC2
		c1.retrieveRouteMap("Ellicott Tunnel", "Maynard");
		c1.retrieveFare("Ellicott Tunnel", "Maynard");
		c1.retrieveSchedule("Ellicott Tunnel");
		c1.retrieveRealTimeLocation();
		
		//UC3, UC9
		c1.requestVan("South Campus");
		c2.requestVan("South Campus");
		
		
		//UC4 is already being done by the code written for trip, so not needed
		
		//UC6 - done
		
		/*
		String number = System.console().readLine();
		int route_number=Integer.parseInt(number);
		String time = System.console().readLine();
		double time_request = Double.parseDouble(time);
		Route x = centralControlSystem.returnRoute(route_number);
		Driver.PlyadditionalBus(x,time_request);
		*/

	}

}
