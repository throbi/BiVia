package hu.bivia.bivia.test.uTest;

import com.jayway.android.robotium.solo.Solo;

import hu.bivia.bivia.View.BiViaMainActivityView;
import hu.bivia.bivia.ViewModel.BiViaMainPageViewModel;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import static org.mockito.Mockito.*;

/**
 * Tests the UI (i.e. View in MVVM) with a mocked view model. 
 */
public class BiViaMainPageUITest 
	extends 
		ActivityInstrumentationTestCase2<BiViaMainActivityView>{

	BiViaMainActivityView myView;
	BiViaMainPageViewModel myViewModel;
	Context myTargetContext;
	BiViaMainPageViewModel myMockViewModel;
	
	private Solo solo;
	
	//region --- test setup ----------------------------------------------------
	public BiViaMainPageUITest(Class<BiViaMainActivityView> activityClass) {
		super(activityClass);
	}
	
	public BiViaMainPageUITest() {
		super(BiViaMainActivityView.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
				
		myView = getActivity();
		myViewModel = myView._test_getViewModel();
		myTargetContext = getInstrumentation().getTargetContext();
		
		solo = new Solo(getInstrumentation(), getActivity());
		
		myMockViewModel = mock(BiViaMainPageViewModel.class);
		
		myView._test_setViewModel(myMockViewModel);
	}
		
	public void testPreconditions() {	
		assertNotNull(myView);
	}
	
	//endregion --- test setup -------------------------------------------------			
	
	//region --- startup & lifecycle  ------------------------------------------
	public void testStartup_expectCorrectUIElements(){
		testPreconditions();
		
		assertNotNull(myView.findViewById(hu.bivia.bivia.R.id.distanceTextView));
		assertNotNull(myView.findViewById(hu.bivia.bivia.R.id.gps_progress));
		assertNotNull(myView.findViewById(hu.bivia.bivia.R.id.gpsStatusTextView));
		assertNotNull(myView.findViewById(hu.bivia.bivia.R.id.startButton));
		assertNotNull(myView.findViewById(hu.bivia.bivia.R.id.stopButton));		
	}
	
	public void testOnDestroy_expectViewModelCall(){
		testPreconditions();
				
		myView.onDestroy();
		
		verify(myMockViewModel, times(1)).onDestroy();		
	}
	
	//endregion --- startup & lifecycle  ---------------------------------------
	
	//region --- IBiViaView implementations ------------------------------------
	
	@UiThreadTest
	public void testStartButtonClicked(){
		testPreconditions();
		
		myView.enableUI();
		
		solo.clickOnButton(myTargetContext.getString(hu.bivia.bivia.R.string.start));
		
		verify(myMockViewModel, times(1)).startDistanceMeasurement();
		
		assertTrue(((Button)myView.findViewById(hu.bivia.bivia.R.id.stopButton)).isEnabled());
		assertFalse(((Button)myView.findViewById(hu.bivia.bivia.R.id.startButton)).isEnabled());
		assertEquals("0 km", ((TextView)myView.findViewById(hu.bivia.bivia.R.id.distanceTextView)).getText().toString());
	}
	
	@UiThreadTest
	public void testStopButtonClicked(){
		
		myView.enableUI();
		
		testStartButtonClicked();
		
		solo.clickOnButton(myTargetContext.getString(hu.bivia.bivia.R.string.stop));
		
		assertFalse(((Button)myView.findViewById(hu.bivia.bivia.R.id.stopButton)).isEnabled());
		assertTrue(((Button)myView.findViewById(hu.bivia.bivia.R.id.startButton)).isEnabled());
	}
	
	
	public void testShowEnableGPSDialog(){		
		myView.enableUI();
		myView.showEnableGPSDialog();
		
		assertTrue(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_title), true));
		assertTrue(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_prompt), true));
		assertTrue(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_button), true));
	}
	
	@UiThreadTest
	public void testHideEnableGPSDialogByUser(){		
		testPreconditions();
		testShowEnableGPSDialog();
		
		solo.clickOnButton(hu.bivia.bivia.R.string.enable_gps_button);
		
		assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_title), true));
		assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_prompt), true));
		assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_button), true));
	}
	
	public void testHideEnableGPSDialogByViewModel(){
		testPreconditions();
		testShowEnableGPSDialog();
		
		testPreconditions();
		testShowEnableGPSDialog();
		
		myView.hideEnableGPSDialog();
		
		assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_title), true));
		assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_prompt), true));
		assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_button), true));
	}
	
	@UiThreadTest
	public void testDisplayDistance(){
		testPreconditions();
		
		myView.enableUI();
		
		float distance1 = 111;
		float distance2 = 222;
		
		myView.displayDistance(distance1);
		assertTrue(solo.searchText("000.111 km", true));
		
		myView.displayDistance(distance2);
		assertTrue(solo.searchText("000.222 km", true));
	}
	
	@UiThreadTest
	public void testEnableUI(){
		testPreconditions();
		
		myView.enableUI();
		
		// no prompt dialog
		assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_title), true));
		assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_prompt), true));
		assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_button), true));
		
		// buttons disabled, no distance displayed
		assertFalse(solo.getButton(hu.bivia.bivia.R.id.startButton).isEnabled());
		assertFalse(solo.getButton(hu.bivia.bivia.R.id.stopButton).isEnabled());
		assertEquals(myTargetContext.getString(hu.bivia.bivia.R.string.gps_count), 
				solo.getText(hu.bivia.bivia.R.id.distanceTextView).getText().toString());
		
		
	}
	
	public void testDisableUI(){
		testPreconditions();
	}
	
	public void testResetUIButtons(boolean isMeasuring){
		testPreconditions();
	}
	
	public void testShowExitDialog(){
		testPreconditions();
	}
	
	public void _testDisplayGooglePlayErrorDialog(){
		testPreconditions();
		
		// should create some google play error, first
	}
	
	
	//endregion --- IBiViaView implementations ---------------------------------
	
	//region --- utils ---------------------------------------------------------
	
	public static void checkDisabledUI(BiViaMainActivityView view, Context targetContext) {
		TextView distanceTextView = (TextView)view.findViewById(hu.bivia.bivia.R.id.distanceTextView);
		assertEquals(targetContext.getString(hu.bivia.bivia.R.string.gps_count), distanceTextView.getText().toString());
		
		ProgressBar gpsProgress = (ProgressBar)view.findViewById(hu.bivia.bivia.R.id.gps_progress);
		assertEquals(View.VISIBLE, gpsProgress.getVisibility());
		
		TextView gpsStatusTextView = (TextView)view.findViewById(hu.bivia.bivia.R.id.gpsStatusTextView);
		assertEquals(targetContext.getString(hu.bivia.bivia.R.string.gps_booting), gpsStatusTextView.getText().toString());
		
		Button startButton = (Button)view.findViewById(hu.bivia.bivia.R.id.startButton);
		assertFalse(startButton.isEnabled());
		
		Button stopButton = (Button)view.findViewById(hu.bivia.bivia.R.id.stopButton);
		assertFalse(stopButton.isEnabled());		
	}
			
	//endregion --- utils ------------------------------------------------------
}
