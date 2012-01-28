package com.home.lepradroid.base;

import java.util.ArrayList;

import com.home.lepradroid.R;
import com.home.lepradroid.interfaces.LogoutListener;
import com.home.lepradroid.interfaces.ProgressListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.tasks.BaseTask;
import com.home.lepradroid.tasks.LogoutTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;

import android.app.ActivityGroup;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class BaseActivity extends ActivityGroup implements UpdateListener, ProgressListener, LogoutListener
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
        menu.add(0, MENU_RELOAD, 0, Utils.getString(R.string.Reload_Menu)).setIcon(R.drawable.ic_reload);
        menu.add(0, MENU_LOGOUT, 1, Utils.getString(R.string.Logout_Menu)).setIcon(R.drawable.ic_logout);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId())
        {
        case MENU_LOGOUT:
            new LogoutTask().execute();
            return true;
        }
        return false;
    }
    
    protected void detachAllTasks()
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

    public void OnLogout()
    {
        finish();
    }
}
