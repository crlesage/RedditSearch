package com.lesage.chris.redditfetch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends AppCompatActivity implements URLFetch.Callback {
    private static final String TAG = "Testing: ";
    static public String AppName = "RedFetch";
    public static ProgressBar progressBar;
    // Reddit json search will return up to 100 records, 25 by default
    // Can only display a subset of those returned
    protected final int maxRedditRecords = 100;
    protected DynamicAdapter redditRecordAdapter;
    protected SwipeDetector swipeDetector = new SwipeDetector();

    protected final String SUB_REDDIT = "aww";
    protected static ListView listRecords;
    protected static final int ONE_POST_INFO = 1;

    public static boolean resetList = false;
    //public static boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // XXX You need to setContentView
        setContentView(R.layout.activity_main);
        redditRecordAdapter = new DynamicAdapter(this);
        //progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/4th of the available memory for this memory cache.
        BitmapCache.cacheSize = maxMemory / 4;
        final Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        BitmapCache.maxW = size.x;
        BitmapCache.maxH = size.y;

        // XXX Much initialiation remains.
        final EditText editText = (EditText) findViewById(R.id.searchTerm);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
            // TODO Auto-generated method stub
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //Checks if there is connectivity to the internet before fetching
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    String text = editText.getText().toString();
                    text = checkText(text);
                    if(redditRecordAdapter.getCount() >= 1) {
                        //Reset the list view
                        resetListView();
                        resetList = true;
                    }
                    try {
                        doFetch(text);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    /* Hide keyboard after enter:
                     * http://stackoverflow.com/questions/7864671/how-to-hide-keyboard-on-enter-key */
                    InputMethodManager imm =
                            (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                } else {
                    Toast.makeText(getApplicationContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                break;
            //case R.id.action_turn_on_off_loading:
                //turnOffOnLoading();
                //break;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Turns on/off the loading progress bars */
//    private void turnOffOnLoading() {
//        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//        if(isLoading) {
//            progressBar.setVisibility(View.GONE);
//            isLoading = false;
//        }
//        else{
//            progressBar.setVisibility(View.VISIBLE);
//            isLoading = true;
//        }
//    }

    /* Makes sure the input text is valid */
    //http://stackoverflow.com/questions/24229660/replacing-spaces-with-20-in-java
    private String checkText(String text) {
        String encodedURL = null;
        try{
            encodedURL = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException ignored){
            //Nothing
        }
        return encodedURL;
    }

    /* The fetch from the URL of reddit (subreddit is aww) with the text searched */
    private void doFetch(String text) throws JSONException {
        URL url = null;
        try{
            //url = new URL("http://www.reddit.com/r/aww/search.json?raw_json=1&q=cat&sort=hot");
            url = new URL("https://www.reddit.com/r/" + SUB_REDDIT + "/search.json?q=" + text + "&sort=hot&limit=" + maxRedditRecords);
        } catch (Exception e){
            e.printStackTrace();
        }
        URLFetch fetch = new URLFetch(this, url, true);
    }

    @Override
    //This is where the string that is the returned and formed to a JSON object
    public void fetchComplete(String result) {
        parseAndStoreResult(result);
    }
    @Override
    public void fetchStart(){
        //Nothing
    }
    @Override
    public void fetchCancel(String url){
        //Nothing
    }

    /* Reads in the String formed by the URL, converts to JSONobject,
     * parses through the json, finding the title, thumbnail, and image.
      * Then adds the values to a redditRecord. */
    public void parseAndStoreResult(String result) {
        try {
            JSONObject jO = new JSONObject(result);
            int jsonIndex = 0;
            if (jO.isNull("data")) {
                return;
            }
            jO = jO.getJSONObject("data");
            JSONArray jA = jO.getJSONArray("children");
            while (jsonIndex < jA.length()) {
                jO = jA.getJSONObject(jsonIndex);
                if (jO.isNull("data")) {
                    jsonIndex++;
                    continue;
                }
                jO = jO.getJSONObject("data");
                // Make sure there is a thumbnail and an image url
                // Most thumbnails I see are jpg or png, but some urls are to imagur, which
                // contain html
                if (jO.isNull("thumbnail")
                        || (!jO.getString("thumbnail").endsWith("jpg")
                        && !jO.getString("thumbnail").endsWith("png"))
                        || jO.isNull("url")
                        || (!jO.getString("url").endsWith("jpg") && !jO.getString("url").endsWith("png"))
                        || jO.isNull("title")) {
                    jsonIndex++;
                    continue;
                }
                String title = jO.getString("title");
                String thumbnailURL = jO.getString("thumbnail");
                String imageURL = jO.getString("url");
                populateListView(title, thumbnailURL, imageURL);
                // Update loop index
                jsonIndex++;
            }
        }catch(Exception e){
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        if(redditRecordAdapter.isEmpty()){
            Toast.makeText(getApplicationContext(), "No results, try to modify search terms\n" + "                Please search again.", Toast.LENGTH_SHORT).show();
        }
        displayListView();
    }

    /* Populates the entire listview with redditRecords */
    public void populateListView (String title, String thumbnailURL, String imageURL) throws MalformedURLException {
        URL tnURL = new URL(thumbnailURL);
        URL iURL = new URL(imageURL);
        //ProgressBar pb = new ProgressBar(getApplicationContext());
        //pb = (ProgressBar) findViewById(R.id.progressBar);
        //pb.setVisibility(View.VISIBLE);
        RedditRecord rr = new RedditRecord();
        rr.setTitle(title);
        rr.setThumbnailURL(tnURL);
        rr.setImageURL(iURL);
        //rr.setProgressBar(progressBar);
        redditRecordAdapter.addItem(rr);
        displayListView();
    }


    /* Display's the ListView on screen */
    public void displayListView(){
        listRecords = (ListView) findViewById(R.id.listOfPosts);
        listRecords.setAdapter(redditRecordAdapter);
        startSwipeListener();
    }

    /* Resets a new listview */
    public void resetListView(){
        redditRecordAdapter.removeAllItems();
        listRecords = (ListView) findViewById(R.id.listOfPosts);
        redditRecordAdapter.notifyDataSetChanged();
        listRecords.setAdapter(redditRecordAdapter);

    }

    /* Listener for swiping and clicks on the listview of posts */
    public void startSwipeListener(){
        listRecords = (ListView) findViewById(R.id.listOfPosts);
        listRecords.setOnTouchListener(swipeDetector);
        listRecords.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (swipeDetector.swipeDetected()) {
                    if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
                        redditRecordAdapter.removeItem(position);
                    } else if (swipeDetector.getAction() == SwipeDetector.Action.Click) {
                        // Starting Intent and going to onePost page
                        RedditRecord rr = (RedditRecord) listRecords.getItemAtPosition(position);
                        Intent intentPreferences = new Intent(getApplicationContext(), OnePost.class);
                        //Bundle infoBundle = new Bundle();
                        Log.d(TAG, rr.getTitle());
                        Log.d(TAG, rr.getImageURL().toString());
                        intentPreferences.putExtra("title", rr.getTitle());
                        intentPreferences.putExtra("thumbnail", rr.getThumbnailURL().toString());
                        intentPreferences.putExtra("image", rr.getImageURL().toString());
                        startActivityForResult(intentPreferences, ONE_POST_INFO);
                    }
                }
            }
        });
    }

    /* Return from the clicked post screen */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1: //OnePost
                break;
        }
    }

    // Your reddit searchers should be constructed along these lines.
        // Consruct a string called ABC that substitutes your search term for the %s
        // ABC = "/r/aww/search.json?q=%s&sort=hot&limit=100"
        // URL searchURL = new URL("https", "www.reddit.com", ABC);
        // Also see
        // https://www.reddit.com/dev/api#GET_search
}