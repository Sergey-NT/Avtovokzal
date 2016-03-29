package com.www.avtovokzal.org;

import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.util.HashMap;

@ReportsCrashes(formUri = "http://www.avtovokzal.org/php/app/log/log.php")

public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();

    private RequestQueue mRequestQueue;

    private static AppController mInstance;

    // Google Analytics
    private static final String PROPERTY_ID = "UA-25056266-2";

    public enum TrackerName {
        APP_TRACKER, GLOBAL_TRACKER,
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    public AppController() {
        super();
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics
                    .newTracker(R.xml.app_tracker)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics
                    .newTracker(PROPERTY_ID) : analytics
                    .newTracker(R.xml.global_tracker);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        ACRA.init(this);
        FixNoClassDefFoundError81083();
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    private void FixNoClassDefFoundError81083() {
        try {
            Class.forName("android.os.AsyncTask");
        }
        catch(Throwable ignore) {}
    }
}