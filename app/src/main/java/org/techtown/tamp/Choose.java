package org.techtown.tamp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class Choose extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        Intent intent = getIntent();
        String poisStr = intent.getStringExtra("pois");
        JsonArray pois = new JsonParser().parse(poisStr).getAsJsonArray();

        ArrayList<String> nameList = new ArrayList<>();
        for(int i = 0; i < pois.size(); i++){
            JsonObject poi = pois.get(i).getAsJsonObject();
            String name = poi.get("name").getAsString();
            nameList.add(name);
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nameList);
        ListView listView = (ListView)findViewById(R.id.pois_list);
        listView.setAdapter(adapter);
    }
}