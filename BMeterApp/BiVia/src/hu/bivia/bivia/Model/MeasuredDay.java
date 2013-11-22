package hu.bivia.bivia.Model;

import java.util.ArrayList;
import java.util.Date;

/**
 * Groups measurements for a day.
 * @author horvath.robert
 *
 */
public class MeasuredDay {

	private ArrayList<Ride> myMeasurements = new ArrayList<Ride>();
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
	
	public void addMeasurement(Ride newMeasurement){
		myMeasurements.add(newMeasurement);
		myTotalDistance += newMeasurement.getDistance();
		myTotalTimeMillis += newMeasurement.getRideTimeMillis();
		
		refreshAverageSpeed();
	}
	
	private void refreshAverageSpeed() {
		float averageSpeedSum = 0;
		for(int i=0; i< myMeasurements.size(); i++){
			averageSpeedSum += myMeasurements.get(i).getAverageSpeed();
		}
		myAverageSpeed = averageSpeedSum / myMeasurements.size();		
	}

	public Date getDate(){
		return myDate;
	}

	public Ride getMeasurement(int position) {
		return myMeasurements.get(position);
	}

	public int getMeasurementCount() {
		return myMeasurements.size();
	}

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
