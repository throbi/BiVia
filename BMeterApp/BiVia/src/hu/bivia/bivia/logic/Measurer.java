package hu.bivia.bivia.logic;

import java.util.Date;

import hu.bivia.bivia.R;
import hu.bivia.bivia.model.Measurement;
import hu.bivia.bivia.model.Ride;
import hu.bivia.bivia.viewModel.BiViaMainPageViewModel;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

/**
 * Handles GPS related stuff
 */
public class Measurer 
	implements 
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, 
		LocationListener,
		GpsStatus.Listener{

	/**
	 * MVVM on Android :)
	 * View must be an Activity, but there is no interface for it...
	 */
	private Activity myActivity;
		
	private BiViaMainPageViewModel myViewModel;
	
	/**
	 * Measured distance in meters
	 */
	private float myDistanceMeters;

	private long myStartTimeMillis;
	
	private Date myStartTime;

	//region --- injections for testing - !!! REMOVE FROM RELEASE !!! ----------

	/**
	 * Needed for mocking.
	 */
	public Measurer() {
	}				
		
	/**
	 * Allows injection for test cases. Must be removed from releases!
	 * @param mockLocationManager
	 */
	public void _test_setLocacationManager(LocationManager mockLocationManager){		
		if(myLocationManager != null){
			myLocationManager.removeGpsStatusListener(this);
			myViewModel.reportGPSEnabled();
		}
		
		myLocationManager = mockLocationManager;			
	}
	
	/**
	 * Allows injection for test cases. Must be removed from releases!
	 * @param mockLocationManager
	 */
	public void _test_setViewModle(BiViaMainPageViewModel mockViewModel) {
		myViewModel = mockViewModel;
	}
		
	//endregion --- injections for testing - !!! REMOVE FROM RELEASE !!! -------
	
	//region --- Lifecycle management ------------------------------------------
	public Measurer(BiViaMainPageViewModel viewModel, Activity activity) {
		if(viewModel == null){
			Log.d(BiViaMainPageViewModel.APPTAG, "Initialized with null view model!");
		}
		if(activity == null){
			Log.d(BiViaMainPageViewModel.APPTAG, "Initialized with null activity!");
		}
		
		if(viewModel == null || activity == null){		
			BiViaMainPageViewModel.forceExit();
		}	
		
		myActivity = activity;
		myViewModel = viewModel;
	}
	
	/**
	 * Sets up the measurer, must be called ASAP.
	 */
	public void initialize() {
		setupGPS();
	    checkForEnabledGPS();		
	}
	//endregion --- Lifecycle management ---------------------------------------
	
	//region --- public API ---------------------------------------------------
	
	/**
	 * Check for GPS service. Resets counter and position then starts measuring.
	 */
	public void startMeasuring() {
		if(areServicesConnected()){
    		myDistanceMeters = 0;
    		myLatestLocation = null;
    		
    		// this is for measuring elapsed milliseconds
    		myStartTimeMillis = SystemClock.elapsedRealtime();
    		myElapsedTimeMillis = 0;
    		
    		// this is for knowing when a ride starts in local time
    		myStartTime = new Date();
    		setIsMeasuring(true);    		    		    		 	
    	} else {
    		myViewModel.reportNoGPSService();
    	}		
	}
		
	/**
	 * Stops the current measurement.
	 */
	public void stopMeasuring() {
		setIsMeasuring(false);
		
		reportRide();
	}
	//endregion --- public API -------------------------------------------------
	
	//region --- GPS stuff -----------------------------------------------------        

	/**
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int UPDATE_INTERVAL_IN_MILISECONDS = 1 * 1000;
    private static final float SMALLEST_DISPLACEMENT_TO_REPORT_IN_METERS = (float)1;
    
    private LocationClient myLocationClient;
    private Location myLatestLocation;
    private LocationRequest myLocationRequest;
    private LocationManager myLocationManager;
    
    private boolean myIsGPSEnabled = false;
    private boolean myIsGPSFixed = false;
	public boolean getIsGPSFixed(){ 
    	return myIsGPSFixed; 
    }           
    public void setIsGPSFixed(boolean newValue){
    	myIsGPSFixed = newValue;
    	
    	if(myIsGPSFixed){
    		myViewModel.reportIsGPSFixed(true);
    	} else {
    		setIsMeasuring(false);
    		myViewModel.reportIsGPSFixed(false);
    	}    		
    }
    
    private boolean myIsMeasuring = false;

	private float myAverageSpeed;

	private long myElapsedTimeMillis;
	
    public synchronized boolean getIsMeasuring(){
    	return myIsMeasuring;
    }    
    public synchronized void setIsMeasuring(boolean newValue){
    	myIsMeasuring = newValue;    	
    	myViewModel.reportIsMeasuring(myIsMeasuring);
    }
    
    /**
     * Prompts the user if GPS is disabled
     */
    public void checkForEnabledGPS(){
    	if(myLocationManager == null){
    		myLocationManager = (LocationManager) 
    				myActivity.getSystemService(android.content.Context.LOCATION_SERVICE);
    	}
    		
    	if(myLocationManager != null){
    		if (!myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
    			// user should enable GPS
    			myViewModel.requestEnableGPS();
    		} else {
    			myIsGPSEnabled = true;
    		}
    			
    	} else {
    		myViewModel.reportNoGPSService();
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
                        Log.d(BiViaMainPageViewModel.APPTAG, getString(R.string.problem_resolved_by_play_services));
                    break;

                    // If any other result was returned by Google Play services
                    default:
                        // Log the result
                        Log.d(BiViaMainPageViewModel.APPTAG, getString(R.string.problem_not_resolved_by_play_services));
                        setIsGPSFixed(false);
                    break;
                }
            default:
               Log.d(BiViaMainPageViewModel.APPTAG, getString(R.string.unknown_activity_request_code) + requestCode);
               break;
        }
    }
    
	/**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    public boolean areServicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(myActivity);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(BiViaMainPageViewModel.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
        	myViewModel.displayGooglePlayErrorDialog(resultCode, 0);            
            return false;
        }
    }   

	public boolean isGPSEnabled() {
		return myIsGPSEnabled;
	}
	
	/**
	 * Notifies the view model about a new measurement.
	 */
	private void reportMeasurement() {
		// milliseconds
		myElapsedTimeMillis = (SystemClock.elapsedRealtime() - myStartTimeMillis);
		
		// OK to skip, will be calculated on next hit
		if(myElapsedTimeMillis > 0){
			myAverageSpeed = calculateSpeedInKmPerHour(myDistanceMeters / 1000F, myElapsedTimeMillis);
			myViewModel.reportMeasurement(new Measurement(myDistanceMeters / 1000, myAverageSpeed));
		} 
	}

	/**
	 * Notifies the view model about a new measured ride i.e. when the STOP
	 * button is pressed on the UI. Time and distance until the last GPS update 
	 * is reported (not until the push of the STOP button).
	 */
	private void reportRide() {
		// do not report zero rides
		if(myElapsedTimeMillis > 0 && myDistanceMeters > 0){	
			Ride ride = new Ride(myStartTime, myDistanceMeters / 1000, myAverageSpeed, myElapsedTimeMillis);
			myViewModel.reportRide(ride);
		}
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
                connectionResult.startResolutionForResult(myActivity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
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
            myViewModel.displayGooglePlayErrorDialog(connectionResult.getErrorCode(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
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
		if(areServicesConnected()){
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
		setIsGPSFixed(false);
	}
	
	@Override
	public void onLocationChanged(Location newLocation) {
		if(newLocation != null){
			
			if(!getIsGPSFixed()){
				setIsGPSFixed(true);				
			} else if (!myIsGPSEnabled){				
				myIsGPSEnabled = true;
				setIsGPSFixed(true);				
			}
			
			if(getIsMeasuring()){
				if(myLatestLocation != null){
					myDistanceMeters += newLocation.distanceTo(myLatestLocation);
					reportMeasurement();
				}
			
				myLatestLocation = newLocation;
			}
		}
	}
    
	@Override
	public void onGpsStatusChanged(int event) {
		switch (event) {        
        	case GpsStatus.GPS_EVENT_STOPPED:
        		myIsGPSEnabled = false;
        		myViewModel.requestEnableGPS();
        		break;
        	case GpsStatus.GPS_EVENT_STARTED:
        		myIsGPSEnabled = true;        		
        		myViewModel.reportGPSEnabled();
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
   
    /**
     * Readable calculation.
     * @param distanceKm
     * @param elapsedTimeMs
     * @return Km/h
     */
    public static float calculateSpeedInKmPerHour(float distanceKm, long elapsedTimeMs ){
    	float timeSeconds = (float)elapsedTimeMs / 1000;
    	float timeHours = timeSeconds / 3600F;
    	return distanceKm / timeHours;
    }
    //endregion --- utils -------------------------------------------------------				

}
