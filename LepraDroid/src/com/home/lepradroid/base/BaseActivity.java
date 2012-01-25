package com.home.lepradroid.base;

import java.util.ArrayList;

import com.home.lepradroid.R;
import com.home.lepradroid.interfaces.ProgressListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.tasks.BaseTask;
import com.home.lepradroid.tasks.TaskWrapper;

import android.app.ActivityGroup;
import android.os.Bundle;
import android.view.Menu;

public class BaseActivity extends ActivityGroup implements UpdateListener, ProgressListener
{
    protected static final int MENU_RELOAD = 0;
    protected static final int MENU_LOGOUT = 1;
    
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
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(0, MENU_RELOAD, 0, "Reload").setIcon(R.drawable.ic_reload);
        menu.add(0, MENU_LOGOUT, 1, "Logout").setIcon(R.drawable.ic_logout);
        return true;
    }
    
    private void detachAllTasks()
    {
        for(TaskWrapper task : tasks)
        {
            Object rawTask = task.retainTask();
            if(rawTask instanceof BaseTask)
            {
            	((BaseTask)rawTask).finish();
            }
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
