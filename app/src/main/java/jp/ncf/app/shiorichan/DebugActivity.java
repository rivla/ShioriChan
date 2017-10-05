package jp.ncf.app.shiorichan;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


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
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ideally on 2017/10/05.
 */

public class DebugActivity extends Activity

{

    private Location mLastLocation;//開始時、現在地の座標を保存する変数


    // csv読み込み用クラスの宣言
    private CSVReader csv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_main);
        // CSVReaderクラスのインスタンス生成
        csv = new CSVReader();




        //ボタン、テキストボックス定義
        final EditText edit=(EditText)findViewById(R.id.editText);
        final TextView textView=(TextView)findViewById(R.id.textView);
        final TextView textView2=(TextView)findViewById(R.id.textView2); // 公共クラウドシステム出力用
        final Button lnglatButton=(Button)findViewById(R.id.lnglatButton);
        final Button candButton=(Button)findViewById(R.id.candButton);
        final ListView listView=(ListView)findViewById(R.id.listView);//候補地表示用
        Button returnButton = (Button) findViewById(R.id.backButton);

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


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
                    Log.d("test", String.valueOf(Value.lat));
                    //nextPageTokenが空→1ページ目を表示
                    if (Value.next_page_token == null)
                        new HttpGetCand(listView).execute(new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + Double.toString(Value.lat) + "," + Double.toString(Value.lng) + "&radius=500&language=ja&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                        //nextPageTokenに値がある→nextPageTokenのページを表示
                    else
                        new HttpGetCand(listView).execute(new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + Double.toString(Value.lat) + "," + Double.toString(Value.lng) + "&pagetoken=" + Value.next_page_token + "&radius=500&language=ja&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                // 公共クラウドシステム（API用） //
                // 候補地ボタンを押したときに，公共クラウドシステムの結果も同時に取得する
                try {
                    new HttpGetKoukyouCloudSystem(textView2).execute(new URL("https://www.chiikinogennki.soumu.go.jp/k-cloud-api/v001/kanko/%E7%BE%8E%E8%A1%93%E9%A4%A8/json?limit=3"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                // 公共クラウドシステム（csb読み込み用） //
                // csvファイルを読み込んでリスト形式で受け取る
                List csvlist = csv.ReadCSV(getApplicationContext(), "Kanko.csv");

                String size = String.valueOf(csvlist.size()); // サイズの取得
                String text = "";

                // 取得したデータのループ
                for (int i = 0; i < csvlist.size()-495; i++) {
                    // ある一行のデータをリストでループ
                    List tk_list = (List) csvlist.get(i);
                    for (int j = 0; j < tk_list.size(); j++) {
                        text += tk_list.get(j);
                    }
                    text += "\n";
                }
                // textView2.setText((CharSequence) csvlist2.get(0));
                textView2.setText(text);

            }
        });

        // リストビュークリックリスナ
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ListView listView = (ListView) parent;
                // クリックされたアイテムを取得します
                String item = (String) listView.getItemAtPosition(position);
                if (item != "") {
                    //日本語をURLに変換
                    String urlEncodeResult = null;
                    try {
                        urlEncodeResult = URLEncoder.encode(item, "UTF-8");
                        //クリックされたリストをPlaces detail APIに送信
                        new HttpGetDetail(textView).execute(new URL("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + urlEncodeResult + "&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk&language=ja"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
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

    private ListView listView;
    public HttpGetCand(ListView listView) {
        super();
        this.listView=listView;
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
            //候補地格納用リスト
            ArrayList<String> candList=new ArrayList<String>();
            //表示用の文字列生成
            for(int i=0;i<candLength;i++){
                candList.add(jsonObject.getJSONArray("results").getJSONObject(i).getString("place_id"));//候補地のIDをリストに格納する
            }
            //リストビュー表示用アダプタ
            ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(MainActivity.getInstance()
                    ,android.R.layout.simple_list_item_1,candList);
            //リストビュー表示
            listView.setAdapter(arrayAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}



// 公共クラウドシステムAPI用、非同期処理クラス
// HttpGetCandをコピーして改良しただけ
final class HttpGetKoukyouCloudSystem extends AsyncTask<URL, Void, String> {

    // textviewの宣言
    private TextView textView;
    public HttpGetKoukyouCloudSystem(TextView textView) {
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
            // 取得結果の総数を取得する
            // 公共クラウドシステムの結果はtourspotsのリストになっている
            int candLength=jsonObject.getJSONArray("tourspots").length();
            String candString="=== 公共クラウドシステムの取得結果 ===\n";
            // 表示用の文字列生成
            for(int i=0;i<candLength;i++){
                // 名前部分を参照する
                candString=candString+jsonObject.getJSONArray("tourspots").getJSONObject(i).getJSONObject("name").getJSONObject("name1").getString("written")+"\n";
            }
            Log.d("test koukyou",result);
            textView.setText(candString); // textviewに取得結果をセットする
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}


// ロケーション詳細取得用、非同期処理クラス
final class HttpGetDetail extends AsyncTask<URL, Void, String> {

    // textviewの宣言
    private TextView textView;
    public HttpGetDetail(TextView textView) {
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
            //
            String rate=jsonObject.getJSONObject("result").getString("rating");//詳細検索結果、レート値
            String name=jsonObject.getJSONObject("result").getString("name");//詳細検索をした場所の名前
            double destinationLat=jsonObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").getDouble("lat");//詳細検索された場所の緯度
            double destinationLng=jsonObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").getDouble("lng");//詳細検索された場所の経度
            float[] distance=new float[3];//二点間の距離算出結果を格納する変数
            Location.distanceBetween(Value.lat,Value.lng,destinationLat,destinationLng,distance);//入力された場所と候補地との距離算出
            //トーストで表示
            Toast.makeText(MainActivity.getInstance(),"rate:"+rate+"name:"+name+"distance:"+String.valueOf(distance[0])+"m",Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

