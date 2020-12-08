package com.example.weatherapp;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Api {

    public static final String APP_ID = "b46c33aaad6d492032f937033d90e493";

    public static Location current_location = null;
    public static String convertUnixToDate(long dt){
        Date date = new Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, EEE, dd-MM-yyyy");
        String formatted = sdf.format(date);
        return formatted;
    }

    public static String covertUnixToHour(long sunrise) {
        Date date = new Date(sunrise*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String formatted = sdf.format(date);
        return formatted;
    }
}
