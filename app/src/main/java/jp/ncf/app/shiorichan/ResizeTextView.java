package jp.ncf.app.shiorichan;

import android.content.Context;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * フォントサイズ自動調整TextView
 */
public class ResizeTextView extends TextView
{
    private static final String TAG = ResizeTextView.class.getSimpleName();

    /**
     * コンストラクタ
     * @param context
     */
    public ResizeTextView(Context context)
    {
        super(context);
    }

    /**
     * コンストラクタ
     * @param context
     * @param attrs
     */
    public ResizeTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        processAttributeSet(attrs);
    }

    /**
     * 子Viewの位置を決める
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        Log.d("onlayout","");
        super.onLayout(changed, left, top, right, bottom);
        resize();
    }

    /**
     * テキストサイズ調整
     */
    public void resize()
    {
        /** 最小のテキストサイズ */
        final float MIN_TEXT_SIZE = 10f;

        int viewHeight = this.getHeight(); // Viewの縦幅
        int viewWidth = this.getWidth(); // Viewの横幅

        Log.d("height",String.valueOf(viewHeight));
        // テキストサイズ
        float textSize = getTextSize();

        // Paintにテキストサイズ設定
        Paint paint = new Paint();
        paint.setTextSize(textSize);

        // テキスト取得
        if (modelText == null){
            modelText = getText().toString();
        }

        // テキストの縦幅取得
        FontMetrics fm = paint.getFontMetrics();
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

        // テキストサイズ設定
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    String modelText;
    int numberLine = 1;


    /**
     * 基準となる改行を含む文字列の最も文字列が大きい部分がViewの枠に収まるようにフォントサイズを調整する.(改行には適応してない模様)
     * 文字列に改行を含まない場合、それをそのまま基準にする.
     * 表示される文字列の最大数がわかっている時に有効利用できる.
     * @param modelText
     */
    protected void setModelText(String modelText){
        if (modelText != null){
            String[] str = modelText.split("\n");
            numberLine = str.length;
            boolean includeLinefeed = false;
            if (str.length > 1) includeLinefeed = true;

            if (includeLinefeed){
                String a = null;        // 一時変数
                String model = null;
                for (int i = 0;i<str.length;i++){
                    if (a == null) a = str[i];
                    else{
                        // 2周目以降
                        if (a.length() >= str[i].length() ) model = a;
                        else model = str[i];
                    }
                }
                this.modelText = model;
            }else{
                this.modelText = modelText;
            }
        }
    }

    protected void processAttributeSet(AttributeSet attrs) {
        //This method reads the parameters given in the xml file and sets the properties according to it
        this.setModelText(attrs.getAttributeValue(null,"model_text"));
    }
}