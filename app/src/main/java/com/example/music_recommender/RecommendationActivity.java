package com.example.music_recommender;

import static com.example.music_recommender.MainActivity.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RecommendationActivity extends AppCompatActivity {

    private ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommendation_view);
        setupList();
    }

    private void setupList() {
        Intent previousIntent = getIntent();
        ArrayList<SpotifyItem> tracks = (ArrayList<SpotifyItem>) previousIntent.getSerializableExtra("recommendations");

        Log.d("DEBUGGING", "tracks " + TextUtils.join(",", tracks));

        listView = (ListView) findViewById(R.id.recommendation_list);

        SpotifyRecommendationAdapter adapter = new SpotifyRecommendationAdapter(getContext(), 0, tracks);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
