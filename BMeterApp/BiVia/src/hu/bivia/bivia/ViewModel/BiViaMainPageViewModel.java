package hu.bivia.bivia.ViewModel;

import hu.bivia.bivia.R;
import hu.bivia.bivia.View.IBiViaView;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class BiViaMainPageViewModel implements 
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener, 
	LocationListener,
	GpsStatus.Listener{

	private IBiViaView myView;
	
	/**
	 * MVVM on Android :)
	 * View must be an Activity, but there is no interface for it...
	 */
	private Activity myActivity;

	public static final String APPTAG = "BiViaDebugTag";
	private float myDistance;
	
	private boolean myUserWantsToExitWhileThereIsNoPointToUseThisAppWithNoGPSHardware = false;
	public void setUserWantsToExitWhileThereIsNoPointToUseThisAppWithNoGPSHardware(boolean newValue) {
		myUserWantsToExitWhileThereIsNoPointToUseThisAppWithNoGPSHardware = newValue;
	}
	public boolean getUserWantsToExitWhileThereIsNoPointToUseThisAppWithNoGPSHardware(){
		return myUserWantsToExitWhileThereIsNoPointToUseThisAppWithNoGPSHardware;
	}
	
	//region --- injections for testing - !!! REMOVE FROM RELEASE !!! ----------

	/**
	 * Allows injection for test cases. Must be removed from releases!
	 * @param mockLocationManager
	 */
	public void _test_setLocacationManager(LocationManager mockLocationManager){		
		if(myLocationManager != null){
			myLocationManager.removeGpsStatusListener(this);
			myView.hideEnableGPSDialog();
		}
		
		myLocationManager = mockLocationManager;			
	}
	
	//endregion --- injections for testing - !!! REMOVE FROM RELEASE !!! -------

	//region --- Lifecycle management ------------------------------------------
	public static final String EXTRA_MESSAGE = "hu.bivia.bivia.MESSAGE";		
	
	/**
	 * Prepare the GPS
	 * @param view 
	 */
	public BiViaMainPageViewModel(IBiViaView view) {
		if(view != null){
			myView = view;			
			myActivity = (Activity)view;
		} else {
			forceExit();
		}		
	}
	
	public void onUICreate(Bundle savedInstanceState) {
		setupGPS();
	    checkForEnabledGPS();        
	}    

	public void onDestroy(){
		if(myUserWantsToExitWhileThereIsNoPointToUseThisAppWithNoGPSHardware){
			forceExit();			
		}			
	}
	
	private void forceExit() {
		android.os.Process.killProcess(android.os.Process.myPid());		
	}
    //endregion --- Lifecycle management ---------------------------------------

	//region --- handle user input ---------------------------------------------
		
	/**
	 * User wants to start the distance measurement
	 */
	public void startDistanceMeasurement() {
		if(servicesConnected()){
    		myDistance = 0;
    		myView.displayDistance(myDistance);
    		setIsMeasuring(true);    		    		    		 	
    	}		
	}
	
	/**
	 * User wants to stop the distance measurement
	 */
	public void stopDistanceMeasurement() {
		setIsMeasuring(false);
    	
    	// TODO: add to list...		
	}
	
	//endregion --- handle user input ------------------------------------------
	
	//region --- GPS stuff -----------------------------------------------------
    
    /**
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int UPDATE_INTERVAL_IN_MILISECONDS = 1 * 1000;
    private static final float SMALLEST_DISPLACEMENT_TO_REPORT_IN_METERS = (float)1;
    
    private LocationClient myLocationClient;
    private Location myLocation;
    private LocationRequest myLocationRequest;
    private LocationManager myLocationManager;
    
    private boolean myIsGPSEnabled = false;
    private boolean myIsGPSInitialized = false;
    public boolean getIsGPSInitializedK(){ 
    	return myIsGPSInitialized; 
    }           
    public void setIsGPSInitialized(boolean newValue){
    	myIsGPSInitialized = newValue;
    	
    	if(myIsGPSInitialized){
    		myView.enableUI();
    	} else {
    		setIsMeasuring(false);
    		myView.disableUI();
    	}    		
    }
    
    private boolean myIsMeasuring = false;
    public boolean getIsMeasuring(){
    	return myIsMeasuring;
    }    
    public void setIsMeasuring(boolean newValue){
    	myIsMeasuring = newValue;
    	myView.resetUIButtons(myIsMeasuring);
    }
    
    /**
     * Prompts the user if GPS is disabled
     */
    private void checkForEnabledGPS(){
    	if(myLocationManager == null){
    		myLocationManager = (LocationManager) 
    				myActivity.getSystemService(android.content.Context.LOCATION_SERVICE);
    	}
    		
    	if(myLocationManager != null){
    		if (!myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
    			// user should enable GPS
    			myView.showEnableGPSDialog();
    		}     		
    	} else {
    		// no GPS service
    		myView.showExitDialog();    			
    	}
    }    	    	    		    
    
    /**
     * Sets up GPS and starts updates
     */
    private void setupGPS() {
        myLocationClient = new LocationClient(myActivity, this, this);
        myLocationClient.connect();
		
        myLocationRequest = new LocationRequest();
        myLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILISECONDS);
        myLocationRequest.setFastestInterval(UPDATE_INTERVAL_IN_MILISECONDS);
        myLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT_TO_REPORT_IN_METERS);
        myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}
    
    /**
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:
                        // Log the result
                        Log.d(APPTAG, getString(R.string.problem_resolved_by_play_services));
                    break;

                    // If any other result was returned by Google Play services
                    default:
                        // Log the result
                        Log.d(APPTAG, getString(R.string.problem_not_resolved_by_play_services));
                        setIsGPSInitialized(false);
                    break;
                }
            default:
               Log.d(APPTAG, getString(R.string.unknown_activity_request_code) + requestCode);
               break;
        }
    }
    
	/**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(myActivity);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
        	myView.displayGooglePlayErrorDialog(resultCode, 0);            
            return false;
        }
    }   

	public boolean isGPSEnabled() {
		return myIsGPSEnabled;
	}
	
	//endregion --- GPS stuff --------------------------------------------------
	
	//region --- interface implementations -------------------------------------
	
    /**
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult((Activity)myView, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            myView.displayGooglePlayErrorDialog(connectionResult.getErrorCode(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
        }
	}

	/**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, one can
     * request the current location or start periodic updates
     */
	@Override
	public void onConnected(Bundle bundle) {
		// does not mean that the GPS is on, wait for the first update
		requestGPSUpdates();
	}
	
	/**
	 * Tells the location clinet to send some location updates
	 */
	public void requestGPSUpdates(){
		if(servicesConnected()){
			myLocationManager.addGpsStatusListener(this);
			myLocationClient.requestLocationUpdates(myLocationRequest, this);
		}
	}

	/**
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
	@Override
	public void onDisconnected() {
		setIsGPSInitialized(false);
	}
	
	@Override
	public void onLocationChanged(Location newLocation) {
		if(newLocation != null){
			
			if(!getIsGPSInitializedK()){
				setIsGPSInitialized(true);				
			} else if (!myIsGPSEnabled){				
				myIsGPSEnabled = true;
				myView.enableUI();
			}
			
			if(getIsMeasuring()){
				if(myLocation != null){
					myDistance += newLocation.distanceTo(myLocation);
					myView.displayDistance(myDistance);
				}
			
				myLocation = newLocation;
			}
		}
	}
    
	@Override
	public void onGpsStatusChanged(int event) {
		switch (event) {        
        	case GpsStatus.GPS_EVENT_STOPPED:
        		myIsGPSEnabled = false;
        		myView.showEnableGPSDialog();
        		myView.disableUI();
        		break;
        	case GpsStatus.GPS_EVENT_STARTED:
        		myIsGPSEnabled = true;
        		myView.hideEnableGPSDialog();        		
        		break;
		}
		
	}
	//endregion --- interface implementations ----------------------------------
		
	//region --- utils ---------------------------------------------------------	    
    /**
     * Gets localized string from the resources
     * @param resId
     * @return
     */
    private String getString(int resId) {
    	return myActivity.getString(resId); 
	}
   //endregion --- utils -------------------------------------------------------	
	

}
