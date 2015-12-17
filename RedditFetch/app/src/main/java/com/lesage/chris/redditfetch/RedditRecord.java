package com.lesage.chris.redditfetch;

import java.net.URL;

// This is a simple data aggregation class that holds items of interest
// for a reddit post.  When you parse the reddit JSON, you will extract
// these items of interest and add records into your dynamic array adapter.
public class RedditRecord {
    public String title;
    public URL thumbnailURL;
    public URL imageURL;
    //public ProgressBar progressBar;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public URL getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(URL thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public URL getImageURL() {
        return imageURL;
    }

    public void setImageURL(URL imageURL) {
        this.imageURL = imageURL;
    }

//    public ProgressBar getProgressBar() {
//        return progressBar;
//    }
//
//    public void setProgressBar(ProgressBar progressBar) {
//        this.progressBar = progressBar;
//    }
//
//    public void progressBarOff(){
//        this.getProgressBar().setVisibility(View.GONE);
//    }
//
//    public void progressBarOn(){
//        this.getProgressBar().setVisibility(View.VISIBLE);
//    }
}

