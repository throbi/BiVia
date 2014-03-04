package hu.bivia.viewModel;

import java.util.List;

import hu.bivia.R;
import hu.bivia.logic.BamUploader;
import hu.bivia.logic.Measurer;
import hu.bivia.logic.dataAccess.BiViaDataAccessHelper;
import hu.bivia.model.MeasuredDay;
import hu.bivia.model.Measurement;
import hu.bivia.model.Ride;
import hu.bivia.view.BiViaMainActivityView;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.Toast;

public class BiViaMainPageViewModel {

	private BiViaMainActivityView myView;
	
	private Measurer myMeasurer;

	public static final String APPTAG = "BiViaDebugTag";
	
	
	private boolean myUserWantsToExitWhileThereIsNoPointToUseThisAppWithNoGPSHardware = false;

	private ExpandableListView myMeasuredDaysListView;

	public void setUserWantsToExitWhileThereIsNoPointToUseThisAppWithNoGPSHardware(boolean newValue) {
		myUserWantsToExitWhileThereIsNoPointToUseThisAppWithNoGPSHardware = newValue;
	}
	
	//region --- injections for testing - !!! REMOVE FROM RELEASE !!! ----------

	/**
	 * Needed for mocking.
	 */
	public BiViaMainPageViewModel() {
	}	
	
	/**
	 * Allows injection for test cases. Must be removed from releases!
	 * @param mockMeasurer
	 */
	public Measurer _test_getMeasurer() {
		return myMeasurer;
	}
	
	public void _test_setMeasurer(Measurer mockMeasurer) {
		myMeasurer = mockMeasurer;
	}
	//endregion --- injections for testing - !!! REMOVE FROM RELEASE !!! -------

	//region --- Lifecycle management ------------------------------------------
	public static final String EXTRA_MESSAGE = "hu.bivia.bivia.MESSAGE";		
	
	/**
	 * Prepare the GPS
	 * @param activityView 
	 */
	public BiViaMainPageViewModel(BiViaMainActivityView activityView) {
		if(activityView != null){
			myView = activityView;			
		} else {
			forceExit();
		}
		
		myMeasurer = new Measurer(this, activityView);
		myDataHelper = new BiViaDataAccessHelper(myView);
	}
	
	public void onUICreate(Bundle savedInstanceState) {
		myMeasurer.initialize();
		myMeasuredDaysListView = (ExpandableListView) myView.findViewById(R.id.measuredDays);
		displayRidesFromDB();
	}    

	public void onDestroy(){
		if(myUserWantsToExitWhileThereIsNoPointToUseThisAppWithNoGPSHardware){
			forceExit();			
		}			
	}
	
	public static void forceExit() {
		Log.d(APPTAG, "Exiting...");
		android.os.Process.killProcess(android.os.Process.myPid());		
	}
    //endregion --- Lifecycle management ---------------------------------------

	//region --- handle user input ---------------------------------------------
		
	/**
	 * User wants to start the distance measurement
	 */
	public void startDistanceMeasurement() {
		myMeasurer.startMeasuring();		
	}
	
	/**
	 * User wants to stop the distance measurement
	 */
	public void stopDistanceMeasurement() {
		myMeasurer.stopMeasuring();
	}
	
	/** The user wants to delete a ride */
	public void deleteRide(final Ride ride) {
		
		// ask confirmation
		myView.showYesNoDialog(
				hu.bivia.R.string.delete_ride_title, 
				hu.bivia.R.string.delete_ride_message, 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int button) {
						if(button == DialogInterface.BUTTON_POSITIVE){
							// delete from db
							myDataHelper.deleteRide(ride);
							// delete from view & refresh
							myView.deleteRide(ride);
						}
						
					}
				});
		
		// refresh UI
		
	}
	
	/** The user wants to upload a measured day */
	public void uploadeMeasuredDay(MeasuredDay measuredDay) {
		
		// allow multiple simultaneous uploads
		(new BamUploader(myView, this)).uploadMeasuredDay(measuredDay);
	}
	
	//endregion --- handle user input ------------------------------------------

	//region --- handle db -----------------------------------------------------
	
	private BiViaDataAccessHelper myDataHelper;
	
	private void displayRidesFromDB() {
		List<Ride> rides = myDataHelper.getAllRides();
		
		for(int i=0; i< rides.size(); i++){
			displayRide(rides.get(i));
		}
	}
	
	private void saveRide(Ride ride) {
		myDataHelper.saveRide(ride);
	}
	//endregion --- handle db --------------------------------------------------
	
	//region --- call-backs ----------------------------------------------------
	
	/**
	 * Notifies the viewm model about measured distance update
	 * @param measurement
	 */
	public void reportMeasurement(Measurement measurement){		
		myView.displayDistance(measurement);
	}
	
	/**
	 * Called by the Measurer to report a completed ride.
	 * @param ride
	 */
	public void reportRide(Ride ride) {
		displayRide(ride);
		saveRide(ride);
	}

	/**
	 * The measurer needs the GPS to be enabled
	 */
	public void requestEnableGPS() {
		myView.disableUI();
		myView.showEnableGPSButton();				
	}
	
	/**
	 * The measurer is happy that GPS is enabled
	 */
	public void reportGPSEnabled() {
		myView.hideEnableGPSButton();		
	}	
	
	/**
	 * The measurer needs to display an error dialog from the depths of Google
	 * Play Location Service
	 * @param resultCode
	 * @param requestCode
	 */
	public void displayGooglePlayErrorDialog(int resultCode,int requestCode) {
		myView.displayGooglePlayErrorDialog(resultCode, requestCode);
	}
	
	/**
	 * Called by the measurer when there is no GPS on the device
	 */
	public void reportNoGPSService() {
		myView.showExitDialog();		
	}
	
	/**
	 * Called by the measurer whenever the measured distance is updated
	 * @param myIsMeasuring
	 */
	public void reportIsMeasuring(boolean myIsMeasuring) {
		myView.resetUIButtons(myIsMeasuring);		
	}
	
	/**
	 * Called by the measurer on GPS fix or when GPS signal is lost
	 * @param gpsIsFixed
	 */
	public void reportIsGPSFixed(boolean gpsIsFixed) {
		if(gpsIsFixed){
			myView.enableUI();
		} else {
			myView.disableUI();
		}
		
	}
	
	/**
	 * Returns the measurer's state
	 * @return true or false
	 */
	public boolean getIsMeasuring() {		
		return myMeasurer.getIsMeasuring();
	}
	
	/**
	 * Returns whether the GPS is enabled or not
	 * @return true or false
	 */
	public boolean isGPSEnabled() {
		return myMeasurer.isGPSEnabled();
	}
	
	/**
	 * Called from the UI when the activity gets a result from outside (e.g. 
	 * Google Play Location Service could not fix itself)
	 * @param requestCode
	 * @param resultCode
	 * @param intent
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		myMeasurer.onActivityResult(requestCode, resultCode, intent);		
	}
	
	/** TODO: refactor ASAP! the view model should not be called to do this */
	public void expandMeasuredDay(int groupNumber) {
		myMeasuredDaysListView.expandGroup(groupNumber);
	}

	/** TODO: refactor ASAP! the view model should not be called to do this */
	public void collapseMeasuredDay(int groupNumber) {
		myMeasuredDaysListView.collapseGroup(groupNumber);
	}

	/** notifies the user to enable networking */
	public void requestEnableNetwork() {
		// TODO: make proper dialog with buttons to enable Wifi or mobile net
		Toast.makeText(myView, myView.getText(R.string.enable_internet), Toast.LENGTH_LONG).show();
	}
	
	/** uploader calls this when it is ready */
	public void uplodFinished(MeasuredDay day) {
		myView.uploadFinished(day);
	}
	//endregion --- call-backs -------------------------------------------------
	
	//region --- notify UI -----------------------------------------------------
	private void displayRide(Ride ride) {
		myView.displayRide(ride);
	}
	//endregion --- notify UI --------------------------------------------------

}
