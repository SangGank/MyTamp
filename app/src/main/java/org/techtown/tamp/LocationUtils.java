package org.techtown.tamp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;

import org.techtown.tamp.MainActivity;

public class LocationUtils implements LocationListener {

    double startX;
    double startY;

    private MainActivity mainActivity; // MainActivity 참조

    private LocationManager locationManager;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    public LocationUtils(MainActivity mainActivity) {

        this.mainActivity = mainActivity;

        // 위치 권한 요청하기
        ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);

        // 위치 관련 객체 생성하기
        locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);

        // 위치 정보 요청하기
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, this);
        }

        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            double latitude = lastKnownLocation.getLatitude();
            double longitude = lastKnownLocation.getLongitude();
            // 위도(latitude)와 경도(longitude) 사용
            startX = longitude;
            startY = latitude;
            mainActivity.onLocationReceived(startY, startX);
        }
    }

    // 위치 정보 수신하기
    @Override
    public void onLocationChanged(Location location) {
        // 위치 정보 보내기
        startX = location.getLongitude();
        startY = location.getLatitude();
        sendLocationToServer(location.getLatitude(), location.getLongitude());







//        mainActivity.removePath(startY,startX);

        // MainActivity에서 onLocationReceived 메서드 호출하여 좌표 전달
       mainActivity.onLocationReceived(startY, startX);
        mainActivity.useRoute(startY,startX);


        // 주기적으로 위치 정보 출력하기
        Log.d("주기적 위치 정보", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    // 권한 요청 결과 처리하기
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 권한이 승인된 경우 위치 정보 요청하기
            // 위치 권한 요청하기
            if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
            }
        }
    }


    private void sendLocationToServer(double latitude, double longitude) {
        // 위치 정보를 서버로 보내는 코드 작성
    }

    public double getX(){
        return startX;
    }
    public double getY(){
        return startY;
    }

    public void stopLocationUpdates() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        mainActivity = null;
    }


}
