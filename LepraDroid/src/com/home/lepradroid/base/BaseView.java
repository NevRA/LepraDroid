package com.home.lepradroid.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.home.lepradroid.interfaces.ExitListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;

public abstract class BaseView extends ViewGroup implements UpdateListener, ExitListener
{
    public View contentView;
    
    public BaseView(Context context)
    {
        super(context);
        ListenersWorker.Instance().registerListener(this);
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4)
    { 
    }
}
