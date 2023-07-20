package com.example.music_recommender;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

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
        spotifyAuthSetup();
        initSearchButton();
        images();
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
    private void spotifyAuthSetup() {
        int REQUEST_CODE = 1337;
        String REDIRECT_URI = "https://music_recommender/callback";

        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder("70b117a5ea7246a9ac6836458e9124f9", AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        int REQUEST_CODE = 1337;
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    spotifyRequester = new SpotifyRequester(response.getAccessToken());
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }
    private void images() {
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.album_cover);
        ImageTester imageTester = new ImageTester(getResizedBitmap(image, 300));
        Log.d("DEBUGGING", "most common: " + imageTester.getMostCommon());

        TextView v = (TextView) findViewById(R.id.testColor);
        v.setBackground( new DrawableGradient(new int[] { 0xffff6347, 0xffe580ff }, 0).SetTransparency(10));

    }

    public class DrawableGradient extends GradientDrawable {
        DrawableGradient(int[] colors, int cornerRadius) {
            super(Orientation.LEFT_RIGHT, colors);

            try {
                this.setShape(GradientDrawable.RECTANGLE);
                this.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                this.setCornerRadius(cornerRadius);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public DrawableGradient SetTransparency(int transparencyPercent) {
            this.setAlpha(255 - ((255 * transparencyPercent) / 100));

            return this;
        }
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}

