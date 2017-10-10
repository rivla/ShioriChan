package jp.ncf.app.shiorichan;

import java.util.Comparator;

/**
 * Created by ideally on 2017/10/10.
 */

public class SpotStructureComparator implements Comparator<SpotStructure> {
    //比較メソッド（データクラスを比較して-1, 0, 1を返すように記述する）
    public int compare(SpotStructure a ,SpotStructure b) {
        double no1 = a.distance;
        double no2 = b.distance;

        if (no1 > no2) {
            return 1;

        } else if (no1 == no2) {
            return 0;

        } else {
            return -1;

        }
    }
}
