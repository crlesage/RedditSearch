package com.lesage.chris.redditfetch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;


import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

public class URLFetch implements RateLimit.RateLimitCallback {
    private static final String TAG = URLFetch.class.getSimpleName();

    public interface Callback {
        void fetchStart();
        void fetchComplete(String result) throws JSONException;
        void fetchCancel(String url);
    }

    protected static RateLimit rateLimit;
    protected Callback callback = null;
    protected URL url;
    protected AsyncDownloader asyncDownloader;

    public URLFetch(Callback callback, URL url, boolean front) throws JSONException {
        this.callback = callback;
        this.url = url;
        this.asyncDownloader = new AsyncDownloader();
        if(MainActivity.resetList){
            RateLimit.getInstance().clear();
            MainActivity.resetList = false;
        }
        if (front) {
            RateLimit.getInstance().addFront(this);
        } else {
            RateLimit.getInstance().add(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return url.equals(((URLFetch) obj).url);
    }

    public void rateLimitReady() throws JSONException {
        // XXX write me: execute asyncDownloader
        String result = null;
        try {
            result = this.asyncDownloader.execute(this.url).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        //fetchComplete returns the string 'key' to the bitmap or json
        this.callback.fetchComplete(result);
    }

    public static class AsyncDownloader extends AsyncTask<URL, Integer, String> {
        // XXX Write me
        // Got from http://stackoverflow.com/questions/14164128/cant-access-findviewbyid-in-asynctask
        // To get the view/context of the asynctask, so I am able to update them post execute
        @Override
        /* http://www.survivingwithandroid.com/2013/01/android-async-listview-jee-and-restful.html
         * Reference to this site for help with JSON reading from URL (while in background thread)
        */
        protected String doInBackground(URL... urls){
            InputStream in;
            String result = "";
            int responseCode = -1;
            try {
                URL url = urls[0];
                URLConnection urlConn = url.openConnection();
                if (!(urlConn instanceof HttpURLConnection)) {
                    throw new IOException("URL is not an Http URL");
                }
                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setRequestProperty("User-Agent", "android:edu.utexas.cs371m.crlesage.redfetch:v1.0 (by/u/crlesage)");
                httpConn.setRequestMethod("GET");
                //httpConn.setDoInput(true);
                httpConn.connect();
                responseCode = httpConn.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    in = httpConn.getInputStream();
                    if(url.toString().endsWith("jpg") || url.toString().endsWith("png")) {
                        Bitmap bitmap = BitmapFactory.decodeStream(in);
                        result = url.toString();
                        BitmapCache.getInstance().setBitmap(result, bitmap);
                    }
                    else {
                        try {
                            BufferedReader rd = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
                            String jsonText = readAll(rd);
                            return jsonText;
                        } finally {
                            in.close();
                        }
                    }
                    in.close();
                }
                httpConn.disconnect();
                return result;
            }
            catch (Throwable t){
                Log.d(TAG, t.toString());
                t.printStackTrace();
            }
            return result;
        }

        // Note:
        // At some point in this code you will open a network
        // connection.  That code will look something like this.
        //  urlConn = (HttpURLConnection) url.openConnection();
        // Once you open the connection, you MUST set the User-Agent,
        // which is an identifying string for your app.  This is what
        // you should use
        //  urlConn.setRequestProperty("User-Agent",
        // "android:edu.utexas.cs371m.YOURID.redfetch:v1.0 (by
        // /u/YOURREDDITID)");
        // In the above code you should substitute something identifying like your CS
        // username or eid for YOURID, and if you have a reddit ID, use
        // it for YOURREDDITID.  If not, use YOURID again.
        // If you don't set User-Agent properly, you will lose a lot of points.
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
