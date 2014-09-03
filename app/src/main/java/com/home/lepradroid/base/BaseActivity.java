package com.home.lepradroid.base;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class BaseActivity extends ActivityGroup implements UpdateListener, ProgressListener, LogoutListener
{
    protected static final int MENU_RELOAD      = 0;
    protected static final int MENU_LOGOUT      = 1;
    protected static final int MENU_ADD_COMMENT  
                                                = 2;
    protected static final int MENU_COMMENT_NAVIGATE  
                                                = 3;
    protected static final int MENU_SETTINGS  
                                                = 4;
    protected static final int MENU_ADD_STUFF  
                                                = 5;
    protected static final int MENU_DEL_STUFF  
                                                = 6;
    protected static final int MENU_ADD_FAV  
                                                = 7;
    protected static final int MENU_DEL_FAV  
                                                = 8;
    protected static final int MENU_INBOX
                                                = 9;
    
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
    
    public void unbindDrawables(View view)
    {
        if (view.getBackground() != null)
        {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup)
        {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
            {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            if(view instanceof ListView) return;
            ((ViewGroup) view).removeAllViews();
        }
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId())
        {
        case MENU_LOGOUT:
            AlertDialog.Builder exitAlert = new AlertDialog.Builder(this);
            exitAlert.setCancelable(false);
            exitAlert.setTitle(R.string.Logout_Menu);
            String message = this.getString(R.string.Logout_Menu_Confirm);
            exitAlert.setMessage(message);
            exitAlert.setPositiveButton(this.getString(R.string.Yes_Please),
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            new LogoutTask().execute();
                            dialog.cancel();
                        }
                    });
            exitAlert.setNegativeButton(this.getString(R.string.No_Thanks),
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });
            exitAlert.show();
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
    
    public <T> void popAllTasksLikeThis(Class<T> type)
    {
        for(TaskWrapper task : tasks)
        {
            BaseTask rawTask = task.getTask();
            if(type.isAssignableFrom(rawTask.getClass()))
            {
                rawTask.finish();
                tasks.remove(task);
                break;
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
