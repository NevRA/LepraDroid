package com.home.lepradroid.settings;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.Preference;
import android.util.AttributeSet;
import com.home.lepradroid.LepraDroidApplication;
import com.home.lepradroid.R;
import com.home.lepradroid.utils.FileCache;

public class CachePreference extends Preference
{
    private CalculateCacheSizeTask calculateCacheSizeTask;

    public CachePreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        updateCacheSize(false);
    }

    @Override
    protected void onPrepareForRemoval()
    {
        calculateCacheSizeTask.cancel(true);
        super.onPrepareForRemoval();
    }

    private void updateCacheSize(boolean clearCache)
    {
        if(calculateCacheSizeTask != null)
            calculateCacheSizeTask.cancel(true);
        calculateCacheSizeTask = new CalculateCacheSizeTask(clearCache);
        calculateCacheSizeTask.execute();
    }

    @Override
    protected void onClick()
    {
        updateCacheSize(true);

        super.onClick();
    }

    private class CalculateCacheSizeTask extends AsyncTask<Void, Long, Double>
    {
        private boolean clearCache;
        public CalculateCacheSizeTask(boolean clearCache)
        {
             this.clearCache = clearCache;
        }

        @Override
        protected void onPreExecute()
        {
            if(!isCancelled())
            {
                setSummary(getContext().getString(clearCache ? R.string.Cleaning : R.string.Loading));
            }
        }

        @Override
        protected void onPostExecute(Double result)
        {
            if(!isCancelled())
            {
                String summary = result + " MB";
                setSummary(summary);
            }
        }

        @Override
        protected Double doInBackground(Void... arg0)
        {
            FileCache cache = new FileCache(LepraDroidApplication.getInstance());

            if(clearCache)
            {
                cache.clear();
            }

            if(isCancelled())
                return 0.0;

            return cache.getSizeInMegabytes();
        }
    }
}