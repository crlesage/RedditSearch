package com.lesage.chris.redditfetch;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.LruCache;



public class BitmapCache {
    // We assume main activity will set this before getInstance() is called
    public static int cacheSize = 0;
    // Clients can set these to screen dimentions to ensure Bitmap scaling
    public static int maxW;
    public static int maxH;
    public static Bitmap defaultThumbnailBitmap;
    public static Bitmap errorImageBitmap;
    protected static int thumbH = 135;
    protected static int thumbW = 135;
    private LruCache<String, Bitmap> mMemoryCache;

    private BitmapCache() {
        if( this.cacheSize == 0 ) {
            Log.d(MainActivity.AppName, "Yikes, BitmapCache.cacheSize not set!");
            return;
        }
        mMemoryCache = new LruCache<String, Bitmap>(this.cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
        this.defaultThumbnailBitmap = makeDefaultThumbnail();
        this.errorImageBitmap = makeErrorImage();
    }

    private static class BitmapCacheHolder {
        public static BitmapCache helper = new BitmapCache();
    }

    public static BitmapCache getInstance() {
        return BitmapCacheHolder.helper;
    }

    static protected Bitmap makeErrorImage() {
        int h = thumbH;
        int w = thumbW;
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint();
        Paint darkPaint = new Paint();
        darkPaint.setColor(Color.RED);
        canvas.drawRect(0, 0, w, h, darkPaint);
        return bm;
    }
    static protected Bitmap makeDefaultThumbnail(){
        int h = thumbH;
        int w = thumbW;
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bm);

        Paint darkPaint = new Paint();
        darkPaint.setColor(Color.rgb(50, 50, 50));

        Paint lightPaint = new Paint();
        lightPaint.setColor(Color.rgb(200, 200, 200));

        canvas.drawCircle(w / 2, h / 2, h / 2, darkPaint);
        canvas.drawCircle(w / 2, h / 2, h / 4, lightPaint);

        return bm;
    }

    // Thanks to https://stackoverflow.com/questions/15440647/scaled-bitmap-maintaining-aspect-ratio
    protected static Bitmap scaleBitmapAndKeepRatio(Bitmap bitmap) {
            Bitmap resizedBitmap = null;
            int originalWidth = bitmap.getWidth();
            int originalHeight = bitmap.getHeight();
            int newWidth = -1;
            int newHeight = -1;
            float multFactor = -1.0F;
            if(originalHeight > originalWidth) {
                newHeight = maxH;
                multFactor = (float) originalWidth/(float) originalHeight;
                newWidth = (int) (newHeight*multFactor);
            } else if(originalWidth > originalHeight) {
                newWidth = maxW;
                multFactor = (float) originalHeight/ (float)originalWidth;
                newHeight = (int) (newWidth*multFactor);
            } else {
                // originalHeight==originalWidth
                newHeight = maxH;
                newWidth = maxW;
            }
            resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            return resizedBitmap;
        }
    public synchronized void setBitmap(String key, Bitmap bitmap) {
        if (mMemoryCache.get(key) == null) {
            // Only scale if we have to
            if( bitmap.getWidth() > maxW || bitmap.getHeight() > maxH ) {
                bitmap = scaleBitmapAndKeepRatio(bitmap);
            }
            mMemoryCache.put(key, bitmap);
            bitmap = mMemoryCache.get(key);
            if( bitmap == null ){
                Log.d(MainActivity.AppName, "Yikes, bitmap too big " + key);
                mMemoryCache.put(key, errorImageBitmap);
            }
        }
    }

    public synchronized Bitmap getBitmap(String key) {
        Bitmap bm = mMemoryCache.get(key);
        if( bm == null ) {
            Log.d(MainActivity.AppName, "BMCache miss " + key);
        } else {
            Log.d(MainActivity.AppName, "BMCache hit " + key);
        }
        return bm;
    }
}
