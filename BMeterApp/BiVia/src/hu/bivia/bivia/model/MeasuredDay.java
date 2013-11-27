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
	private long myTotalTimeMillis;
	private float myAverageSpeed;
	
	public MeasuredDay(Date date) {		
		myDate = date;
		myTotalDistance = 0;
		myTotalTimeMillis = 0;
	}
	
	public void addRide(Ride newRide){
		myRides.add(newRide);
		myTotalDistance += newRide.getDistance();
		myTotalTimeMillis += newRide.getRideTimeMs();
		
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
		return myTotalTimeMillis;
	}

	public Object getAverageSpeed() {		
		return myAverageSpeed;
	}
	
}
