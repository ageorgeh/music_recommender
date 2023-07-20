package com.example.music_recommender;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ImageTester {

    Bitmap image;

    public ImageTester(Bitmap image) {
        this.image = image;

    }


    public float getMostCommon(){
        int width = image.getWidth();
        int height = image.getHeight();
        float numPixels = width*height;
        float average = 0;
        HashMap<Color, Integer> dict = new HashMap<>();

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                Color pixel = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    pixel = image.getColor(x,y);
                }
                if (!addToMap(dict, pixel)) {
                    dict.put(pixel, -1);
                }
            }
        }
        Log.d("DEBUGGING", "map: " + dict);
        Map<Color, Integer> result = dict.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a,b)->b, LinkedHashMap::new));

        for (Map.Entry<Color, Integer> entry : result.entrySet()) {
            Color key = entry.getKey();
            Integer value = entry.getValue();
            Log.d("DEBUGGING", "Colors : " + key + ", " +value);
        }

        return average;
    }

    private boolean addToMap(HashMap<Color, Integer> dict, Color pixel) {
        for (Map.Entry<Color, Integer> entry : dict.entrySet()) {
            Color key = entry.getKey();
            Integer value = entry.getValue();
            if (distanceSquared(pixel, key) < 70E-8) {
                dict.put(key, value-1);
                return true;
            }
        }
        return false;
    }

    private double distanceSquared(Color a, Color b) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            float deltaR = a.red() - b.red();
            float deltaG = a.green() - b.green();
            float deltaB = a.blue() - b.blue();
            float deltaAlpha = a.alpha() - b.alpha();
            double rgbDistanceSquared = (deltaR * deltaR + deltaG * deltaG + deltaB * deltaB) / 3.0;
            //Log.d("DEBUGGING", "number: " + deltaAlpha * deltaAlpha / 2.0 + rgbDistanceSquared * a.alpha() * b.alpha() / (255 * 255));
            return deltaAlpha * deltaAlpha / 2.0 + rgbDistanceSquared * a.alpha() * b.alpha() / (255 * 255);
        } else {
            return 0;
        }
    }


}
