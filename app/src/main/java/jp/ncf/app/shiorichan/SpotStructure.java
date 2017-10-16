package jp.ncf.app.shiorichan;

import com.google.android.gms.maps.model.StreetViewPanoramaOrientation;

/**
 * Created by ideally on 2017/10/06.
 */

class SpotStructure {
    String placeID;
    String name;
    String genre;
    String prefecture;
    double rate;
    double lat;
    double lng;
    double distance;
    double eval;
    double explainText;

    public SpotStructure(String placeID,String name,String genre,String prefecture,double rate,double lat,double lng,double distance,String.explainText){
        this.prefecture=prefecture;
        this.placeID=placeID;
        this.name=name;
        this.genre=genre;
        this.rate=rate;
        this.lat=lat;
        this.lng=lng;
        this.distance=distance;
        this.explainText=explainText;
        this.
    }
    public String getPrefecture(){
        return this.prefecture;
    }
    public String getName(){
        return this.name;
    }
    public String getGenre(){
        return this.genre;
    }
    public String getplaceID(){
        return this.placeID;
    }
    public double getRate(){
        return this.rate;
    }
    public double getLat(){
        return this.lat;
    }
    public double getLng(){
        return this.lng;
    }
    public double getDistance(){
        return this.distance;
    }
    public double getExplainText(){
        return this.explainText;
    }
}
