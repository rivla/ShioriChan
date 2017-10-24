package jp.ncf.app.shiorichan;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

// ogawa test comment
//値渡し用、静的変数
class Value {
    public static double lat = 0.0;//デバッグアクティビティでのみ使っている変数なので、そちらがいらなくなったら消す
    public static double lng = 0.0;//デバッグアクティビティでのみ使っている変数なので、そちらがいらなくなったら消す
    public static String next_page_token = null;//デバッグアクティビティでのみ使っている変数なので、そちらがいらなくなったら消す
    public static String genre = "";
    public static double genre_sim = 0.0;
    public static ArrayList<String> genre_list = new ArrayList(); // ジャンルリスト
    public static ArrayList genre_sim_list = new ArrayList(); // ジャンルの類似度リスト
    public static ArrayList<SpotStructure> itineraryPlaceList=new ArrayList<SpotStructure>();
    public static String nowPrefecture=null;
    public static String input_text = null; // 自由テキスト入力文字列
    public static ArrayList<String> input_list = new ArrayList(); // 入力テキストリスト
    public static ArrayList<String> removeGenreList=new ArrayList<String>();
    public static boolean error_flag = false; // 入力エラーのフラグ
    public static JSONObject spots_json;
    public static JSONObject neighborDicObject;
    public static JSONObject pair_json;
    public static int perfect_match_num = -1; // 完全一致する観光地名の場所を保存するための変数（リストの要素番号）
    public static boolean neighborOrJapanFlg=false;
    public static Date departureTime=new Date();
    public static Date arriveTime=new Date();
    // デフォルトは-1であるため，0以上であれば完全一致した観光地があると判断できる
}


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        android.location.LocationListener,
        GoogleApiClient.OnConnectionFailedListener{
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;//開始時に自己位置を取得するため、googleapiを利用
    private Location location;//開始時、現在地の座標を保存する変数
    private JsonReader json;
    final int REQUEST_PERMISSION=1000;
    private HttpGetter httpGet;
    private HttpBitmapGetter httpBitmapGet;
    final Handler handler = new Handler(Looper.getMainLooper());
    ProgressDialog progressDialog;//読み込み中表示クラス
    private InputMethodManager inputMethodManager;//エンターを押したらキーボードが閉じるように、キーボード取得オブジェクト

    private static MainActivity instance = null;

    public static MainActivity getInstance() {
        return instance;//context取得用メソッド　このクラス外でも、MainActivity.getInstance()でcontextを取得できる
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.title_page);


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

        Value.departureTime.setHours(10);
        Value.departureTime.setMinutes(0);
        Value.departureTime.setSeconds(0);
        Value.arriveTime.setHours(22);
        Value.arriveTime.setMinutes(0);
        Value.arriveTime.setSeconds(0);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear().commit();







//        final int[] departureTime = {10,0};//(時間,分)の順に格納
//        final int[] arriveTime = {20,0};//(時間,分)の順に格納
        /*
        final Date departureTime=new Date();
        departureTime.setHours(10);
        departureTime.setMinutes(0);
        departureTime.setSeconds(0);
        final Date arriveTime=new Date();
        arriveTime.setHours(22);
        arriveTime.setMinutes(0);
        arriveTime.setSeconds(0);
        */
        json=new JsonReader();//Json読み込み用クラスのインスタンス
        httpGet=new HttpGetter();//Httpリクエスト送信用クラスのインスタンス
        progressDialog = new ProgressDialog(this);//読み込み中表示,// 初期設定
        progressDialog.setTitle("初期値");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);




//        Button shioriButton = (Button) findViewById(R.id.shioriButton);
        final ImageButton startButton = (ImageButton) findViewById(R.id.startButton);
        ImageButton customButton=(ImageButton)findViewById(R.id.imageButton);
//        final Button departureTimeButton=(Button)findViewById(R.id.departureTimeButton);
//        departureTimeButton.setText(String.format("%02d:%02d",Value.departureTime.getHours(),Value.departureTime.getMinutes()));
//        final Button arriveTimeButton=(Button)findViewById(R.id.arriveTimeButton);
//        arriveTimeButton.setText(String.format("%02d:%02d",Value.arriveTime.getHours(),Value.arriveTime.getMinutes()));
        final EditText editText = (EditText)findViewById(R.id.editText);        // EditTextオブジェクトを取得
//        final RadioGroup prefRadioGroup=(RadioGroup) findViewById(R.id.PrefRadioGroup);
//        prefRadioGroup.check(R.id.neighborRadio);//ラジオボタンを予めチェック

//        気になるジャンルを教えてね！\n動物が見たいなら「動物園」, 歴史に興味があるなら「神社」って入れてくれればOKよ！\n私が旅のしおりを作ってあげるわ！
        final FontFitTextView fukidashiText=(FontFitTextView) findViewById(R.id.textView21);
//        fukidashiText.setText("気になるジャンルを教えてね");

/*

        String modelText="気になるね！";
        int numberLine = 5;

        final float MIN_TEXT_SIZE = 15f;

        int viewHeight = fukidashiText.getHeight(); // Viewの縦幅
        int viewWidth = fukidashiText.getWidth(); // Viewの横幅

        Log.d("start main height",String.valueOf(viewHeight));
        // テキストサイズ
        float textSize = fukidashiText.getTextSize();

        // Paintにテキストサイズ設定
        Paint paint = new Paint();
        paint.setTextSize(textSize);

        // テキスト取得
        if (modelText == null){
            modelText = fukidashiText.getText().toString();
        }

        // テキストの縦幅取得
        Paint.FontMetrics fm = paint.getFontMetrics();
        float textHeight = (float) (Math.abs(fm.top)) + (Math.abs(fm.descent));

        // テキストの横幅取得
        float textWidth = paint.measureText(modelText);

        // 縦幅と、横幅が収まるまでループ
        while (viewHeight < textHeight | viewWidth < textWidth)
        {
            Log.d("test","loop"+String.valueOf(textWidth)+" "+String .valueOf(viewHeight));
            // 調整しているテキストサイズが、定義している最小サイズ以下か。
            if (MIN_TEXT_SIZE >= textSize)
            {
                // 最小サイズ以下になる場合は最小サイズ
                textSize = MIN_TEXT_SIZE;
                break;
            }

            // テキストサイズをデクリメント
            textSize--;

            // Paintにテキストサイズ設定
            paint.setTextSize(textSize);

            // テキストの縦幅を再取得
            // 改行を考慮する
            fm = paint.getFontMetrics();
            textHeight = (float) (Math.abs(fm.top)) + (Math.abs(fm.descent)*numberLine);

            // テキストの横幅を再取得
            textWidth = paint.measureText(modelText);
        }

        Log.d("textSize",String .valueOf(textSize));
        // テキストサイズ設定
        fukidashiText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);




        fukidashiText.setTextSize(TypedValue.COMPLEX_UNIT_PX,100);
        */






        //キーボード表示を制御するためのオブジェクト
        inputMethodManager =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        //EditTextにリスナーをセット
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override            //コールバックとしてonKey()メソッドを定義
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //イベントを取得するタイミングには、ボタンが押されてなおかつエンターキーだったときを指定
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    //キーボードを閉じる
                    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    startButton.performClick();
                    return true;
                }
                return false;
            }
        });
        customButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), MyPreferenceActivity.class);
                startActivity(intent);
            }
        });

        /*
        // ラジオグループのチェック状態が変更された時に呼び出されるコールバックリスナーを登録します
        prefRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = (RadioButton) findViewById(checkedId);
                if (radioButton.getText().equals("全国")) {
                    Value.neighborOrJapanFlg=true;
                } else if (radioButton.getText().equals("隣接県")) {
                    Value.neighborOrJapanFlg=false;
                } else {
                    Log.e("test","radioButonError");
                }
            }
        });
        */
/*
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
                TimePickerDialog dialog = new TimePickerDialog(MainActivity.getInstance(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Value.departureTime.setHours(hourOfDay);
                        Value.departureTime.setMinutes(minute);
                        departureTimeButton.setText(String.format("%02d:%02d", Value.departureTime.getHours(), Value.departureTime.getMinutes()));//時刻をボタンの文字にセット

                    }
                }, Value.departureTime.getHours(), Value.departureTime.getMinutes(), true);//初期値を入れる箇所
                dialog.show();
            }
        });
        */
/*
        //到着時刻選択ボタン
        arriveTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(MainActivity.getInstance(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Value.arriveTime.setHours(hourOfDay);
                        Value.arriveTime.setMinutes(minute);
                        arriveTimeButton.setText(String.format("%02d:%02d", Value.arriveTime.getHours(), Value.arriveTime.getMinutes()));//時刻をボタンの文字にセット

                    }
                }, Value.arriveTime.getHours(), Value.arriveTime.getMinutes(), true);//初期値を入れる箇所
                dialog.show();
            }
        });
*/

        //スレッド上でJsonの読み込みを開始する。ユーザーが入力を行っている間、並行して読み出しが出来る。
        final LoadJsonInThread loadJsonInThread =new LoadJsonInThread();
        loadJsonInThread.start();
        //スタートボタン
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //preferenceで設定されたデータを受け取る
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                //昼食が必要かどうかのフラグ
                final boolean lunchFlg = sharedPreferences.getBoolean("lunchFlg", true);


                Value.neighborOrJapanFlg = sharedPreferences.getBoolean("JapanOrNeighborFlg", false);

                final String edittext_preference = sharedPreferences.getString("departurePlace", "");
                Log.d("edit",edittext_preference);


                progressDialog.setTitle("しおり作成中…。");
                progressDialog.show();
                //◆スレッド処理開始
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        //Jsonの読み出しが終わっていなかったら、それをまつ。
                        try {
                            loadJsonInThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //******************//実機のgoogleplacesversionの問題で緯度経度が取れない場合は、岐阜の座標を代入する*****************************
//                        EditText nowPlaceEditText=(EditText) findViewById(R.id.nowPlaceEditText);
                        if(edittext_preference.equals("")){
                            if(location==null) {
                                Log.d("test","GPSが取れませんでした");
                                location = new Location("a");//文字列はprovider（適当に入れました)
                                location.setLatitude(35.4650334);
                                location.setLongitude(136.73929506);
                            }else {
                                Log.d("test", "GPSの値を使用します");
                            }
                        }else{
                            //日本語をURLに変換
                            String urlEncodeResult= null;
                            // AndroidのGPSが上手く取れない場合に出るエラーに対処するため
                            location = new Location("a");//文字列はprovider（適当に入れました)
                            try {
                                urlEncodeResult = URLEncoder.encode(edittext_preference ,"UTF-8");
                                JSONObject nowPlaceGeoCodingResult = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/geocode/json?address="+urlEncodeResult+"&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk&language=ja"));
                                Log.d("test",nowPlaceGeoCodingResult.toString());
                                if(nowPlaceGeoCodingResult.getJSONArray("results").getJSONObject(0).getString("formatted_address").substring(0,2).equals("日本")) {
                                    Log.d("test", "formatted address is" + nowPlaceGeoCodingResult.getJSONArray("results").getJSONObject(0).getString("formatted_address"));
                                    location.setLatitude(nowPlaceGeoCodingResult.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                                    location.setLongitude(nowPlaceGeoCodingResult.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                                }else{
                                    Log.d("test","ここは日本ではありません");
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }



                        Log.d("test", "startButton pusshed");

                        // 初期化処理
                        Value.error_flag = false; // 入力エラーフラグの初期化
                        Value.genre = "";       // ジャンルの初期化
                        Value.itineraryPlaceList = new ArrayList<SpotStructure>();
                        Value.genre_list=new ArrayList<String>();

                        // ====== 自由テキスト入力受け取り ======

                        // 入力された文字を取得
                        final String input_text = editText.getText().toString();
                        Value.input_text = input_text;
                        Log.d("inputtext", Value.input_text);



                        // 入力テキストが未入力であった場合
                        if (Value.input_text.length() == 0) {
                            Log.d("input error message", "何も入力されていません");
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    editText.setError("何も入力されていません");
                                }
                            });
                            Value.error_flag = true;
                        }

                        // 入力テキストが存在する場合，ジャンルとのマッチングを行う
                        else {
                            // 入力テキストを空白でパースする
                            Value.input_list = parseText(Value.input_text);
                            // ジャンルリストを作成する
                            for (int i = 0; i < Value.input_list.size(); i++) {

                                // ====== 自由テキストをジャンルに変換 ======
                                // 自由テキストが辞書に登録されている場合の処理
                                try {
                                    // 自由テキストに対応するジャンル名のリストを取得する
                                    JSONArray genre_sim_json_list = Value.pair_json.getJSONArray(Value.input_list.get(i));

                                    // ジャンルを決定する
                                    Value.genre = (String) genre_sim_json_list.get(0);
                                    Value.genre_sim = (double) genre_sim_json_list.get(1);
//                                    // ジャンルが複数ある場合はランダムで1つ選択する
//                                    else {
//                                        // 乱数を発生する
//                                        Random rand = new Random();
//                                        int rand_n = rand.nextInt(genre_list.length());
//                                        Value.genre = (String) genre_list.get(rand_n);
//                                        for(int j=0;j<genre_list.length();j++){
//                                            if(genre_list.get(j).equals(Value.input_list.get(i))){
//                                                Value.genre=(String)genre_list.get(j);
//                                            }
//                                        }
//                                    }
                                    // 出力確認
                                    Log.d("genre result", Value.genre + "," + String.valueOf(Value.genre_sim));
                                    // ジャンルリストにジャンルを格納する
                                    Value.genre_list.add(Value.genre);
                                    Value.genre_sim_list.add(Value.genre_sim);

                                } catch (JSONException e) {
                                    // 入力テキストが辞書に登録されていなかった場合
                                    e.printStackTrace();
                                    Log.d("input error message", Value.input_list.get(i) + " はアプリに登録されていません");
                                    final String error_word = Value.input_list.get(i);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            editText.setError(error_word + " はアプリに登録されていません");
                                        }
                                    });
                                    Value.error_flag = true;
                                }
                            }
                        }

                        if(Value.error_flag){
                            progressDialog.dismiss();//読み込み中表示、終了
                        }
                        // 入力エラーが発生していなければ処理を実行する
                        if (Value.error_flag == false) {


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


//****************************************開始地点を定義***************************************
                            if(edittext_preference.equals("")){
                                Value.itineraryPlaceList.add(new SpotStructure(null, "現在地", "", Value.nowPrefecture, 0, location.getLatitude(), location.getLongitude(), 0, null, null, Value.departureTime,null, null, null));
                            }else{
                                Value.itineraryPlaceList.add(new SpotStructure(null, edittext_preference, "", Value.nowPrefecture, 0, location.getLatitude(), location.getLongitude(), 0, null, null, Value.departureTime,null, null, null));
                            }
                            Log.d("出発地の時刻", Value.itineraryPlaceList.get(0).departTime.toString());

//********************レビュー順にソートし、一つ目の候補地を確定する************************
                            final ArrayList<SpotStructure> firstCandsList = new ArrayList<SpotStructure>();//ソート用リスト初期化
                            ArrayList<SpotStructure> firstCandsList_org = new ArrayList<SpotStructure>();//ソートしない用リスト初期化
                            double rate_double_mean = 0.0; // ratingの和（平均を取るための変数）
                            double match_spot_count = 0.0; // 隣接県でマッチしたスポット数（平均を取るための変数）
                            try {
                                int counter = 0; // 隣接県もしくは完全一致した場合のカウンタ
                                for (int i = 0; i < Value.spots_json.getJSONArray("spots").length(); i++) {
                                    String pref_str = Value.spots_json.getJSONArray("spots").getJSONObject(i).getString("prefectures");
                                    String genre_str = Value.spots_json.getJSONArray("spots").getJSONObject(i).getString("genreS");
                                    String name_str = Value.spots_json.getJSONArray("spots").getJSONObject(i).getString("name");
                                    //隣接県である，もしくは，入力テキストと観光地名が完全一致した場合，リストに格納する
                                    if (CheckNeighborPrefecture(pref_str, Value.neighborDicObject) || (Value.input_list.contains(name_str))) {

                                        // 入力テキストと観光地名が完全一致した場合
                                        if (Value.input_list.contains(name_str)) {
                                            Log.d("perfect match", name_str);
                                            Value.perfect_match_num = counter; // リストの要素番号を保存する
                                        }

                                        // String name_str = Value.spots_json.getJSONArray("spots").getJSONObject(i).getString("name");
                                        String placeID_str = Value.spots_json.getJSONArray("spots").getJSONObject(i).getString("place_id");
                                        String explainText = Value.spots_json.getJSONArray("spots").getJSONObject(i).getString("explain");
                                        double rate_double = Value.spots_json.getJSONArray("spots").getJSONObject(i).getDouble("rating");
                                        double lat_double = Value.spots_json.getJSONArray("spots").getJSONObject(i).getDouble("lat");
                                        double lng_double = Value.spots_json.getJSONArray("spots").getJSONObject(i).getDouble("lng");
                                        double name_length = Value.spots_json.getJSONArray("spots").getJSONObject(i).getDouble("name_length");
                                        double explain_length = Value.spots_json.getJSONArray("spots").getJSONObject(i).getDouble("explain_length");

                                        // ====== レビューの平均点を用いた評価値 ======
                                        rate_double = rate_double / 5.0; // 最大で1.0になるように正規化する

                                        // ====== ジャンルとの類似度を用いた評価値 ======
                                        double match_genre_double = 0.0;
                                        for (int j = 0; j < Value.genre_list.size(); j++) {
                                            if (genre_str.equals(Value.genre_list.get(j))) {
                                                match_genre_double += (double) Value.genre_sim_list.get(j);
                                            }
                                        }

                                        double match_name_double = 0.0;
                                        double match_explain_double = 0.0;
                                        for (int j = 0; j < Value.input_list.size(); j++) {
                                            // ====== 自由テキストと観光地名の正規化TFを用いた評価値 ======
                                            double match_name_count = (double) isCount(name_str, Value.input_list.get(j));
                                            match_name_count = match_name_count / name_length;
                                            match_name_double += match_name_count;

                                            // ====== 自由テキストと説明文のマッチング ======
                                            double match_explain_count = (double) isCount(explainText, Value.input_list.get(j));
                                            match_explain_count = match_explain_count / explain_length;
                                            match_explain_double += match_explain_count;
                                        }
//                                        Log.d("genreDouble",name_str+String.valueOf(match_genre_double));
//                                        Log.d("nameDouble",name_str+String.valueOf(match_name_double));
//                                        Log.d("explainDouble",name_str+String.valueOf(match_explain_double));
                                        // ratingの値を更新する
                                        rate_double += match_genre_double + match_name_double + match_explain_double;
                                        rate_double = rate_double * 10000;  // 10000倍する
                                        rate_double = Math.round(rate_double); // 小数点以下を切り捨てる
                                        rate_double = rate_double / 10000.0; // 10000で割る
                                        if (rate_double > 1.0) {
                                            Log.d("rate result", name_str + String.valueOf(rate_double));
                                        }

                                        // ratingの加算を行う
                                        rate_double_mean += rate_double;
                                        // スポットのマッチの加算を行う
                                        match_spot_count += 1.0;

                                        float[] distance = new float[3];//二点間の距離算出結果を格納する変数
                                        Location.distanceBetween(location.getLatitude(), location.getLongitude(), lat_double, lng_double, distance);//入力された場所と候補地との距離算出
                                        firstCandsList.add(new SpotStructure(placeID_str, name_str, genre_str, pref_str, rate_double, lat_double, lng_double, distance[0], explainText, null, null, null, null,null));
                                        firstCandsList_org.add(new SpotStructure(placeID_str, name_str, genre_str, pref_str, rate_double, lat_double, lng_double, distance[0], explainText, null, null, null, null,null));
                                        counter ++;
                                    }
                                }
                                rate_double_mean = rate_double_mean / match_spot_count; // ratingの平均を取る
                            } catch (JSONException e) {
                                Log.e("test", e.toString());
                            }
                            Log.d("test", "first candidate size is" + firstCandsList.size());

                            //Comparatorを用いレビューが高い順にソートする
                            Collections.sort(firstCandsList, new SpotStructureRateComparator());

                            boolean firstPlaceCorrectFlg = false;
                            int minDistanceNumber = 0;

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(firstCandsList.get(0).rate<1.5){
                                            progressDialog.setTitle("期待できないかも…。");
                                        }else if(firstCandsList.get(0).rate<2.5){
                                            progressDialog.setTitle("もうちょっとまってね！");
                                        }else{
                                            progressDialog.setTitle("期待しててね！");
                                        }
                                    }
                                });


                            while (!firstPlaceCorrectFlg) {

                                // 完全一致する観光地が存在する場合，第一候補を強制的に決定する
                                if (Value.perfect_match_num != -1) {
                                    Value.itineraryPlaceList.add(firstCandsList_org.get(Value.perfect_match_num));
                                }


                                // 完全一致する観光地が存在しない場合，レビューが高い順にソートして決定する
                                else {
                                    //レビュー4.5以上の候補地から一番近い場所を選ぶ
                                    minDistanceNumber = 0;
                                    double minDistance = Double.MAX_VALUE;
                                    double center_val = (firstCandsList.get(0).rate + rate_double_mean) / 2;
                                    for (int i = 0; firstCandsList.get(i).rate> firstCandsList.get(0).rate - 0.05 ; i++) {
                                        Log.d("result", firstCandsList.get(i).rate + String.valueOf(firstCandsList.get(i).name));
                                        if (firstCandsList.get(i).distance < minDistance) {
                                            minDistance = firstCandsList.get(i).distance;
                                            minDistanceNumber = i;
                                        }
                                    }
                                    //一番初めに訪れる観光地決定、旅程リストに追加
                                    Value.itineraryPlaceList.add(firstCandsList.get(minDistanceNumber));
                                    //Value.itineraryPlaceList.add(firstCandsList.get(0));
                                }

                                //directionAPIに出発地と第一観光地を渡し、どれくらい時間がかかるかを求める。
                                try {
                                    JSONObject tempDirectionSearch = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/directions/json?origin=" + location.getLatitude() + "," + location.getLongitude() + "&destination=place_id:" + Value.itineraryPlaceList.get(1).placeID + "&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                    //かかる時間（秒）
                                    int tempSecondToDestination = tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");
                                    //カレンダー型を用いることで時間の加算を行う
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(Value.itineraryPlaceList.get(0).departTime);
                                    calendar.add(Calendar.SECOND, tempSecondToDestination);
                                    Date tempArriveTime=calendar.getTime();
                                    //かかる時間＋観光地で観光に使う時間を求め、代入
                                    Date tempDepartTime = addGenreWaitTime(calendar.getTime(), Value.itineraryPlaceList.get(1).genre);
                                    //ポリライン（あとで地図に表示をする、経由道路の道情報）を代入
                                    String tempPolyline = tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                                    //得た情報をセットする
                                    Value.itineraryPlaceList.set(1, new SpotStructure(Value.itineraryPlaceList.get(1).placeID,
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
                                            tempArriveTime,
                                            Value.itineraryPlaceList.get(1).mapImage,
                                            tempPolyline));
                                    firstPlaceCorrectFlg = true;
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    Value.itineraryPlaceList.remove(1);
                                    firstCandsList.remove(minDistanceNumber);
                                    e.printStackTrace();
                                }
                            }
                            Log.d("test", "first:" + Value.itineraryPlaceList.get(1).name + "dist:" + String.valueOf(Value.itineraryPlaceList.get(1).distance) + "id" + String.valueOf(Value.itineraryPlaceList.get(1).placeID) + "depTime" + Value.itineraryPlaceList.get(1).departTime.toString());
                            Value.perfect_match_num = -1; // 初期化
                            Value.removeGenreList.add(Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).genre);

//******************************昼食の場所が一つ目の観光地のあとと仮定して、その場所付近の昼食場所をgoogle neabysearchで検索する**********************
// 昼食のタイミングが固定なのは非常にまずい。
                            // 第一候補が食べ物関係でない場合かつ設定画面でランチを必要としている場合のみ実行する
                            if (Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).genre.equals("郷土料理店") == false && Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).genre.equals("その他（食べる）") == false && lunchFlg) {
                                JSONObject nearbySearchResult = null;
                                ArrayList<SpotStructure> lunchCandsList = new ArrayList<SpotStructure>();//ソート用リスト初期化

                                try {//googleにリクエストを送信し、1番目の観光地付近にあるレストランを全てリストに入れる
                                    nearbySearchResult = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + Double.toString(Value.itineraryPlaceList.get(1).lat) + "," + Double.toString(Value.itineraryPlaceList.get(1).lng) + "&radius=5000&type=restaurant&language=ja&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                    int candLength = nearbySearchResult.getJSONArray("results").length();
                                    if (candLength == 0) {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.getInstance(), "候補地付近にレストランがありません。", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                    for (int i = 0; i < candLength; i++) {
                                        String pref_str = "";
                                        String genre_str = "レストラン";
                                        String name_str = nearbySearchResult.getJSONArray("results").getJSONObject(i).getString("name");
                                        String placeID_str = nearbySearchResult.getJSONArray("results").getJSONObject(i).getString("place_id");
                                        String explainText = "";
                                        double rate_double = 0;
                                        double lat_double = nearbySearchResult.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                                        double lng_double = nearbySearchResult.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                                        float[] distance = new float[3];//二点間の距離算出結果を格納する変数
                                        Location.distanceBetween(Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).lat, Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).lng, lat_double, lng_double, distance);//入力された場所と候補地との距離算出
                                        lunchCandsList.add(new SpotStructure(placeID_str, name_str, genre_str, pref_str, rate_double, lat_double, lng_double, distance[0], explainText, null, null, null, null,null));
                                    }
                                } catch (JSONException e) {
                                    Log.e("test", e.toString());
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
                                //レストランの入ったリストを距離順にソート
                                Collections.sort(lunchCandsList, new SpotStructureDistanceComparator());

                                boolean lunchPlaceCorrectFlg = false;
                                if(lunchCandsList.isEmpty()){
                                    lunchPlaceCorrectFlg=true;
                                }
                                while (!lunchPlaceCorrectFlg) {
                                    //最も距離の近いレストランの場所をitineraryPlaceListに代入
                                    Value.itineraryPlaceList.add(lunchCandsList.get(0));
                                    try {//第一観光地からレストランまでに掛かる時間を求める
                                        JSONObject tempDirectionSearch = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/directions/json?origin=place_id:" + Value.itineraryPlaceList.get(1).placeID + "&destination=place_id:" + Value.itineraryPlaceList.get(2).placeID + "&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                        Log.d("test", tempDirectionSearch.toString());
                                        int tempSecondToDestination = tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-2).departTime);
                                        calendar.add(Calendar.SECOND, tempSecondToDestination);
                                        Date tempArriveTime=calendar.getTime();
                                        Date tempDepartTime = addGenreWaitTime(calendar.getTime(), Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).genre);
                                        String tempPolyline = tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                                        Value.itineraryPlaceList.set(Value.itineraryPlaceList.size()-1, new SpotStructure(
                                                Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).placeID,
                                                Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).name,
                                                Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).genre,
                                                Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).prefecture,
                                                Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).rate,
                                                Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).lat,
                                                Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).lng,
                                                Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).distance,
                                                Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).explainText,
                                                Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).image,
                                                tempDepartTime,
                                                tempArriveTime,
                                                Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).mapImage,
                                                tempPolyline));
                                        lunchPlaceCorrectFlg = true;
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        Value.itineraryPlaceList.remove(Value.itineraryPlaceList.size()-1);
                                        lunchCandsList.remove(0);
                                        e.printStackTrace();
                                    }
                                }

//                                Log.d("test", "lunch:" + Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).name + "dist:" + String.valueOf(Value.itineraryPlaceList.get(2).distance) + "id" + String.valueOf(Value.itineraryPlaceList.get(2).placeID) + "depTime" + Value.itineraryPlaceList.get(2).departTime.toString());
                                Log.d("test2", Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).departTime.toString());
                            }

//******************二箇所目以降の候補地を確定させる*************************
                            ArrayList<SpotStructure> secondOrLaterCandsList = new ArrayList<SpotStructure>();//リスト初期化
                            try {
                                //第一観光地からみて、出発地とは反対方向にある観光地を探す。
                                boolean southFromFirstPlace = false;
                                boolean eastFromFirstPlace = false;
                                if (Value.itineraryPlaceList.get(0).lat < Value.itineraryPlaceList.get(1).lat) {
                                    southFromFirstPlace = false;
                                } else {
                                    southFromFirstPlace = true;
                                }
                                if (Value.itineraryPlaceList.get(0).lng < Value.itineraryPlaceList.get(1).lng) {
                                    eastFromFirstPlace = true;
                                } else {
                                    eastFromFirstPlace = false;
                                }
                                for (int i = 0; i < Value.spots_json.getJSONArray("spots").length(); i++) {
                                    String pref_str = Value.spots_json.getJSONArray("spots").getJSONObject(i).getString("prefectures");
                                    double lat_double = Value.spots_json.getJSONArray("spots").getJSONObject(i).getDouble("lat");
                                    double lng_double = Value.spots_json.getJSONArray("spots").getJSONObject(i).getDouble("lng");
                                    float[] distance = new float[3];//二点間の距離算出結果を格納する変数
                                    Location.distanceBetween(Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).lat, Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).lng, lat_double, lng_double, distance);//入力された場所と候補地との距離算出
                                    boolean tempThresholdFlg = false;
                                    //隣接県かつ近すぎない候補地をリストに代入
                                    //怪しい、地図のアップデートが必要
                                    if (CheckNeighborPrefecture(pref_str, Value.neighborDicObject) && distance[0] > 500) {
                                        if (southFromFirstPlace == true && eastFromFirstPlace == true) {
                                            //出発地の方向にある観光地はリストに入れない
                                            if (lat_double < Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).lat && lng_double > Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).lng) {
                                                tempThresholdFlg = true;
                                            }
                                        } else if (southFromFirstPlace == true && eastFromFirstPlace == false) {
                                            if (lat_double < Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).lat && lng_double < Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).lng) {
                                                tempThresholdFlg = true;
                                            }
                                        } else if (southFromFirstPlace == false && eastFromFirstPlace == true) {
                                            if (lat_double > Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).lat && lng_double < Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).lng) {
                                                tempThresholdFlg = true;
                                            }
                                        } else {
                                            if (lat_double > Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).lat && lng_double > Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).lng) {
                                                tempThresholdFlg = true;
                                            }
                                        }
                                    }
                                    //条件を満たした観光地のみをリストに入れる
                                    if (tempThresholdFlg) {
                                        String genre_str = Value.spots_json.getJSONArray("spots").getJSONObject(i).getString("genreS");
                                        String name_str = Value.spots_json.getJSONArray("spots").getJSONObject(i).getString("name");
                                        String placeID_str = Value.spots_json.getJSONArray("spots").getJSONObject(i).getString("place_id");
                                        double rate_double = Value.spots_json.getJSONArray("spots").getJSONObject(i).getDouble("rating");
                                        String explainText = Value.spots_json.getJSONArray("spots").getJSONObject(i).getString("explain");
                                        secondOrLaterCandsList.add(new SpotStructure(placeID_str, name_str, genre_str, pref_str, rate_double, lat_double, lng_double, distance[0], explainText, null, null,null, null, null));
                                    }
                                }
                            } catch (JSONException e) {
                            }
                            //Comparatorを用い距離が短い順にソートする
                            Collections.sort(secondOrLaterCandsList, new SpotStructureDistanceComparator());
                            // Log.d("test3", Value.itineraryPlaceList.get(2).departTime.toString());

                            //一箇所目が遠すぎて昼を食べずに帰る時の処理
                            boolean getBackHomeFlg = false;//次の観光地を探索するか決めるフラグ
                            Date ifReturnArriveTime = null;//現時点の観光地から家に帰ったとして、家に着く時間を格納する
                            String ifReturnPolyline = null;//現時点の観光地から家に帰った場合のポリライン
                                try {
                                    JSONObject tempDirectionSearchToHome = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/directions/json?origin=place_id:" + Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).placeID + "&destination=" + location.getLatitude() + "," + location.getLongitude() + "&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                    int tempSecondToDestinationToHome = tempDirectionSearchToHome.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");
                                    Calendar calendarToHome = Calendar.getInstance();
                                    calendarToHome.setTime(Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).departTime);
                                    calendarToHome.add(Calendar.SECOND, tempSecondToDestinationToHome);
                                    //この観光地を見た後家に帰ったとして、家に着く時間
                                    Date tempReturnHomeTime = calendarToHome.getTime();
                                    Log.d("test:tempReturnHomeTime", tempReturnHomeTime.toString());
                                    //到着時間と比較
                                    if (Value.arriveTime.compareTo(tempReturnHomeTime) == -1) {//家にかえる場合、直前の観光地を消す。ここのifがtrueになるのは、第一候補地が近すぎて昼にすら行けなかった場合。
                                        getBackHomeFlg = true;
                                        if (Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).genre.equals("郷土料理店") == false && Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).genre.equals("その他（食べる）") == false && lunchFlg) {
                                            //昼食場所を消去
                                            Value.itineraryPlaceList.remove(Value.itineraryPlaceList.size() - 1);
                                        }
                                        //リクエストをスローし、第一観光地からまっすぐ家にかえる場合を算出する。
                                        Log.d("test", Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).placeID);
                                        Log.d("test", location.toString());
                                        tempDirectionSearchToHome = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/directions/json?origin=place_id:" + Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).placeID + "&destination=" + location.getLatitude() + "," + location.getLongitude() + "&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                        tempSecondToDestinationToHome = tempDirectionSearchToHome.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");
                                        calendarToHome = Calendar.getInstance();
                                        calendarToHome.setTime(Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).departTime);
                                        calendarToHome.add(Calendar.SECOND, tempSecondToDestinationToHome);
                                        //第一観光地から直帰する場合にかかる時間を記述
                                        ifReturnArriveTime = calendarToHome.getTime();
                                        ifReturnPolyline = tempDirectionSearchToHome.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                                    } else {
                                        ifReturnArriveTime = tempReturnHomeTime;//取得した、家に帰る場合の到着時刻とポリラインを保持。ここで最後の観光地になった時にこの値を使う。
                                        ifReturnPolyline = tempDirectionSearchToHome.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.getInstance(), "帰り道がありません", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }

//                            Log.d("test4",Value.itineraryPlaceList.get(2).departTime.toString());


                            //ifReturnArriveTImeが到着予定時間を越すまで、観光地検索を繰り返す
                            while (!getBackHomeFlg) {
                                //ジャンル被りを防ぐため、これまでに登場したジャンルの観光地はリストから消去する
                                ListIterator<SpotStructure> i = secondOrLaterCandsList.listIterator();//ループをまわしながらarraylistの中身を削除する場合はこれを使う必要がある
                                while(i.hasNext()) {
                                    SpotStructure tempSpotStructure=i.next();//次のリストを参照
                                    if(Value.removeGenreList.contains(tempSpotStructure.genre)){
                                        i.remove();
                                    }
                                }
                                Collections.sort(secondOrLaterCandsList, new SpotStructureDistanceComparator());
                                Value.itineraryPlaceList.add(secondOrLaterCandsList.get(0));
                                secondOrLaterCandsList.remove(0);
                                try {
                                    int focusPlaceNum = Value.itineraryPlaceList.size() - 1;
                                    int beforePlaceNum = Value.itineraryPlaceList.size() - 2;
                                    JSONObject tempDirectionSearch = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/directions/json?origin=place_id:" + Value.itineraryPlaceList.get(beforePlaceNum).placeID + "&destination=place_id:" + Value.itineraryPlaceList.get(focusPlaceNum).placeID + "&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                    Log.d("test", tempDirectionSearch.toString());
                                    int tempSecondToDestination = tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");
                                    Calendar calendar = Calendar.getInstance();
                                    Log.d("calender1", calendar.toString());
                                    Log.d("iti Num is", String.valueOf(focusPlaceNum));
                                    Log.d("test", Value.itineraryPlaceList.get(beforePlaceNum).name);
                                    calendar.setTime(Value.itineraryPlaceList.get(beforePlaceNum).departTime);
                                    calendar.add(Calendar.SECOND, tempSecondToDestination);
                                    Date tempArriveTime=calendar.getTime();
                                    Date tempDepartTime = addGenreWaitTime(calendar.getTime(), Value.itineraryPlaceList.get(focusPlaceNum).genre);
                                    Log.d("genre", Value.itineraryPlaceList.get(focusPlaceNum).genre);
                                    Log.d("tempDepartTime", tempDepartTime.toString());
                                    // Log.d("test5", Value.itineraryPlaceList.get(2).departTime.toString());

                                    String tempPolyline = tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                                    Value.itineraryPlaceList.set(focusPlaceNum, new SpotStructure(
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
                                            tempArriveTime,
                                            Value.itineraryPlaceList.get(focusPlaceNum).mapImage,
                                            tempPolyline));
                                    Log.d("test", String.valueOf(focusPlaceNum) + ":" + Value.itineraryPlaceList.get(focusPlaceNum).name);
                                    Log.d("dist:", String.valueOf(Value.itineraryPlaceList.get(focusPlaceNum).distance));
                                    Log.d("id", String.valueOf(Value.itineraryPlaceList.get(focusPlaceNum).placeID));
                                    Log.d("depTime", Value.itineraryPlaceList.get(focusPlaceNum).departTime.toString());
                                    //ジャンル被りを防ぐ為のリストに、先程決まったジャンルを入れる
                                    Value.removeGenreList.add(Value.itineraryPlaceList.get(Value.itineraryPlaceList.size()-1).genre);
                                    try {
                                        JSONObject tempDirectionSearchToHome = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/directions/json?origin=place_id:" + Value.itineraryPlaceList.get(focusPlaceNum).placeID + "&destination=" + location.getLatitude() + "," + location.getLongitude() + "&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                        int tempSecondToDestinationToHome = tempDirectionSearchToHome.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");
                                        Calendar calendarToHome = Calendar.getInstance();
                                        calendarToHome.setTime(Value.itineraryPlaceList.get(focusPlaceNum).departTime);
                                        calendarToHome.add(Calendar.SECOND, tempSecondToDestinationToHome);
                                        Date tempReturnHomeTime = calendarToHome.getTime();
                                        if (Value.arriveTime.compareTo(tempReturnHomeTime) == -1) {
                                            getBackHomeFlg = true;
                                            Value.itineraryPlaceList.remove(Value.itineraryPlaceList.size() - 1);
                                            Value.removeGenreList.remove(Value.removeGenreList.size()-1);
                                        } else {
                                            ifReturnArriveTime = tempReturnHomeTime;
                                            ifReturnPolyline = tempDirectionSearchToHome.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    //JSONExceptionを吐いた場合、選択された候補地は必要な情報が足りず不適切なため削除し、次の候補地を使う。
                                    Value.itineraryPlaceList.remove(Value.itineraryPlaceList.size() - 1);
                                    e.printStackTrace();
                                }
                            }

//*************************************到着地を旅程リストに代入する***********************************
                            if(edittext_preference.equals("")) {
                                Value.itineraryPlaceList.add(new SpotStructure(null, "現在地", "", Value.nowPrefecture, 0, location.getLatitude(), location.getLongitude(), 0, null, null, null, ifReturnArriveTime, null, ifReturnPolyline));
                            }else{
                                Value.itineraryPlaceList.add(new SpotStructure(null, edittext_preference, "", Value.nowPrefecture, 0, location.getLatitude(), location.getLongitude(), 0, null, null, null, ifReturnArriveTime, null, ifReturnPolyline));
                            }
                            // Log.d("test5.5", Value.itineraryPlaceList.get(2).name);
                            // Log.d("test6", Value.itineraryPlaceList.get(2).departTime.toString());
                            for (int i = 0; i < Value.itineraryPlaceList.size(); i++) {
                                if(Value.itineraryPlaceList.get(i).departTime!=null) {
                                    Log.d("test", "num:" + String.valueOf(i) + "name:" + Value.itineraryPlaceList.get(i).name + "rate" + String.valueOf(Value.itineraryPlaceList.get(i).rate) + "depTime" + Value.itineraryPlaceList.get(i).departTime.toString());
                                }
                            }

//**********************************しおりに載せる観光地は全て決定されたため、各観光地に対してDetail検索を行い写真とレストランのレート、地図を取得する
                            for (int i = 1; i < Value.itineraryPlaceList.size()-1; i++) {
                                Log.d("itiSize is",String.valueOf(Value.itineraryPlaceList.size()));
                                Log.d("executing detail search", "i is"+String.valueOf(i)+" "+ Value.itineraryPlaceList.get(i).name);
                                try {
                                    Bitmap mapsStaticsResult = null;
                                    if (i != 0) {
                                        Log.d("test", Value.itineraryPlaceList.get(i).polyline);
                                        //ポリラインをhttpリクエストとして渡し、地図情報を得る
                                        mapsStaticsResult = httpBitmapGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/staticmap?size=400x400&path=color:0xff0000ff|weight:5%7Cenc:" + Value.itineraryPlaceList.get(i).polyline + "&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                    }
                                    JSONObject detailSearchResult = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + Value.itineraryPlaceList.get(i).placeID + "&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk&language=ja"));
                                    double rate_double = 0;
                                    String tempExplainMessage = Value.itineraryPlaceList.get(i).explainText;
                                    //レストランの評価値がなかったり、第一候補地でレビュー値を変更しているのでここで読み込む
                                    if (!detailSearchResult.isNull("result")) {
                                        if (!detailSearchResult.getJSONObject("result").isNull("rating")) {
                                            rate_double = detailSearchResult.getJSONObject("result").getDouble("rating");
                                        }
                                        if (!detailSearchResult.getJSONObject("result").isNull("formatted_address")) {
                                            tempExplainMessage = tempExplainMessage + "\n\n住所：" + detailSearchResult.getJSONObject("result").getString("formatted_address").substring(3+9,detailSearchResult.getJSONObject("result").getString("formatted_address").length()) + "\n";
                                        }
                                        if (!detailSearchResult.getJSONObject("result").isNull("formatted_phone_number")) {
                                            tempExplainMessage = tempExplainMessage + "電話番号：" + detailSearchResult.getJSONObject("result").getString("formatted_phone_number") + "\n";
                                        }
                                        if (!detailSearchResult.getJSONObject("result").isNull("website")) {
                                            tempExplainMessage = tempExplainMessage + "URL：" + detailSearchResult.getJSONObject("result").getString("website") + "\n";
                                        }

                                    }
                                    Bitmap img_bitmap = null;
                                    if (detailSearchResult.getJSONObject("result").isNull("photos")) {
                                        Log.d("test", String.valueOf(i) + " is not have photo");
                                        /*画像がない場合は県の画像を代わりに置く
                                        Resources r=getResources();
                                        if(prefStringToId(detailSearchResult.getJSONObject("result").getString("formatted_address"))!=0){
                                            img_bitmap= BitmapFactory.decodeResource(r,prefStringToId(detailSearchResult.getJSONObject("result").getString("formatted_address")));
                                        }
                                        */
                                    } else {
                                        //観光地の写真が存在する場合は、ここでリクエストを送りBitmapを得る
                                        img_bitmap = httpBitmapGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + detailSearchResult.getJSONObject("result").getJSONArray("photos").getJSONObject(0).getString("photo_reference") + "&key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                    }
                                    Value.itineraryPlaceList.set(i, new SpotStructure(
                                            Value.itineraryPlaceList.get(i).placeID,
                                            Value.itineraryPlaceList.get(i).name,
                                            Value.itineraryPlaceList.get(i).genre,
                                            addressToPrefecture(detailSearchResult.getJSONObject("result").getString("formatted_address")),                                            rate_double,
                                            Value.itineraryPlaceList.get(i).lat,
                                            Value.itineraryPlaceList.get(i).lng,
                                            Value.itineraryPlaceList.get(i).distance,
                                            tempExplainMessage,
                                            img_bitmap,
                                            Value.itineraryPlaceList.get(i).departTime,
                                            Value.itineraryPlaceList.get(i).arriveTime,
                                            mapsStaticsResult,
                                            Value.itineraryPlaceList.get(i).polyline
                                    ));
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            /*
//*****************************旅程全体の地図取得******************************
                            String waypoints = "waypoints=";
                            for (int i = 1; i < Value.itineraryPlaceList.size() - 2; i++) {
                                waypoints = waypoints + "place_id:" + Value.itineraryPlaceList.get(i).placeID + "|";
                            }
                            waypoints = waypoints.substring(0, waypoints.length() - 1);

                            try {
                                JSONObject tempDirectionSearch = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/directions/json?" +
                                        "origin=" + location.getLatitude() + "," + location.getLongitude() + "&" +
                                        "destination=place_id:" + Value.itineraryPlaceList.get(1).placeID + "&" +
                                        waypoints + "&" +
                                        "key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                Log.d("test", tempDirectionSearch.toString());
                                String tempPolyline = tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");

                                Bitmap mapsStaticsResult = httpBitmapGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/staticmap?" +
                                        "size=400x400&" +
                                        "path=color:0xff0000ff|weight:5%7Cenc:" + tempPolyline + "&" +
                                        "key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                Value.itineraryPlaceList.set(0, new SpotStructure(
                                        Value.itineraryPlaceList.get(0).placeID,
                                        Value.itineraryPlaceList.get(0).name,
                                        Value.itineraryPlaceList.get(0).genre,
                                        Value.itineraryPlaceList.get(0).prefecture,
                                        Value.itineraryPlaceList.get(0).rate,
                                        Value.itineraryPlaceList.get(0).lat,
                                        Value.itineraryPlaceList.get(0).lng,
                                        Value.itineraryPlaceList.get(0).distance,
                                        Value.itineraryPlaceList.get(0).explainText,
                                        Value.itineraryPlaceList.get(0).image,
                                        Value.itineraryPlaceList.get(0).departTime,
                                        Value.itineraryPlaceList.get(0).arriveTime,
                                        mapsStaticsResult,
                                        Value.itineraryPlaceList.get(0).polyline
                                ));
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
*/

//*****************************現在地→第一観光地の地図取得******************************
                            /*
                            String waypoints =null;
                            String markers=null;
                            markers="";
                            for (int i = 1; i < 2; i++) {
                                markers=markers+"markers=color:purple|label:"+String.valueOf(i)+"|"+String.valueOf(Value.itineraryPlaceList.get(i).lat)+","+String.valueOf(Value.itineraryPlaceList.get(i).lng)+"&";
                            }

                            try {
                                JSONObject tempDirectionSearch = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/directions/json?" +
                                        "origin=place_id:" + Value.itineraryPlaceList.get(0).placeID + "&" +
                                        "destination=place_id:" + Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 2).placeID + "&" +
                                        waypoints + "&" +
                                        "key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                Log.d("test", tempDirectionSearch.toString());
                                String tempPolyline = tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");


                                Bitmap mapsStaticsResult = httpBitmapGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/staticmap?" +
                                        "size=400x400&" +markers+
                                        "path=color:0xff0000ff|weight:5%7Cenc:" + tempPolyline + "&" +
                                        "key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                Value.itineraryPlaceList.set(Value.itineraryPlaceList.size() - 1, new SpotStructure(
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).placeID,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).name,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).genre,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).prefecture,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).rate,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).lat,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).lng,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).distance,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).explainText,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).image,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).departTime,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).arriveTime,
                                        mapsStaticsResult,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).polyline
                                ));
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
/*

//*****************************目的地周辺の地図取得******************************
                            /*
                            waypoints = "waypoints=";
                            for (int i = 2; i < Value.itineraryPlaceList.size() - 3; i++) {
                                waypoints = waypoints + "place_id:" + Value.itineraryPlaceList.get(i).placeID + "|";
                            }
                            waypoints = waypoints.substring(0, waypoints.length() - 1);

                            markers="";
                            for (int i = 1; i < Value.itineraryPlaceList.size() - 1; i++) {
                                markers=markers+"markers=color:purple|label:"+String.valueOf(i)+"|"+String.valueOf(Value.itineraryPlaceList.get(i).lat)+","+String.valueOf(Value.itineraryPlaceList.get(i).lng)+"&";
                            }

                            try {
                                JSONObject tempDirectionSearch = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/directions/json?" +
                                        "origin=place_id:" + Value.itineraryPlaceList.get(1).placeID + "&" +
                                        "destination=place_id:" + Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 2).placeID + "&" +
                                        waypoints + "&" +
                                        "key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                Log.d("test", tempDirectionSearch.toString());
                                String tempPolyline = tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");


                                Bitmap mapsStaticsResult = httpBitmapGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/staticmap?" +
                                        "size=400x400&" +markers+
                                        "path=color:0xff0000ff|weight:5%7Cenc:" + tempPolyline + "&" +
                                        "key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
                                        */

                            try {
                                Value.itineraryPlaceList.set(0, new SpotStructure(
                                        Value.itineraryPlaceList.get(0).placeID,
                                        Value.itineraryPlaceList.get(0).name,
                                        Value.itineraryPlaceList.get(0).genre,
                                        Value.itineraryPlaceList.get(0).prefecture,
                                        Value.itineraryPlaceList.get(0).rate,
                                        Value.itineraryPlaceList.get(0).lat,
                                        Value.itineraryPlaceList.get(0).lng,
                                        Value.itineraryPlaceList.get(0).distance,
                                        Value.itineraryPlaceList.get(0).explainText,
                                        Value.itineraryPlaceList.get(0).image,
                                        Value.itineraryPlaceList.get(0).departTime,
                                        Value.itineraryPlaceList.get(0).arriveTime,
                                        staticsMapMaker(0,1,true),
                                        Value.itineraryPlaceList.get(0).polyline
                                ));
                                //最終観光地→現在地
                                Value.itineraryPlaceList.set(Value.itineraryPlaceList.size() - 2, new SpotStructure(
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 2).placeID,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 2).name,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 2).genre,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 2).prefecture,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 2).rate,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 2).lat,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 2).lng,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 2).distance,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 2).explainText,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 2).image,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 2).departTime,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 2).arriveTime,
                                        staticsMapMaker(Value.itineraryPlaceList.size()-2,Value.itineraryPlaceList.size()-1,true),
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 2).polyline
                                ));
                                //観光地周辺
                                Value.itineraryPlaceList.set(Value.itineraryPlaceList.size() - 1, new SpotStructure(
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).placeID,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).name,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).genre,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).prefecture,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).rate,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).lat,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).lng,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).distance,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).explainText,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).image,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).departTime,
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).arriveTime,
                                        staticsMapMaker(1,Value.itineraryPlaceList.size()-2,false),
                                        Value.itineraryPlaceList.get(Value.itineraryPlaceList.size() - 1).polyline
                                ));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("test",e.toString());
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                                Log.e("test",e.toString());
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
                        } // 入力エラーのif文のカッコ
                    }
                }).start();


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
//            Toast.makeText(MainActivity.getInstance(), "位置情報を取得する権限がありません。設定→アプリ→Shiorichanより設定を変更して下さい", Toast.LENGTH_LONG).show();
            Log.d("test","perm out");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);

            } else {
                Toast toast = Toast.makeText(this, "許可されないとアプリが実行できません", Toast.LENGTH_SHORT);
                toast.show();

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, REQUEST_PERMISSION);

            }
            return;
        }

        Log.d("test","onconnected!");
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);//端末より、最後にGPSで取得された座標を取得
        if(location!=null){
            Log.d("test","location is not null,"+location.toString());
        }else{
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(3000);//位置更新を受け取る間隔(msec)　3秒おき
            mLocationRequest.setFastestInterval(1000);//速くて１秒おき
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//正確さ優先
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d("test","locationChanged!!!");
                    mGoogleApiClient.disconnect();
                }
            });
            Log.d("test","location is null...");

        }
    }
    // パーミッションの許可要請ダイアログ、結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.d("test","accept permission!");
                location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);//端末より、最後にGPSで取得された座標を取得
                if(location!=null){
                    Log.d("test","location is not null,"+location.toString());
                }else{
                    mLocationRequest = new LocationRequest();
                    mLocationRequest.setInterval(3000);//位置更新を受け取る間隔(msec)　3秒おき
                    mLocationRequest.setFastestInterval(1000);//速くて１秒おき
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//正確さ優先
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.d("test","locationChanged!!!");
                            mGoogleApiClient.disconnect();
                        }
                    });
                    Log.d("test","location is null...");

                }

                return;

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this, "これ以上なにもできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
    //gps取得用クラスの子メソッド、消せない
    @Override
    public void onConnectionSuspended(int i) {
        Log.d("test","onconnectSuspended");
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("test","connectFail"+connectionResult.toString());
        if(-1!=connectionResult.toString().indexOf("SERVICE_VERSION_UPDATE_REQUIRED")){
            Toast.makeText(MainActivity.getInstance(), "GooglePlayServiceのバージョンが古いためGPSの値が取得できません。", Toast.LENGTH_LONG).show();
        }

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
        if(Value.neighborOrJapanFlg){
            return true;
        }
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

    /*
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
        }else if(genre.equals("その他")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }
        Log.e("test","category to spending time error!");
        return null;
    }
    */

    public Date addGenreWaitTime(Date date,String genre){
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        if(genre.equals("レストラン")){
            calendar.add(Calendar.HOUR,1);
            return calendar.getTime();
        }
        if(genre.equals("山岳")) {
            calendar.add(Calendar.HOUR,3);
            return calendar.getTime();
        }else if(genre.equals("高原")) {
            calendar.add(Calendar.HOUR, 3);
            return calendar.getTime();
        }else if(genre.equals("湖沼")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("河川景観")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("海岸景観")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("海中公園")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("その他（特殊地形）")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("自然現象")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("町並み")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("郷土景観")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("展望施設")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("公園")) {
            calendar.add(Calendar.HOUR, 1);
            return calendar.getTime();
        }else if(genre.equals("庭園")) {
            calendar.add(Calendar.HOUR, 1);
            return calendar.getTime();
        }else if(genre.equals("動物")) {
            calendar.add(Calendar.HOUR, 1);
            return calendar.getTime();
        }else if(genre.equals("植物")) {
            calendar.add(Calendar.HOUR, 1);
            return calendar.getTime();
        }else if(genre.equals("城郭")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("旧街道")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("史跡")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("歴史的建造物")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("近代的建造物")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("博物館")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("美術館")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("動・植物園")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("水族館")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("産業観光施設")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("道の駅（見る）")) {
            calendar.add(Calendar.HOUR, 1);
            return calendar.getTime();
        }else if(genre.equals("神社・仏閣等")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("地域風俗")) {
            calendar.add(Calendar.HOUR, 1);
            return calendar.getTime();
        }else if(genre.equals("アニメ・音楽舞台")) {
            calendar.add(Calendar.HOUR, 1);
            return calendar.getTime();
        }else if(genre.equals("映画・ドラマロケ他")) {
            calendar.add(Calendar.HOUR, 1);
            return calendar.getTime();
        }else if(genre.equals("その他（名所）")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("センター施設")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("道の駅（遊ぶ）")) {
            calendar.add(Calendar.HOUR, 1);
            return calendar.getTime();
        }else if(genre.equals("スポーツ・リゾート施設")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("サイクリングセンター")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("キャンプ場")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("フィールド・アスレチック")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("フィールド・アーチェリー場")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("スケート場")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("マリーナ・ヨットハーバー")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("サイクリングコース")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("ハイキングコース")) {
            calendar.add(Calendar.HOUR, 3);
            return calendar.getTime();
        }else if(genre.equals("自然歩道・自然研究路")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("オリエンテーチング・パーマネントコース")) {
            calendar.add(Calendar.HOUR, 3);
            return calendar.getTime();
        }else if(genre.equals("海水浴場")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("観光農林業（体験含む）")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("観光牧場（体験含む）")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("観光漁業（体験含む）")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("テーマパーク・レジャーランド")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("温泉")) {
            calendar.add(Calendar.HOUR, 1);
            return calendar.getTime();
        }else if(genre.equals("その他（遊ぶ）")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("ショッピング店")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("伝統工芸技術")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("その他（買う）")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("郷土料理店")) {
            calendar.add(Calendar.HOUR, 1);
            return calendar.getTime();
        }else if(genre.equals("その他（食べる）")) {
            calendar.add(Calendar.HOUR, 1);
            return calendar.getTime();
        }else if(genre.equals("ケーブルカー・ロープウェイ")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("レンタサイクル")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("遊覧船")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("観光列車")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("観光周遊バス")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("その他（乗り物）")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("その他（イベント）")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }else if(genre.equals("スキー場")) {
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }

        Log.e("test","category to spending time error!");
        return null;

    }
    public String addressToPrefecture(String pref){
        if(-1!=pref.indexOf("三重県")){
            return "三重県";
        }else if(-1!=pref.indexOf("京都府")){
            return "京都府";
        }else if(-1!=pref.indexOf("佐賀県")){
            return "佐賀県";
        }else if(-1!=pref.indexOf("兵庫県")){
            return "兵庫県";
        }else if(-1!=pref.indexOf("北海道")){
            return "北海道";
        }else if(-1!=pref.indexOf("千葉県")){
            return "千葉県";
        }else if(-1!=pref.indexOf("和歌山県")){
            return "和歌山";
        }else if(-1!=pref.indexOf("埼玉県")){
            return "埼玉県";
        }else if(-1!=pref.indexOf("大分県")){
            return "大分県";
        }else if(-1!=pref.indexOf("大阪府")){
            return "大阪府";
        }else if(-1!=pref.indexOf("奈良県")){
            return "奈良県";
        }else if(-1!=pref.indexOf("宮城県")){
            return "宮城県";
        }else if(-1!=pref.indexOf("富山県")){
            return "富山県";
        }else if(-1!=pref.indexOf("山口県")){
            return "山口県";
        }else if(-1!=pref.indexOf("山形県")){
            return "山形県";
        }else if(-1!=pref.indexOf("山梨県")){
            return "山梨県";
        }else if(-1!=pref.indexOf("岐阜県")){
            return "岐阜県";
        }else if(-1!=pref.indexOf("岡山県")){
            return "岡山県";
        }else if(-1!=pref.indexOf("岩手県")){
            return "岩手県";
        }else if(-1!=pref.indexOf("島根県")){
            return "島根県";
        }else if(-1!=pref.indexOf("広島県")){
            return "広島県";
        }else if(-1!=pref.indexOf("徳島県")){
            return "徳島県";
        }else if(-1!=pref.indexOf("愛媛県")){
            return "愛媛県";
        }else if(-1!=pref.indexOf("愛知県")){
            return "愛知県";
        }else if(-1!=pref.indexOf("新潟県")){
            return "新潟県";
        }else if(-1!=pref.indexOf("東京都")){
            return "東京都";
        }else if(-1!=pref.indexOf("栃木県")){
            return "栃木県";
        }else if(-1!=pref.indexOf("沖縄県")){
            return "沖縄県";
        }else if(-1!=pref.indexOf("滋賀県")){
            return "滋賀県";
        }else if(-1!=pref.indexOf("熊本県")){
            return "熊本県";
        }else if(-1!=pref.indexOf("石川県")){
            return "石川県";
        }else if(-1!=pref.indexOf("神奈川県")){
            return "神奈川県";
        }else if(-1!=pref.indexOf("福井県")){
            return "福井県";
        }else if(-1!=pref.indexOf("福岡県")){
            return "福岡県";
        }else if(-1!=pref.indexOf("福島県")){
            return "福島県";
        }else if(-1!=pref.indexOf("秋田県")){
            return "秋田県";
        }else if(-1!=pref.indexOf("群馬県")){
            return "群馬県";
        }else if(-1!=pref.indexOf("茨城県")){
            return "茨城県";
        }else if(-1!=pref.indexOf("長崎県")){
            return "長崎県";
        }else if(-1!=pref.indexOf("長野県")){
            return "長野県";
        }else if(-1!=pref.indexOf("青森県")){
            return "青森県";
        }else if(-1!=pref.indexOf("静岡県")){
            return "静岡県";
        }else if(-1!=pref.indexOf("鹿児島県")){
            return "鹿児島県";
        }else if(-1!=pref.indexOf("高知県")){
            return "高知県";
        }else if(-1!=pref.indexOf("宮崎県")){
            return "宮崎県";
        }else if(-1!=pref.indexOf("鳥取県")){
            return "鳥取県";
        }
        Log.e("test","prefStringToIdに県名がないです"+pref);
        return "";
    }

    // ====== テキストのマッチング（部分一致）を判定するメソッド ======
    public boolean isMatch(String target, String word) {
        // 観光地名に自由テキストが含まれているかどうか
        if (target.matches(".*" + word + ".*")) {
            return true;
        } else {
            return false;
        }
    }

    // ====== テキストのマッチング（一致回数）を返すメソッド ======
    public int isCount(String target, String word) {
        // (説明文の文字数ー自由テキストの文字を削除した説明文の文字数) / (自由テキストの文字数)
        // つまり，(説明文に存在する自由テキストの文字数の合計) / （自由テキストの文字数）＝自由テキストの個数
        return (target.length() - target.replaceAll(word, "").length()) / word.length();
    }

    // ====== 入力テキストを空白でパースするメソッド ======
    public ArrayList parseText(String input_text) {
        String[] input_list = input_text.replace("　", " ").split(" ", 0);

        // ArrayList型に変換する
        ArrayList<String> input_list_new = new ArrayList();
        for (int i=0; i<input_list.length; i++) {
            input_list_new.add(input_list[i]);
        }
        return input_list_new;
    }
    //itineralyList上の開始位置と終了位置を渡すと、その区間の地図画像を返す関数.booleanはポリラインを使って通る道を表示するかどうか
    public Bitmap staticsMapMaker(int startNumber,int endNumber,boolean drawPolyline) throws JSONException, MalformedURLException {
        //画像格納変数
        Bitmap mapsStaticsResult = null;
        //ポリラインを描くかどうか
        if(drawPolyline){
            String waypoints = "";
            //3箇所以上通る場所がある場合は、経由地をwaypointとして指定する必要がある.waypointを用意するかどうかの条件文。
            if(endNumber-startNumber>1) {
                waypoints=waypoints+"waypoints=";
                for (int i = startNumber+1; i < endNumber-1; i++) {
                    waypoints = waypoints + "place_id:" + Value.itineraryPlaceList.get(i).placeID + "|";
                }
                waypoints=waypoints+"&";
            }
            //描画するマーカーの設定。
            String markers="";
            for (int i = startNumber; i <= endNumber; i++) {
                if(i==0){
                    markers=markers+"markers=color:purple|label:S|"+String.valueOf(Value.itineraryPlaceList.get(i).lat)+","+String.valueOf(Value.itineraryPlaceList.get(i).lng)+"&";
                }else if(i==Value.itineraryPlaceList.size()-1){
                    markers=markers+"markers=color:purple|label:G|"+String.valueOf(Value.itineraryPlaceList.get(i).lat)+","+String.valueOf(Value.itineraryPlaceList.get(i).lng)+"&";
                }else{
                    markers=markers+"markers=color:purple|label:"+String.valueOf(i)+"|"+String.valueOf(Value.itineraryPlaceList.get(i).lat)+","+String.valueOf(Value.itineraryPlaceList.get(i).lng)+"&";
                }
            }
            //directionAPIにリクエストを送り、ポリラインを取得。
            JSONObject tempDirectionSearch = httpGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + String.valueOf(Value.itineraryPlaceList.get(startNumber).lat)+","
                    +String.valueOf(Value.itineraryPlaceList.get(startNumber).lng) + "&" +
                    "destination=" + String.valueOf(Value.itineraryPlaceList.get(endNumber).lat) +","+
                    String.valueOf(Value.itineraryPlaceList.get(endNumber).lng) +"&" +
                    waypoints +
                    "transit_mode=rail&"+
                    "key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
            //staticsAPIにリクエストを送り、画像を取得。
            String tempPolyline = tempDirectionSearch.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
            mapsStaticsResult = httpBitmapGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/staticmap?" +
                    "size=400x400&" +markers+
                    "path=color:0xff0000ff|weight:5%7Cenc:" + tempPolyline + "&" +
                    "key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
        }else{
            //以下、ポリラインを描画しないバージョン。
            String markers="";
            for (int i = startNumber; i <= endNumber; i++) {
                markers=markers+"markers=color:purple|label:"+String.valueOf(i)+"|"+String.valueOf(Value.itineraryPlaceList.get(i).lat)+","+String.valueOf(Value.itineraryPlaceList.get(i).lng)+"&";
            }
            mapsStaticsResult = httpBitmapGet.HttpPlaces(new URL("https://maps.googleapis.com/maps/api/staticmap?" +
                    "size=400x400&" +markers+
                    "path=color:0xff0000ff|weight:5%7Cenc:"  +
                    "key=AIzaSyCke0pASXyPnnJR-GAAvN3Bz7GltgomfEk"));
            
        }
    return mapsStaticsResult;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("test","location changed");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}

//スレッドを使い、並列でjsonの読み出しを行うクラス。
class LoadJsonInThread extends Thread {

    public void run() {
        JsonReader json=new JsonReader();//Json読み込み用クラスのインスタンス
//**************公共クラウドシステムの入ったjsonファイルを取得する******************
        ////// 公共クラウドシステム（jsonファイル読み込み用） ///////
        // jsonファイルを読み込む
        // 辞書とジャンル名の対応jsonを読み込む
        Value.pair_json = json.ReadJson(MainActivity.getInstance(), "pair_limit.json");
        Value.spots_json = json.ReadJson(MainActivity.getInstance(), "kanko_all_add_limit.json");
        Value.neighborDicObject = json.ReadJson(MainActivity.getInstance(), "neighbor_pref.json");//隣接県情報の入ったjson読み出し
    }
}