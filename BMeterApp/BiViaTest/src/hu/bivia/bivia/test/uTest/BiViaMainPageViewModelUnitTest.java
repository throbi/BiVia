package hu.bivia.bivia.test.uTest;

import org.mockito.internal.util.reflection.Whitebox;

import hu.bivia.bivia.View.BiViaMainActivityView;
import hu.bivia.bivia.View.IBiViaView;
import hu.bivia.bivia.ViewModel.BiViaMainPageViewModel;
import android.test.ActivityInstrumentationTestCase2;

import static org.mockito.Mockito.*;

/**
 * Unit tests for the main page view model
 */
public class BiViaMainPageViewModelUnitTest 
	extends
		ActivityInstrumentationTestCase2<BiViaMainActivityView>{

	private IBiViaView myMockView;
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
		
		myMockView = mock(IBiViaView.class);
		myTestTarget = new BiViaMainPageViewModel(myMockView);
	}
	
	private void testPreconditions() {
		assertNotNull(myMockView);
		assertNotNull(myTestTarget);
	}
	
	//endregion --- test setup -------------------------------------------------

	//region --- Lifecycle management ------------------------------------------	
	
	public void testCreation_OK(){
		testPreconditions();
	}
	
	// creation is tested  by integration tests:
	// 		testStartup_GPSEnabled_expectDisabledUI
	//		testStartup_GPSDisabled_expectDisabledUIAndPromptDialog
	
	//endregion --- Lifecycle management ---------------------------------------

	//region --- handle user input ---------------------------------------------
	
	public void testStartMeasurement_serviceConnected_expectMeasurementStart(){
		BiViaMainPageViewModel mockTarget = mock(BiViaMainPageViewModel.class);
		Whitebox.setInternalState(mockTarget, "myDistance", 11.1);		
		
		when(mockTarget.servicesConnected()).thenReturn(true);
		
		mockTarget.startDistanceMeasurement();
		
		assertTrue(mockTarget.getIsMeasuring());
		assertEquals(0.0, Whitebox.getInternalState(mockTarget, "myDistance"));
		
		verify(mockTarget, times(1)).servicesConnected();
	}
	
	public void testStartMeasurement_serviceNotConnected_expectNothing(){
		BiViaMainPageViewModel mockTarget = mock(BiViaMainPageViewModel.class);
		Whitebox.setInternalState(mockTarget, "myDistance", 11.1);		
		
		when(mockTarget.servicesConnected()).thenReturn(false);
		
		mockTarget.startDistanceMeasurement();
		
		assertFalse(mockTarget.getIsMeasuring());
		assertEquals(11.1, Whitebox.getInternalState(mockTarget, "myDistance"));
		
		verify(mockTarget, times(0)).servicesConnected();
	}
	//endregion --- handle user input ------------------------------------------
}
