package com.example.runningtracker;

import android.net.Uri;

// contract for content provider
public class RunContract {

    public static final String AUTHORITY = "com.example.runningtracker.RunProvider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    public static final Uri RUN_URI = Uri.parse("content://"+AUTHORITY+"/run");

    //field names
    public static final String ID = "id";
    public static final String SPEED = "speed";
    public static final String DISTANCE = "distance";
    public static final String TIME = "time";
    public static final String DATE = "date";
    public static final String NAME = "name";
    public static final String TEMPERATURE = "temperature";
    public static final String RATING = "rating";

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/runs.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/runs.data.text";

}
