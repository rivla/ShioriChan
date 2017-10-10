package jp.ncf.app.shiorichan;

import com.google.android.gms.maps.model.StreetViewPanoramaOrientation;

/**
 * Created by ideally on 2017/10/06.
 */

public class SpotStructure {
    String placeID;
    String name;
    double rate;
    double lat;
    double lng;
    public SpotStructure(String placeID,String name,double rate,double lat,double lng){
        this.placeID=placeID;
        this.name=name;
        this.rate=rate;
        this.lat=lat;
        this.lng=lng;
    }
}
