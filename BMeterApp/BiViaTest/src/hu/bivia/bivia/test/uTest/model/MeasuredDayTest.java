package hu.bivia.bivia.test.uTest.model;

import java.util.Date;

import hu.bivia.model.MeasuredDay;
import hu.bivia.model.Ride;
import hu.bivia.view.BiViaMainActivityView;
import android.test.ActivityInstrumentationTestCase2;

public class MeasuredDayTest  extends
ActivityInstrumentationTestCase2<BiViaMainActivityView>{

	//region --- test setup ----------------------------------------------------
	public MeasuredDayTest(Class<BiViaMainActivityView> activityClass) {
		super(activityClass);
	}
	
	public MeasuredDayTest() {
		super(BiViaMainActivityView.class);
	}
	//endregion --- test setup -------------------------------------------------
	
	public void testDailyAverageCalculation(){
		
		// 1 km/h
		Ride ride1 = new Ride(new Date(), 1000, 1, 3600*1000);
		// 10 km/h
		Ride ride2 = new Ride(new Date(), 5000, 10, 1800*1000);
		// 100 km/h
		Ride ride3 = new Ride(new Date(), 200000, 100, 2*3600*1000);
		
		MeasuredDay target = new MeasuredDay(ride1.getStartTime());
		
		target.addRide(ride1);
		assertEquals(ride1.getAverageSpeed(), target.getAverageSpeed());
		
		target.addRide(ride2);
		float averageSpeed = (ride1.getDistance() + ride2.getDistance()) * 1000 /
				(ride1.getRideTimeMs() + ride2.getRideTimeMs()) * 3.6F ;
		assertEquals(averageSpeed, target.getAverageSpeed());
		
		target.addRide(ride3);
		averageSpeed = (ride1.getDistance() + ride2.getDistance() + ride3.getDistance()) * 1000 /
				(ride1.getRideTimeMs() + ride2.getRideTimeMs() + ride3.getRideTimeMs()) * 3.6F ;
		assertEquals(averageSpeed, target.getAverageSpeed());
		
	}
	
}
