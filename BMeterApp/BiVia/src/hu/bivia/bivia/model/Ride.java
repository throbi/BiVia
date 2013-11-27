package hu.bivia.bivia.model;

import java.util.Date;

public class Ride extends Measurement{

	private Date myStartTime;
	private long myRideTimeMillis;

	/**
	 * Constructor for non-mutable ride
	 * @param startTime
	 * @param distanceKm
	 * @param averageSpeedKmh
	 * @param rideTimeMs
	 */
	public Ride(Date startTime,  float distanceKm, float averageSpeedKmh, long rideTimeMs) {
		super(distanceKm, averageSpeedKmh);
		myStartTime = startTime;
		myRideTimeMillis = rideTimeMs;
	}
	
	public Date getStartTime() {
		return myStartTime;		
	}

	public long getRideTimeMs() {
		return myRideTimeMillis;
	}
}
