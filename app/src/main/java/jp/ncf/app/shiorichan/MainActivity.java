package jp.ncf.app.shiorichan;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

// ogawa test comment
//値渡し用、静的変数
class Value {
    public static double lat = 0.0;//デバッグアクティビティでのみ使っている変数なので、そちらがいらなくなったら消す
    public static double lng = 0.0;//デバッグアクティビティでのみ使っている変数なので、そちらがいらなくなったら消す
    public static String next_page_token = null;//デバッグアクティビティでのみ使っている変数なので、そちらがいらなくなったら消す
    public static String genre = "";
    public static ArrayList<SpotStructure> itineraryPlaceList=new ArrayList<SpotStructure>();
    public static String nowPrefecture=null;
    public static String input_text = null; // 自由テキスト入力文字列
    public static boolean error_flag = false; // 入力エラーのフラグ
}


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    String genreStrings[] = {"自然景観","施設景観","公園・庭園","動・植物","文化史跡","文化施設","神社仏閣","地域風俗・風習","その他","祭事","イベント","イベント鑑賞","文化施設","スポーツ・レジャー","温泉","名産品","郷土料理店","車","その他乗り物","旅館","ホテル","民宿・ペンション"};//ジャンルを格納するリスト
    GoogleApiClient mGoogleApiClient;//開始時に自己位置を取得するため、googleapiを利用
    private Location location;//開始時、現在地の座標を保存する変数
    private JsonReader json;
    private HttpGetter httpGet;
    private HttpBitmapGetter httpBitmapGet;
    final Handler handler = new Handler(Looper.getMainLooper());
    ProgressDialog progressDialog;//読み込み中表示クラス


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

//        final int[] departureTime = {10,0};//(時間,分)の順に格納
//        final int[] arriveTime = {20,0};//(時間,分)の順に格納
        final Date departureTime=new Date();
        departureTime.setHours(10);
        departureTime.setMinutes(0);
        departureTime.setSeconds(0);
        final Date arriveTime=new Date();
        arriveTime.setHours(20);
        arriveTime.setMinutes(0);
        arriveTime.setSeconds(0);
        json=new JsonReader();//Json読み込み用クラスのインスタンス
        httpGet=new HttpGetter();//Httpリクエスト送信用クラスのインスタンス
        progressDialog = new ProgressDialog(this);//読み込み中表示,// 初期設定
        progressDialog.setTitle("読み込み中…");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        Button shioriButton = (Button) findViewById(R.id.shioriButton);
        Button sendButton = (Button) findViewById(R.id.debugButton);
        final Button departureTimeButton=(Button)findViewById(R.id.departureTimeButton);
        departureTimeButton.setText(String.format("%02d:%02d",departureTime.getHours(),departureTime.getMinutes()));
        final Button arriveTimeButton=(Button)findViewById(R.id.arriveTimeButton);
        arriveTimeButton.setText(String.format("%02d:%02d",arriveTime.getHours(),arriveTime.getMinutes()));
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
                        departureTime.setHours(hourOfDay);
                        departureTime.setMinutes(minute);
                        departureTimeButton.setText(String.format("%02d:%02d",departureTime.getHours(),departureTime.getMinutes()));//時刻をボタンの文字にセット

                    }
                },departureTime.getHours(),departureTime.getMinutes(),true);//初期値を入れる箇所
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
                        arriveTime.setHours(hourOfDay);
                        arriveTime.setMinutes(minute);
                        arriveTimeButton.setText(String.format("%02d:%02d",arriveTime.getHours(),arriveTime.getMinutes()));//時刻をボタンの文字にセット

                    }
                },arriveTime.getHours(),arriveTime.getMinutes(),true);//初期値を入れる箇所
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

                // 初期化処理
                Value.error_flag = false; // 入力エラーフラグの初期化
                Value.genre = "";       // ジャンルの初期化
                Value.itineraryPlaceList=new ArrayList<SpotStructure>();

                // ====== 自由テキスト入力受け取り ======
                // EditTextオブジェクトを取得
                EditText editText = (EditText)findViewById(R.id.editText);

                // 入力された文字を取得
                final String input_text = editText.getText().toString();
                Value.input_text = input_text;
                Log.d("inputtext", Value.input_text);

                // 入力テキストが未入力であった場合
                if (Value.input_text.length() == 0) {
                    Log.d("input error message", "何も入力されていません");
                    editText.setError("何も入力されていません");
                    Value.error_flag = true;
                }

                // 入力テキストが存在する場合，ジャンルとのマッチングを行う
                else {
                    // ====== 自由テキストをジャンルに変換 ======
                    // 辞書とジャンル名の対応jsonを読み込む
                    JSONObject pair_json = json.ReadJson(getApplicationContext(), "pair.json");
                    // 自由テキストが辞書に登録されている場合の処理
                    try {
                        // 自由テキストに対応するジャンル名のリストを取得する
                        JSONArray genre_list = pair_json.getJSONArray(Value.input_text);

                        // ジャンルが１つであれば決定
                        if (genre_list.length() == 1) {
                            Value.genre = (String) genre_list.get(0);
                        }
                        // ジャンルが複数ある場合はランダムで1つ選択する
                        else {
                            // 乱数を発生する
                            Random rand = new Random();
                            int rand_n = rand.nextInt(genre_list.length());
                            Value.genre = (String) genre_list.get(rand_n);
                        }
                        // 出力確認
                        Log.d("genre result", Value.genre);
                    } catch (JSONException e) {
                        // 入力テキストが辞書に登録されていなかった場合
                        e.printStackTrace();
                        Log.d("input error message", Value.input_text + " は辞書に登録されていません");
                        editText.setError(Value.input_text + " は辞書に登録されていません");
                        Value.error_flag = true;
                    }
                }

                // 入力エラーが発生していなければ処理を実行する
                if (Value.error_flag == false) {

                    progressDialog.show();
                    //◆スレッド処理開始
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

//******************//実機のgoogleplacesversionの問題で緯度経度が取れない場合は、岐阜の座標を代入する*****************************
        /*                if(location==null) {
                            location = new Location("a");//文字列はprovider（適当に入れました)
                            location.setLatitude(35.4650334);
                            location.setLongitude(136.73929506);
                        }
         */
                            location = new Location("a");//とりあえずデバッグ用として、本来GPSの値が入るところに浜松の値を代入
                            location.setLatitude(34.788739);
                            location.setLongitude(137.6420052);

//********************現在地の緯度経度から今いる県を取得する*********************************
                            Geocoder mGeocoder;    //緯度・経度から地名への変換
                            mGeocoder = new Geocoder(getApplicationContext(), Locale.JAPAN);
                            //GeoCoder を用いて県名を取得
                            StringBuffer buff = new StringBuffer();
                            try {
                                List<Address> addrs = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                for (Address addr : addrs) {
                                    buff.append(addr.getAdminArea());
                                }
                            } catch (IOException e) {
                                Log.e("HelloLocationActivity", e.toString());
                            }
                            //取得した県名
                            Value.nowPrefecture = buff.toString();


//**************公共クラウドシステムの入ったjsonファイルを取得する******************
                            ////// 公共クラウドシステム（jsonファイル読み込み用） ///////
                            // jsonファイルを読み込む
                            JSONObject spots_json = json.ReadJson(getApplicationContext(), "kanko_all_add_limit.json");
                            int spotsLength = 0;
                            try {
                                // 観光地の総数を取得
                                spotsLength = spots_json.getJSONArray("spots").length();
                                Log.d("spotsLength", String.valueOf(spotsLength));
                            } catch (JSONException e) {
                            }

//****************************************開始地点を定義***************************************
                            Value.itineraryPlaceList.add(new SpotStructure(null, "出発地", null, Value.nowPrefecture, 0, location.getLatitude(), location.getLongitude(), 0, null, null,departureTime,null,null));
                            Log.d("出発地の時刻",Value.itineraryPlaceList.get(0).departTime.toString());

//********************レビュー順にソートし、一つ目の候補地を確定する************************
                            ArrayList<SpotStructure> firstCandsList = new ArrayList<SpotStructure>();//ソート用リスト初期化
                            JSONObject neighborDicObject = json.ReadJson(getApplicationContext(), "neighbor_pref.json");//隣接県情報の入ったjson読み出し
                            try {
                                for (int i = 0; i < spotsLength; i++) {
                                    String pref_str = spots_json.getJSONArray("spots").getJSONObject(i).getString("prefectures");
                                    String genre_str = spots_json.getJSONArray("spots").getJSONObject(i).getString("genreM");
                                    //隣接県であり、指定ジャンルと一致した場合リストに格納する
                                    if ((CheckNeighborPrefecture(pref_str, neighborDicObject) && genre_str.equals(Value.genre))) {
                                        String name_str = spots_json.getJSONArray("spots").getJSONObject(i).getString("name");
                                        String placeID_str = spots_json.getJSONArray("spots").getJSONObject(i).getString("place_id");
                                        String explainText = spots_json.getJSONArray("spots").getJSONObject(i).getString("explain");
                                        double rate_double = spots_json.getJSONArray("spots").getJSONObject(i).getDouble("rating");
                                        double lat_double = spots_json.getJSONArray("spots").getJSONObject(i).getDouble("lat");
                                        double lng_double = spots_json.getJSONArray("spots").getJSONObject(i).getDouble("lng");
                                        float[] distance = new float[3];//二点間の距離算出結果を格納する変数
                                        Location.distanceBetween(location.getLatitude(), location.getLongitude(), lat_double, lng_double, distance);//入力された場所と候補地との距離算出
                                        firstCandsList.add(new SpotStructure(placeID_str, name_str, genre_str, pref_str, rate_double, lat_double, lng_double, distance[0], explainText, null,null,null,null));
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e("test", e.toString());
                            }
                            Log.d("test", "first candidate size is" + firstCandsList.size());
                            //Comparatorを用いレビューが高い順にソートする
                            Collections.sort(firstCandsList, new SpotStructureRateComparator());
                            //レビュー4.5以上の候補地から一番近い場所を選ぶ
                            int minDistanceNumber = 0;
                            double minDistance = Double.MAX_VALUE;
                            for (int i = 0; firstCandsList.get(i).rate > 4.5; i++) {
                                if (firstCandsList.get(i).distance < minDistance) {
                                    minDistance = firstCandsList.get(i).distance;
                                    minDistanceNumber = i;
                                }
                            }
                            //一番初めに訪れる観光地決定、旅程リストに追加
                            Value.itineraryPlaceList.add(firstCandsList.get(minDistanceNumber));

                            //directionAPIに出発地と第一観光地を渡し、どれくらい時間がかかるかを求める。
                            try {
                                JSONObject tempDirectionSearch = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/directions/json?origin="+location.getLatitude()+","+location.getLongitude()+"&destination=place_id:"+Value.itineraryPlaceList.get(1).placeID+"&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                //かかる時間（秒）
                                int tempSecondToDestination=tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");
                                //カレンダー型を用いることで時間の加算を行う
                                Calendar calendar=Calendar.getInstance();
                                calendar.setTime(Value.itineraryPlaceList.get(0).departTime);
                                calendar.add(Calendar.SECOND,tempSecondToDestination);
                                //かかる時間＋観光地で観光に使う時間を求め、代入
                                Date tempDepartTime=addGenreWaitTime(calendar.getTime(),Value.itineraryPlaceList.get(1).genre);
                                //ポリライン（あとで地図に表示をする、経由道路の道情報）を代入
                                String tempPolyline=tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                                //得た情報をセットする
                                Value.itineraryPlaceList.set(1,new SpotStructure(Value.itineraryPlaceList.get(1).placeID,
                                        Value.itineraryPlaceList.get(1).name,
                                        Value.itineraryPlaceList.get(1).genre,
                                        Value.itineraryPlaceList.get(1).prefecture,
                                        Value.itineraryPlaceList.get(1).rate,
                                        Value.itineraryPlaceList.get(1).lat,
                                        Value.itineraryPlaceList.get(1).lng,
                                        Value.itineraryPlaceList.get(1).distance,
                                        Value.itineraryPlaceList.get(1).explainText,
                                        Value.itineraryPlaceList.get(1).image,
                                        tempDepartTime,
                                        Value.itineraryPlaceList.get(1).mapImage,
                                        tempPolyline));
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d("test", "first:" + Value.itineraryPlaceList.get(1).name + "dist:" + String.valueOf(Value.itineraryPlaceList.get(1).distance) + "id" + String.valueOf(Value.itineraryPlaceList.get(1).placeID)+"depTime"+Value.itineraryPlaceList.get(1).departTime.toString());

//******************************昼食の場所が一つ目の観光地のあとと仮定して、その場所付近の昼食場所をgoogle neabysearchで検索する**********************
// 昼食のタイミングが固定なのは非常にまずい。
                            JSONObject nearbySearchResult = null;
                            ArrayList<SpotStructure> lunchCandsList = new ArrayList<SpotStructure>();//ソート用リスト初期化

                            try {//googleにリクエストを送信し、1番目の観光地付近にあるレストランを全てリストに入れる
                                nearbySearchResult = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + Double.toString(Value.itineraryPlaceList.get(1).lat) + "," + Double.toString(Value.itineraryPlaceList.get(1).lng) + "&radius=2000&type=restaurant&language=ja&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                int candLength = nearbySearchResult.getJSONArray("results").length();
                                for (int i = 0; i < candLength; i++) {
                                    String pref_str = "restaurant don't have pref,if you needs, do DetailSearch";
                                    String genre_str = "レストラン";
                                    String name_str = nearbySearchResult.getJSONArray("results").getJSONObject(i).getString("name");
                                    String placeID_str = nearbySearchResult.getJSONArray("results").getJSONObject(i).getString("place_id");
                                    String explainText = "restaurant isn't have explain message";
                                    double rate_double = 0;
                                    double lat_double = nearbySearchResult.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                                    double lng_double = nearbySearchResult.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                                    float[] distance = new float[3];//二点間の距離算出結果を格納する変数
                                    Location.distanceBetween(Value.itineraryPlaceList.get(1).lat, Value.itineraryPlaceList.get(1).lng, lat_double, lng_double, distance);//入力された場所と候補地との距離算出
                                    lunchCandsList.add(new SpotStructure(placeID_str, name_str, genre_str, pref_str, rate_double, lat_double, lng_double, distance[0], explainText, null,null,null,null));
                                }
                            } catch (JSONException e) {
                                Log.e("test", e.toString());
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                            //レストランの入ったリストを距離順にソート
                            Collections.sort(lunchCandsList, new SpotStructureDistanceComparator());
                            //最も距離の近いレストランの場所をitineraryPlaceListに代入
                            Value.itineraryPlaceList.add(lunchCandsList.get(0));

                            try {//第一観光地からレストランまでに掛かる時間を求める
                                JSONObject tempDirectionSearch = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/directions/json?origin=place_id:"+Value.itineraryPlaceList.get(1).placeID+"&destination=place_id:"+Value.itineraryPlaceList.get(2).placeID+"&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                Log.d("test",tempDirectionSearch.toString());
                                int tempSecondToDestination=tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");
                                Calendar calendar=Calendar.getInstance();
                                calendar.setTime(Value.itineraryPlaceList.get(1).departTime);
                                calendar.add(Calendar.SECOND,tempSecondToDestination);
                                Date tempDepartTime=addGenreWaitTime(calendar.getTime(),Value.itineraryPlaceList.get(2).genre);
                                String tempPolyline=tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                                Value.itineraryPlaceList.set(2,new SpotStructure(
                                        Value.itineraryPlaceList.get(2).placeID,
                                        Value.itineraryPlaceList.get(2).name,
                                        Value.itineraryPlaceList.get(2).genre,
                                        Value.itineraryPlaceList.get(2).prefecture,
                                        Value.itineraryPlaceList.get(2).rate,
                                        Value.itineraryPlaceList.get(2).lat,
                                        Value.itineraryPlaceList.get(2).lng,
                                        Value.itineraryPlaceList.get(2).distance,
                                        Value.itineraryPlaceList.get(2).explainText,
                                        Value.itineraryPlaceList.get(2).image,
                                        tempDepartTime,
                                        Value.itineraryPlaceList.get(2).mapImage,
                                        tempPolyline));
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d("test", "lunch:" + Value.itineraryPlaceList.get(2).name + "dist:" + String.valueOf(Value.itineraryPlaceList.get(2).distance) + "id" + String.valueOf(Value.itineraryPlaceList.get(2).placeID)+"depTime"+Value.itineraryPlaceList.get(2).departTime.toString());


//******************二箇所目以降の候補地を確定させる*************************
                            ArrayList<SpotStructure> secondOrLaterCandsList = new ArrayList<SpotStructure>();//リスト初期化
                            try {
                                //第一観光地からみて、出発地とは反対方向にある観光地を探す。
                                boolean southFromFirstPlace=false;
                                boolean eastFromFirstPlace=false;
                                if(Value.itineraryPlaceList.get(0).lat<Value.itineraryPlaceList.get(1).lat){
                                    southFromFirstPlace=false;
                                }else{
                                    southFromFirstPlace=true;
                                }
                                if(Value.itineraryPlaceList.get(0).lng<Value.itineraryPlaceList.get(1).lng){
                                    eastFromFirstPlace=true;
                                }else{
                                    eastFromFirstPlace=false;
                                }
                                for (int i = 0; i < spotsLength; i++) {
                                    String pref_str = spots_json.getJSONArray("spots").getJSONObject(i).getString("prefectures");
                                    double lat_double = spots_json.getJSONArray("spots").getJSONObject(i).getDouble("lat");
                                    double lng_double = spots_json.getJSONArray("spots").getJSONObject(i).getDouble("lng");
                                    float[] distance = new float[3];//二点間の距離算出結果を格納する変数
                                    Location.distanceBetween(Value.itineraryPlaceList.get(1).lat, Value.itineraryPlaceList.get(1).lng, lat_double, lng_double, distance);//入力された場所と候補地との距離算出
                                    boolean tempThresholdFlg=false;
                                    //隣接県かつ近すぎない候補地をリストに代入
                                    if (CheckNeighborPrefecture(pref_str, neighborDicObject) && distance[0]>500) {
                                        if(southFromFirstPlace==true && eastFromFirstPlace==true){
                                            //出発地の方向にある観光地はリストに入れない
                                            if(lat_double<Value.itineraryPlaceList.get(0).lat && lng_double>Value.itineraryPlaceList.get(0).lng){
                                                tempThresholdFlg=true;
                                            }
                                        } else if (southFromFirstPlace == true && eastFromFirstPlace == false) {
                                            if(lat_double<Value.itineraryPlaceList.get(0).lat && lng_double<Value.itineraryPlaceList.get(0).lng){
                                                tempThresholdFlg=true;
                                            }
                                        }else if(southFromFirstPlace==false&& eastFromFirstPlace==true){
                                            if(lat_double>Value.itineraryPlaceList.get(0).lat && lng_double<Value.itineraryPlaceList.get(0).lng){
                                                tempThresholdFlg=true;
                                            }
                                        }else{
                                            if(lat_double>Value.itineraryPlaceList.get(0).lat && lng_double>Value.itineraryPlaceList.get(0).lng){
                                                tempThresholdFlg=true;
                                            }
                                        }
                                    }
                                    //条件を満たした観光地のみをリストに入れる
                                    if(tempThresholdFlg) {
                                        String genre_str = spots_json.getJSONArray("spots").getJSONObject(i).getString("genreM");
                                        String name_str = spots_json.getJSONArray("spots").getJSONObject(i).getString("name");
                                        String placeID_str = spots_json.getJSONArray("spots").getJSONObject(i).getString("place_id");
                                        double rate_double = spots_json.getJSONArray("spots").getJSONObject(i).getDouble("rating");
                                        String explainText = spots_json.getJSONArray("spots").getJSONObject(i).getString("explain");
                                        secondOrLaterCandsList.add(new SpotStructure(placeID_str, name_str, genre_str, pref_str, rate_double, lat_double, lng_double, distance[0], explainText, null, null, null, null));
                                    }
                                }
                            } catch (JSONException e) {
                            }
                            //Comparatorを用い距離が短い順にソートする
                            Collections.sort(secondOrLaterCandsList, new SpotStructureDistanceComparator());
                            Date ifReturnArriveTime = null;//現時点の観光地から家に帰った場合、家に着く時間を格納する
                            String ifReturnPolyline=null;//現時点の観光地から家に帰った場合のポリライン
                            try {
                                JSONObject tempDirectionSearchToHome = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/directions/json?origin=place_id:"+ Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).placeID+"&destination="+location.getLatitude()+","+location.getLongitude()+"&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                int tempSecondToDestinationToHome = tempDirectionSearchToHome.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");
                                Calendar calendarToHome=Calendar.getInstance();
                                calendarToHome.setTime(Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).departTime);
                                calendarToHome.add(Calendar.SECOND,tempSecondToDestinationToHome);
                                ifReturnArriveTime=addGenreWaitTime(calendarToHome.getTime(),Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).genre);
                                ifReturnPolyline=tempDirectionSearchToHome.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                            } catch (JSONException e) {
                                e.printStackTrace();

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                            //ifReturnArriveTImeが到着予定時間を越すまで、観光地検索を繰り返す
                            while(arriveTime.compareTo(ifReturnArriveTime)==1){
                                Value.itineraryPlaceList.add(secondOrLaterCandsList.get(0));
                                secondOrLaterCandsList.remove(0);
                                try {
                                    int focusPlaceNum=Value.itineraryPlaceList.size()-1;
                                    int beforePlaceNum=Value.itineraryPlaceList.size()-2;
                                    JSONObject tempDirectionSearch = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/directions/json?origin=place_id:"+Value.itineraryPlaceList.get(beforePlaceNum).placeID+"&destination=place_id:"+Value.itineraryPlaceList.get(focusPlaceNum).placeID+"&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                    int tempSecondToDestination=tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");
                                    Calendar calendar=Calendar.getInstance();
                                    Log.d("calender1",calendar.toString());
                                    calendar.setTime(Value.itineraryPlaceList.get(beforePlaceNum).departTime);
                                    Log.d("calender2",calendar.toString());
                                    calendar.add(Calendar.SECOND,tempSecondToDestination);
                                    Log.d("calender3",calendar.toString());
                                    Date tempDepartTime=addGenreWaitTime(calendar.getTime(),Value.itineraryPlaceList.get(focusPlaceNum).genre);
                                    Log.d("genre",Value.itineraryPlaceList.get(focusPlaceNum).genre);
                                    Log.d("tempDepartTime",tempDepartTime.toString());
                                    String tempPolyline=tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                                    Value.itineraryPlaceList.set(focusPlaceNum,new SpotStructure(
                                            Value.itineraryPlaceList.get(focusPlaceNum).placeID,
                                            Value.itineraryPlaceList.get(focusPlaceNum).name,
                                            Value.itineraryPlaceList.get(focusPlaceNum).genre,
                                            Value.itineraryPlaceList.get(focusPlaceNum).prefecture,
                                            Value.itineraryPlaceList.get(focusPlaceNum).rate,
                                            Value.itineraryPlaceList.get(focusPlaceNum).lat,
                                            Value.itineraryPlaceList.get(focusPlaceNum).lng,
                                            Value.itineraryPlaceList.get(focusPlaceNum).distance,
                                            Value.itineraryPlaceList.get(focusPlaceNum).explainText,
                                            Value.itineraryPlaceList.get(focusPlaceNum).image,
                                            tempDepartTime,
                                            Value.itineraryPlaceList.get(focusPlaceNum).mapImage,
                                            tempPolyline));
                                    Log.d("test", String.valueOf(focusPlaceNum)+":" + Value.itineraryPlaceList.get(focusPlaceNum).name);
                                            Log.d("dist:" , String.valueOf(Value.itineraryPlaceList.get(focusPlaceNum).distance));
                                            Log.d("id" , String.valueOf(Value.itineraryPlaceList.get(focusPlaceNum).placeID));
                                            Log.d("depTime",Value.itineraryPlaceList.get(focusPlaceNum).departTime.toString());
                                    try {
                                        JSONObject tempDirectionSearchToHome = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/directions/json?origin=place_id:"+ Value.itineraryPlaceList.get(focusPlaceNum).placeID+"&destination="+location.getLatitude()+","+location.getLongitude()+"&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                        int tempSecondToDestinationToHome = tempDirectionSearchToHome.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");
                                        Calendar calendarToHome=Calendar.getInstance();
                                        calendarToHome.setTime(Value.itineraryPlaceList.get(focusPlaceNum).departTime);
                                        calendarToHome.add(Calendar.SECOND,tempSecondToDestinationToHome);
                                        ifReturnArriveTime=addGenreWaitTime(calendarToHome.getTime(),Value.itineraryPlaceList.get(focusPlaceNum).genre);
                                        ifReturnPolyline=tempDirectionSearchToHome.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

//*************************************到着地を旅程リストに代入する***********************************
                            Value.itineraryPlaceList.add(new SpotStructure(null,"到着地",null,Value.nowPrefecture,0,location.getLatitude(),location.getLongitude(),0,null,null,ifReturnArriveTime,null,ifReturnPolyline));

                            for (int i = 0; i < Value.itineraryPlaceList.size(); i++) {
                                Log.d("test", "num:" + String.valueOf(i) + "name:" + Value.itineraryPlaceList.get(i).name + "rate" + String.valueOf(Value.itineraryPlaceList.get(i).rate) + "depTime"+Value.itineraryPlaceList.get(i).departTime.toString());
                            }

//**********************************しおりに載せる観光地は全て決定されたため、各観光地に対してDetail検索を行い写真とレストランのレート、地図を取得する
                            for (int i = 0; i < Value.itineraryPlaceList.size(); i++) {
                                Log.d("executing detail search", Value.itineraryPlaceList.get(i).name);
                                try {
                                    Bitmap mapsStaticsResult=null;
                                    if(i!=0){
                                        Log.d("test",Value.itineraryPlaceList.get(i).polyline);
                                        //ポリラインをhttpリクエストとして渡し、地図情報を得る
                                        mapsStaticsResult = httpBitmapGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/staticmap?size=400x400&path=color:0xff0000ff|weight:5%7Cenc:"+Value.itineraryPlaceList.get(i).polyline+"&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                    }
                                    JSONObject detailSearchResult = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + Value.itineraryPlaceList.get(i).placeID + "&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk&language=ja"));
                                    String pref_str = Value.itineraryPlaceList.get(i).prefecture;
                                    String genre_str = Value.itineraryPlaceList.get(i).genre;
                                    String name_str = Value.itineraryPlaceList.get(i).name;
                                    String placeID_str = Value.itineraryPlaceList.get(i).placeID;
                                    double rate_double = 0;
                                    if(!detailSearchResult.isNull("result")) {
                                        if (!detailSearchResult.getJSONObject("result").isNull("rating")) {
                                            //レストランは詳細検索を行っておらず、レート値がないためここで得る
                                            rate_double = detailSearchResult.getJSONObject("result").getDouble("rating");
                                        }
                                    }
                                    double lat_double = Value.itineraryPlaceList.get(i).lat;
                                    double lng_double = Value.itineraryPlaceList.get(i).lat;
                                    String explainText = Value.itineraryPlaceList.get(i).explainText;
                                    Bitmap img_bitmap = null;
                                    if (detailSearchResult.getJSONObject("result").isNull("photos")) {
                                        Log.d("test", String.valueOf(i) + " is not have photo");
                                    } else {
                                        //観光地の写真が存在する場合は、ここでリクエストを送りBitmapを得る
                                        img_bitmap = httpBitmapGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + detailSearchResult.getJSONObject("result").getJSONArray("photos").getJSONObject(0).getString("photo_reference") + "&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                    }
                                    Value.itineraryPlaceList.set(i, new SpotStructure(placeID_str, name_str, genre_str, pref_str, rate_double, lat_double, lng_double, 0, explainText, img_bitmap,null,mapsStaticsResult,null));
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            progressDialog.dismiss();//読み込み中表示、終了
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //ShioriViewにインテントを移す
                                    Intent intent = new Intent(getApplication(), ShioriView.class);
                                    startActivity(intent);
                                    Log.d("test", "in runnable run");
                                }
                            });
                        }
                    }).start();
                } // 入力エラーのif文のカッコ


//******************************
/*
                //スレッド処理開始
                new Thread(new Runnable() {
                    @Override
                    public void run() {
>>>>>>> Stashed changes
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
//                                    Log.d("test", tempSpotStructure.prefecture);
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
//                        double maxEval=0;
//                        for(int j = 0; j < Value.spotList.size(); j++) {
//                            SpotStructure tempspot = (SpotStructure) Value.spotList.get(j);
//                            if(maxEval<tempspot.eval){
//                                maxEval=tempspot.eval;
//                            }
//                        }
//                        Log.d("test","max eval is"+String.valueOf(maxEval));
//                        //Comparatorを用い距離順にソートする
                        Collections.sort(Value.spotList,new SpotStructureRateComparator());
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
            */
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

    //******************googleApiより、gpsの値を取得するのに必要なメソッドここから*****************
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

    public boolean CheckNeighborPrefecture(String checkedPlace,JSONObject neighborDicObject) throws JSONException {
        //jsonファイルを参照し、隣接しているかをチェックする
        if(Value.nowPrefecture.equals(checkedPlace)){
//            Log.d("test","true"+Value.nowPlace+"chk"+checkedPlace);
            return true;
        }
        JSONArray neighborArray=neighborDicObject.getJSONArray(Value.nowPrefecture);
        for(int i=0;i<neighborArray.length();i++){
            if(checkedPlace.equals(neighborArray.getString(i))){
//                Log.d("test","true"+neighborArray.getString(i)+"chk"+checkedPlace);
                return true;
            }
        }
//        Log.d("test","false"+checkedPlace);
        return false;
    }
    public Date addGenreWaitTime(Date date,String genre){
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        if(genre.equals("レストラン")){
            calendar.add(Calendar.HOUR,1);
            return calendar.getTime();
        }
        if(genre.equals("自然景観")) {
            calendar.add(Calendar.HOUR,1);
            return calendar.getTime();
        }else if(genre.equals("施設景観")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("公園・庭園")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("動・植物")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("文化史跡")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("文化施設")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("神社仏閣")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("地域風俗・風習")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("祭事")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("イベント")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("イベント鑑賞")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("文化施設")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("スポーツ・レジャー")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("温泉")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("名産品")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("郷土料理店")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("車")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("その他乗り物")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("旅館")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("ホテル")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("民宿・ペンション")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }
        Log.e("test","category to spending time error!");
        return null;

    }
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









