package com.home.lepradroid.utils;

import com.home.lepradroid.LepraDroidApplication;
import com.home.lepradroid.R;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Utils
{
    public static String getString(Context context, int resourseId)
    {
        return context.getResources().getString(resourseId);
    }
    
    public static String getString(int resourseId)
    {
        try
        {
            return getString(LepraDroidApplication.getInstance(), resourseId);
        }
        catch (Throwable e)
        {
            Logger.e(e);
            return "";
        }
    }
    
    public static void showError(Context context, String error)
    {
        final AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);
        alt_bld.setTitle(Utils.getString(R.string.Error));
        alt_bld.setMessage(error);
        alt_bld.setIcon(R.drawable.ic_launcher);
        alt_bld.setPositiveButton(Utils.getString(android.R.string.ok), new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int which) 
            {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = alt_bld.create();
        alertDialog.show();
    }
    
    public static void showError(Context context, Throwable t)
    {
        if(t != null)
        {
            Logger.e(t);
            String message = t.getMessage();
            if(message != null)
                showError(context, message);
            else
                showError(context, getString(R.string.Unknown_Error));
        }
    }
    
    public static void clearData()
    {
    	ServerWorker.Instance().clearPosts();
    }
    
    public static void clearLogonInfo()
    {
        try
        {
            ServerWorker.Instance().clearSessionInfo();
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
    }
}
