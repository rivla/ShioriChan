package jp.ncf.app.shiorichan;

import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ImageView;
import android.widget.RatingBar;

/**
 * Created by ideally on 2017/10/10.
 * 担当 : fukui
 */

// ここに、しおり用のビューを作成する
public class ShioriView extends Activity {

    private ViewFlipper viewFlipper;
    //フリックのＸ位置
    private float X,firstX;
    // フリックの遊び部分（最低限移動しないといけない距離）
    private float adjust = 100;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewFlipperでつかうlayoutの設定
        setContentView(R.layout.shiori_main);
        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);

        // デバッグ用 スライドショー //
        // viewFlipper.setAutoStart(true);     //自動でスライドショーを開始
        // viewFlipper.setFlipInterval(1000);  //更新間隔(ms単位)

        // メモ : 情報の参照
        // Value.itineraryPlaceList.get(INDEX).distance;
        // Value.itineraryPlaceList.size() // 長さが必要な場合
        // Log.d("受け取ったデータの長さ",""+Value.itineraryPlaceList.size());

        // ViewFlipper にページレイアウトを作る //
        // === 基本的にはxmlを呼び出して, テキストを上書きして, 格納していく === //

        // === first page === //
        View v1 = this.getLayoutInflater().inflate(R.layout.first_page, null);
        TextView textView18 = (TextView)v1.findViewById(R.id.textView18);
        textView18.setText("〇〇のしおり"); // もし変更するならココで
        TextView textView19 = (TextView)v1.findViewById(R.id.textView19);
        textView19.setText("名前 : しおり"); //
        if (Value.itineraryPlaceList.size() > 1) { // もし受け取ったルートの長さが2以上なら...
            ImageView imageView5 = (ImageView)v1.findViewById(R.id.imageView5); // 表紙の中央の画像
            imageView5.setImageBitmap(Value.itineraryPlaceList.get(1).getImage()); //
        } else{
            Log.d("from ShioriView.java : ","受け取ったスケジュールの場所の数が"+Value.itineraryPlaceList.size()+"個です");
        }
            viewFlipper.addView(v1); // ViewFlipperにv1のレイアウトを追加


            // === Schedule page === //
            // 5つごとに1ページ作成,
            //
            // バグ : 時刻の情報がいくつかNullPointException になる
            // ー＞今のところ、コメントアウトで外してあります
            //

            View v2 = getLayoutInflater().inflate(R.layout.schedule_page, null);
            for (int i = 0; i < Value.itineraryPlaceList.size(); i++) {
                switch (i % 5) {
                    case 0:
                        // 1つ目の行き先
                        if (Value.itineraryPlaceList.get(i).getName() != null) {
                            TextView textView4 = (TextView) v2.findViewById(R.id.textView4);
                            textView4.setText(String.format("%02d:%02d", Value.itineraryPlaceList.get(i).departTime.getHours(), Value.itineraryPlaceList.get(i).departTime.getMinutes())); //
                            TextView textView5 = (TextView) v2.findViewById(R.id.textView5); // v2.findViewByIdで指定する
                            textView5.setText(Value.itineraryPlaceList.get(i).getName());
                        }
                        break;
                    case 1:
                        // 2つ目の行き先
                        if (Value.itineraryPlaceList.get(i).getName() != null) {
                            ImageView imageView6 = (ImageView) v2.findViewById(R.id.imageView6);
                            imageView6.setImageResource(R.mipmap.arrow_blue);
                            TextView textView6 = (TextView) v2.findViewById(R.id.textView6);
                            textView6.setText(String.format("%02d:%02d", Value.itineraryPlaceList.get(i).departTime.getHours(), Value.itineraryPlaceList.get(i).departTime.getMinutes())); //
                            TextView textView7 = (TextView) v2.findViewById(R.id.textView7);
                            textView7.setText(Value.itineraryPlaceList.get(i).getName());
                        }
                        break;
                    case 2:
                        // 3つ目の行き先
                        if (Value.itineraryPlaceList.get(i).getName() != null) {
                            ImageView imageView7 = (ImageView) v2.findViewById(R.id.imageView7);
                            imageView7.setImageResource(R.mipmap.arrow_blue);
                            TextView textView8 = (TextView) v2.findViewById(R.id.textView8);
                            textView8.setText(String.format("%02d:%02d", Value.itineraryPlaceList.get(i).departTime.getHours(), Value.itineraryPlaceList.get(i).departTime.getMinutes())); //
                            TextView textView9 = (TextView) v2.findViewById(R.id.textView9);
                            textView9.setText(Value.itineraryPlaceList.get(i).getName());
                        }
                        break;
                    case 3:
                        // 4つ目の行き先
                        if (Value.itineraryPlaceList.get(i).getName() != null) {
                            ImageView imageView8 = (ImageView) v2.findViewById(R.id.imageView8);
                            imageView8.setImageResource(R.mipmap.arrow_blue);
                            TextView textView10 = (TextView) v2.findViewById(R.id.textView10);
                            textView10.setText(String.format("%02d:%02d", Value.itineraryPlaceList.get(i).departTime.getHours(), Value.itineraryPlaceList.get(i).departTime.getMinutes())); //
                            TextView textView11 = (TextView) v2.findViewById(R.id.textView11);
                            textView11.setText(Value.itineraryPlaceList.get(i).getName());
                        }
                        break;
                    case 4:
                        // 5つ目の行き先
                        if (Value.itineraryPlaceList.get(i).getName() != null) {
                            ImageView imageView9 = (ImageView) v2.findViewById(R.id.imageView9);
                            imageView9.setImageResource(R.mipmap.arrow_blue);
                            TextView textView12 = (TextView) v2.findViewById(R.id.textView12);
                            textView12.setText(String.format("%02d:%02d", Value.itineraryPlaceList.get(i).departTime.getHours(), Value.itineraryPlaceList.get(i).departTime.getMinutes())); //
                            TextView textView13 = (TextView) v2.findViewById(R.id.textView13);
                            textView13.setText(Value.itineraryPlaceList.get(i).getName());
                        }

                        // viewFlipperに追加
                        viewFlipper.addView(v2);
                        // === 2ページ目終了 === //
                        v2 = getLayoutInflater().inflate(R.layout.schedule_page, null); // 2週目用
                        break;
                    default: // まずありえない
                        Log.d("from ShioriView.java : ","i%5 が0から4以外の数値を返しました");
                        break;
                }
            }
            // viewFlipperに追加
            viewFlipper.addView(v2);


            // === 3ページ目 観光地の情報=== //
            View v3 = this.getLayoutInflater().inflate(R.layout.place_infomation, null);
            for (int i = 1; i < Value.itineraryPlaceList.size() - 1; i++) { // 出発地点(0)と到着地(最後)は飛ばす
                // 観光地の画像
                if (Value.itineraryPlaceList.get(i).getImage() != null) {
                    ImageView imageView3 = (ImageView) v3.findViewById(R.id.imageView3);
                    imageView3.setImageBitmap(Value.itineraryPlaceList.get(i).getImage()); //
                }
                // 観光地の名称
                TextView textView15 = (TextView) v3.findViewById(R.id.textView15);
                textView15.setText(Value.itineraryPlaceList.get(i).getName());
                // 観光地の説明文
                TextView textView16 = (TextView) v3.findViewById(R.id.textView16);
                textView16.setText(Value.itineraryPlaceList.get(i).getExplainText());

                // 評価値の☆をつける
                RatingBar ratingBar = (RatingBar) v3.findViewById(R.id.ratingBar);
                ratingBar.setRating((float) Value.itineraryPlaceList.get(i).getRate());

                // viewFlipperに追加
                viewFlipper.addView(v3);

                v3 = this.getLayoutInflater().inflate(R.layout.place_infomation, null); // 2週目以降
            }

            // === 観光地の周辺地図のページ === //
            // Mapをつくる
            // Value.itineraryPlaceList.get(0).mapImage // これでgoogle のマップが取ってこれる
            // ImageViewをつくってそこに画像を投げる

            View v4 = this.getLayoutInflater().inflate(R.layout.map_page, null);
            // 周辺地図
            if (Value.itineraryPlaceList.size() > 0) {
                ImageView imageView2 = (ImageView) v4.findViewById(R.id.imageView2);
                imageView2.setImageBitmap(Value.itineraryPlaceList.get(0).mapImage); //
            }
            else{
                Log.d("from ShioriView.java : ","地図を作成出来ませんでした\nValue.itineraryPlaceList.get(0).mapImage を確認して下さい");
            }
            // viewFlipperに追加
            viewFlipper.addView(v4);

        }


    // Using the following method, we will handle all screen swaps.
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {

            case MotionEvent.ACTION_DOWN:
                firstX = touchevent.getX();
                break;
            case MotionEvent.ACTION_UP:
                X = touchevent.getX();

                // Handling left to right screen swap.
                if (X - firstX > adjust) {
                    Log.d("from ShioriView.java : ","右方向のスワイプを検知しました -> 先のページに移動します");
                    // Next screen comes in from left.
                    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_from_left));
                    // Current screen goes out from right.
                    viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_to_right));

                    // Display previous screen.
                    viewFlipper.showPrevious();
                }

                // Handling right to left screen swap.
                else if (firstX - X > adjust) {
                    Log.d("from ShioriView.java : ","左方向のスワイプを検知しました -> 前のページに移動します");
                    // Next screen comes in from right.
                    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_from_right));
                    // Current screen goes out from left.
                    viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_to_left));

                    // Display next screen.
                    viewFlipper.showNext();
                }
                break;
        }
        return false;
    }


}

