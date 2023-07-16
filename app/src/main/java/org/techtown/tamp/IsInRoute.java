package org.techtown.tamp;

import com.naver.maps.geometry.LatLng;

import java.util.List;

public class IsInRoute {

    public int InRoute(double latitude, double longitude, List<LatLng> latLngs, int num){

        double size=0.00005;
        try{
            for(int i =0; i<num; i++){
                double y_lat = latLngs.get(i).latitude;
                double y_long = latLngs.get(i).longitude;
//                if(y_lat>=latitude-size && y_lat<=latitude+size &&
//                        y_long>=longitude-size && y_long<=longitude+size){
                if(inArea(latitude,longitude,y_lat,y_long,size)){
                    return i;

                }
            }

        }catch (Exception e){

        }

        return -1;
    }
    public boolean inArea( double in_lat,double in_lon, double po_lat, double po_lon, double size){
        // in_lat,in_lon (중심점)을 기준으로 size이내에 들어와 있는가

        //범위 안에 들어와 있으면 True를 반환
        if(po_lat>= in_lat - size && po_lat <= in_lat + size &&
                po_lon>= in_lon - size && po_lon<= in_lon + size){

            return true;

        }else{

            return false;

        }
    }
}
