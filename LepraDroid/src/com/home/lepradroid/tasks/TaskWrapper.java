package com.home.lepradroid.tasks;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.interfaces.ProgressTracker;
import com.home.lepradroid.utils.Utils;


import android.app.ProgressDialog;
import android.os.AsyncTask;

public class TaskWrapper implements ProgressTracker  
{
    private BaseTask            task;
    private BaseActivity        progressListener;
    private ProgressDialog      progressDialog;
    
    private void init(BaseActivity progressListener, BaseTask task, String progressMessage)
    {
        this.task = task;
        this.progressListener = progressListener;
        
        if(task == null)
            return;
        
        if(progressListener != null && task.getStatus() != AsyncTask.Status.FINISHED )
        {
            progressDialog = new ProgressDialog(progressListener);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(progressMessage);
        }
        
        task.setProgressTracker(this);
        
        if(     task.getStatus() != AsyncTask.Status.RUNNING &&
                task.getStatus() != AsyncTask.Status.FINISHED)
        {   
            task.execute();
        }
    }
    
    public TaskWrapper(BaseActivity progress, BaseTask task, String progressMessage)
    {
        init(progress, task, progressMessage);
    }
        
    public void onProgress(String message)
    {
        if(progressDialog != null)
        {
            if(!progressDialog.isShowing())
                progressDialog.show();
            
            if(message != null)
                progressDialog.setMessage(message);
        }
        else if(progressListener != null)
        {
            progressListener.onBeginProgress(""); 
        }
    }
    
    public Object retainTask() 
    {
        // Detach task from tracker (this) before retain
        if (task != null) 
        {
            task.setProgressTracker(null);
        }
        onComplete(null);
        // Retain task
        return task;
    }
    
    protected void finalize() throws Throwable
    {
        onComplete(null);
    }
    
    public void onComplete(Throwable t)
    {
        if(progressDialog != null)
        {
            try
            {
                progressDialog.cancel(); // if activity already closed   
            }
            catch(Exception e){}
            
            progressDialog = null;
        }
        if(progressListener != null)
        {
            progressListener.onEndProgress(""); // TODO
        }
        
        if(t != null)
        {
            if(progressListener != null)
                Utils.showError(progressListener, t);
        }
    }
}
