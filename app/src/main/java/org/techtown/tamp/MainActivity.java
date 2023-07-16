package org.techtown.tamp;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.MapUtils;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonArray;
import com.google.maps.android.SphericalUtil;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.CameraUpdateParams;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PathOverlay;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;
import org.techtown.tamp.LocationUtils;
import com.naver.maps.map.Projection;
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.overlay.PolylineOverlay;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements PedestrianRouteFinder.OnRouteFoundListener, OnMapReadyCallback {

    private NaverMap naverMap;
    private LocationUtils locationUtils;

    private Marker marker = new Marker();
    private PathOverlay path;
    private List<LatLng> latLngs = new ArrayList<>();
//
//    // 맵 크기
    private int mapWidth;
    private int mapHeight;

    //화면의 크기
    private PointF map_size;
    //내 위치 표시 하는 곳
    private PointF map_size_me ;

    private boolean isRedBorder = false;
    private boolean isUpdateRunning = false;

    IsInRoute inRoute = new IsInRoute();


    long startTime=0;
    long endTime=0;

    int stop = 1;
    int end_num = 0;
    int overlay_num = 0;

    AngleCal angleCal = new AngleCal();


    double startX = 127.0467338;
    double startY = 37.2805057;
    String startName = "출발지 이름";
    double endX ;
    double endY ;
    String endName = "도착지 이름";


    int c_num = 0;
    int border_num = 0;
    LinearLayout layout;

    //뒤로가기 변수
    int intentNum=0;

// 얼굴감지 변수

    File caseFile;
    CascadeClassifier faceDetector;

    private ExecutorService cameraExecutor;

    private ProcessCameraProvider cameraProvider;
    private ImageAnalysis imageAnalysis;

    private boolean isCameraRunning = false;

    private Mat mat;
    FrameLayout frameLayout;


    int resWidth;

    int maxWidth = 0;
    double distanceCM ;

    private static MainActivity instance; // 전역 변수로 MainActivity 인스턴스 선언
    private PolylineOverlay previousPolylineOverlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        인터넷 연결상태
//
//        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        if (connectivityManager != null)
//
//        {
//            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//
//            if (networkInfo != null && networkInfo.isConnected()) {
//                Log.d("연결", "됨");
//                // 인터넷이 연결된 상태입니다.
//            } else {
//                Log.d("연결", "안됨");
//
//                // 인터넷이 연결되지 않은 상태입니다.
//            }
//        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        instance = this;


        layout=findViewById(R.id.layout);

        frameLayout = findViewById(R.id.frame_layout);


        Intent intent = getIntent();
        c_num = intent.getIntExtra("c_num",0);
        if(c_num == 0) {
            Intent POIintent = new Intent(MainActivity.this, POIActivity.class);

            startActivity(POIintent);

        }


        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, baseCallback);
        } else {
            try {
                baseCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        endName = intent.getStringExtra("POIName");
        endY = getIntent().getDoubleExtra("frontLat", 1000);
        endX = getIntent().getDoubleExtra("frontLon", 1000);
        stop =1;

        locationUtils = new LocationUtils(this);

        Log.d("location","내위치 : "+ startX+ ", "+startY);



        if(!(endX==1000&&endY==1000)){
            intentNum=1;
            PedestrianRouteFinder routeFinder = new PedestrianRouteFinder(startX, startY, startName, endX, endY, endName, this);
            routeFinder.execute();

        }



        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

    }

    public static MainActivity getInstance() {
        return instance;
    }


    @Override
    public void onRouteFound(List<LatLng> find_path) {
        // 도보 경로를 찾았을 때 처리
        Toast.makeText(this, "경로를 찾았습니다.", Toast.LENGTH_SHORT).show();

        latLngs=find_path;
        if (path != null) {
            path.setMap(null);
            path = null;
        }


        path = new PathOverlay();
        path.setCoords(latLngs);
        path.setColor(Color.RED); // 선 색상 지정

        path.setWidth(15); // 선 두께 지정
        path.setOutlineWidth(0);

        path.setMap(naverMap);
        startTime = System.currentTimeMillis();
        Log.d("최초시작시간","시간" + startTime);

        startCamera();

    }

    @Override
    public void onBackPressed() {


        if(intentNum==1){
            stopCamera();
            finish();
            intentNum=0;
        }else{

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();


        }
    }


    @Override
    public void onRouteNotFound() {
        // 도보 경로를 찾지 못했을 때 처리
        Toast.makeText(this, "도보 경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();


    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        mapWidth= naverMap.getWidth();
        mapHeight = naverMap.getHeight();


        map_size = new PointF(mapWidth,mapHeight);

        map_size_me = new PointF(0f,mapHeight*0.45f);

        Log.d("location","내위치2 : "+ startX+ ", "+startY);
        LatLng myLatLng = new LatLng(startY, startX);
        naverMap.moveCamera(CameraUpdate.scrollBy(map_size_me));
        naverMap.setCameraPosition(new CameraPosition(myLatLng, 17));

        marker.setPosition(myLatLng);
        marker.setMap(naverMap);
        marker.setWidth(100);
        marker.setHeight(100);
        marker.setIcon(OverlayImage.fromResource(R.drawable.upicon1));
    }


    //지도 이동
    public void moveMap(){

        int route_num ;


        //
        if (startTime ==0) {
             return ;
        } else {
             route_num = inRoute.InRoute(startY, startX, latLngs, 100);
        }

        if(latLngs.size()<3 || inRoute.inArea(endX,endY,startX,startY,0.0001)){

            EndMission();
        }else if (route_num<3 && route_num >=0){
            removePath(startY,startX,10);

        }else if(route_num >= 0){

            latLngs= latLngs.subList(route_num, latLngs.size());
            removePath(startY,startX,10);

        }else{
            endTime = System.currentTimeMillis();
            long during = endTime-startTime;
            if (during>10000){

                PedestrianRouteFinder routeFinder = new PedestrianRouteFinder(startX, startY, startName, endX, endY, endName, this);
                routeFinder.execute();

            }
        }

        if (latLngs.size()>3) {
            double direction = angleCal.angle_cal(latLngs.get(0), latLngs.get(2));

            //현재 내 위치
            LatLng moveLatLng = new LatLng(startY, startX);
            marker.setPosition(moveLatLng);
            marker.setMap(naverMap);
            marker.setIcon(OverlayImage.fromResource(R.drawable.upicon1));

            if (naverMap != null) {
                CameraPosition camera1 = new CameraPosition(moveLatLng, 17, 0, direction);
                naverMap.setCameraPosition(camera1);
                naverMap.moveCamera(CameraUpdate.scrollBy(map_size_me));
            }
        }

    }

    //경로 삭제

    public void removePath(double latitude, double longitude,int num){


        double a= 0.00005;


        for(int i =0; i<num ; i++)
        {
            if(!latLngs.isEmpty()){
                double x_lat =latLngs.get(0).latitude;
                double x_long =latLngs.get(0).longitude;


                if(inRoute.inArea(latitude,longitude,x_lat,x_long,a)){
                    latLngs.remove(0);

                }
                else{
                    break;
                }

            }else{
                break;
            }
            if(latLngs.size()>1){
                path.setCoords(latLngs);
            }
//


        }
        startTime = System.currentTimeMillis();


    }


    //내 위치 받기

    // 위치 정보를 수신하면 호출되는 메서드
    public void onLocationReceived(double latitude, double longitude) {

        startX = longitude;
        startY = latitude;
        if(end_num == 1)
        {
            EndMission();
        }

    }

// 경로 찾는 도중과 아닐 떄
    public void useRoute(double latitude, double longitude){


        startX = longitude;
        startY = latitude;

        if(stop==1){
            moveMap();
        }else{
            EndMission();

        }
    }

    public void EndMission(){

        if(stop==1) {
            Toast.makeText(this, "목적지 부근에 도착했습니다. ", Toast.LENGTH_SHORT).show();

            stop=0;
            Log.d("도착", "완료");
            super.onBackPressed();
        }
//        LatLng moveLatLng = new LatLng(startY, startX);
//        marker.setPosition(moveLatLng);
//        marker.setMap(naverMap);
//        marker.setIcon(OverlayImage.fromResource(R.drawable.upicon1));
//
//
//        CameraPosition camera1 = new CameraPosition(moveLatLng, 17, 0,0);
//        naverMap.setCameraPosition(camera1);
//
//        map_size_me = new PointF(0f, 0f);
//        naverMap.moveCamera(CameraUpdate.scrollBy(map_size_me));
//
//
//        if (path != null) {
//            path.setMap(null);
//            path = null;
//        }
    }


// 얼굴 감지 부분


    public void startCameraAnalysis() {

        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {

                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);

                // 이미지 데이터를 Mat으로 변환합니다.
                resWidth=image.getWidth();
                mat = new Mat(image.getHeight(), resWidth, CvType.CV_8UC1);
                mat.put(0, 0, data);

                //detect Face
                MatOfRect facedetections = new MatOfRect();
                faceDetector.detectMultiScale(mat, facedetections);


                maxWidth = 0;

                for (Rect react : facedetections.toArray()) {
                    maxWidth = Math.max(maxWidth, react.width);

                }

                Log.d("최대width", " " + maxWidth);

                if(maxWidth==0){
                    updateView(1000);
                }else{

                    distanceCM = 6510/maxWidth;
                    updateView(distanceCM);

                }

                // 분석이 끝나면 반드시 image.close()를 호출하여 ImageProxy를 해제합니다.
                image.close();
            }
        });

        // ProcessCameraProvider를 사용하여 카메라를 바인딩하고 imageAnalysis를 사용하여 이미지 프로세싱을 시작합니다.
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void updateView(double cm) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Log.d("센치미터",""+cm);

                FrameLayout frameLayout = findViewById(R.id.frame_layout);
                if(border_num == 0) {
                    if (cm < 500) {
                        frameLayout.setBackgroundResource(R.drawable.border_red);
                        isRedBorder = true;  // 상태를 빨간색 테두리로 변경
                        border_num = 1;

                    } else {
                        frameLayout.setBackgroundResource(R.drawable.border);

                    }
                }

                if(border_num == 1){
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            border_num = 0;  // 2초 후에 b_num 값을 0으로 변경
                        }
                    }, 500);
                }


            }
        });
    }

//
//    private void setMapViewBorder(@ColorRes int colorRes) {
//        int borderColor = ContextCompat.getColor(this, colorRes);
//        View mapView = findViewById(R.id.map_fragment);
//        GradientDrawable drawable = new GradientDrawable();
//        drawable.setStroke(25, borderColor); // 테두리 두께와 색상 설정
//        mapView.setBackground(drawable);
//    }

    public void startCamera() {
        isCameraRunning = true;
        cameraExecutor = Executors.newSingleThreadExecutor();
        if(cameraProvider !=null){
            cameraProvider.unbindAll();

        }
        startCameraAnalysis();



    }

    public void stopCamera() {
        if (isCameraRunning==true){
            isCameraRunning = false;
            cameraExecutor.shutdownNow();

            cameraProvider.unbindAll();

        }


    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        locationUtils.stopLocationUpdates();
        stopCamera();




    }



    public BaseLoaderCallback baseCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) throws IOException {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    caseFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");

                    FileOutputStream fos = new FileOutputStream(caseFile);

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    fos.close();

                    faceDetector = new CascadeClassifier(caseFile.getAbsolutePath());
                    if (faceDetector.empty()) {
                        faceDetector = null;

                    } else {
                        cascadeDir.delete();
                    }


                }

                break;

                default:
                    super.onManagerConnected(status);
            }
//
        }
    };


}
