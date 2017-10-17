package jp.ncf.app.shiorichan;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

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
        setContentView(R.layout.shiori_main);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);

   //     viewFlipper.setAutoStart(true);     //自動でスライドショーを開始
   //     viewFlipper.setFlipInterval(1000);  //更新間隔(ms単位)


        // ViewFlipperでつかうlayoutの設定
        // === xmlを呼び出して, テキストを上書きする === //
        View v = this.getLayoutInflater().inflate(R.layout.first_page, null);
        viewFlipper.addView(v);

        //
//        Value.itineraryPlaceList.get(suuzui).distance;
//         Value.itineraryPlaceList.size() // 長さが必要な場合

        Log.d("受け取ったデータの長さ",""+Value.itineraryPlaceList.size());

        // === 2ページ目 Schedule === //
        View v2 = getLayoutInflater().inflate(R.layout.schedule_page,null);
        // 1つ目の行き先
        TextView textView4 = (TextView)v2.findViewById(R.id.textView4);
        textView4.setText("時:刻"); //
        TextView textView5 = (TextView)v2.findViewById(R.id.textView5); // v2.findViewByIdで指定する
        textView5.setText(Value.itineraryPlaceList.get(0).getName());
        // 2つ目の行き先
        TextView textView6 = (TextView)v2.findViewById(R.id.textView6);
        textView4.setText("時刻"); //
        TextView textView7 = (TextView)v2.findViewById(R.id.textView7);
        textView5.setText(Value.itineraryPlaceList.get(1).getName());
        // 3つ目の行き先
//        TextView textView8 = (TextView)v2.findViewById(R.id.textView8);
//        textView4.setText("z:zz"); //
//        TextView textView9 = (TextView)v2.findViewById(R.id.textView9);
//        textView5.setText("どてめし");
        // 4つ目の行き先
//        TextView textView10 = (TextView)v2.findViewById(R.id.textView10);
//        textView4.setText("a:aa"); //
//        TextView textView11 = (TextView)v2.findViewById(R.id.textView11);
//        textView5.setText("どてめし");
        // 5つ目の行き先
//        TextView textView12 = (TextView)v2.findViewById(R.id.textView12);
//        textView4.setText("b:bb"); //
//        TextView textView13 = (TextView)v2.findViewById(R.id.textView13);
//        textView5.setText("どてめし");
        // viewFlipperに追加
        viewFlipper.addView(v2);
        // === 2ページ目終了 === //

        // === 3ページ目 === //
        View v3 = this.getLayoutInflater().inflate(R.layout.place_infomation, null);
        // 観光地の画像
//        ImageView imageView3 = (ImageView)v3.findViewById(R.id.imageView3);
//        textView4.setImage("指定先"); //
        // 観光地の名称
//        TextView textView15 = (TextView)v3.findViewById(R.id.textView15);
//        textView5.setText("どてめし");
        // 観光地の説明文
//        TextView textView16 = (TextView)v3.findViewById(R.id.textView16);
//        textView5.setText("どてめし");

        viewFlipper.addView(v3);
    }

    // Using the following method, we will handle all screen swaps.
    @Override
    public boolean onTouchEvent(MotionEvent touchevent) {
        Log.d("test","in ontouchevent");
        switch (touchevent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                firstX = touchevent.getX();
                break;
            case MotionEvent.ACTION_UP:
                X = touchevent.getX();

                // Handling left to right screen swap.
                if (X - firstX > adjust) {
                    // Next screen comes in from left.
                    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_from_left));
                    // Current screen goes out from right.
                    viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_to_right));

                    // Display next screen.
                    viewFlipper.showNext();
                }

                // Handling right to left screen swap.
                else if (firstX - X > adjust) {
                    // Next screen comes in from right.
                    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_from_right));
                    // Current screen goes out from left.
                    viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_to_left));


                    // Display previous screen.
                    viewFlipper.showPrevious();
                }
                break;
        }
        return false;
    }


    // フリックイベントで
//    public boolean onFling(MotionEvent e1 // TouchDown時のイベント
//            ,MotionEvent e2   // TouchDown後、指の移動毎に発生するイベント
//            ,float velocityX  // X方向の移動距離
//            ,float velocityY)  // Y方向の移動距離
//    {
//        // 絶対値の取得
//        float dx = Math.abs(velocityX);
//        float dy = Math.abs(velocityY);
//        // 指の移動方向(縦横)および距離の判定
//        Log.d("移動距離", dx+"  "+dy);
//        if (dx > dy && dx > 100) {
//            // 指の移動方向(左右)の判定
//            if (e1.getX() < e2.getX()) {
//                viewFlipper.showPrevious();
//            } else {
//                viewFlipper.showNext();
//            }
//            return true;
//        }
//        return false;
//    }


}

