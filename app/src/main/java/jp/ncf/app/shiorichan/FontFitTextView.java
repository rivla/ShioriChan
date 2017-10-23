package jp.ncf.app.shiorichan;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * サイズ自動調整TextView
 *
 */
public class FontFitTextView extends TextView {

    /** 最小のテキストサイズ */
    private static final float MIN_TEXT_SIZE = 10f;

    /**
     * コンストラクタ
     * @param context
     */
    public FontFitTextView(Context context) {
        super(context);
    }

    /**
     * コンストラクタ
     * @param context
     * @param attrs
     */
    public FontFitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d("tesT","onlayout");
        super.onLayout(changed, left, top, right, bottom);

        resize();

    }

    /**
     * テキストサイズ調整
     */
    private void resize() {

        Paint paint = new Paint();

        // Viewの幅
        int viewWidth = this.getWidth();
        // テキストサイズ
        float textSize = getTextSize();

        // Paintにテキストサイズ設定
        paint.setTextSize(textSize);

        String[] splitString=this.getText().toString().split("\n",0);
        int tempMaxStringLength=0;
        for(int i =0;i<splitString.length;i++){
            if(splitString[tempMaxStringLength].length()<splitString[i].length())tempMaxStringLength=i;
        }
        // テキストの横幅取得
        float textWidth = paint.measureText(splitString[tempMaxStringLength]);
        Log.d("splitString",splitString[tempMaxStringLength]);

        while (viewWidth <  textWidth) {
            Log.d("test",String .valueOf(textWidth));
            Log.d("test",String.valueOf(viewWidth));
            // 横幅に収まるまでループ

            if (MIN_TEXT_SIZE >= textSize) {
                // 最小サイズ以下になる場合は最小サイズ
                textSize = MIN_TEXT_SIZE;
                break;
            }

            // テキストサイズをデクリメント
            textSize--;

            // Paintにテキストサイズ設定
            paint.setTextSize(textSize);
            // テキストの横幅を再取得
            textWidth = paint.measureText(splitString[tempMaxStringLength]);

        }
        Log.d("in font fit text size i",String .valueOf(textSize));
        // テキストサイズ設定
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

}