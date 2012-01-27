package com.home.lepradroid.base;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;

public class BaseView extends LinearLayout implements UpdateListener
{
    public BaseView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        ListenersWorker.Instance().registerListener(this);
    }
}
