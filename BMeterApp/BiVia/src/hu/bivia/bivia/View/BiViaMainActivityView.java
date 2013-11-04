package hu.bivia.bivia.View;

import java.text.DecimalFormat;

import com.google.android.gms.common.GooglePlayServicesUtil;

import hu.bivia.bivia.ErrorDialogFragment;
import hu.bivia.bivia.R;
import hu.bivia.bivia.ViewModel.BiViaMainPageViewModel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BiViaMainActivityView 
	extends 
		FragmentActivity 
	implements
		IBiViaView{  
		
	private BiViaMainPageViewModel myViewModel;

	//region --- injections for testing - !!! REMOVE FROM RELEASE !!! ----------
	
	public void _test_setViewModel(BiViaMainPageViewModel mockViewModel) {
		myViewModel = mockViewModel;		
	}
	
	public BiViaMainPageViewModel _test_getViewModel(){
		return myViewModel;
	}
	
	//endregion --- injections for testing - !!! REMOVE FROM RELEASE !!! -------
	
    //region --- Lifecycle management ------------------------------------------
	public static final String EXTRA_MESSAGE = "hu.bivia.bivia.MESSAGE";		
	
	/**
	 * Constructor, creates the view model, too
	 */
	public BiViaMainActivityView() {
		myViewModel = new BiViaMainPageViewModel(this);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                      
        
        // UI
        createDialogs();
        setContentView(R.layout.activity_bmeter_main);
        getUIElements();
        disableUI();
                
        myViewModel.onUICreate(savedInstanceState);
    }    

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.bmeter_main, menu);
        return true;
    }    
           
	@Override
	public void onDestroy(){
		myViewModel.onDestroy();
		super.onDestroy();
	}
	
    //endregion --- Lifecycle management ---------------------------------------
 
	//region --- Activity stuff ------------------------------------------------
	
	/**
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        myViewModel.onActivityResult(requestCode, resultCode, intent);
    }
	
    //endregion --- Activity stuff ---------------------------------------------
	
    //region --- IBiViaView implementations ------------------------------------
	@Override
	public void startButtonClicked(View view){
		myViewModel.startDistanceMeasurement();    	
	}
	
	@Override
	public void stopButtonClicked(View view){
		myViewModel.stopDistanceMeasurement();
	}
	
	@Override
	public void displayDistance(float distanceInMeters) {
		if(myDistanceTextView != null){
			String formattedDistance = myDecimalFormatter.format(distanceInMeters / 1000) + 
					" km";
			myDistanceTextView.setText(formattedDistance);
		}
	}
	
	@Override
	public void hideEnableGPSDialog() {
		myEnableGPSDialog.hide();
	}
	
	@Override
	public void showEnableGPSDialog() {
		myEnableGPSDialog.show();
	}
	
	@Override
	public void enableUI(){
		hideGPSProgressBar();
		resetUIButtons(myViewModel.getIsMeasuring());		
	}	
		
	@Override
	public void disableUI(){
		myStartButton.setEnabled(false);
		
		// should be able to stop even if there is no GPS
		myStopButton.setEnabled(myViewModel.getIsMeasuring());
		
		showGPSProgressBar();
	}
	
	@Override
	public void resetUIButtons(boolean isMeasuring){
		if(myViewModel.isGPSEnabled()){
			myStartButton.setEnabled(!isMeasuring);
		}
		myStopButton.setEnabled(isMeasuring);
				
	}
	    
    @Override
	public void showExitDialog(){
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setMessage(R.string.no_gps_service)
		.setTitle(R.string.forced_exit)
		.setCancelable(false)
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {					
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				myViewModel.setUserWantsToExitWhileThereIsNoPointToUseThisAppWithNoGPSHardware(true);
				finish();
			}
		}).create().show();
    }     
    
    @Override
	public void displayGooglePlayErrorDialog(int resultCode, int requestCode) {
		// Display an error dialog
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, requestCode);            
    	if (dialog != null) {
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            errorFragment.setDialog(dialog);
            errorFragment.show(getSupportFragmentManager(), BiViaMainPageViewModel.APPTAG);
        }	
	}
	
    //endregion --- IBiViaView implementations ---------------------------------
	
    //region --- UI handling ---------------------------------------------------
	
	private DecimalFormat myDecimalFormatter = new DecimalFormat("000.000");
	
	private TextView myGPSStateView, myDistanceTextView;	
	private Button myStartButton, myStopButton;
	private ProgressBar myGPSProgressBar;
	
	private AlertDialog myEnableGPSDialog;
	
	/**
	 * Gets references to UI elements.
	 */
	private void getUIElements(){	
		myGPSProgressBar = (ProgressBar)findViewById(R.id.gps_progress);
		myGPSStateView = (TextView)findViewById(R.id.gpsStatusTextView);
		myDistanceTextView = (TextView)findViewById(R.id.distanceTextView);
		myStartButton = (Button)findViewById(R.id.startButton);
		myStopButton = (Button)findViewById(R.id.stopButton);
	}
	
	/**
	 * Notifies the user about waiting for GPS fix.
	 */
	private void hideGPSProgressBar(){
		myGPSStateView.setText(R.string.gps_ok);
		myGPSProgressBar.setVisibility(View.GONE);
	}
	
	/**
	 * Notifies user about GPS fix
	 */
	private void showGPSProgressBar(){
		myGPSStateView.setText(R.string.gps_booting);
		myGPSProgressBar.setVisibility(View.VISIBLE);
	}
	
    /**
     * Creates dialogs to be shown later
     */
    private void createDialogs(){
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
        });
        myEnableGPSDialog = alertDialogBuilder.create();
    }

 
	//endregion --- UI handling ------------------------------------------------        
}
