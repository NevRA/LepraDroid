package com.home.lepradroid.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;

import com.home.lepradroid.LepraDroidApplication;
import com.home.lepradroid.R;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;

public class Utils
{
    public static String getString(Context context, int resourseId)
    {
        return context.getResources().getString(resourseId);
    }

    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
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
    
    public static Drawable getImageFromByteArray(byte array[])
    {
        try
        {
            return Drawable.createFromStream(new ByteArrayInputStream(array), "src");
        }
        catch (Throwable t)
        {
            Logger.e(t);
            return null;
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
        ServerWorker.Instance().clearComments();
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
    
    public static Spanned getCommentsStringFromPost(Post post)
    {
        if(post.totalComments != -1 && post.newComments != -1)
            return Html.fromHtml(post.totalComments.toString() + " " + Utils.getString(R.string.Total_Comments) + " / " + "<b>" + post.newComments + " " + Utils.getString(R.string.New_Comments) + "</b>");
        if(post.totalComments != -1)
            return Html.fromHtml(post.totalComments.toString() + " " + Utils.getString(R.string.Total_Comments));
        else
            return Html.fromHtml(Utils.getString(R.string.No_Comments));
    }
}
