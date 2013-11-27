package hu.bivia.bivia.model;

public class Measurement {

	private float myDistance;
	private float myAverageSpeed;

	/**
	 * Inits non-mutable meaurement.
	 * @param distance km
	 * @param averageSpeed km/h
	 */
	public Measurement(float distance, float averageSpeed){
		myDistance = distance;
		myAverageSpeed = averageSpeed;
	}
	
	/**
	 * Distance in kilometers
	 * @return
	 */
	public float getDistance(){
		return myDistance;
	}
	
	/**
	 * Average speed in km/h
	 * @return
	 */
	public float getAverageSpeed(){
		return myAverageSpeed;
	}
	
}
