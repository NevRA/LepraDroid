package com.home.lepradroid.tasks;

import android.app.ProgressDialog;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.interfaces.ProgressTracker;
import com.home.lepradroid.utils.Utils;
import com.home.lepradroid.utils.WorkerTask;

public class TaskWrapper implements ProgressTracker  
{
    private BaseTask            task;
    private BaseActivity        progressListener;
    private ProgressDialog      progressDialog;
    
    private void init(BaseActivity progressListener, BaseTask task, boolean showProgressDialog, String progressMessage)
    {
        this.task                   = task;
        this.progressListener       = progressListener;
        
        if(task == null)
            return;
        
        if(showProgressDialog && progressListener != null && task.getStatus() != WorkerTask.Status.FINISHED )
        {
            progressDialog = new ProgressDialog(progressListener);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(progressMessage);
        }
        
        task.setProgressTracker(this);
        
        if(     task.getStatus() != WorkerTask.Status.RUNNING &&
                task.getStatus() != WorkerTask.Status.FINISHED)
        {   
            task.execute();
        }
    }
    
    public TaskWrapper(BaseActivity progressListener, BaseTask task, String progressMessage)
    {
        init(progressListener, task, false, progressMessage);
    }
    
    public TaskWrapper(BaseActivity progressListener, BaseTask task, boolean showProgressDialog, String progressMessage)
    {
        init(progressListener, task, showProgressDialog, progressMessage);
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
            progressListener.onEndProgress("");
        
        if(t != null)
            if(progressListener != null)
                Utils.showError(progressListener, t);
        
        progressListener = null;
    }
}
