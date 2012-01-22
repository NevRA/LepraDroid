package com.home.lepradroid.utils;

import com.home.lepradroid.commons.Commons;

import android.util.Log;

public class Logger
{
    public static void e(Throwable t)
    {
        if(t != null)
        {
            Log.e(Commons.APP_TAG, t.getMessage() + " [" + Log.getStackTraceString(t) + "]");
        }
    }
}
