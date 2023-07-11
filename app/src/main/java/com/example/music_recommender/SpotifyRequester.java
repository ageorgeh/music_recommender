package com.example.music_recommender;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SpotifyRequester {
    String token=null;
    public SpotifyRequester(String token) {
        this.token = token;
    }
    public String getToken() {
        return token;
    }
    public SpotifyRequester() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            //Background work here
            authRequest();
            executor.shutdown();
            handler.post(() -> {
                //UI Thread work here
            });
        });
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {

        }
    }
    public ArrayList<SpotifyItem> search(String q, SearchType[] types) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        AtomicReference<ArrayList<SpotifyItem>> spotifyItems = new AtomicReference<>(new ArrayList<SpotifyItem>());
        executor.execute(() -> {
            //Background work here
            spotifyItems.set(searchRequest(q, types));
            executor.shutdown();
            handler.post(() -> {
                //UI Thread work here
            });
        });
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {

        }
        return spotifyItems.get();
    }
    private ArrayList<SpotifyItem> searchRequest(String q, SearchType[] types) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create("", mediaType);
        String url = "https://api.spotify.com/v1/search?"
                +"q="+q.replace(" ", "%20")
                +"&type="+ TextUtils.join(",", types);

        Log.d("DEBUGGING", "URL: " + url);


        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Authorization", "Bearer " + token)
                .build();
        try {
            Response response = client.newCall(request).execute();
            JSONObject data_obj = new JSONObject(response.body().string());
            Log.d("DEBUGGING", data_obj.toString());
            return convertJSONtoItems(data_obj.getJSONObject(types[0].toString() + "s").
                     getJSONArray("items"), types[0]);

        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }
    private ArrayList<SpotifyItem> convertJSONtoItems(JSONArray items, SearchType type) {
        ArrayList<SpotifyItem> spotifyItems = new ArrayList<SpotifyItem>();
        for (int i=0;i<5;i++) {
            try {
                JSONObject item = items.getJSONObject(i);
                if (type == SearchType.track) {
                    spotifyItems.add(new SpotifyTrack(item.getString("id"),
                            item.getString("name"),
                            new SpotifyArtist(item.getJSONArray("artists").getJSONObject(0).getString("id"),
                                    item.getJSONArray("artists").getJSONObject(0).getString("name")),
                            item.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url"),
                            item.getString("preview_url")));

                } else {
                    spotifyItems.add(new SpotifyArtist(item.getString("id"),
                            item.getString("name")));
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return spotifyItems;
    }
    private void authRequest() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create("", mediaType);
        Request request = new Request.Builder()
                .url("https://accounts.spotify.com/api/token?grant_type=client_credentials")
                .method("POST", body)
                .addHeader("Authorization", "Basic ")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Cookie", "__Host-device_id=AQDRkzaFoZpTlWs86cTLvCNZ0w_6bROHhdsr8SPP2I1opYGhERWnAr__6s9UuznTmdtyz3JanUdjWJaMKKWhevbEm_kgeeis1-g; sp_tr=false")
                .build();
        try {
            Response response = client.newCall(request).execute();
            JSONObject data_obj = new JSONObject(response.body().string());
            token = data_obj.getString("access_token");
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<SpotifyItem> recommend(ArrayList<SpotifyItem> artists,
                                            ArrayList<SpotifyItem> tracks,
                                            ArrayList<String> genres) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        AtomicReference<ArrayList<SpotifyItem>> spotifyItems = new AtomicReference<>(new ArrayList<SpotifyItem>());
        executor.execute(() -> {
            //Background work here
            spotifyItems.set(recommendRequest(artists, tracks, genres));
            executor.shutdown();
            handler.post(() -> {
                //UI Thread work here

            });
        });
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {

        }
        return spotifyItems.get();
    }

    private ArrayList<SpotifyItem> recommendRequest(ArrayList<SpotifyItem> artists,
                                                    ArrayList<SpotifyItem> tracks,
                                                    ArrayList<String> genres) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create("", mediaType);
        String url = "https://api.spotify.com/v1/recommendations?"
            + "seed_artists="+TextUtils.join(",", convertToIDs(artists))
            + "&seed_genres="+TextUtils.join(",", genres)
            + "&seed_tracks="+TextUtils.join(",", convertToIDs(tracks));

        Log.d("DEBUGGING", "Recommender url: "+url);

        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Authorization", "Bearer " + token)
                .build();
        try{
            Response response = client.newCall(request).execute();
            JSONObject data_obj = new JSONObject(response.body().string());
            Log.d("DEBUGGING", "Recommender response: "+ data_obj);
            return convertJSONtoItems(data_obj.getJSONArray("tracks"), SearchType.track);
        } catch (IOException ignored) {

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private ArrayList<String> convertToIDs (ArrayList<SpotifyItem> items) {
        ArrayList<String> strings = new ArrayList<>();
        for (int i=0; i<items.size(); i++) {
            strings.add(items.get(i).getId());
        }
        return strings;
    }
}
