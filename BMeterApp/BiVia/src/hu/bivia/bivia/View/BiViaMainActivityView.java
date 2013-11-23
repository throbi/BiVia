package hu.bivia.bivia.View;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.common.GooglePlayServicesUtil;

import hu.bivia.bivia.R;
import hu.bivia.bivia.Model.MeasuredDay;
import hu.bivia.bivia.Model.Measurement;
import hu.bivia.bivia.Model.Ride;
import hu.bivia.bivia.ViewModel.BiViaMainPageViewModel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class BiViaMainActivityView 
	extends 
		FragmentActivity {  
		
	private BiViaMainPageViewModel myViewModel;

	private ArrayList<MeasuredDay> myRidingDays;

	private ExpandableListView myListView;

	private MeasuredDayExpandalbleAdapter myExpandableListAdapter;	

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
        setContentView(R.layout.activity_bivia_main);
        getUIElements();
        disableUI();
                
        myViewModel.onUICreate(savedInstanceState);                               
    }    
	
	@Override
	protected void onStart() { 
		super.onStart();		
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
	
    //region --- view implementations ------------------------------------------
	public void startButtonClicked(View view){
		startTimer();		
		myViewModel.startDistanceMeasurement();
		myAverageSpeedTextView.setText(R.string.average_speed);
		myDistanceTextView.setText(R.string.gps_count);
	}	
	
	public void stopButtonClicked(View view){
		myViewModel.stopDistanceMeasurement();
		stopTimer();
	}
	
	/**
	 * Displays the current measurement, i.e. distance, elapsed time, average
	 * speed 
	 * @param measurement
	 */
	public void displayDistance(Measurement measurement) {
		String formattedDistance = decimalFormatter.format(measurement.getDistance()) + 
				" km";
		myDistanceTextView.setText(formattedDistance);
		
		String formattedSpeed = decimalFormatter.format(measurement.getAverageSpeed()) + " km/h";
		myAverageSpeedTextView.setText(formattedSpeed);
		showGPSHit();
	}
	
	/**
	 * Adds the new ride to the expandable list
	 * @param ride
	 */
	public void displayRide(Ride ride) {
		if(myRidingDays == null){
			myRidingDays = new ArrayList<MeasuredDay>();
			myExpandableListAdapter = new MeasuredDayExpandalbleAdapter(this, myRidingDays);
	        myListView.setAdapter(myExpandableListAdapter);
		}

		MeasuredDay today = null;
		if(myRidingDays.size() > 0 && 
				sameDay(myRidingDays.get(0).getDate(), ride.getStartTime())){
			today = myRidingDays.get(0);
		} else {			
			// first ride for today
			today = new MeasuredDay(ride.getStartTime());						
			myRidingDays.add(0, today);			
		}
		
		today.addRide(ride);		
		myExpandableListAdapter.notifyDataSetChanged();
		myListView.expandGroup(0);
		
		// timer should be stopped by now, but might show a later time
		myEllapsedTimeTextView.setText(formatElapsedMillis(ride.getRideTimeMillis()));
	}		

	public void hideEnableGPSDialog() {
		myEnableGPSDialog.hide();
	}
	
	public void showEnableGPSDialog() {
		myEnableGPSDialog.show();
	}
	
	public void enableUI(){
		hideGPSProgressBar();
		resetUIButtons(myViewModel.getIsMeasuring());
		showGPSHit();
		
		myDistanceTextView.setTextColor(Color.parseColor("#21addb"));
		myEllapsedTimeTextView.setTextColor(Color.parseColor("#aaaacc"));
		myAverageSpeedTextView.setTextColor(Color.parseColor("#aaaacc"));
	}	
			
	public void disableUI(){
		myStartButton.setEnabled(false);
		
		// should be able to stop even if there is no GPS
		myStopButton.setEnabled(myViewModel.getIsMeasuring());
		
		myDistanceTextView.setTextColor(Color.parseColor("#164757"));
		myEllapsedTimeTextView.setTextColor(Color.parseColor("#222222"));
		myAverageSpeedTextView.setTextColor(Color.parseColor("#222222"));
		
		showGPSProgressBar();
	}
	
	public void resetUIButtons(boolean isMeasuring){
		if(myViewModel.isGPSEnabled()){
			myStartButton.setEnabled(!isMeasuring);
		}
		myStopButton.setEnabled(isMeasuring);
				
	}
	    
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
    
	public void displayGooglePlayErrorDialog(int resultCode, int requestCode) {
		// Display an error dialog
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, requestCode);            
    	if (dialog == null) {        	
        	dialog = new Dialog(this);
        	dialog.setTitle(getString(R.string.googleError_title));
        	TextView textView = new TextView(this);
        	textView.setText(getString(R.string.googleError_text));
            dialog.setContentView(textView);
        }
    	
    	ErrorDialogFragment errorFragment = new ErrorDialogFragment();
    	errorFragment.setDialog(dialog);
        errorFragment.show(getSupportFragmentManager(), BiViaMainPageViewModel.APPTAG);
	}
	
    //endregion --- view implementations ---------------------------------------

	//region --- timer ---------------------------------------------------------	

	private long myStartTime;

	private Runnable TimerMethod = new Runnable() {
		
		@Override
		public void run() {
			displayEllapsedTime();			
		}
	};

	private TimerTask myTimerTask;

	private Timer myTimer;
	
	private void startTimer() {	
		myStartTime = SystemClock.elapsedRealtime();
		myTimer = new Timer();
		myTimerTask = new TimerTask() {          
			@Override
			public void run() {
				updateTimer();
			}
		};		
	    
		try
		{
			myTimer.schedule(myTimerTask, 0, 1000);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void updateTimer() {
		this.runOnUiThread(TimerMethod);		
	}

	private void displayEllapsedTime(){	
		long elapsedMilllis = SystemClock.elapsedRealtime() - myStartTime;				
		
		myEllapsedTimeTextView.setText(formatElapsedMillis(elapsedMilllis));
	}	

	public void stopTimer(){
		myTimerTask.cancel();
		myTimer.purge();
	}
	
	//endregion --- timer ------------------------------------------------------
	
    //region --- UI handling ---------------------------------------------------	
	
	private TextView myDistanceTextView, myEllapsedTimeTextView, myAverageSpeedTextView;	
	private Button myStartButton, myStopButton;
	
	private AlertDialog myEnableGPSDialog;

	private ViewGroup myGPSHitGroup;
	private Animation myFadeOutAnimation;
	private ViewGroup myGPSWaitingGroups;
	
	/**
	 * Gets references to UI elements.
	 */
	private void getUIElements(){			
		myDistanceTextView = (TextView)findViewById(R.id.distanceTextView);
		myEllapsedTimeTextView = (TextView)findViewById(R.id.ellapsedTime);
		myAverageSpeedTextView = (TextView)findViewById(R.id.averageSpeed);
		myStartButton = (Button)findViewById(R.id.startButton);
		myStopButton = (Button)findViewById(R.id.stopButton);
		
		myGPSHitGroup = (ViewGroup)findViewById(R.id.gpsHitGroup);
		myGPSWaitingGroups = (ViewGroup)findViewById(R.id.gpsWaitingGroup);
		
		myFadeOutAnimation = AnimationUtils.loadAnimation(this, R.animator.gpshit_animator);
		
		myListView = (ExpandableListView) findViewById(R.id.measuredDays);
	}
	
	/**
	 * Notifies the user about waiting for GPS fix.
	 */
	private void hideGPSProgressBar(){
		myGPSWaitingGroups.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * Notifies user about GPS fix
	 */
	private void showGPSProgressBar(){
		myGPSHitGroup.setVisibility(View.INVISIBLE);
		myGPSWaitingGroups.setVisibility(View.VISIBLE);
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

    private void showGPSHit(){
    	myGPSWaitingGroups.setVisibility(View.INVISIBLE);
    	myGPSHitGroup.setVisibility(View.VISIBLE);
    	myGPSHitGroup.startAnimation(myFadeOutAnimation);    	
    }
 
	//endregion --- UI handling ------------------------------------------------        

    //region --- formatters ----------------------------------------------------
	
  	public static final DecimalFormat decimalFormatter = 
  			new DecimalFormat("000.000");

	public static final SimpleDateFormat timeFormatter = 
			new SimpleDateFormat("H:mm:ss", Locale.getDefault());
  	
	public static final SimpleDateFormat dateFormatter = 
			new SimpleDateFormat("EEEE, yyyy/MM/dd", Locale.getDefault());
	
	public static String formatElapsedMillis(long elapsedMilllis) {
		int seconds = (int) (elapsedMilllis / 1000) % 60 ;
		int minutes = (int) ((elapsedMilllis / (1000*60)) % 60);
		int hours   = (int) ((elapsedMilllis / (1000*60*60)) % 24);
		
		return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
	}
	
  	//endregion --- formatters -------------------------------------------------
	
	//region --- utils ---------------------------------------------------------
		
	private boolean sameDay(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
		                  cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

		return sameDay;
	}
	
	//endregion --- utils ------------------------------------------------------
}
