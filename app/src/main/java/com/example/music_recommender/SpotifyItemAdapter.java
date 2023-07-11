package com.example.music_recommender;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SpotifyItemAdapter extends ArrayAdapter<SpotifyItem> {
    public SpotifyItemAdapter(Context context, int resource, List<SpotifyItem> spotifyItemList) {
        super(context,resource,spotifyItemList);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SpotifyItem spotifyItem = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spotify_item_search_cell, parent, false);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.itemName);

        tv.setText(spotifyItem.getName());

        return convertView;
    }
}
