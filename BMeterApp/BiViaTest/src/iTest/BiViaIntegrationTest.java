package iTest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jayway.android.robotium.solo.Solo;

import hu.bivia.bivia.View.BiViaMainActivityView;
import hu.bivia.bivia.test.uTest.BiViaMainPageUITest;
import android.content.Context;
import android.location.LocationManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

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
	@UiThreadTest
	public void testStartup_GPSEnabled_expectDisabledUI(){
		testPreconditions();
		
		LocationManager mockLocationManager = mock(LocationManager.class);
		
		when(mockLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			.thenReturn(true);
				
		myView._test_getViewModel()._test_setLocacationManager(mockLocationManager);
		myView._test_getViewModel().checkForEnabledGPS();
	
		BiViaMainPageUITest.checkDisabledUI(myView, myTargetContext);
		
		// enable GPS dialog should not be visible
		boolean enableGPSDialogVisible = solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_title), true);
		
		assertFalse(enableGPSDialogVisible);
	}		

	@UiThreadTest
	public void testStartup_GPSDisabled_expectDisabledUIAndPromptDialog(){
		testPreconditions();
		
		LocationManager mockLocationManager = mock(LocationManager.class);
		
		when(mockLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			.thenReturn(false);
				
		myView._test_getViewModel()._test_setLocacationManager(mockLocationManager);
		myView._test_getViewModel().checkForEnabledGPS();
	
		BiViaMainPageUITest.checkDisabledUI(myView, myTargetContext);		
		
		// check for visible dialog
		assertTrue(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_title), true));
	}
	//endregion --- start up tests ---------------------------------------------
}
