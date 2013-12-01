package hu.bivia.bivia.model;

import hu.bivia.bivia.logic.Measurer;

import java.util.ArrayList;
import java.util.Date;

/**
 * Groups measurements for a day.
 * @author horvath.robert
 *
 */
public class MeasuredDay {

	private ArrayList<Ride> myRides = new ArrayList<Ride>();
	private Date myDate;
	
	/**
	 * Distance in meters.
	 */
	private float myTotalDistance;
	private long myTotalRideTimeMillis;
	private float myAverageSpeed;
	
	public MeasuredDay(Date date) {		
		myDate = date;
		myTotalDistance = 0;
		myTotalRideTimeMillis = 0;
	}
	
	public void addRide(Ride newRide){
		myRides.add(newRide);
		myTotalDistance += newRide.getDistance();
		myTotalRideTimeMillis += newRide.getRideTimeMs();
		
		refreshAverageSpeed();
	}
	
	private void refreshAverageSpeed() {
		float totalDistanceKm = 0;
		long totalRideTimeMs = 0;
		for(int i=0; i< myRides.size(); i++){
			totalDistanceKm += myRides.get(i).getDistance();
			totalRideTimeMs += myRides.get(i).getRideTimeMs();
		}
		myAverageSpeed = Measurer.calculateSpeedInKmPerHour(totalDistanceKm, totalRideTimeMs);		
	}

	public Date getDate(){
		return myDate;
	}

	public Ride getRide(int position) {
		return myRides.get(position);
	}

	public int getRideCount() {
		return myRides.size();
	}

	/**
	 * Day's distance in kilometers
	 * @return
	 */
	public float getTotalDistance() {
		return myTotalDistance;
	}

	public long getTotalTimeMillis() {
		return myTotalRideTimeMillis;
	}

	public Object getAverageSpeed() {		
		return myAverageSpeed;
	}

	/**
	 * Deletes ride from the given index and updates the total distance, total 
	 * ride time and re-calculates the average speed.
	 * @param rideIndex
	 */
	public void deleteRideFromPosition(int rideIndex) {
		Ride ride = myRides.get(rideIndex);
		myTotalDistance -= ride.getDistance();
		myTotalRideTimeMillis -= ride.getRideTimeMs();
		
		myRides.remove(rideIndex);
		refreshAverageSpeed();
	}
	
}
