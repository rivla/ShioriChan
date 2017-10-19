package jp.ncf.app.shiorichan;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * Created by ideally on 2017/10/19.
 */

public class TitleActivity extends Activity{


    private Location mLastLocation;//開始時、現在地の座標を保存する変数


    // csv読み込み用クラスの宣言
    private CSVReader csv;

    // jsonファイル読み込み用クラスの宣言
    private JsonReader json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.title_page);
        ImageButton startButton=(ImageButton) findViewById(R.id.startButton);
//        Button customButton=(Button)findViewById(R.id.customButton);

        Value.departureTime.setHours(10);
        Value.departureTime.setMinutes(0);
        Value.departureTime.setSeconds(0);
        Value.arriveTime.setHours(22);
        Value.arriveTime.setMinutes(0);
        Value.arriveTime.setSeconds(0);

/*
        //設定画面
        customButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), MyPreferenceActivity.class);
                startActivity(intent);
            }
        });
        */

        //しおり表示モードへ入るボタン
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
