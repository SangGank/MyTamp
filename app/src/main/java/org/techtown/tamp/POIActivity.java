package org.techtown.tamp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class POIActivity extends AppCompatActivity {

        private EditText searchEditText;
        private Button searchButton;
        private ListView resultListView;
        private ArrayList<String> resultList;
        private ArrayAdapter<String> resultAdapter;

        private MainActivity mainActivity; // MainActivity 참조

        private static final long BACK_PRESS_DURATION = 2000; // 2초
        private long backPressTime = 0;

        private static final String API_KEY = "BsLq7qWTpya7LgfQnO8RnWYhgrzQHtA9UFiaU5pf"; // 여기에 본인의 TMap API KEY를 입력하세요.

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_poiactivity);

            mainActivity = MainActivity.getInstance();


            searchEditText = findViewById(R.id.search_edit_text);
            searchButton = findViewById(R.id.search_button);
            resultListView = findViewById(R.id.poi_list);

            resultList = new ArrayList<>();
            resultAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, resultList);
            resultListView.setAdapter(resultAdapter);

            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String query = searchEditText.getText().toString();
                    if (!TextUtils.isEmpty(query)) {
                        searchPOI(query);
                    }
                }
            });
        }

        private void searchPOI(String query) {
            String encodedSearchKeyword = encodeURL(query);

            String url = "https://apis.openapi.sk.com/tmap/pois?version=1&searchKeyword=" + encodedSearchKeyword +
                    "&appKey=" + API_KEY;

            Log.d("주소",""+url);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {

                                JSONObject poiInfo = response.getJSONObject("searchPoiInfo").getJSONObject("pois");

                                JSONArray poiArray = poiInfo.getJSONArray("poi");

                                resultList.clear();


                                for (int i = 0;i < Math.min(poiArray.length(), 10); i++) {
                                    JSONObject poi = poiArray.getJSONObject(i);
                                    String name = poi.getString("name");
                                    resultList.add(name);
                                }

                                resultAdapter.notifyDataSetChanged();
                                // 검색 결과 리스트 항목 클릭 시 frontLat, frontLon 값 추출 후 MainActivity로 이동
                                resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        try {
                                            JSONObject poi = poiArray.getJSONObject(i);
                                            double frontLat = poi.getDouble("frontLat");
                                            double frontLon = poi.getDouble("frontLon");
                                            String name = poi.getString("name"); // 선택된 POI의 이름(name) 가져오기

                                            Intent intent = new Intent(POIActivity.this, MainActivity.class);

                                            intent.putExtra("frontLat", frontLat);
                                            intent.putExtra("frontLon", frontLon);
                                            intent.putExtra("POIName", name);
                                            intent.putExtra("c_num", 1);
                                            startActivity(intent);
                                            finish();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(POIActivity.this, "검색에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });

            Volley.newRequestQueue(this).add(request);
        }

    @Override
    public void onBackPressed() {

        long currentTime = System.currentTimeMillis();

                // 뒤로 가기 버튼을 두 번 연속해서 누른 경우
        if (currentTime - backPressTime <= BACK_PRESS_DURATION) {

            finishAffinity();
        } else {
            Toast.makeText(this, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            backPressTime = currentTime;
        }
    }


    public String encodeURL(String url) {
        try {
            String encodedURL = URLEncoder.encode(url, "UTF-8");
            return encodedURL;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

}
