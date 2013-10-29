package hu.bmeter.bmeter;

import java.text.DecimalFormat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class BMeterMainActivity 
	extends 
		FragmentActivity 
	implements 
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, 
		LocationListener 
	{  
	
	public static final String APPTAG = "BiViaDebugTag";
	
	private float myDistance;
	private boolean myUserWantsToExitWhileThereIsNoPointToUseThisAppWithNoGPSHardware = false;
	
    //region --- Lifecycle management ------------------------------------------
	public static final String EXTRA_MESSAGE = "hu.bmeter.bmeter.MESSAGE";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                      
        
        setContentView(R.layout.activity_bmeter_main);
        getUIElements();
        DisableUI();
        
        checkForEnabledGPS();
        
        setupGPS();                               
    }    

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.bmeter_main, menu);
        return true;
    }    
           
	@Override
	public void onDestroy(){
		if(myUserWantsToExitWhileThereIsNoPointToUseThisAppWithNoGPSHardware){
			android.os.Process.killProcess(android.os.Process.myPid());			
		}
		super.onDestroy();
	}
	
    //endregion --- Lifecycle management ---------------------------------------
    
    //region --- UI handling ---------------------------------------------------
	
	private DecimalFormat myDecimalFormatter = new DecimalFormat("000.000");
	
	private TextView myGPSStateView, myDistanceTextView;	
	private Button myStartButton, myStopButton;
	private ProgressBar myGPSProgressBar;
	
	public void getUIElements(){	
		myGPSProgressBar = (ProgressBar)findViewById(R.id.gps_progress);
		myGPSStateView = (TextView)findViewById(R.id.gpsStatusTextView);
		myDistanceTextView = (TextView)findViewById(R.id.distanceTextView);
		myStartButton = (Button)findViewById(R.id.startButton);
		myStopButton = (Button)findViewById(R.id.stopButton);
	}
	
	/**
	 * Hides the GPS init. animation and enables the controls
	 */
	public void EnableUI(){
		Toast.makeText(this, R.string.gps_connected, Toast.LENGTH_SHORT).show();
		myGPSStateView.setText(R.string.gps_ok);
		myGPSProgressBar.setVisibility(View.GONE);		
		ResetUIButtons(false);		
	}	
	
	/**
	 * Disables controls, shows a GPS init. animation
	 */
	public void DisableUI(){
		myStartButton.setEnabled(false);
		myStopButton.setEnabled(false);
	}
	
	/**
	 * Resets Start/Stop based on the input.
	 * @param isMeasuring
	 */
	public void ResetUIButtons(boolean isMeasuring){
		myStartButton.setEnabled(!isMeasuring);
		myStopButton.setEnabled(isMeasuring);
	}
	
    /**
     * Called when the start button is clicked
     * @param view
     */
    public void StartButtonClicked(View view){
    	if(servicesConnected()){
    		myDistance = 0;
    		DisplayDistance(myDistance);
    		setMeasureDistance(true);    		    		    		 	
    	}    	
    }

    /**
     * Formats and displays the current distance.
     * @param distanceInMeters
     */
    private void DisplayDistance(float distanceInMeters) {
    	if(myDistanceTextView != null){
    		String formattedDistance = myDecimalFormatter.format(distanceInMeters / 1000) + 
    				" km";
    		myDistanceTextView.setText(formattedDistance);
    	}
	}

	/**
     * Called when the stop button is clicked
     * @param view
     */
    public void StopButtonClicked(View view){
    	setMeasureDistance(false);
    	
    	// TODO: add to list...
    }
    
    /**
     * Shows alert and exit, should never be called (Defensieve Programmierung)
     */
    public void showExitDialog(){
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setMessage(R.string.no_gps_service)
		.setTitle(R.string.forced_exit)
		.setCancelable(false)
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {					
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				myUserWantsToExitWhileThereIsNoPointToUseThisAppWithNoGPSHardware = true;
				finish();
			}
		}).create().show();
    }
    
    /**
     * Propmts the user to enable GPS
     */
    public void showEnableGPSDialog(){    	
    	 AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
         alertDialogBuilder.setMessage(R.string.enable_gps_prompt)
         .setTitle(R.string.enable_gps_title)
         .setCancelable(false)
         .setPositiveButton(R.string.enable_gps_button,
                 new DialogInterface.OnClickListener(){
             public void onClick(DialogInterface dialog, int id){
                 Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                 startActivity(callGPSSettingIntent);                 
             }
         }).create().show();               
    }
    
    //endregion --- UI handling ------------------------------------------------
    
    //region --- GPS stuff -----------------------------------------------------
    
    private static final int UPDATE_INTERVAL_IN_MILISECONDS = 1 * 1000;
    private static final float SMALLEST_DISPLACEMENT_TO_REPORT_IN_METERS = (float)1;
    
    private LocationClient myLocationClient;
    private Location myLocation;
    private LocationRequest myLocationRequest;
    private LocationManager myLocationManager;
    
    private boolean myIsGPSOK = false;
    public boolean getIsGPSOK(){ 
    	return myIsGPSOK; 
    }           
    public void setIsGPSOK(boolean newValue){
    	myIsGPSOK = newValue;
    	
    	if(myIsGPSOK){
    		EnableUI();
    	} else {
    		setMeasureDistance(false);
    		DisableUI();
    	}    		
    }
    
    private boolean myMeasureDistance = false;
    public boolean getMeasureDistance(){
    	return myMeasureDistance;
    }    
    public void setMeasureDistance(boolean newValue){
    	myMeasureDistance = newValue;
    	ResetUIButtons(myMeasureDistance);
    }
    
    /**
     * Prompts the user if GPS is disabled
     */
    private void checkForEnabledGPS(){
    	if(myLocationManager == null){
    		myLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    		// no GPS service
    		if(myLocationManager != null){
    			if (!myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
    				showEnableGPSDialog();
    			}
    		} else {
    			showExitDialog();    			
    		}
    	}    	    	
    		
    }
    
    /**
     * Sets up GPS and starts updates
     */
    private void setupGPS() {
        myLocationClient = new LocationClient(this, this, this);
        myLocationClient.connect();
		
        myLocationRequest = new LocationRequest();
        myLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILISECONDS);
        myLocationRequest.setFastestInterval(UPDATE_INTERVAL_IN_MILISECONDS);
        myLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT_TO_REPORT_IN_METERS);
        myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);         
	}
    
    /**
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /**
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
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
                        setIsGPSOK(false);
                    break;
                }
            default:
               Log.d(APPTAG, getString(R.string.unknown_activity_request_code, requestCode));
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
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), APPTAG);
            }
            return false;
        }
    }   

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
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
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
            showErrorDialog(connectionResult.getErrorCode());
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
		if(servicesConnected()){
			myLocationClient.requestLocationUpdates(myLocationRequest, this);
		}
	}

	/**
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
	@Override
	public void onDisconnected() {
		setIsGPSOK(false);
		Toast.makeText(this, R.string.gps_disconnected, Toast.LENGTH_SHORT).show();		
	}
	
	@Override
	public void onLocationChanged(Location newLocation) {
		if(newLocation != null){
			
			if(!getIsGPSOK()){
				setIsGPSOK(true);				
			}
			
			if(getMeasureDistance()){
				if(myLocation != null){
					myDistance += newLocation.distanceTo(myLocation);
					DisplayDistance(myDistance);
				}
			
				myLocation = newLocation;
			}
		}
	}
    
    //endregion --- GPS stuff --------------------------------------------------
	
	//region --- utils ---------------------------------------------------------
	
	/**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            this,
            CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getSupportFragmentManager(), APPTAG);
        }
    }
   //endregion --- utils -------------------------------------------------------
}
