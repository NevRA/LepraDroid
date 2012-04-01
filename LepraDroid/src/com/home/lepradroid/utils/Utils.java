package com.home.lepradroid.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.jsoup.Jsoup;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Selection;
import android.text.Spanned;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.home.lepradroid.LepraDroidApplication;
import com.home.lepradroid.Main;
import com.home.lepradroid.R;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Comment;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.PostCommentTask;
import com.home.lepradroid.tasks.TaskWrapper;

public class Utils
{
    public static class GzipDecompressingEntity extends HttpEntityWrapper
    {
        public GzipDecompressingEntity(final HttpEntity entity)
        {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException,
                IllegalStateException
        {
            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();

            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength()
        {
            // length of ungzipped content is not known
            return -1;
        }
    }
    
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
    
    public static Spanned getRatingStringFromBaseItem(BaseItem item)
    {
        final Integer voteWeight = SettingsWorker.Instance().loadVoteWeight();
        if(!item.PlusVoted && !item.MinusVoted)
            return Html.fromHtml(Integer.toString(item.Rating));
        else if(item.PlusVoted)
            return Html.fromHtml(Integer.toString(item.Rating - voteWeight) + " + <font color='green'>" + voteWeight.toString() + "</font>");
        else
            return Html.fromHtml(Integer.toString(item.Rating + voteWeight) + " - <font color='red'>" + voteWeight.toString() + "</font>");
    }
    
    public static Spanned getCommentsStringFromPost(Post post)
    {
        if(post.TotalComments != -1 && post.NewComments != -1)
            return Html.fromHtml(post.TotalComments.toString() + " " + Utils.getString(R.string.Total_Comments) + " / " + "<b>" + post.NewComments + " " + Utils.getString(R.string.New_Comments) + "</b>");
        if(post.TotalComments != -1)
            return Html.fromHtml(post.TotalComments.toString() + " " + Utils.getString(R.string.Total_Comments));
        else
            return Html.fromHtml(Utils.getString(R.string.No_Comments));
    }
    
    public static void addComment(final Context context, final UUID groupId, final UUID postId, final UUID commentId)
    {
        final Post post = (Post)ServerWorker.Instance().getPostById(groupId, postId);
        final Comment comment = (Comment)ServerWorker.Instance().getComment(groupId, postId, commentId);
        
        final EditText input = new EditText(context);
        final String text = comment != null ? comment.Author + ": " : "";
        
        input.setText(text);
        Selection.setSelection(input.getText(), text.length());
        
        new AlertDialog.Builder(context)
        .setTitle(Utils.getString(R.string.Add_Comment_Title))
        .setView(input)
        .setPositiveButton(Utils.getString(R.string.yarrr_label), new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int whichButton) 
            {
                final String value = input.getText().toString(); 
                
                new TaskWrapper(null, new PostCommentTask(post.Id, post.commentsWtf, comment != null ? comment.Pid : "", post.Pid, comment != null ? comment.Level + 1 : 0, value), null);
            }
        }).setNegativeButton(Utils.getString(android.R.string.cancel), new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int whichButton) 
            {
            }
        }).show();
        
        input.postDelayed(new Runnable() 
        {
            @Override
            public void run() 
            {
                final InputMethodManager keyboard = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(input, 0);
            }
        },200);
    }
    
    public static boolean isIntNumber(String num)
    {
        try
        {
            Integer.parseInt(num);
        } 
        catch(NumberFormatException nfe) 
        {
            return false;
        }
        
        return true;
    }
    
    public static boolean isCustomBlogPosts(UUID groupId)
    {
        return (!groupId.equals(Commons.MAIN_POSTS_ID) && !groupId.equals(Commons.MYSTUFF_POSTS_ID) && !groupId.equals(Commons.FAVORITE_POSTS_ID) && !groupId.equals(Commons.INBOX_POSTS_ID));
    }
    
    public static String replaceBadHtmlTags(String html)
    {
        return html.replaceAll("(&#150;|&#151;)", "-").replaceAll("(&#133;)", "...");
    }
    
    public static int updateWidget(RemoteViews remoteViews, Badge badge)
    {
        Integer newCounter = badge.things.first + badge.things.second + badge.inbox.first + badge.inbox.second;
        SettingsWorker.Instance().saveUnreadCounter(newCounter);
        
        remoteViews.setViewVisibility(R.id.widget_counter, badge.isNoNewItems() ? View.INVISIBLE : View.VISIBLE);
        remoteViews.setTextViewText(R.id.widget_counter, badge.ToString());
        
        return newCounter;
    }
    
    public static String html2text(String html) 
    {
        return Jsoup.parse(html).text();
    }
    
    public static int getNotificationDefaults(Context context)
    {
        int ret = 0;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.getBoolean(Utils.getString(R.string.MainSettings_NotifyVibrateId), true))
            ret |= Notification.DEFAULT_VIBRATE;
       
        return ret;
    }
    
    public static boolean isNotificationsEnabled(Context context)
    {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Utils.getString(R.string.MainSettings_NotifyId), true);
    }
    
    public static Uri getNotificationSound(Context context)
    {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String strRingtonePreference = prefs.getString(Utils.getString(R.string.MainSettings_NotifySoundId), "DEFAULT_SOUND");        
        return Uri.parse(strRingtonePreference);
    }
    
    public static boolean isNormalFontSize(Context context)
    {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(Utils.getString(R.string.MainSettings_FontSizeId), "normal").equals("normal");        
    }
    
    public static void setTextViewFontSize(Context context, TextView view)
    {
        if(!isNormalFontSize(context))
        {
            view.setTextSize(view.getTextSize() * 1.1f);
        }
    }
    
    public static void setWebViewFontSize(Context context, WebView view)
    {
        if(!isNormalFontSize(context))
        {
            view.getSettings().setDefaultFontSize((int)(view.getSettings().getDefaultFontSize() * 1.3f));
        }
    }
    
    public static boolean isImagesEnabled(Context context)
    {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Utils.getString(R.string.MainSettings_ImagesId), true);        
    }
    
    public static void removeNotification(Context context)
    {
        final NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        notificationManager.cancel(Commons.NOTIFICATION_ID);
    }
    
    public static void pushNotification(Context context)
    {
        if(!isNotificationsEnabled(context)) return;
        
        final Notification notification = new Notification(
                R.drawable.ic_launcher, 
                Utils.getString(R.string.New_unread_messages),
                System.currentTimeMillis());
        
        final NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        final Intent intent = new Intent(context, Main.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
           
        notification.setLatestEventInfo(
                context, 
                Utils.getString(R.string.New_unread_messages), 
                Utils.getString(R.string.New_unread_messages_summary),
                PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT)); 

        notification.icon = R.drawable.ic_launcher;
        notification.defaults |= getNotificationDefaults(context);
        notification.sound = getNotificationSound(context);

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS; 
        notification.ledARGB = Color.GREEN; 
        notification.ledOffMS = 400; 
        notification.ledOnMS = 300;
        
        notificationManager.notify(Commons.NOTIFICATION_ID, notification );
    }
}
