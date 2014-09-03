package com.home.lepradroid;

import android.app.Application;
import android.content.Context;

public class LepraDroidApplication extends Application
{
    private static Context context; 
    
    public void onCreate()
    {
        context = this;
    }
    
    public static Context getInstance()
    {
        return context;
    }
}
