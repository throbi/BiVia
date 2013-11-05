package hu.bivia.bivia.test.uTest;

import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import hu.bivia.bivia.View.BiViaMainActivityView;
import hu.bivia.bivia.View.IBiViaView;
import hu.bivia.bivia.ViewModel.BiViaMainPageViewModel;
import android.test.ActivityInstrumentationTestCase2;

import static org.mockito.Mockito.*;

/**
 * Unit tests for the main page view model
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BiViaMainPageViewModel.class)
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
		BiViaMainPageViewModel mockTarget = PowerMockito.mock(BiViaMainPageViewModel.class);
		Whitebox.setInternalState(mockTarget, "myDistance", 11.1);		
		
		try {
			PowerMockito.when(mockTarget, "servicesConnected").thenReturn(true);
		} catch (Exception e) {
			e.printStackTrace();
			assertFalse("failed to mock private method 'servicesConnected()'", true);
		}
		
		mockTarget.startDistanceMeasurement();
		
		assertTrue(mockTarget.getIsMeasuring());
		assertEquals(0.0, Whitebox.getInternalState(mockTarget, "myDistance"));
		
		try {
			PowerMockito.verifyPrivate(mockTarget, times(1));
		} catch (Exception e) {
			e.printStackTrace();
			assertFalse("failed to verify mocked private method 'servicesConnected()'", true);
		}
	}
	
	public void testStartMeasurement_serviceNotConnected_expectNothing(){
		BiViaMainPageViewModel mockTarget = PowerMockito.mock(BiViaMainPageViewModel.class);
		Whitebox.setInternalState(mockTarget, "myDistance", 11.1);		
		
		try {
			PowerMockito.when(mockTarget, "servicesConnected").thenReturn(false);
		} catch (Exception e) {
			e.printStackTrace();
			assertFalse("failed to mock private method 'servicesConnected()'", true);
		}
		
		mockTarget.startDistanceMeasurement();
		
		assertFalse(mockTarget.getIsMeasuring());
		assertEquals(11.1, Whitebox.getInternalState(mockTarget, "myDistance"));
		
		try {
			PowerMockito.verifyPrivate(mockTarget, times(0));
		} catch (Exception e) {
			e.printStackTrace();
			assertFalse("failed to verify mocked private method 'servicesConnected()'", true);
		}
	}
	//endregion --- handle user input ------------------------------------------
}
