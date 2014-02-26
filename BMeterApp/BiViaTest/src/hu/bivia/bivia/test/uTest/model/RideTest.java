package hu.bivia.bivia.test.uTest.model;

import java.util.Date;

import hu.bivia.model.Ride;
import hu.bivia.view.BiViaMainActivityView;
import android.test.ActivityInstrumentationTestCase2;

public class RideTest extends
	ActivityInstrumentationTestCase2<BiViaMainActivityView> {

	//region --- test setup ----------------------------------------------------
	public RideTest(Class<BiViaMainActivityView> activityClass) {
		super(activityClass);
	}
	
	public RideTest() {
		super(BiViaMainActivityView.class);
	}
	//endregion --- test setup -------------------------------------------------
	
	public void testCreation(){
		Date startTime = new Date();
		float distance = 1000; //meters
		float averageSpeed = 1; // km/h
		long rideTime = 3600 * 1000; // 1 hour in milliseconds
		
		Ride target = new Ride(startTime, distance, averageSpeed, rideTime);
		
		assertEquals(distance, target.getDistance());
		assertEquals(averageSpeed, target.getAverageSpeed());
		assertEquals(rideTime, target.getRideTimeMs());
	}
}
