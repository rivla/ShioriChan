package jp.ncf.app.shiorichan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by ideally on 2017/10/06.
 */

public class HttpBitmapGetter {
    static public Bitmap HttpPlaces(URL url){
        // 取得したテキストを格納する変数
        final StringBuilder result = new StringBuilder();

        HttpURLConnection con = null;
        Bitmap image;
        try {
            URL imageUrl = url;
            InputStream imageIs;
            imageIs = imageUrl.openStream();
            image = BitmapFactory.decodeStream(imageIs);
            return image;
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
