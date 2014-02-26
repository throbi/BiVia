package hu.bivia.view.ui_elements;

import hu.bivia.bivia.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    public static final String BAM_NAME_KEY = "preference_key_bam_user_name";
	public static final String BAM_PWD_KEY = "preference_key_bam_password";
	
	public static final String NOT_SET = "preference not set";

	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
