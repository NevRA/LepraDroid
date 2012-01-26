package com.home.lepradroid.base;

import com.home.lepradroid.interfaces.UpdateListener;

import android.content.Context;
import android.view.View;

public abstract class BaseView extends View implements UpdateListener
{
    public BaseView(Context context)
    {
        super(context);
    }
    
    public abstract void attachView();
}
