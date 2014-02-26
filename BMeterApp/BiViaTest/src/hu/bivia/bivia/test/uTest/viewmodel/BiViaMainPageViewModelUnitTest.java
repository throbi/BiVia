package hu.bivia.bivia.test.uTest.viewmodel;

import hu.bivia.logic.Measurer;
import hu.bivia.model.Measurement;
import hu.bivia.view.BiViaMainActivityView;
import hu.bivia.viewModel.BiViaMainPageViewModel;
import android.test.ActivityInstrumentationTestCase2;

import static org.mockito.Mockito.*;

/**
 * Unit tests for the main page view model
 */
public class BiViaMainPageViewModelUnitTest 
	extends
		ActivityInstrumentationTestCase2<BiViaMainActivityView>{

	private BiViaMainActivityView myMockView;
	private BiViaMainPageViewModel myTestTarget;
	
	//region --- test setup ----------------------------------------------------
	
	public BiViaMainPageViewModelUnitTest(Class<BiViaMainActivityView> activityClass) {
		super(activityClass);
	}

	public BiViaMainPageViewModelUnitTest() {
		super(BiViaMainActivityView.class);
	}
	
	@Override
	protected void setUp() throws Exception {	
		super.setUp();
		
		myMockView = mock(BiViaMainActivityView.class);
		myTestTarget = new BiViaMainPageViewModel(myMockView);
	}
	
	public void testPreconditions() {
		assertNotNull(myMockView);
		assertNotNull(myTestTarget);
	}
	
	//endregion --- test setup -------------------------------------------------

	//region --- Lifecycle management ------------------------------------------	
	
	public void testCreation_OK(){
		testPreconditions();
		
		// the rest is alredy tested on the UI
	}
	
	// creation is tested  by integration tests:
	// 		testStartup_GPSEnabled_expectDisabledUI
	//		testStartup_GPSDisabled_expectDisabledUIAndPromptDialog
	
	//endregion --- Lifecycle management ---------------------------------------

	//region --- calling Measurer API ------------------------------------------		
	
	public void testStartMeasurement(){
		
		Measurer mockMeasurer = mock(Measurer.class);		
		when(mockMeasurer.getIsMeasuring()).thenReturn(true);
				
		myTestTarget._test_setMeasurer(mockMeasurer);						
		
		myTestTarget.startDistanceMeasurement();
		
		assertTrue(myTestTarget.getIsMeasuring());
		
		verify(mockMeasurer, times(1)).startMeasuring();
		verify(mockMeasurer, times(1)).getIsMeasuring();
	}
	
	public void testStopMeasurement(){
		
		Measurer mockMeasurer = mock(Measurer.class);		
		when(mockMeasurer.getIsMeasuring()).thenReturn(false);
				
		myTestTarget._test_setMeasurer(mockMeasurer);						
		
		myTestTarget.stopDistanceMeasurement();
		
		assertFalse(myTestTarget.getIsMeasuring());
		
		verify(mockMeasurer, times(1)).stopMeasuring();
		verify(mockMeasurer, times(1)).getIsMeasuring();
	}
	
	public void testGetIsMeasuring(){
		Measurer mockMeasurer = mock(Measurer.class);				
		myTestTarget._test_setMeasurer(mockMeasurer);	
		
		myTestTarget.getIsMeasuring();
		
		verify(mockMeasurer, times(1)).getIsMeasuring();
	}
	//endregion --- calling Measurer API ---------------------------------------

	//region --- handling calls from the measurer ------------------------------
	
	public void testReportGPSEnabled(){
		myTestTarget.reportGPSEnabled();
		verify(myMockView, times(1)).hideEnableGPSButton();
	}
	
	public void testReportMeasuredDistance(){
		Measurement measurement = new Measurement(111, 0);
		myTestTarget.reportMeasurement(measurement);
		verify(myMockView, times(1)).displayDistance(measurement);
	}
	
	public void testReportNoGPSService(){
		myTestTarget.reportNoGPSService();
		verify(myMockView, times(1)).showExitDialog();
	}	
	
	public void testRequestEnableGPS(){		
		myTestTarget.requestEnableGPS();		
		verify(myMockView, times(1)).showEnableGPSButton();
	}
	
	public void testReportIsGPSFixed(){
		myTestTarget.reportIsGPSFixed(true);
		verify(myMockView, times(1)).enableUI();

		myTestTarget.reportIsGPSFixed(false);
		verify(myMockView, times(1)).disableUI();
	}
	
	public void testReportIsMeasuring(){
		myTestTarget.reportIsMeasuring(true);
		verify(myMockView, times(1)).resetUIButtons(true);
	
		myTestTarget.reportIsMeasuring(false);
		verify(myMockView, times(1)).resetUIButtons(false);
	}
	//endregion --- handling calls from the measurer ---------------------------
	
}
