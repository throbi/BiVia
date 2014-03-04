package hu.bivia.logic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import hu.bivia.R;
import hu.bivia.model.MeasuredDay;
import hu.bivia.view.ui_elements.SettingsActivity;
import hu.bivia.viewModel.BiViaMainPageViewModel;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Uploads data to http://kerekparosklub.hu/bam
 * 
 * @author horvath.robert
 */
public class BamUploader {

	// VM-wide cookie manager
	private static java.net.CookieManager myCookieManager = null;

	// BAM specific formatters
	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd", Locale.getDefault());
	public static final DecimalFormat decimalFormatter = new DecimalFormat(
			".000");

	private String myLoginURL = "http://kerekparosklub.hu/?q=user";
	private String myBamUser = SettingsActivity.NOT_SET;
	private String myBamPassword = SettingsActivity.NOT_SET;
	private String myUploadURL = "http://kerekparosklub.hu/save_distance";

	private MeasuredDay myDay;

	private Activity myActivity;
	private BiViaMainPageViewModel myViewModel;

	public BamUploader(Activity activity, BiViaMainPageViewModel viewModel) {
		if (myCookieManager == null) {
			myCookieManager = new java.net.CookieManager();
			CookieHandler.setDefault(myCookieManager);
		}

		myActivity = activity;
		myViewModel = viewModel;
	}

	/**
	 * Checks for Internet connection, returns true when a connection is
	 * available.
	 * */
	public boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) myActivity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		// if no network is available networkInfo will be null
		// otherwise check if we are connected
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	/** Uploads one measured day, overwrites previously uploaded value. */
	public void uploadMeasuredDay(MeasuredDay measuredDay) {

		if (!updateBamUserData()) {
			myViewModel.uplodFinished(measuredDay);
		} else {
			if (isNetworkAvailable()) {
				myDay = measuredDay;
				uploadData(measuredDay);
			} else {
				myViewModel.uplodFinished(measuredDay);
				myViewModel.requestEnableNetwork();
			}
		}
	}

	/**
	 * Reads BAM user data from preferences, pops up preference window if not
	 * set. Returns false when no user data has been set yet.
	 */
	private boolean updateBamUserData() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(myActivity);
		myBamUser = sharedPref.getString(SettingsActivity.BAM_NAME_KEY,
				SettingsActivity.NOT_SET);
		myBamPassword = sharedPref.getString(SettingsActivity.BAM_PWD_KEY,
				SettingsActivity.NOT_SET);

		if (myBamUser.equals(SettingsActivity.NOT_SET)
				|| myBamPassword.equals(SettingsActivity.NOT_SET)
				|| myBamUser.isEmpty() || myBamPassword.isEmpty()) {
			Toast.makeText(myActivity,
					myActivity.getString(R.string.bam_user_not_set),
					Toast.LENGTH_LONG).show();
			myActivity.startActivity(new Intent(myActivity,
					SettingsActivity.class));
			return false;
		} else {
			// reset cookies, password might have been updated since last run
			myCookieManager.getCookieStore().removeAll();
			return true;
		}
	}

	/** Tries to log in, returns true on success */
	private void uploadData(MeasuredDay measuredDayToBeUploaded) {
		SendHttpRequestTask httpBackgroundTask = new SendHttpRequestTask();

		String loginData = "name=" + myBamUser + "&pass=" + myBamPassword
				+ "&form_id=user_login";

		String uploadData = "datum="
				+ dateFormatter.format(measuredDayToBeUploaded.getDate())
				+ "&megtett_km="
				+ decimalFormatter.format(
						measuredDayToBeUploaded.getTotalDistance()).replace(
						",", ".") + "&vonat_km=" + "&bubi=" + "&car="
				+ "&suit=0" + "&child=0" + "&rain=0" + "&tire=0";

		httpBackgroundTask.execute(myLoginURL, loginData, myUploadURL,
				uploadData);
	}

	/** Executes HTTP requests in the background */
	private class SendHttpRequestTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String result = "";

			if (login(params[0], params[1])) {
				// login ok, try upload
				if (upload(params[2], params[3])) {
					// upload ok
					result = myActivity.getText(R.string.bam_upload_ok) + " "
							+ dateFormatter.format(myDay.getDate()) + ", "
							+ decimalFormatter.format(myDay.getTotalDistance())
							+ " km.";
				} else {
					// upload failed
					result = myActivity.getText(R.string.bam_upload_failed)
							+ " " + dateFormatter.format(myDay.getDate())
							+ ", "
							+ decimalFormatter.format(myDay.getTotalDistance())
							+ " km.";
				}
			} else {
				// login failed
				result = myActivity.getText(R.string.bam_login_failed) + " "
						+ myBamUser;
			}

			return result;
		}

		/** Performs the upload, returns true on success */
		private boolean upload(String uploadURL, String uploadParams) {
			String responseHTML = post(uploadURL, uploadParams);

			return (responseHTML != null && (responseHTML.equals("Frissítve.") || responseHTML
					.equals("Mentve.")));
		}

		/** Performs login, returns true on success */
		private boolean login(String loginURL, String loginParams) {

			String responseHTML = post(loginURL, loginParams);
			return responseHTML.contains(myBamUser)
					&& !responseHTML
							.contains("Ismeretlen felhasználó vagy hibás jelszó");
		}

		/** executes the HTTP POST and returns the web server response */
		private String post(String url, String params) {
			String result = "";
			try {
				// connect
				HttpURLConnection connection = (HttpURLConnection) (new URL(url))
						.openConnection();
				connection.setRequestMethod("POST");
				connection
						.setRequestProperty("User-Agent",
								"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:24.0) Gecko/20100101 Firefox/24.0");
				connection.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.connect();

				// send login info
				connection.getOutputStream().write(params.getBytes());

				// read result
				BufferedReader r = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				StringBuilder total = new StringBuilder();
				String line;
				while ((line = r.readLine()) != null) {
					total.append(line);
				}
				result = total.toString();

				connection.disconnect();
			} catch (Throwable t) {
				result = "Hiba";
			}

			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(myActivity, result, Toast.LENGTH_LONG).show();
			myViewModel.uplodFinished(myDay);
		}

	}
}
