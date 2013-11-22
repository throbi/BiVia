package hu.bivia.bivia.Model;

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
		myTotalTimeMillis += newRide.getRideTimeMillis();
		
		refreshAverageSpeed();
	}
	
	private void refreshAverageSpeed() {
		float averageSpeedSum = 0;
		for(int i=0; i< myRides.size(); i++){
			averageSpeedSum += myRides.get(i).getAverageSpeed();
		}
		myAverageSpeed = averageSpeedSum / myRides.size();		
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
