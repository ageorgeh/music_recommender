package com.example.music_recommender;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
{
    private SearchView trackSearchView;
    private SearchView artistSearchView;
    private ArrayList<SpotifyItem> trackSearch;
    private ArrayList<SpotifyItem> artistSearch;
    private static Context context;
    private SpotifyRequester spotifyRequester;
    public static Context getContext() {
        return context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);
        initSearchWidgets();
        spotifyRequester = new SpotifyRequester();
        String spotifyToken = spotifyRequester.getToken();
        initSearchButton();
    }
    private void initSearchButton () {
        Button clickButton = (Button) findViewById(R.id.button);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Spinner artistSpinner = (Spinner) findViewById(R.id.spinnerArtist);
                ArrayList<SpotifyItem> artists = new ArrayList<SpotifyItem>();
                artists.add((SpotifyArtist)artistSpinner.getSelectedItem());

                Spinner trackSpinner = (Spinner) findViewById(R.id.spinnerTrack);
                ArrayList<SpotifyItem> tracks = new ArrayList<SpotifyItem>();
                tracks.add((SpotifyArtist)artistSpinner.getSelectedItem());

                ArrayList<SpotifyItem> recommendations = spotifyRequester.recommend(artists, tracks, new ArrayList<String>(Arrays.asList("rock", "pop")));
                Log.d("DEBUGGING", TextUtils.join(",", recommendations));

                Intent showDetail = new Intent(getApplicationContext(), RecommendationActivity.class);
                showDetail.putExtra("recommendations",recommendations);
                startActivity(showDetail);
            }
        });
    }
    private void initSearchWidgets() {
        trackSearchView = (SearchView) findViewById(R.id.TrackSearchView);
        trackSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                trackSearch = spotifyRequester.search(s, new SearchType[]{SearchType.track});
                Log.d("DEBUGGING", TextUtils.join(",", trackSearch));
                Spinner spinnerTrack =findViewById(R.id.spinnerTrack);

                SpotifyItemAdapter adapter = new SpotifyItemAdapter(getApplicationContext(), 0, trackSearch);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                spinnerTrack.setAdapter(adapter);

                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {

                return false;
            }
        });
        artistSearchView = (SearchView) findViewById(R.id.ArtistSearchView);
        artistSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                artistSearch = spotifyRequester.search(s, new SearchType[]{SearchType.artist});
                Log.d("DEBUGGING", TextUtils.join(",", artistSearch));
                Spinner spinnerLanguages =findViewById(R.id.spinnerArtist);

                SpotifyItemAdapter adapter = new SpotifyItemAdapter(getApplicationContext(), 0, artistSearch);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                spinnerLanguages.setAdapter(adapter);

                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {

                return false;
            }
        });
    }
}

