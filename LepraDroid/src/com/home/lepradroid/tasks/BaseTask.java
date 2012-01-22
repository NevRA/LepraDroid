package com.home.lepradroid.tasks;

import java.lang.reflect.Method;

import com.home.lepradroid.interfaces.ProgressTracker;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.utils.Logger;

import android.os.AsyncTask;
import android.util.Pair;

public abstract class BaseTask extends AsyncTask<Void, Pair<UpdateListener, Pair<Method, Object[]>>, Throwable> 
{
    protected ProgressTracker progress;
    protected Throwable e; 
    
    public BaseTask()
    {
    }
    
    @Override
    protected void onProgressUpdate(Pair<UpdateListener, Pair<Method, Object[]>>... listeners) 
    {       
        for(int i = 0; i < listeners.length; ++i )
        {
            Pair<UpdateListener, Pair<Method, Object[]>> pairs = listeners[i];
            UpdateListener listener = pairs.first;
            Pair<Method, Object[]> pair = pairs.second;
            
            try
            {
                pair.first.invoke(listener, pair.second);
            } 
            catch (Throwable t)
            {
               Logger.e(t);
            } 
        }
    }
    
    protected void setException(Throwable e) 
    {
        this.e = e;
    }

    @Override
    protected void onPostExecute(Throwable result) 
    { 
        if(progress!=null)
            progress.onComplete(e);
    }
    
    public void setProgressTracker(ProgressTracker progress)
    {
        this.progress = progress;
        showProgressDialog();
    }
    
    public void showProgressDialog()
    {
        if(progress != null)
            progress.onProgress(null);
    }
    
    public void finish()
    {
        cancel(true);
    }
}