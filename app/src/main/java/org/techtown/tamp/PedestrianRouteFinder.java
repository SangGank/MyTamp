package org.techtown.tamp;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.naver.maps.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PedestrianRouteFinder extends AsyncTask<Void, Void, List<LatLng>> {

    private static final String TAG = "PedestrianRouteFinder";

    private static final String TMAP_API_KEY = "BsLq7qWTpya7LgfQnO8RnWYhgrzQHtA9UFiaU5pf";
    private static final String TMAP_API_URL = "https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&format=json";

    private final double startX;
    private final double startY;
    private final String startName;
    private final double endX;
    private final double endY;
    private final String endName;

    List<LatLng> latLngs = new ArrayList<>();

    AngleCal myAngle = new AngleCal();

    private final OnRouteFoundListener listener;

    public PedestrianRouteFinder(double startX, double startY, String startName,
                                 double endX, double endY, String endName,
                                 OnRouteFoundListener listener) {
        this.startX = startX;
        this.startY = startY;
        this.startName = startName;
        this.endX = endX;
        this.endY = endY;
        this.endName = endName;
        this.listener = listener;

    }




    @Override
    protected List<LatLng> doInBackground(Void... voids) {
        OkHttpClient client = new OkHttpClient();


        Log.d("location","내위치2 : "+ startX+ ", "+startY);
        String url = TMAP_API_URL + "&appKey=" + TMAP_API_KEY +
                "&startX=" + startX + "&startY=" + startY + "&startName=" + startName +
                "&endX=" + endX + "&endY=" + endY + "&endName=" + endName;

        Request request = new Request.Builder()
                .url(url)
                .build();


        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new Exception("Unexpected response code: " + response.code());
            }

            JsonObject jsonResponse = JsonParser.parseString(response.body().string()).getAsJsonObject();

//            Log.d(TAG, jsonResponse.toString());

            JsonArray features = jsonResponse.getAsJsonArray("features");


            JsonArray latLngList = new JsonArray();

            JsonPrimitive linString = new JsonPrimitive("LineString");


            for (int j = 0; j < features.size(); j++) {
                try {
//
                    JsonObject geometry = features.get(j).getAsJsonObject().getAsJsonObject("geometry");


                    if (geometry.get("type").equals(linString)) {
//
                        JsonArray coordinates = geometry.getAsJsonArray("coordinates");
                        latLngList.addAll(coordinates);

                        DecimalFormat df = new DecimalFormat("0.00000");

                        // coordinates를 사용하여 지도에 경로 표시 등을 구현할 수 있습니다.
                        double distance_interval = 1;
                        int path_leng=coordinates.size();

                        //전체경로
                        for (int i = 0; i <path_leng; i++) {
                            JsonArray coords = coordinates.get(i).getAsJsonArray();
                            LatLng latLng = new LatLng(Double.parseDouble(df.format(coords.get(1).getAsDouble())), Double.parseDouble(df.format(coords.get(0).getAsDouble())));


                            try {
                                LatLng latLng_at = latLngs.get(latLngs.size()-1);


                                double angle = myAngle.angle_cal(latLng_at, latLng);
                                double distance = latLng_at.distanceTo(latLng);
                                if (distance > distance_interval) {
                                    int numNewPoints = (int) (distance / distance_interval);


                                    double bearing = Math.toRadians(angle);
                                    double step = distance / numNewPoints;
                                    for (int k = 1; k < numNewPoints; k++) {
                                        LatLng newLatLng = latLng_at.offset(step*k * Math.cos(bearing),step*k * Math.sin(bearing));
                                        latLngs.add(newLatLng);
                                    }

                                }


                            }catch (Exception e){

                            }

                            latLngs.add(latLng);

                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }

            }

//

            return latLngs;

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON response", e);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching route from TMap API", e);
        }

        return null;
    }




    @Override
    protected void onPostExecute(List<LatLng> latLngs) {

//        List<List<Integer>> latLngList = new ArrayList<>();

        if(latLngs!=null){
            Log.d("경로 " ,"찾았습니다");
            listener.onRouteFound(latLngs);

        }
         else {
            Log.d("경로 " ,"못 찾았습니다");

            listener.onRouteNotFound();
        }

    }

    public interface OnRouteFoundListener {

        void onRouteFound(List<LatLng> latLngs);
        void onRouteNotFound();
    }
}
