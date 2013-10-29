package hu.bmeter.bmeter;

import java.text.DecimalFormat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
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
	private TextView myDistanceTextView;
	
    //region --- Creation and starting -----------------------------------------
	public static final String EXTRA_MESSAGE = "hu.bmeter.bmeter.MESSAGE";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                      
        setupGPS();                       
        
        setContentView(R.layout.activity_bmeter_main);
        myDistanceTextView = (TextView)findViewById(R.id.distanceTextView);
    }    

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bmeter_main, menu);
        return true;
    }    
           
	
    //endregion --- Creation and starting --------------------------------------
    
    //region --- UI handling ---------------------------------------------------
	
	private DecimalFormat myDecimalFormatter = new DecimalFormat("000.000");
	
    /**
     * Called when the start button is clicked
     * @param view
     */
    public void StartButtonClicked(View view){
    	if(servicesConnected()){
    		myDistance = 0;
    		DisplayDistance(myDistance);
    		myLocationClient.requestLocationUpdates(myLocationRequest, this);
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
    	myLocationClient.removeLocationUpdates(this);
    }
    //endregion --- UI handling ------------------------------------------------
    
    //region --- GPS stuff -----------------------------------------------------
    
    private static final int UPDATE_INTERVAL_IN_MILISECONDS = 1 * 1000;
    private static final float SMALLEST_DISPLACEMENT_TO_REPORT_IN_METERS = (float)1;
    
    private LocationClient myLocationClient;
    private Location myLocation;
    private LocationRequest myLocationRequest;
    
    private void setupGPS() {

    	//Create a new location client, using the enclosing class to handle callbacks.
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
     * Handle results returned to the FragmentActivity
     * by Google Play location services
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {            
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :            
             // If the result code is Activity.RESULT_OK, try to connect again             
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    //@TODO: Try the request again
                    break;
                }            
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
	public void onConnected(Bundle arg0) {
		// Display the connection status
        Toast.makeText(this, R.string.gps_connected, Toast.LENGTH_SHORT).show();		
	}

	/**
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
	@Override
	public void onDisconnected() {
		Toast.makeText(this, R.string.gps_disconnected, Toast.LENGTH_SHORT).show();		
	}
	
	@Override
	public void onLocationChanged(Location newLocation) {
		if(myLocation != null){			
			myDistance += newLocation.distanceTo(myLocation);
			DisplayDistance(myDistance);
		}
		myLocation = newLocation;		
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
