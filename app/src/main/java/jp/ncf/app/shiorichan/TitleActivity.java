package jp.ncf.app.shiorichan;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
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
