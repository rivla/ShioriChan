package jp.ncf.app.shiorichan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

// ogawa test comment
//値渡し用、静的変数
class Value{
    public static double lat=0.0;//緯度
    public static double lng=0.0;//経度
    public static String next_page_token=null;
}



public class MainActivity extends AppCompatActivity {
    String genreStrings[] ={"史跡","花火大会"};//ジャンルを格納するリスト//本当はcsvとかから引っ張ってきたほうがいいような

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
        Spinner genreSpinner=(Spinner)findViewById(R.id.genreSpinner);


        //デバッグモードへ入るボタン
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), DebugActivity.class);
                startActivity(intent);
            }
        });

        // ジャンルスピナー用ArrayAdapter
        ArrayAdapter<String> adapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, genreStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       genreSpinner.setAdapter(adapter);

        // ジャンル選択用スピナーのリスナーを登録
        genreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //　アイテムが選択された時
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                String item = (String) spinner.getSelectedItem();
                //トーストで表示
                Toast.makeText(MainActivity.getInstance(),item+"が押されました",Toast.LENGTH_LONG).show();

            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

    }
}