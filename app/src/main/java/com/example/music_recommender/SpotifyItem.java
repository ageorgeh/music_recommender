package com.example.music_recommender;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class SpotifyItem implements Serializable {

    private String spotifyID;
    private String name;
    private String imageURL;


    public SpotifyItem(String id, String name, String imageURL) {
        this.spotifyID = id;
        this.name = name;
        this.imageURL = imageURL;
    }

    public SpotifyItem(String id, String name) {
        this.spotifyID = id;
        this.name = name;
    }

    public String getId() {
        return spotifyID;
    }

    public void setId(String id) {
        this.spotifyID = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return imageURL;
    }


    @NonNull
    @Override
    public String toString() {

        return name;
    }
}
