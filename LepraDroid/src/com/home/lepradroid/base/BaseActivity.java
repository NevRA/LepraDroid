package com.home.lepradroid.base;

import com.home.lepradroid.interfaces.ProgressListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity implements UpdateListener, ProgressListener
{
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
    
    @Override
    protected void onDestroy()
    {
        ListenersWorker.Instance().unregisterListener(this);
        super.onDestroy();
    }
}
