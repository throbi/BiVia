package hu.bmeter.bmeter;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class BMeterMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmeter_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bmeter_main, menu);
        return true;
    }
    
}
