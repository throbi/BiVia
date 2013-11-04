package hu.bivia.bivia.View;

import android.view.View;

/**
 * Contract bitween the ViewModel and a view. 
 * Could not think of an "-able" name for it :( 
 */
public interface IBiViaView {

	//region --- user input ----------------------------------------------------
	
	/**
     * Called when the start button is clicked
     * @param view
     */
    public void startButtonClicked(View view);
    
    /**
     * Called when the stop button is clicked
     * @param view
     */
    public void stopButtonClicked(View view);
	
	//endregion --- user input -------------------------------------------------
	
	//region --- called by the view model --------------------------------------
    
    /**
	 * Show the dialog that asks the user to enable GPS
	 */
    public void showEnableGPSDialog();

    /**
	 * Hide the dialog that asks the user to enable GPS
	 */
	public void hideEnableGPSDialog();
	
	/**
     * Formats and displays the current distance.
     * @param distanceInMeters
     */
	public void displayDistance(float distanceInMeters);

	/**
	 * Hides the GPS init. animation and enables the controls
	 */
	public void enableUI();

	/**
	 * Disables controls, shows a GPS init. animation
	 */
	public void disableUI();
	
	/**
	 * Resets Start/Stop based on the input.
	 * @param isMeasuring
	 */
	public void resetUIButtons(boolean isMeasuring);

	/**
     * Shows alert and exit, should never be called (Defensieve Programmierung)
     */
	public void showExitDialog();

	/**
	 * Gets the error dialog from the google play service and shows it.
	 * @param resultCode
	 */
	public void displayGooglePlayErrorDialog(int resultCode, int requestCode);


	//endregion --- called by the view model -----------------------------------
}
