package jp.ncf.app.shiorichan;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ideally on 2017/10/10.
 * 担当 : fukui
 */

// ここに、しおり用のビューを作成する
public class ShioriView extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.shiori_main);

        // ViewPager用のペーシを作成
        ViewPager mViewPager = (ViewPager)findViewById(R.id.viewpager);
        PagerAdapter mPagerAdapter = new MyPagerAdapter();
        mViewPager.setAdapter(mPagerAdapter);

    }

    private class MyPagerAdapter extends PagerAdapter {
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // レイアウトファイル名を配列で指定します
            // pages : スライドで表示できるページ画面の配列
            int[] pages = {R.layout.first_page, R.layout.schedule_page, R.layout.place_infomation};

            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View layout ;
            layout = inflater.inflate(pages[position], null);
            ((ViewPager) container).addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager)container).removeView((View)object);
        }

        @Override
        public int getCount() {
            // ページ数を返します。
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }
    }
}
