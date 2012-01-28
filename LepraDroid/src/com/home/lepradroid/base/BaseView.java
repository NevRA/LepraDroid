package com.home.lepradroid.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;

public class BaseView extends ViewGroup implements UpdateListener
{
    public View contentView;
    
    public BaseView(Context context)
    {
        super(context);
        ListenersWorker.Instance().registerListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        // TODO Auto-generated method stub
        
    }
}
