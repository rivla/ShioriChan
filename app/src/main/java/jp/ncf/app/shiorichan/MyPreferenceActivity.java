package jp.ncf.app.shiorichan;

import android.annotation.TargetApi;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.TimePicker;

/**
 * Created by ideally on 2017/10/19.
 */
//preference画面を制御するactivity.
public class MyPreferenceActivity extends PreferenceActivity implements TimePickerDialog.OnTimeSetListener {
    //timepickerの受取関数は一つしか作れないため、フラグでdepartureButtonかarriveButtonを判別
    boolean departureFlg=false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.activity_settings);

        //timepickerはないため、preferenceからtimepickerを呼び出す。
        Preference departureTimePref = (Preference) findPreference("departureTime");
        departureTimePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public boolean onPreferenceClick(Preference preference) {
                departureFlg=true;
                showDateDialog();
                return false;
            }
        });
        Preference arriveTimePref = (Preference) findPreference("arriveTime");
        arriveTimePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public boolean onPreferenceClick(Preference preference) {
                departureFlg=false;
                showDateDialog();
                return false;
            }
        });
    }
    //DatePickerを表示する関数
    private void showDateDialog(){
        if(departureFlg) {
            new TimePickerDialog(this, this, Value.departureTime.getHours(),Value.departureTime.getMinutes(),true).show();
        }else{
            new TimePickerDialog(this, this, Value.arriveTime.getHours(),Value.arriveTime.getMinutes(),true).show();
        }
    }

    //DatePickerで時間が指定された時に呼ばれる関数
    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        if(departureFlg) {
            Value.departureTime.setHours(i);
            Value.departureTime.setMinutes(i1);
        }else{
            Value.arriveTime.setHours(i);
            Value.arriveTime.setMinutes(i1);
        }
    }
}