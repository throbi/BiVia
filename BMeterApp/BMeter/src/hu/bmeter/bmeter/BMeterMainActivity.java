package hu.bmeter.bmeter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

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
import android.widget.Toast;

public class BMeterMainActivity 
	extends 
		FragmentActivity 
	implements 
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener 
	{  
	
	public static final String APPTAG = "BiViaDebugTag";
	
    //region --- Creation and starting -----------------------------------------
	public static final String EXTRA_MESSAGE = "hu.bmeter.bmeter.MESSAGE";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        
        //Create a new location client, using the enclosing class to handle callbacks.
        myLocationClient = new LocationClient(this, this, this);
        myLocationClient.connect();
        
        setContentView(R.layout.activity_bmeter_main);        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bmeter_main, menu);
        return true;
    }    
           
    //endregion --- Creation and starting --------------------------------------
    
    //region --- UI handling ---------------------------------------------------
    /**
     * Called when the start button is clicked
     * @param view
     */
    public void StartButtonClicked(View view){
    	if(servicesConnected()){
    		myLocation = myLocationClient.getLastLocation();
    	}    	
    }

    /**
     * Called when the stop button is clicked
     * @param view
     */
    public void StopButtonClicked(View view){
    	StartButtonClicked(view);
    }
    //endregion --- UI handling ------------------------------------------------
    
    //region --- GPS stuff -----------------------------------------------------
    
    LocationClient myLocationClient;
    Location myLocation;
    
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
