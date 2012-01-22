package com.home.lepradroid.base;

import java.util.ArrayList;

import com.home.lepradroid.interfaces.ProgressListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.tasks.TaskWrapper;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity implements UpdateListener, ProgressListener
{
    private ArrayList<TaskWrapper> tasks = new ArrayList<TaskWrapper>();
    
    public void onBeginProgress(String message)
    {
    }

    public void onEndProgress(String message)
    {
    }
    
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        ListenersWorker.Instance().registerListener(this);
    }
    
    private void detachAllTasks()
    {
        for(TaskWrapper task : tasks)
        {
            task.retainTask();
        }
    }
    
    public void pushNewTask(TaskWrapper task)
    {
        tasks.add(task);
    }
    
    @Override
    protected void onDestroy()
    {
        ListenersWorker.Instance().unregisterListener(this);
        detachAllTasks();
        super.onDestroy();
    }
}
