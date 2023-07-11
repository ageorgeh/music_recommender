package com.example.music_recommender;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class SpotifyRecommendationAdapter extends ArrayAdapter<SpotifyItem> {
    public SpotifyRecommendationAdapter(Context context, int resource, List<SpotifyItem> spotifyItemList) {
        super(context,resource,spotifyItemList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SpotifyTrack spotifyItem = (SpotifyTrack) getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spotify_item_cell, parent, false);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.trackName);
        tv.setText(spotifyItem.toString());

        ImageView imageView = (ImageView) convertView.findViewById(R.id.trackCover);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            //Background work here
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(spotifyItem.getImage()).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            Bitmap finalMIcon1 = mIcon11;
            handler.post(() -> {
                //UI Thread work here
                imageView.setImageBitmap(finalMIcon1);

            });
        });

        Log.d("DEBUGGING", "media url: "+ spotifyItem.getPreviewURL());

        if (!Objects.equals(spotifyItem.getPreviewURL(), "null")) {

            setupMedia(convertView, spotifyItem, handler);
        }

        return convertView;
    }


    private void setupMedia(View convertView, SpotifyTrack spotifyItem, Handler handler) {
        ImageButton imageButton;
        imageButton = (ImageButton) convertView.findViewById(R.id.imageButton);
        imageButton.setVisibility(View.VISIBLE);
        imageButton.setBackgroundResource(android.R.drawable.ic_media_play);

        AtomicInteger mediaFileLengthInMilliseconds = new AtomicInteger();
        MediaPlayer mediaPlayer = new MediaPlayer();

        SeekBar seekBarProgress = (SeekBar)convertView.findViewById(R.id.seekBar);
        seekBarProgress.setVisibility(View.VISIBLE);
        seekBarProgress.setMax(99);
        seekBarProgress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v.getId() == R.id.seekBar){
                    /** Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/

                    SeekBar sb = (SeekBar)v;
                    int playPositionInMillisecconds = (mediaFileLengthInMilliseconds.get() / 100) * sb.getProgress();
                    mediaPlayer.seekTo(playPositionInMillisecconds);
                }
                return false;
            }
        });
        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                /** Method which updates the SeekBar secondary progress by current song loading from URL position*/
                seekBarProgress.setSecondaryProgress(percent);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                /** MediaPlayer onCompletion event handler. Method which calls then song playing is complete*/
                imageButton.setBackgroundResource(android.R.drawable.ic_media_play);
            }
        });

        ExecutorService musicExecutor = Executors.newSingleThreadExecutor();
        musicExecutor.execute(() -> {
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            try {
                mediaPlayer.setDataSource(spotifyItem.getPreviewURL()+".mp3");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                mediaPlayer.prepare(); // might take long! (for buffering, etc)
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            mediaFileLengthInMilliseconds.set(mediaPlayer.getDuration());
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    imageButton.setBackgroundResource(android.R.drawable.ic_media_pause); // TODO
                }else {
                    mediaPlayer.pause();
                    imageButton.setBackgroundResource(android.R.drawable.ic_media_play);
                }
                primarySeekBarProgressUpdater(mediaPlayer, seekBarProgress, mediaFileLengthInMilliseconds, handler);
            }
        });
    }
    private void primarySeekBarProgressUpdater(MediaPlayer mediaPlayer, SeekBar seekBarProgress, AtomicInteger mediaFileLengthInMilliseconds, Handler handler) {
        seekBarProgress.setProgress((int)(((float)mediaPlayer.getCurrentPosition()/mediaFileLengthInMilliseconds.get())*100)); // This math construction give a percentage of "was playing"/"song length"

            Runnable notification = new Runnable() {
                public void run() {
                    primarySeekBarProgressUpdater(mediaPlayer, seekBarProgress, mediaFileLengthInMilliseconds, handler);
                }
            };
            handler.postDelayed(notification,1000);
    }
}





