package com.home.lepradroid.utils;

import com.home.lepradroid.commons.Commons;

import android.util.Log;

public class Logger
{
    public static void e(String message)
    {
        if(message != null)
        {
            Log.e(Commons.APP_TAG, message);
        }
    }
    
    public static void e(Throwable t)
    {
        if(t != null)
        {
            Log.e(Commons.APP_TAG, t.getMessage() + " [" + Log.getStackTraceString(t) + "]");
        }
    }
    
    public static void d(String message)
    {
        if(message != null)
        {
            Log.d(Commons.APP_TAG, message);
        }
    }
}
