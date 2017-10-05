package jp.ncf.app.shiorichan;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by kazu on 17/10/05.
 */
public class JsonReader {
    static public JSONObject ReadJson(Context csContext, String strFileName)
    {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        JSONObject json = null;
        try {
            // 事前に用意しておいた、jsonファイルを読み込みます
            AssetManager csAsset = csContext.getResources().getAssets();
            inputStream = csAsset.open(strFileName);
            bufferedReader =
                    new BufferedReader(new InputStreamReader(inputStream));
            String str = bufferedReader.readLine();

            // JSONObject に変換します
            json = new JSONObject(str);

            // JSONObject を文字列に変換してログ出力します
//            Log.d("json test", json.toString(4));

            inputStream.close();
            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
