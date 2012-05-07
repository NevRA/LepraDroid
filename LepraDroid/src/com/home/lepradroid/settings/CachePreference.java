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
    private final FileCache cache;

    public CachePreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        cache = new FileCache(LepraDroidApplication.getInstance());

        updateCacheSize();
    }

    @Override
    protected void onPrepareForRemoval()
    {
        calculateCacheSizeTask.cancel(true);
        super.onPrepareForRemoval();
    }

    private void updateCacheSize()
    {
        if(calculateCacheSizeTask != null)
            calculateCacheSizeTask.cancel(true);
        calculateCacheSizeTask = new CalculateCacheSizeTask();
        calculateCacheSizeTask.execute();
    }

    @Override
    protected void onClick()
    {
        setSummary(getContext().getString(R.string.Cleaning));

        cache.clear();

        updateCacheSize();

        super.onClick();
    }

    private class CalculateCacheSizeTask extends AsyncTask<Void, Long, Double>
    {
        @Override
        protected void onPreExecute()
        {
            setSummary(getContext().getString(R.string.Loading));
        }

        @Override
        protected void onPostExecute(Double result)
        {
            String summary = result + " MB";
            setSummary(summary);
        }

        @Override
        protected Double doInBackground(Void... arg0)
        {
            return cache.getSizeInMegabytes();
        }
    }
}