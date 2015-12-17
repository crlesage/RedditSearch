package com.lesage.chris.redditfetch;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;

import org.json.JSONException;

import java.util.concurrent.ConcurrentLinkedDeque;

// This is a Singleton class that makes sure the app as a whole does not
// fetch more than a URL every 2 seconds.  It is a bit fancier than the version
// I distributed in class because it will "save up" two seconds so that a 
// new request after 2 seconds of inactivity will immediately fetch.
public class RateLimit {
    public interface RateLimitCallback {
        void rateLimitReady() throws JSONException;
    }
    private RateLimit rateLimit = null;
    protected Handler handler;
    protected Runnable rateLimitRequest;
    protected final int rateLimitMillis = 2000; // 2 sec
    protected boolean okToRun;
    protected static ConcurrentLinkedDeque<RateLimitCallback> rateLimitCallbacks;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private RateLimit() {
        handler  = new Handler();
        rateLimitCallbacks = new ConcurrentLinkedDeque<>();
        rateLimitRequest = new Runnable() {
            @Override
            public void run() {
                okToRun = true;
                try {
                    runIfOk();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handler.postDelayed(this, rateLimitMillis);
            }
        };
        handler.postDelayed(rateLimitRequest, rateLimitMillis);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void runIfOk() throws JSONException {
        if( okToRun && !rateLimitCallbacks.isEmpty() ) {
            okToRun = false;
            RateLimitCallback rlc = rateLimitCallbacks.pop();
            rlc.rateLimitReady();
        }
    }

    // See https://en.wikipedia.org/wiki/Double-checked_locking
    // To understand this idiom
    private static class RateLimitHolder {
        public static final RateLimit rateLimit = new RateLimit();
    }

    public static RateLimit getInstance() {
       return RateLimitHolder.rateLimit;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void add(RateLimitCallback rlc) throws JSONException {
        if( !rateLimitCallbacks.contains( rlc ) ) {
            rateLimitCallbacks.add(rlc);
        }
        runIfOk();
    }
    // Add to front of queue, for important fetches
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void addFront(RateLimitCallback rlc) throws JSONException {
        if( !rateLimitCallbacks.contains( rlc )) {
            rateLimitCallbacks.push(rlc);
        }
        runIfOk();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void clear () throws JSONException {
        rateLimitCallbacks.clear();
        runIfOk();
    }
}

