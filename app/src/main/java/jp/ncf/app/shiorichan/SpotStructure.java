package jp.ncf.app.shiorichan;

import android.graphics.Bitmap;

import java.sql.Time;
import java.util.Date;

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
    String explainText;
    Bitmap image;
    Date departTime;
    Bitmap mapImage;
    String polyline;

    public SpotStructure(String placeID,String name,String genre,String prefecture,double rate,double lat,double lng,double distance,String explainText,Bitmap image,Date departTime,Bitmap mapImage,String polyline){
        this.prefecture=prefecture;
        this.placeID=placeID;
        this.name=name;
        this.genre=genre;
        this.rate=rate;
        this.lat=lat;
        this.lng=lng;
        this.distance=distance;
        this.explainText=explainText;
        this.image=image;
        this.departTime=departTime;
        this.mapImage=mapImage;
        this.polyline=polyline;
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
    public String getExplainText(){return this.explainText;}
    public Bitmap getImage(){
        return this.image;
    }
}
