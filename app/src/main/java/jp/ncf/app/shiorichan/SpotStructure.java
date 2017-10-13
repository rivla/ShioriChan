package jp.ncf.app.shiorichan;

import com.google.android.gms.maps.model.StreetViewPanoramaOrientation;

/**
 * Created by ideally on 2017/10/06.
 */

public class SpotStructure {
    String placeID;
    String name;
    String genre;
    String prefecture;
    double rate;
    double lat;
    double lng;
    double distance;
    double eval;

    public SpotStructure(String placeID,String name,String genre,String prefecture,double rate,double lat,double lng,double distance,double eval){
        this.prefecture=prefecture;
        this.placeID=placeID;
        this.name=name;
        this.genre=genre;
        this.rate=rate;
        this.lat=lat;
        this.lng=lng;
        this.distance=distance;
        this.eval=eval;
    }
}
