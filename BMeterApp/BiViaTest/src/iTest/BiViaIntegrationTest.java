package iTest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jayway.android.robotium.solo.Solo;

import hu.bivia.bivia.Logic.Measurer;
import hu.bivia.bivia.View.BiViaMainActivityView;
import hu.bivia.bivia.test.uTest.BiViaMainPageUITest;
import android.content.Context;
import android.location.LocationManager;
import android.test.ActivityInstrumentationTestCase2;

public class BiViaIntegrationTest extends 
		ActivityInstrumentationTestCase2<BiViaMainActivityView>{

	BiViaMainActivityView myView;
	Context myTargetContext;
	private Solo solo;
	
	//region --- test setup ----------------------------------------------------
	
	public BiViaIntegrationTest(Class<BiViaMainActivityView> activityClass) {
		super(activityClass);
	}

	public BiViaIntegrationTest() {
		super(BiViaMainActivityView.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
				
		myView = getActivity();
		myTargetContext = getInstrumentation().getTargetContext();
		
		solo = new Solo(getInstrumentation(), getActivity());
	}
		
	public void testPreconditions() {	
		assertNotNull(myView);
	}
	//endregion --- test setup -------------------------------------------------

	//region --- start up tests ------------------------------------------------
	public void testStartup_GPSEnabled_expectDisabledUI(){		
		
		myView.runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				LocationManager mockLocationManager = mock(LocationManager.class);
				
				when(mockLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
					.thenReturn(true);
				
				Measurer measurer = myView._test_getViewModel()._test_getMeasurer();
				measurer._test_setLocacationManager(mockLocationManager);				
				measurer.checkForEnabledGPS();
			}
		});
		
		getInstrumentation().waitForIdleSync();
		
		myView.runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				BiViaMainPageUITest.checkDisabledUI(myView, myTargetContext);		
				
				// check for visible dialog		
				assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_title), true));
			}
		});		
	}		


	public void testStartup_GPSDisabled_expectDisabledUIAndPromptDialog(){		
		
		myView.runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				LocationManager mockLocationManager = mock(LocationManager.class);
				
				when(mockLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
					.thenReturn(false);
								
				Measurer measurer = myView._test_getViewModel()._test_getMeasurer();
				measurer._test_setLocacationManager(mockLocationManager);				
				measurer.checkForEnabledGPS();
			}
		});
		
		getInstrumentation().waitForIdleSync();
		
		myView.runOnUiThread(new Runnable() {			
			@Override
			public void run() {			
				BiViaMainPageUITest.checkDisabledUI(myView, myTargetContext);		
				
				// check for visible dialog				
				assertTrue(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_title), true));
			}
		});		
	}
	//endregion --- start up tests ---------------------------------------------
}
