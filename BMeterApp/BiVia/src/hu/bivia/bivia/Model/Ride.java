package hu.bivia.bivia.Model;

import java.util.Date;

public class Ride extends Measurement{

	private Date myStartTime;
	private long myRideTimeMillis;

	public Ride(Date startTime,  float distance, float averageSpeed, long rideTimeMillis) {
		super(distance, averageSpeed);
		myStartTime = startTime;
		myRideTimeMillis = rideTimeMillis;
	}
	
	public Date getStartTime() {
		return myStartTime;		
	}

	public long getRideTimeMillis() {
		return myRideTimeMillis;
	}
}
