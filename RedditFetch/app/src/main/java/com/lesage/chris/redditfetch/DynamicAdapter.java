package com.lesage.chris.redditfetch;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.json.JSONException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Chris on 10/9/2015.
 */

/* Extension of the DynamicAdaptor class from the class example. */
public class DynamicAdapter extends BaseAdapter implements URLFetch.Callback{
    private ArrayList<RedditRecord> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private ImageView imageView;
    private ProgressBar progressBar;
    private View progressView;

    public DynamicAdapter(Context mContext) {
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(final RedditRecord item) {
        mData.add(item);
        notifyDataSetChanged();
    }
    public void removeItem(int position) {
        mData.remove(position);
        notifyDataSetChanged();
    }

    public void removeAllItems(){
        mData.clear();
        notifyDataSetChanged();
    }

    public boolean isEmpty(){
        return mData.isEmpty();
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).hashCode();
    }
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public RedditRecord getItem(int position) {
        return mData.get(position);
    }

    public void urlFetch (int position) throws JSONException {
        URL urlThumbnail = null;
        try{
            urlThumbnail = new URL(getItem(position).getThumbnailURL().toString());
        } catch (Exception e){
            e.printStackTrace();
        }
        URLFetch fetch = new URLFetch((URLFetch.Callback) this, urlThumbnail, false);
    }

    @Override
    //This is where the string that is the returned and formed to a JSON object
    public void fetchComplete(String result) {
        //Nothing, Url image is added to cache in background
        notifyDataSetChanged();
        progressBar = (ProgressBar) progressView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        //MainActivity.progressBar.setVisibility(View.GONE);
    }
    @Override
    public void fetchStart(){
        //Create a progress bar to show loading
        //MainActivity.progressBar.setVisibility(View.VISIBLE);
        progressBar = (ProgressBar) progressView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
    }
    @Override
    public void fetchCancel(String url){
        //Nothing
        //MainActivity.progressBar.setVisibility(View.GONE);
    }

    protected LinearLayout makeView(LinearLayout theView, int position, ViewGroup parent) throws JSONException {
        //handler.post(render);
        // Setting the title
        String title = (String) (getItem(position).getTitle());
        TextView theTextView = (TextView) theView.findViewById(R.id.picTextRowText);
        theTextView.setText(title);

        // Setting the thumbnail
        urlFetch(position);
        Bitmap bitmap = BitmapCache.getInstance().getBitmap(getItem(position).getThumbnailURL().toString());
        imageView = (ImageView) theView.findViewById(R.id.picTextRowPic);
        imageView.setImageBitmap(bitmap);

        progressView = theView;

        // Setting the progressBar
//        progressBar = (ProgressBar) theView.findViewById(R.id.progressBar);
//        if(imageView.getWidth() == 0 && imageView.getHeight() == 0)
//            progressBar.setVisibility(View.VISIBLE);
//        else
//            progressBar.setVisibility(View.GONE);

        return theView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout view;
        if (convertView == null) {
            view = (LinearLayout) mInflater.inflate(R.layout.pic_text_row, parent, false);
        } else {
            view = (LinearLayout) convertView;
        }
        try {
            view = makeView(view, position, parent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }
}
