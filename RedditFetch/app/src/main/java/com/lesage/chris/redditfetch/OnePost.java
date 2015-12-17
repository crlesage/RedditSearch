package com.lesage.chris.redditfetch;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import java.net.URL;


/**
 * Created by Chris on 10/11/2015.
 */
public class OnePost extends AppCompatActivity implements URLFetch.Callback{
    public String image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one_post_layout);

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Post Info");
        }

        TextView tv = (TextView) findViewById(R.id.titleText);

        Intent activityThatCalled = getIntent();

        String title = activityThatCalled.getExtras().getString("title");
        image = activityThatCalled.getExtras().getString("image");

        tv.setText(title);
        //Test if image is already in cache (aka they already looked at it)
        Bitmap bm = BitmapCache.getInstance().getBitmap(image);
        if(bm == null) {
            try {
                urlFetch(image);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            ImageView theImageView = (ImageView) findViewById(R.id.imagePost);
            theImageView.setImageBitmap(bm);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarOnePost);
            progressBar.setVisibility(View.GONE);
        }
    }

    public void urlFetch (String url) throws JSONException {
        URL urlImage = null;
        try{
            urlImage = new URL(url);
        } catch (Exception e){
            e.printStackTrace();
        }
        URLFetch fetch2 = new URLFetch((URLFetch.Callback) this, urlImage, true);
    }

    @Override
    //This is where the string that is the returned and formed to a JSON object
    public void fetchComplete(String result) {
        //Nothing, Url image is added to cache in background
        Bitmap bitmap = BitmapCache.getInstance().getBitmap(image);
        ImageView theImageView = (ImageView) findViewById(R.id.imagePost);
        theImageView.setImageBitmap(bitmap);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarOnePost);
        progressBar.setVisibility(View.GONE);

    }
    @Override
    public void fetchStart(){
        //Create a progress bar to show loading
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarOnePost);
        progressBar.setVisibility(View.VISIBLE);
    }
    @Override
    public void fetchCancel(String url){
        //Nothing
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
