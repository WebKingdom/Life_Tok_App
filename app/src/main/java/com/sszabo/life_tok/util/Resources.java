package com.sszabo.life_tok.util;

import android.os.SystemClock;
import android.provider.SyncStateContract;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.sszabo.life_tok.model.Event;
import com.sszabo.life_tok.model.MediaObject;

import java.util.Date;

public class Resources {
    public static final Event[] EVENT_OBJECTS = {
            new Event("Event 1 name",
                    "Event 1 description",
                    "gs://life-tok-app.appspot.com/Call of Duty  Black Ops Cold War 2021.05.10 - 13.49.56.02.DVR.mp4",
                    1,
                    new GeoPoint(0, 0),
                    new Timestamp(100, 0)),
            new Event("Event 2 name",
                    "Event 2 description",
                    "gs://life-tok-app.appspot.com/Call of Duty  Black Ops Cold War 2021.05.10 - 13.49.56.02.DVR.mp4",
                    1,
                    new GeoPoint(10, 10),
                    new Timestamp(100, 0)),
    };

    public static final String KEY_FILE_PATH = "FILE_PATH";
    public static final String KEY_IS_PICTURE = "IS_PICTURE";

    private static final String KEY_MAP_VIEW = "MAP_VIEW";
    private static final String KEY_GOOGLE_MAP = "GOOGLE_MAP";
    private static final String KEY_FUSED_LOCATION = "FUSED_LOCATION";
    private static final String KEY_GEOCODER = "GEOCODER";
}
