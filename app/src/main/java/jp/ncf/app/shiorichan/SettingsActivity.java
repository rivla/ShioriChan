package jp.ncf.app.shiorichan;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.WindowManager;

/**
 * Created by ideally on 2017/10/19.
 */

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
       // fragmentTransaction.replace(android.R.id.content,
               // new SettingsPreferenceFragment());
      //  fragmentTransaction.commit();
    }
}