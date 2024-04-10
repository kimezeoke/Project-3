import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class TripPoint {
	
	private int time; // time in minutes
    private double lat; // latitude
    private double lon; // longitude
    private static ArrayList<TripPoint> trip; // ArrayList of every point in a trip
    private static ArrayList<TripPoint> movingTrip; // ArrayList of points that are moving

    

    // constructor given time, latitude, and longitude
    public TripPoint(int time, double lat, double lon) {
        this.time = time;
        this.lat = lat;
        this.lon = lon;
    }

    // returns time
    public int getTime() {
        return time;
    }

    // returns latitude
    public double getLat() {
        return lat;
    }

    // returns longitude
    public double getLon() {
        return lon;
    }

    // returns a copy of trip ArrayList
    public static ArrayList<TripPoint> getTrip() {
        return new ArrayList<>(trip);
    }

    

    public static void readFile(String filename) throws IOException {
    	trip = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim(); // Remove leading/trailing whitespace
            if (!line.contains("Time")) {
                String[] parts = line.split(",");
                int time = Integer.parseInt(parts[0]);
                double lat = Double.parseDouble(parts[1]);
                double lon = Double.parseDouble(parts[2]);
                trip.add(new TripPoint(time, lat, lon));
            }
        }
        reader.close();
    }
    
 // returns the total time of trip in hours
 	public static double totalTime() {
 		int minutes = trip.get(trip.size() - 1).getTime();
        return minutes / 60.0;
 	}
 	
 	// finds the total distance traveled over the trip
 	public static double totalDistance() {
 	    double distance = 0.0;
 	    for (int i = 1; i < trip.size(); ++i) {
 	        distance += haversineDistance(trip.get(i - 1), trip.get(i));
 	    }
 	    return distance;
 	}

 	
 	 public static double avgSpeed(TripPoint a, TripPoint b) {
         int timeInMin = Math.abs(a.getTime() - b.getTime());
         double dis = haversineDistance(a, b);
         double kmpmin = dis / timeInMin;
         return kmpmin * 60;
     }
 	 
 	// uses the haversine formula for great sphere distance between two points
 	public static double haversineDistance(TripPoint first, TripPoint second) {
 	    double lat1 = Math.toRadians(first.getLat());
 	    double lat2 = Math.toRadians(second.getLat());
 	    double lon1 = Math.toRadians(first.getLon());
 	    double lon2 = Math.toRadians(second.getLon());

 	    double dLat = Math.abs(lat2 - lat1);
 	    double dLon = Math.abs(lon2 - lon1);

 	    double a = Math.pow(Math.sin(dLat / 2), 2) +
 	               Math.cos(lat1) * Math.cos(lat2) *
 	               Math.pow(Math.sin(dLon / 2), 2);

 	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

 	    // Earth's radius in kilometers
 	    double radius = 6371;
 	    return radius * c;
 	}


    
    public static int h1StopDetection() {
        double threshold = 0.6; // in kilometers
        movingTrip = new ArrayList<>(trip);
        int stops = 0;
        ArrayList<TripPoint> stoppedPoints = new ArrayList<>();

        for (int i = 1; i < trip.size(); i++) {
            double distance = haversineDistance(trip.get(i - 1), trip.get(i));
            if (distance <= threshold) {
                stoppedPoints.add(movingTrip.get(i));
                stops++;
            }
        }
        movingTrip.removeAll(stoppedPoints);

        return stops;
    }

    
    public static int h2StopDetection() {
        double stopRadius = 0.5; // in kilometers
        movingTrip = new ArrayList<>();
        int stopCount = 0;
        ArrayList<TripPoint> zone = new ArrayList<>();

        for (TripPoint current : trip) {
            if (zone.isEmpty()) {
                zone.add(current);
                continue;
            }

            boolean inStopZone = false;
            for (TripPoint stop : zone) {
                if (haversineDistance(stop, current) <= stopRadius) {
                    inStopZone = true;
                    break;
                }
            }

            if (inStopZone) {
                zone.add(current);
            } else {
                if (zone.size() >= 3) {
                    stopCount += zone.size();
                } else {
                    movingTrip.addAll(zone);
                }
                zone.clear();
                zone.add(current);
            }
        }

        if (zone.size() >= 3) {
            stopCount += zone.size();
        } else {
            movingTrip.addAll(zone);
        }

        return stopCount;
    }

    
    public static double stoppedTime() {
        double totalTime = totalTime(); // Total time of the trip in minutes
        int stopCount = h1StopDetection(); // Get the number of stops using the first heuristic
        double movingTimeInMinutes = (totalTime - stopCount) ; // Calculate moving time in minutes
        return movingTimeInMinutes ; // Calculate stopped time in hours
    }



   
    public static double avgMovingSpeed() {
        double totalDistance = totalDistance(); // Total distance of the trip in km
        double movingTime = movingTime(); // Moving time in hours
        return totalDistance / movingTime; // Average moving speed in km/hr
    }


   
    public static double movingTime() {
        double totalTime = totalTime(); // Total time of the trip in hours
        double stoppedTime = stoppedTime(); // Stopped time in hours
        return totalTime - stoppedTime;
    }



 // returns a copy of the movingTrip ArrayList
    public static ArrayList<TripPoint> getMovingTrip() {
        return new ArrayList<>(movingTrip);
    }

     
    }


