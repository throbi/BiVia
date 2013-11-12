package hu.bivia.bivia.test.uTest;

import hu.bivia.bivia.Logic.Measurer;
import hu.bivia.bivia.View.BiViaMainActivityView;
import hu.bivia.bivia.ViewModel.BiViaMainPageViewModel;
import android.location.LocationManager;
import android.test.ActivityInstrumentationTestCase2;

import static org.mockito.Mockito.*;

public class MeasurerUnitTest extends
		ActivityInstrumentationTestCase2<BiViaMainActivityView>{

	private Measurer myTestTarget;
	private LocationManager myMockLocationManager;
	private BiViaMainPageViewModel myMockViewModel;

	//region --- test setup ----------------------------------------------------
	public MeasurerUnitTest(Class<BiViaMainActivityView> activityClass) {
		super(activityClass);
	}

	public MeasurerUnitTest() {
		super(BiViaMainActivityView.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		myMockLocationManager = mock(LocationManager.class);
		myMockViewModel = mock(BiViaMainPageViewModel.class);
		
		myTestTarget = new Measurer();
		myTestTarget._test_setLocacationManager(myMockLocationManager);		
	}
	
	public void testPreconditions(){
		assertNotNull(myMockLocationManager);
		assertNotNull(myMockViewModel);
		assertNotNull(myTestTarget);
	}
	//enddregion --- test setup ------------------------------------------------
	
	//region --- cals from the user --------------------------------------------
	public void testStartMeasuring(){		
		Measurer spy = spy(myTestTarget);
		
		spy._test_setViewModle(myMockViewModel);
		spy._test_setLocacationManager(myMockLocationManager);		
		
		// no gps service				
		doReturn(false).when(spy).areServicesConnected();
		spy.startMeasuring();
		assertFalse(spy.getIsMeasuring());
		verify(myMockViewModel, times(1)).reportNoGPSService();
		
		// gps service present
		doReturn(true).when(spy).areServicesConnected();		
		spy.startMeasuring();
		assertTrue(spy.getIsMeasuring());		
	}
	
	public void testStopMeasuring(){		
		Measurer spy = spy(myTestTarget);
		
		spy._test_setViewModle(myMockViewModel);
		spy._test_setLocacationManager(myMockLocationManager);		
		
		// gps service present
		doReturn(true).when(spy).areServicesConnected();		
		spy.startMeasuring();
		assertTrue(spy.getIsMeasuring());		
		
		spy.stopMeasuring();
		assertFalse(spy.getIsMeasuring());
	}
	//endregion --- cals from the user -----------------------------------------
}

