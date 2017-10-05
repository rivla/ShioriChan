package jp.ncf.app.shiorichan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

// ogawa test comment
//値渡し用、静的変数
class Value{
    public static double lat=0.0;//緯度
    public static double lng=0.0;//経度
    public static String next_page_token=null;
}




public class MainActivity extends AppCompatActivity {

    // csv読み込み用クラスの宣言
    private CSVReader csv;

    private static MainActivity instance=null;
    public static MainActivity getInstance(){
        return instance;//context取得用メソッド　このクラス外でも、MainActivity.getInstance()でcontextを取得できる
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.input_form);

        Button sendButton = (Button) findViewById(R.id.debugButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), DebugActivity.class);
                startActivity(intent);
            }
        });
    }
}