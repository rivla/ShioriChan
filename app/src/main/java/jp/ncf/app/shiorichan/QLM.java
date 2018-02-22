package jp.ncf.app.shiorichan;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kazu on 18/02/19.
 */
public class QLM {
    static public double calcQLM(Context csContext, JSONObject vec_json, String name_str) throws JSONException {
        ////////////////////////////////////
        // クエリ尤度モデルを計算する関数
        // 引数：コンテキスト，jsonデータ，観光地名
        ////////////////////////////////////

        // ====== クエリ尤度モデルを用いたスコアリング ======
        double likelihood = 0.0; // 尤度を初期化する

        // 現在の観光地における，文書ベクトル（正規化TF）の計算結果を取得する
        JSONObject doc_tf_vec = vec_json.getJSONObject("doc_tf_vecs").getJSONObject(name_str);

        // 観光地全体を用いた，文書コレクションの文書ベクトル（正規化TF）の計算結果を取得する
        JSONObject corpus_tf_vec = vec_json.getJSONObject("corpus_tf_vec");

        // 各種パラメータを設定する
        double doc_length = doc_tf_vec.length(); // 観光地説明文の文書長
        double mu = 100; // スムージングパラメータ
        double frac = 1.0 / (doc_length + mu);
        double not_word_val = 1e-250; // 極小値
        double doc = 0.0;
        double corpus = 0.0;

        // 入力クエリのテキストのループ
        for (int j = 0; j < Value.input_list.size(); j++) {
            // 入力テキストの単語を取得する
            String word = Value.input_list.get(j);

            // 対象の観光地説明文の文書モデルから尤度を得る
            if (doc_tf_vec.isNull(word) == false) {
                // 対象の観光地説明文の文書モデル
                doc = doc_length * frac * Double.parseDouble(doc_tf_vec.getString(word));
            } else {
                // 対象の観光地説明文の文書モデル
                doc = doc_length * frac * not_word_val; // 未知語の場合は極小値
            }

            // 観光地全体の文書コレクションモデルから尤度を得る
            if (corpus_tf_vec.isNull(word) == false) {
                // 観光地全体の文書コレクションモデル
                corpus = mu * frac * Double.parseDouble((corpus_tf_vec.getString(word)));
            } else {
                // 対象の観光地説明文の文書モデル
                corpus = mu * frac * not_word_val; // 未知語の場合は極小値
            }

            // ディリクレスムージングを適用したクエリ尤度モデル
            likelihood += Math.log(doc + corpus);

            // Log.d(name_str, String.valueOf(doc_tf_vec.getString(word).getClass()));
        }
        return likelihood;
    }


    static public double calcQLM2(Context csContext, JSONObject vec_json, JSONObject spot_json) throws JSONException {
        ////////////////////////////////////
        // クエリ尤度モデルを計算する関数(その２)
        // 第一項目の単語マッチングを，isCount()を用いて行う場合の関数
        // 入力クエリは形態素解析を施していないため，入力クエリが分割されそうな単語である場合，
        // 文字列を直接マッチングするこちらの方が精度が高くなるかも
        // 引数：コンテキスト，jsonデータ，観光地名
        ////////////////////////////////////

        // ====== クエリ尤度モデルを用いたスコアリング ======
        double likelihood = 0.0; // 尤度を初期化する

        // 検索対象文書のテキストを作成する
        String name = spot_json.getString("name");
        String explain = spot_json.getString("explain");
        String text = name + explain;

        // 観光地全体を用いた，文書コレクションの文書ベクトル（正規化TF）の計算結果を取得する
        JSONObject corpus_tf_vec = vec_json.getJSONObject("corpus_tf_vec");

        // 各種パラメータを設定する
        double name_length = spot_json.getDouble("name_length");
        double explain_length = spot_json.getDouble("explain_length");
        double doc_length = name_length + explain_length; // 観光地名＋観光地説明文の文書長
        double mu = 100; // スムージングパラメータ
        double frac = 1.0 / (doc_length + mu);
        double not_word_val = 1e-250; // 極小値
        double doc = 0.0;
        double corpus = 0.0;

        // 入力クエリのテキストのループ
        for (int j = 0; j < Value.input_list.size(); j++) {
            // 入力テキストの単語を取得する
            String word = Value.input_list.get(j);

            // 現在の観光地における，正規化TFを計算する
            double doc_tf_value = (double) isCount(text, word) / doc_length;

            // 対象の観光地説明文の文書モデルから尤度を得る
            if (doc_tf_value != 0.0) {
                // 対象の観光地説明文の文書モデル
                doc = doc_length * frac * doc_tf_value;
            } else {
                // 対象の観光地説明文の文書モデル
                doc = doc_length * frac * not_word_val; // 未知語の場合は極小値
            }

            // 観光地全体の文書コレクションモデルから尤度を得る
            if (corpus_tf_vec.isNull(word) == false) {
                // 観光地全体の文書コレクションモデル
                corpus = mu * frac * Double.parseDouble((corpus_tf_vec.getString(word)));
            } else {
                // 対象の観光地説明文の文書モデル
                corpus = mu * frac * not_word_val; // 未知語の場合は極小値
            }

            // ディリクレスムージングを適用したクエリ尤度モデル
            likelihood += Math.log(doc + corpus);

            // Log.d(name_str, String.valueOf(doc_tf_vec.getString(word).getClass()));
        }
        return likelihood;
    }


    // ====== テキストのマッチング（一致回数）を返すメソッド ======
    static public int isCount(String target, String word) {
        // (説明文の文字数ー自由テキストの文字を削除した説明文の文字数) / (自由テキストの文字数)
        // つまり，(説明文に存在する自由テキストの文字数の合計) / （自由テキストの文字数）＝自由テキストの個数
        return (target.length() - target.replaceAll(word, "").length()) / word.length();
    }
}
