package org.techtown.tamp;

import android.location.Location;

import com.naver.maps.geometry.LatLng;

public class AngleCal {
    public double angle_cal(LatLng latLng1, LatLng latLng2){


        Location location1 = new Location("");
        location1.setLatitude(latLng1.latitude);
        location1.setLongitude(latLng1.longitude);

        Location location2 = new Location("");
        location2.setLatitude(latLng2.latitude);
        location2.setLongitude(latLng2.longitude);

        double angle = location1.bearingTo(location2);
        if(angle<0){
            angle+=360;
        }



        return angle;
    }
}
