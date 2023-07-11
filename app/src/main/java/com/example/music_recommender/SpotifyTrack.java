package com.example.music_recommender;

import androidx.annotation.NonNull;

public class SpotifyTrack extends SpotifyItem{
    private SpotifyArtist artist = null;
    private String previewURL;
    public void setArtist(SpotifyArtist artist) {
        this.artist = artist;
    }
    public String getPreviewURL() {
        return previewURL;
    }

    public SpotifyTrack(String id, String name, SpotifyArtist artist) {
        super(id, name);
        this.artist = artist;
    }
    public SpotifyTrack(String id, String name, SpotifyArtist artist, String imageURL, String previewURL) {
        super(id, name, imageURL);
        this.previewURL = previewURL;
        this.artist = artist;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " - " + artist;
    }
}
