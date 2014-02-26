package hu.bivia.bivia.test.uTest.view;

import com.jayway.android.robotium.solo.Solo;

import hu.bivia.model.Measurement;
import hu.bivia.view.BiViaMainActivityView;
import hu.bivia.viewModel.BiViaMainPageViewModel;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.View;
import android.view.ViewGroup;
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
		myTargetContext = getInstrumentation().getTargetContext();
		myMockViewModel = mock(BiViaMainPageViewModel.class);		
		myView._test_setViewModel(myMockViewModel);		
		solo = new Solo(getInstrumentation(), myView);
	}
		
	public void testPreconditions() {	
		assertNotNull(myView);
	}
	
	//endregion --- test setup -------------------------------------------------			
	
	//region --- startup & lifecycle  ------------------------------------------
	public void _testStartup_expectCorrectUIElements(){
		testPreconditions();
		
		assertNotNull(myView.findViewById(hu.bivia.bivia.R.id.distanceTextView));
		assertNotNull(myView.findViewById(hu.bivia.bivia.R.id.startButton));
		assertNotNull(myView.findViewById(hu.bivia.bivia.R.id.stopButton));		
	}
	
	public void _testOnDestroy_expectViewModelCall(){
		testPreconditions();
				
		doThrow(new RuntimeException("onDestroy called on the view model")).when(myMockViewModel).onDestroy();
		
		// no idea why, but this helps...
		try {
		    Thread.sleep(1000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		
		getInstrumentation().runOnMainSync(new Runnable() {
			public void run() {
				try{
					getInstrumentation().callActivityOnDestroy(myView);
				}
				catch(RuntimeException ex){
					assertEquals("onDestroy called on the view model", ex.getMessage());
				}
				catch(Exception ex){
					assertTrue("onDestroy not called on the view model", false);
				}
	        }
	    });
		
		verify(myMockViewModel, times(1)).onDestroy();		
	}
	
	//endregion --- startup & lifecycle  ---------------------------------------
	
	//region --- IBiViaView implementations ------------------------------------
	
	@UiThreadTest
	public void _testStartButtonClicked(){
		testPreconditions();
		
		myView.enableUI();
		
		solo.clickOnButton(myTargetContext.getString(hu.bivia.bivia.R.string.start));
		
		verify(myMockViewModel, times(1)).startDistanceMeasurement();
		
		assertTrue(((Button)myView.findViewById(hu.bivia.bivia.R.id.stopButton)).isEnabled());
		assertFalse(((Button)myView.findViewById(hu.bivia.bivia.R.id.startButton)).isEnabled());
		assertEquals("0 km", ((TextView)myView.findViewById(hu.bivia.bivia.R.id.distanceTextView)).getText().toString());
	}
	
	@UiThreadTest
	public void _testStopButtonClicked(){
		
		myView.enableUI();
		
		_testStartButtonClicked();
		
		solo.clickOnButton(myTargetContext.getString(hu.bivia.bivia.R.string.stop));
		
		assertFalse(((Button)myView.findViewById(hu.bivia.bivia.R.id.stopButton)).isEnabled());
		assertTrue(((Button)myView.findViewById(hu.bivia.bivia.R.id.startButton)).isEnabled());
	}
	
	public void _testShowEnableGPSDialog(){		
		testPreconditions();
		
		myView.runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				myView.showEnableGPSButton();	
			}
		});		
		
		getInstrumentation().waitForIdleSync();
		
		assertTrue(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_title), true));
		assertTrue(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_prompt), true));
		assertTrue(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_button), true));
	}
	
	@UiThreadTest
	public void _testHideEnableGPSDialogByUser(){		
		testPreconditions();
		_testShowEnableGPSDialog();
		
		solo.clickOnButton(hu.bivia.bivia.R.string.enable_gps_button);
		
		assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_title), true));
		assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_prompt), true));
		assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_button), true));
	}
	
	@UiThreadTest
	public void _testHideEnableGPSDialogByViewModel(){
		testPreconditions();
		_testShowEnableGPSDialog();
		
		myView.hideEnableGPSButton();
		
		assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_title), true));
		assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_prompt), true));
		assertFalse(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.enable_gps_button), true));
	}
	
	
	public void _testDisplayDistance(){
		testPreconditions();
		getInstrumentation().waitForIdleSync();
		
		myView.runOnUiThread(new Runnable() {			
			@Override
			public void run() {			
				myView.hideEnableGPSButton();
				myView.enableUI();
				myView.displayDistance(new Measurement(111, 0));
			}
		});
		
		getInstrumentation().waitForIdleSync();
		
		myView.runOnUiThread(new Runnable() {			
			@Override
			public void run() {			
				assertTrue(solo.searchText("000.111 km", true));
				myView.displayDistance(new Measurement(222, 0));
			}
		});
		
		getInstrumentation().waitForIdleSync();
		
		myView.runOnUiThread(new Runnable() {			
			@Override
			public void run() {				
				assertTrue(solo.searchText("000.222 km", true));
			}
		});
	}
	
	@UiThreadTest
	public void _testEnableUI(){
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
		
		
		// GPS progress bar visible
		assertTrue(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.gps_booting), true));
		assertEquals(View.VISIBLE, solo.getView(hu.bivia.bivia.R.id.gps_progress).getVisibility());
	}
	
	public void _testDisableUI(){
		testPreconditions();
		getInstrumentation().waitForIdleSync();
				
		// might have GPS disabled
		myView.runOnUiThread(new Runnable() {			
			@Override
			public void run() {			
				myView.hideEnableGPSButton();	
			}
		});	
		
		getInstrumentation().waitForIdleSync();
		
		// disable the ui
		myView.runOnUiThread(new Runnable() {			
			@Override
			public void run() {			
				myView.disableUI();	
				
				// start enabled, stop disabled, no distance displayed				
				assertTrue(solo.getButton(myTargetContext.getString(hu.bivia.bivia.R.string.start), true).isEnabled());
				assertFalse(solo.getButton(myTargetContext.getString(hu.bivia.bivia.R.string.stop), true).isEnabled());
				assertEquals(myTargetContext.getString(hu.bivia.bivia.R.string.gps_count), 
						solo.getText(hu.bivia.bivia.R.id.distanceTextView).getText().toString());
										
				// GPS progress bar hidden visible
				assertTrue(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.gps_ok), true));
				assertEquals(View.GONE, solo.getView(hu.bivia.bivia.R.id.gps_progress).getVisibility());	
			}
		});
		
	}
	
	@UiThreadTest
	public void _testResetUIButtons_GPSEnabled(){
		testPreconditions();
		
		//when(myMockViewModel.isGPSEnabled()).thenReturn(true);
		
		myView.enableUI();
		
		myView.resetUIButtons(true);

		// start enabled, stop disabled
		assertTrue(solo.getButton(hu.bivia.bivia.R.id.startButton).isEnabled());
		assertFalse(solo.getButton(hu.bivia.bivia.R.id.stopButton).isEnabled());
				
		myView.resetUIButtons(false);
		
		// stop enabled, start disabled
		assertTrue(solo.getButton(hu.bivia.bivia.R.id.stopButton).isEnabled());
		assertFalse(solo.getButton(hu.bivia.bivia.R.id.startButton).isEnabled());
	}
	
	@UiThreadTest
	public void _testResetUIButtons_GPSDisabled(){
		testPreconditions();
		
		//when(myMockViewModel.isGPSEnabled()).thenReturn(true);
		
		myView.enableUI();
		
		myView.resetUIButtons(true);

		// both disabled
		assertFalse(solo.getButton(hu.bivia.bivia.R.id.startButton).isEnabled());
		assertFalse(solo.getButton(hu.bivia.bivia.R.id.stopButton).isEnabled());
				
		myView.resetUIButtons(false);
		
		// stop enabled, start disabled
		assertTrue(solo.getButton(hu.bivia.bivia.R.id.stopButton).isEnabled());
		assertFalse(solo.getButton(hu.bivia.bivia.R.id.startButton).isEnabled());
	}
	
	public void _testShowExitDialog(){
		testPreconditions();
		
		myView.showExitDialog();
		
		assertTrue(solo.searchText(myTargetContext.getString(hu.bivia.bivia.R.string.forced_exit)));
	}
	
	public void _testDisplayGooglePlayErrorDialog(){
		testPreconditions();
		
		// TODO: should create some google play error, first
	}
	
	
	//endregion --- IBiViaView implementations ---------------------------------
	
	//region --- utils ---------------------------------------------------------
	
	public static void checkDisabledUI(BiViaMainActivityView view, Context targetContext) {
		TextView distanceTextView = (TextView)view.findViewById(hu.bivia.bivia.R.id.distanceTextView);
		assertEquals(targetContext.getString(hu.bivia.bivia.R.string.gps_count), distanceTextView.getText().toString());
		
		ProgressBar gpsProgress = (ProgressBar)view.findViewById(hu.bivia.bivia.R.id.gps_progress);
		assertEquals(View.VISIBLE, gpsProgress.getVisibility());
		
		ViewGroup gpsWaiting = (ViewGroup)view.findViewById(hu.bivia.bivia.R.id.gpsWaitingGroup);
		assertEquals(View.VISIBLE, gpsWaiting.getVisibility());
		
		Button startButton = (Button)view.findViewById(hu.bivia.bivia.R.id.startButton);
		assertFalse(startButton.isEnabled());
		
		Button stopButton = (Button)view.findViewById(hu.bivia.bivia.R.id.stopButton);
		assertFalse(stopButton.isEnabled());		
	}

	//endregion --- utils ------------------------------------------------------
}
