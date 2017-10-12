package jp.ncf.app.shiorichan;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CountDownLatch;

// ogawa test comment
//値渡し用、静的変数
class Value {
    public static double lat = 0.0;//緯度
    public static double lng = 0.0;//経度
    public static String next_page_token = null;
    public static String genre=null;
    public static List spotList=new ArrayList<SpotStructure>();
    public static final double alpha=5;
    public static final double beta=5;
}


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    String genreStrings[] = {"自然景観","施設景観","公園・庭園","動・植物","文化史跡","文化施設","神社仏閣","地域風俗・風習","その他","祭事","イベント","イベント鑑賞","文化施設","スポーツ・レジャー","温泉","名産品","郷土料理店","車","その他乗り物","旅館","ホテル","民宿・ペンション"};//ジャンルを格納するリスト//本当はcsvとかから引っ張ってきたほうがいいような
    GoogleApiClient mGoogleApiClient;//開始時に自己位置を取得するため、googleapiを利用
    private Location location;//開始時、現在地の座標を保存する変数
    private JsonReader json;
    private HttpGetter httpGet;
    final Handler handler = new Handler(Looper.getMainLooper());
    public static CountDownLatch _latch = new CountDownLatch(1);

    private static MainActivity instance = null;

    public static MainActivity getInstance() {
        return instance;//context取得用メソッド　このクラス外でも、MainActivity.getInstance()でcontextを取得できる
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.input_form);

        //googleAPI(開始時座標取得)のインスタンスの作成
        if (mGoogleApiClient == null) {
            // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        }

        final int[] departureTime = {10,0};//(時間,分)の順に格納
        final int[] arriveTime = {20,0};//(時間,分)の順に格納
        json=new JsonReader();
        httpGet=new HttpGetter();


        Button shioriButton = (Button) findViewById(R.id.shioriButton);
        Button sendButton = (Button) findViewById(R.id.debugButton);
        final Button departureTimeButton=(Button)findViewById(R.id.departureTimeButton);
        departureTimeButton.setText(String.format("%02d:%02d",departureTime[0],departureTime[1]));
        final Button arriveTimeButton=(Button)findViewById(R.id.arriveTimeButton);
        arriveTimeButton.setText(String.format("%02d:%02d",arriveTime[0],arriveTime[1]));
        Spinner genreSpinner=(Spinner)findViewById(R.id.genreSpinner);
        final Button startButton=(Button)findViewById(R.id.startButton);


        //デバッグモードへ入るボタン
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), DebugActivity.class);
                startActivity(intent);
            }
        });
        //しおり表示モードへ入るボタン
        shioriButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), ShioriView.class);
                startActivity(intent);
            }
        });

        //出発時刻選択ボタン
        departureTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //時間を入力させるUIの起動
                TimePickerDialog dialog = new TimePickerDialog(MainActivity.getInstance(),new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,int minute) {
                        departureTime[0] =hourOfDay;//取得した時刻を変数に代入
                        departureTime[1] =minute;
                        departureTimeButton.setText(String.format("%02d:%02d",departureTime[0],departureTime[1]));//時刻をボタンの文字にセット

                    }
                },departureTime[0],departureTime[1],true);//初期値を入れる箇所
                dialog.show();
            }
        });

        //到着時刻選択ボタン
        arriveTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(MainActivity.getInstance(),new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,int minute) {
                        Log.d("test",String.format("%02d:%02d", hourOfDay,minute));
                        arriveTime[0] =hourOfDay;
                        arriveTime[1] =minute;
                        arriveTimeButton.setText(String.format("%02d:%02d",arriveTime[0],arriveTime[1]));
                    }
                },arriveTime[0],arriveTime[1],true);
                dialog.show();
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
                Value.genre=(String) spinner.getSelectedItem();
            }
            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //スタートボタン
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test", "startButton pusshed");

                // 公共クラウドシステム（jsonファイル読み込み用） //
                // jsonファイルを読み込む
                JSONObject spots_json = json.ReadJson(getApplicationContext(), "kanko_all.json");
                try {
                    // 観光地の総数を取得
                    int spotsLength = spots_json.getJSONArray("spots").length();
                    Log.d("spotsLength", String.valueOf(spotsLength));


                    String text = ""; // 出力用のテキスト
                    int count = 0; // ヒットした観光地のカウンタ

                    // 指定のジャンルにマッチする観光地の名前を抽出する
                    for (int i = 0; i < spotsLength; i++) {
                        String genre_str = "";
                        String name_str = "";
                        String pref_str="";
                        genre_str = spots_json.getJSONArray("spots").getJSONObject(i).getString("genreM");
                        name_str = spots_json.getJSONArray("spots").getJSONObject(i).getString("name");
                        pref_str=spots_json.getJSONArray("spots").getJSONObject(i).getString("prefectures");
                        // 指定のジャンルと一致した場合
                        if (genre_str.equals(Value.genre) && (pref_str.equals("岐阜県")||pref_str.equals("富山県")||pref_str.equals("石川県")||pref_str.equals("福井県")||pref_str.equals("長野県")||pref_str.equals("愛知県")||pref_str.equals("三重県")||pref_str.equals("滋賀県"))) {
                            Value.spotList.add(new SpotStructure(null, name_str,pref_str, 0, 0, 0,0,0));
                        }
                    }
                    Log.d("text", text);
                    Log.d("count", String.valueOf(count));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < Value.spotList.size(); i++) {
                    SpotStructure tempspot = (SpotStructure) Value.spotList.get(i);
//                    Log.d("test",tempspot.name);
                }
                Log.d("test", String.valueOf(Value.spotList.size()));


                //スレッド処理開始
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ListIterator<SpotStructure> i = Value.spotList.listIterator();//ループをまわしながらarraylistの中身を削除する場合はこれを使う必要がある
                        while(i.hasNext()){//存在するリストでループを回す
                            SpotStructure tempSpotStructure=i.next();//次のリストを参照
                            JSONObject geoCordingResult = null;
                                if(location==null) {//実機のgoogleplacesversionの問題で緯度経度が取れない場合は、岐阜の座標を代入する
                                    location = new Location("a");//文字列はprovider（適当に入れました)
                                    location.setLatitude(35.4650334);
                                    location.setLongitude(136.73929506);
                                }
                                try {
                                //placeIDの取得
                                String urlEncodeResult = URLEncoder.encode(tempSpotStructure.name, "UTF-8");
                                geoCordingResult = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" + urlEncodeResult + "&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk&language=ja"));
                                if(geoCordingResult.getJSONArray("results").isNull(0)){//placeIDがない場合リストから削除
                                    i.remove();
                                }else {
//                                    Log.d("test", tempSpotStructure.name);
  //                                  Log.d("test", tempSpotStructure.prefecture);
//                                    Log.d("test", geoCordingResult.getJSONArray("results").getJSONObject(0).getString("place_id"));
                                    String tempPlaceID = geoCordingResult.getJSONArray("results").getJSONObject(0).getString("place_id");
                                    i.set(new SpotStructure(tempPlaceID, tempSpotStructure.name, tempSpotStructure.prefecture, 0, 0, 0,0,0));//取得したIDをリストにセット
                                        //place詳細検索実行
                                        JSONObject detailSearchResult;
                                        urlEncodeResult = URLEncoder.encode(tempPlaceID, "UTF-8");
                                        detailSearchResult = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + urlEncodeResult + "&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk&language=ja"));
                                        double tempRate = 0;
                                        if (!detailSearchResult.getJSONObject("result").isNull("rating")) {//レートの値がない場合は0をセット
                                            tempRate = detailSearchResult.getJSONObject("result").getDouble("rating");//詳細検索結果、レート値
                                        }
                                        double tempLat = detailSearchResult.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").getDouble("lat");//詳細検索された場所の緯度
                                        double tempLng = detailSearchResult.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").getDouble("lng");//詳細検索された場所の経度
                                    //緯度経度、レートをリストにセット
                                        i.set(new SpotStructure(tempPlaceID, tempSpotStructure.name, tempSpotStructure.prefecture, tempRate, tempLat, tempLng,0,0));
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }

                        for(int j = 0; j < Value.spotList.size(); j++) {//二点間の距離を算出し代入
                            SpotStructure tempspot = (SpotStructure) Value.spotList.get(j);
                            float[] distance=new float[3];//二点間の距離算出結果を格納する変数
                            Location.distanceBetween(location.getLatitude(),location.getLongitude(),tempspot.lat,tempspot.lng,distance);//入力された場所と候補地との距離算出
                            tempspot.distance=distance[0];
                            tempspot.eval= Value.alpha*tempspot.distance+(10000000-Value.beta*tempspot.rate);
                        }

                        for(int j = 0; j < Value.spotList.size(); j++) {

                            SpotStructure tempspot = (SpotStructure) Value.spotList.get(j);
                            Log.d("test","distance:"+String.valueOf(tempspot.distance));
                            Log.d("test",tempspot.name+tempspot.placeID+String.valueOf(tempspot.rate)+String.valueOf(tempspot.lat)+String.valueOf(tempspot.lng));
                            Log.d("test","eval:"+String.valueOf(tempspot.eval));
                        }
                        /*
                        double maxEval=0;
                        for(int j = 0; j < Value.spotList.size(); j++) {
                            SpotStructure tempspot = (SpotStructure) Value.spotList.get(j);
                            if(maxEval<tempspot.eval){
                                maxEval=tempspot.eval;
                            }
                        }
                        Log.d("test","max eval is"+String.valueOf(maxEval));
                        */
                        //Comparatorを用い距離順にソートする
                        Collections.sort(Value.spotList,new SpotStructureComparator());
                        for(int j = 0; j < Value.spotList.size(); j++) {

                            SpotStructure tempspot = (SpotStructure) Value.spotList.get(j);
                            Log.d("test","rate:"+String.valueOf(tempspot.rate));
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getApplication(), DebugActivity.class);
                                startActivity(intent);
                                Log.d("test", "in runnable run");
                            }
                        });
                    }
                }).start();
            }
        });
    }

/*
                //googlePlacesAPIのスレッド開始
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject placesResult1=null;
                        JSONObject placesResult2=null;
                        JSONObject placesResult3=null;
                        try {
                            if(location==null){//実機のgoogleplacesversionの問題で緯度経度が取れない場合は、岐阜の座標を代入する
                                location=new Location("a");//文字列はprovider（適当に入れました)
                                location.setLatitude(35.4650334);
                                location.setLongitude(136.73929506);
                            }
                            placesResult1=httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + location.getLatitude() + "," + location.getLongitude() + "&radius=500&language=ja&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                            int candLength1=placesResult1.getJSONArray("results").length();
                            //表示用の文字列生成
                            for(int i=0;i<candLength1;i++){
                                String id=placesResult1.getJSONArray("results").getJSONObject(i).getString("place_id");
                                String name=placesResult1.getJSONArray("results").getJSONObject(i).getString("name");
                                Double lat=placesResult1.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                                Double lng=placesResult1.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                                Value.spotList.add(new SpotStructure(id,name,0,lat,lng));//候補地のIDをリストに格納する
                            }
                            Log.d("test",String.valueOf(location.getLatitude()) + "  a    " + String.valueOf(location.getLongitude()));
                            //nextPageToken抜き取り
                            if(!placesResult1.isNull("next_page_token")){
                                Log.d("test","result1 is not null");
                                placesResult2=httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + location.getLatitude() + "," + location.getLongitude() + "&pagetoken=" + placesResult1.getString("next_page_token") + "&radius=500&language=ja&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                int candLength2=placesResult2.getJSONArray("results").length();
                                Log.d("test",String.valueOf(candLength2));
                                //表示用の文字列生成
                                for(int i=0;i<candLength2;i++){
                                    String id=placesResult2.getJSONArray("results").getJSONObject(i).getString("place_id");
                                    String name=placesResult2.getJSONArray("results").getJSONObject(i).getString("name");
                                    Double lat=placesResult2.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                                    Double lng=placesResult2.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                                    Value.spotList.add(new SpotStructure(id,name,0,lat,lng));//候補地のIDをリストに格納する
                                }

                                Log.d("test","place result executed");
                                if(!placesResult2.isNull("next_page_token")){
                                    placesResult3=httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + location.getLatitude() + "," + location.getLongitude() + "&pagetoken=" + placesResult2.getString("next_page_token") + "&radius=500&language=ja&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                    int candLength3=placesResult3.getJSONArray("results").length();
                                    //表示用の文字列生成
                                    for(int i=0;i<candLength3;i++){
                                        String id=placesResult3.getJSONArray("results").getJSONObject(i).getString("place_id");
                                        String name=placesResult3.getJSONArray("results").getJSONObject(i).getString("name");
                                        Double lat=placesResult3.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                                        Double lng=placesResult3.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                                        Value.spotList.add(new SpotStructure(id,name,0,lat,lng));//候補地のIDをリストに格納する
                                    }
                                }else{
                                }
                            }else{
                            }
                            for(int i=0;i<Value.spotList.size();i++){
                                SpotStructure tempspot=(SpotStructure)Value.spotList.get(i);
                                Log.d("test",tempspot.name);
                            }
//                            for(int i=0;i<20;i++) {
//                                Log.d("test", "i is:"+String.valueOf(i));
//                                Log.d("test", placesResult1.getJSONArray("results").getJSONObject(i).getString("name"));
//                            }
                        } catch (MalformedURLException e1) {
                            e1.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                    new HttpGetPlaces(_latch).execute(new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + location.getLatitude() + "," + location.getLongitude() + "&radius=50000&language=ja&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getApplication(), DebugActivity.class);
                                startActivity(intent);
                                Log.d("test","in runnable run");

                            }
                        });
                    }
                }).start();
                }
    */
             //   SpotStructure A=(SpotStructure)Value.spotList.get(0);
            //    Log.d("test",A.name)

    protected void onStart() {
        //GoogleAPIに接続開始
        mGoogleApiClient.connect();
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(mGoogleApiClient, getIndexApiAction());
    }
    protected void onStop() {
        //定期処理を終了させる　これを呼ばないとアプリを終了させても裏で動き続ける
        //googleAPIへの接続終了
        mGoogleApiClient.disconnect();
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mGoogleApiClient, getIndexApiAction());
    }

    //******************googleApiより、gpsの値を取得するのに必要なメソッド*****************
    //googleAPIにより、アプリ起動時の現在地取得に使用　このメソッドはgoogleAPIのセットアップが終了した後呼び出される
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //パーミッションチェック
            Log.d("test","perm out");
            return;
        }
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);//端末より、最後にGPSで取得された座標を取得
    }
    //gps取得用クラスの子メソッド、消せない
    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
    //開始時位置取得のために必要なメソッド、onStartとonStopで呼ばれる
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }
    //******************googleApiより、gpsの値を取得するのに必要なメソッドここまで*****************
}

/*
//指定座標周辺のロケーション取得用、非同期処理クラス
final class HttpGetPlaces extends AsyncTask<URL, Void, String> {

    private CountDownLatch _latch;

    public HttpGetPlaces(CountDownLatch latch) {
        super();
        this._latch=latch;
    }

    @Override
    protected String doInBackground(URL... urls) {
        try {
            _latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    @Override
        protected void onPostExecute(String result) {

            Log.d("test","result");
            JSONObject jsonObject=null;
            try{
                jsonObject=new JSONObject(result);
                //resultの総数を取得
            int candLength=jsonObject.getJSONArray("results").length();
            //表示用の文字列生成
            for(int i=0;i<candLength;i++){
                String id=jsonObject.getJSONArray("results").getJSONObject(i).getString("place_id");
                String name=jsonObject.getJSONArray("results").getJSONObject(i).getString("name");
                Double lat=jsonObject.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                Double lng=jsonObject.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                Value.spotList.add(new SpotStructure(id,name,0,lat,lng));//候補地のIDをリストに格納する
                Log.d("test","in asynctask,"+name);
            }
            //nextPageToken抜き取り
            if(!jsonObject.isNull("next_page_token")){
                new HttpGetPlaces(_latch).execute(new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + String.valueOf(35) + "," +
                        String.valueOf(135) + "&pagetoken=" + jsonObject.getString("next_page_token") + "&radius=50000&language=ja&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
            }else{

                _latch.countDown();

            }
        } catch (JSONException e) {
            e.printStackTrace();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
*/