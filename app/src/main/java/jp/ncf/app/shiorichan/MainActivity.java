package jp.ncf.app.shiorichan;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

//値渡し用、静的変数
class Value{
    public static Double lat=0.0;//緯度
    public static Double lng=0.0;//経度
    public static String next_page_token=null;
}

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ボタン、テキストボックス定義
        final EditText edit=(EditText)findViewById(R.id.editText);
        final TextView textView=(TextView)findViewById(R.id.textView);
        final Button lnglatButton=(Button)findViewById(R.id.lnglatButton);
        final Button candButton=(Button)findViewById(R.id.candButton);

        //緯度経度取得ボタンリスナ、ボタンが押されるとこのリスナが呼ばれる
        lnglatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Value.next_page_token=null;
                try {
                    //日本語をURLに変換
                    String urlEncodeResult= URLEncoder.encode(edit.getText().toString() ,"UTF-8");
                    //httpを渡し、非同期処理開始
                    new HttpGetLnglat(textView).execute(new URL("https://maps.googleapis.com/maps/api/geocode/json?address="+urlEncodeResult+"&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk&language=ja"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        //候補地取得ボタンリスナ
        candButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d("test",String.valueOf(Value.lat));
                    //nextPageTokenが空→1ページ目を表示
                    if(Value.next_page_token==null)new HttpGetCand(textView).execute(new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+Double.toString(Value.lat)+","+Double.toString(Value.lng)+"&radius=500&language=ja&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                        //nextPageTokenに値がある→nextPageTokenのページを表示
                    else new HttpGetCand(textView).execute(new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+Double.toString(Value.lat)+","+Double.toString(Value.lng)+"&pagetoken="+Value.next_page_token+"&radius=500&language=ja&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}


//緯度経度取得用、非同期処理クラス
final class HttpGetLnglat extends AsyncTask<URL, Void, String> {

    private TextView textView;
    public HttpGetLnglat(TextView textView) {
        super();
        this.textView=textView;
    }

    //executeが呼ばれると実行されるメソッド、データをウェブから取得する
    @Override
    protected String doInBackground(URL... urls) {
        // 取得したテキストを格納する変数
        final StringBuilder result = new StringBuilder();
        // アクセス先URL
        final URL url = urls[0];

        HttpURLConnection con = null;
        try {
            // ローカル処理
            // コネクション取得
            con = (HttpURLConnection) url.openConnection();
            con.connect();

            // HTTPレスポンスコード
            final int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // 通信に成功した
                // テキストを取得する
                final InputStream in = con.getInputStream();
                final String encoding = con.getContentEncoding();
                final InputStreamReader inReader = new InputStreamReader(in);
                final BufferedReader bufReader = new BufferedReader(inReader);
                String line = null;
                // 1行ずつテキストを読み込む
                while((line = bufReader.readLine()) != null) {
                    result.append(line);
                }
                bufReader.close();
                inReader.close();
                in.close();
            }

        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (ProtocolException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (con != null) {
                // コネクションを切断
                con.disconnect();
            }
        }
        return result.toString();
    }
    //バックグラウンド処理が終了した後呼ばれる関数、ここで結果を扱う
    @Override
    protected void onPostExecute(String result) {
        JSONObject jsonObject=null;
        try{
            //取得データをjson読み取り用変数へ代入
            jsonObject=new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            //緯度経度抜き取り
            Value.lat=jsonObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            Value.lng=jsonObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            Log.d("test",result);
            textView.setText("緯度:"+String.valueOf(Value.lat)+"経度:"+String.valueOf(Value.lng));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}



//指定座標周辺のロケーション取得用、非同期処理クラス
final class HttpGetCand extends AsyncTask<URL, Void, String> {

    private TextView textView;
    public HttpGetCand(TextView textView) {
        super();
        this.textView=textView;
    }

    @Override
    protected String doInBackground(URL... urls) {
        // 取得したテキストを格納する変数
        final StringBuilder result = new StringBuilder();
        // アクセス先URL
        final URL url = urls[0];

        HttpURLConnection con = null;
        try {
            // ローカル処理
            // コネクション取得
            con = (HttpURLConnection) url.openConnection();
            con.connect();

            // HTTPレスポンスコード
            final int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // 通信に成功した
                // テキストを取得する
                final InputStream in = con.getInputStream();
                final String encoding = con.getContentEncoding();
                final InputStreamReader inReader = new InputStreamReader(in);
                final BufferedReader bufReader = new BufferedReader(inReader);
                String line = null;
                // 1行ずつテキストを読み込む
                while((line = bufReader.readLine()) != null) {
                    result.append(line);
                }
                bufReader.close();
                inReader.close();
                in.close();
            }

        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (ProtocolException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (con != null) {
                // コネクションを切断
                con.disconnect();
            }
        }

        return result.toString();
    }
    /**
     * バックグランド処理が完了し、UIスレッドに反映する
     */
    @Override
    protected void onPostExecute(String result) {
        JSONObject jsonObject=null;
        try{
            jsonObject=new JSONObject(result);
            //nextPageToken抜き取り
            if(!jsonObject.isNull("next_page_token")){
                Log.d("test","nextpageToken exist");
                Value.next_page_token=jsonObject.getString("next_page_token");
            }
            //resultの総数を取得
            int candLength=jsonObject.getJSONArray("results").length();
            String candString="";
            //表示用の文字列生成
            for(int i=0;i<candLength;i++){
                candString=candString+jsonObject.getJSONArray("results").getJSONObject(i).getString("name")+"\n";
            }
            Log.d("test",result);
            textView.setText(candString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
