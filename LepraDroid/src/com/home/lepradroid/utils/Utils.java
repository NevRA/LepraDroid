package com.home.lepradroid.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import android.app.*;
import android.widget.*;
import com.home.lepradroid.Launcher;
import com.home.lepradroid.tasks.PostInboxTask;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebView;

import com.home.lepradroid.LepraDroidApplication;
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
    private static int      commentLevelIndicatorLength = 0;
    private static Boolean  isNormalFontSize            = null;
    
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
    
    public static void setIsNormalFontSize(boolean isNormalFontSize)
    {
        Utils.isNormalFontSize = isNormalFontSize;
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
        catch(Exception ex)
        {
            //TODO
        }
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
    
    public static Spanned getRatingStringFromBaseItem(BaseItem item, int voteWeight)
    {
        if(voteWeight == -1) voteWeight = SettingsWorker.Instance().loadVoteWeight();

        if(!item.isPlusVoted() && !item.isMinusVoted())
            return Html.fromHtml(Integer.toString(item.getRating()));
        else if(item.isPlusVoted())
            return Html.fromHtml(Integer.toString(item.getRating() - voteWeight) + " + <font color='green'>" + voteWeight + "</font>");
        else
            return Html.fromHtml(Integer.toString(item.getRating() + voteWeight) + " - <font color='red'>" + voteWeight + "</font>");
    }
    
    public static Spanned getCommentsStringFromPost(Post post)
    {
        if(post.getTotalComments() != -1 && post.getNewComments() != -1)
            return Html.fromHtml(post.getTotalComments() + " " + Utils.getString(R.string.Total_Comments) + " / " + "<b>" + post.getNewComments() + " " + Utils.getString(R.string.New_Comments) + "</b>");
        if(post.getTotalComments() != -1)
            return Html.fromHtml(post.getTotalComments() + " " + Utils.getString(R.string.Total_Comments));
        else
            return Html.fromHtml(Utils.getString(R.string.No_Comments));
    }

    public static void addInbox(final Context context, final String userName)
    {
        final EditText input = new EditText(context);

        new AlertDialog.Builder(context)
                .setTitle(Utils.getString(R.string.Add_Inbox_Title))
                .setView(input)
                .setPositiveButton(Utils.getString(R.string.yarrr_label), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        final String value = input.getText().toString();

                        new TaskWrapper(null, new PostInboxTask(userName, value), null);
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
    
    public static void addComment(final Context context, final UUID postId, final UUID commentId)
    {
        final Post post = (Post)ServerWorker.Instance().getPostById(postId);
        final Comment comment = (Comment)ServerWorker.Instance().getComment(postId, commentId);
        final String text = comment != null ? comment.getAuthor() + ": " : "";

        final RelativeLayout view = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.add_comment_view, null);
        final EditText commentView = (EditText) view.findViewById(R.id.comment);
        final Button yarrr = (Button) view.findViewById(R.id.yarrr);
        
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(Utils.getString(R.string.Add_Comment_Title));
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();
        //dialog.getWindow().setLayout(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);

        yarrr.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String value = commentView.getText().toString();
                new TaskWrapper(null, new PostCommentTask(post.getId(), "TODO", comment != null ? comment.getLepraId() : "", post.getLepraId(), (short) (comment != null ? comment.getLevel() + 1 : 0), value), null);

                dialog.cancel();
            }
        });

        commentView.setText(text);
        Selection.setSelection(commentView.getText(), text.length());
        commentView.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                InputMethodManager keyboard = (InputMethodManager)
                        context.getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(commentView, 0);
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
    
    public static String wrapLepraTags(Element root)
    {
        Elements elements = root.getElementsByClass("irony");
        for(Element element : elements)
        {
            String text = element.html();
            element.before("<font color=\"red\"><i>" + text + "</i></font>");
            element.remove();
        }
        
        elements = root.getElementsByClass("moderator");
        for(Element element : elements)
        {
            String text = element.html();
            element.before("<font color=\"grey\"><i>" + text + "</i></font>");
            element.remove();
        }
        
        elements = root.getElementsByTag("h2");
        for(Element element : elements)
        {
            String text = element.html();
            element.before("<font size=+1 color=\"#3270FF\">" + text + "</font>");
            element.remove();
        }
        
        return root.html().replaceAll("\\r|\\n", "");
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.getBoolean(Utils.getString(R.string.WidgetSettings_NotifyVibrateId), true))
            ret |= Notification.DEFAULT_VIBRATE;
       
        return ret;
    }
    
    public static boolean isCommentsLoadingWithPost(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Utils.getString(R.string.MainSettings_LoadCommentsOnPostId), true);
    }
    
    public static boolean isNotifyOnUnreadOnlyOnce(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Utils.getString(R.string.WidgetSettings_NotifyOnlyOnFirstId), true);
    }

    public static boolean isCommentsLoadingIndicatorEnabled(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Utils.getString(R.string.MainSettings_CommentsIndicatorId), true);
    }
    
    public static boolean isNotificationsEnabled(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Utils.getString(R.string.WidgetSettings_NotifyId), true);
    }
    
    public static Uri getNotificationSound(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String strRingtonePreference = prefs.getString(Utils.getString(R.string.WidgetSettings_NotifySoundId), "content://settings/system/notification_sound");
        return Uri.parse(strRingtonePreference);
    }
    
    public static boolean isNormalFontSize()
    {
        if(isNormalFontSize == null)
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LepraDroidApplication.getInstance());
            isNormalFontSize = prefs.getString(Utils.getString(R.string.MainSettings_FontSizeId), "normal").equals("normal"); 
        }
          
        return isNormalFontSize;
    }
    
    public static void setTextViewFontSize(TextView view)
    {
        if(!isNormalFontSize())
        {
            view.setTextSize(view.getTextSize() * 1.5f);
        }
    }
    
    public static void setWebViewFontSize(WebView view)
    {
        if(!isNormalFontSize())
        {
            view.getSettings().setTextSize(TextSize.LARGER);
        }
    }

    public static boolean isClearCacheOnExit()
    {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LepraDroidApplication.getInstance());
        return prefs.getBoolean(Utils.getString(R.string.MainSettings_ClearCacheOnExitId), true);
    }
    
    public static boolean isImagesEnabled()
    {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LepraDroidApplication.getInstance());
        return prefs.getBoolean(Utils.getString(R.string.MainSettings_ImagesId), true);        
    }
    
    public static void removeNotification(Context context)
    {
        final NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        notificationManager.cancel(Commons.NOTIFICATION_ID);
    }

    public static int getPostImagePreviewIsPixelsSize()
    {
        Float size = Commons.POST_PREVIEW_ICON_SIZE;
        if(!isNormalFontSize())
        {
            size = 3 * size;
        }

        return convertDipToPixels(size);
    }

    public static int convertDipToPixels(float dips)
    {
        return (int) (dips * LepraDroidApplication.getInstance().getResources().getDisplayMetrics().density + 0.5f);
    }
    
    public static int getWidthForWebView(int padding)
    {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((WindowManager) LepraDroidApplication.getInstance().getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displaymetrics);
        
        return (int) ((float)(displaymetrics.widthPixels - padding) * 154f / (float)displaymetrics.densityDpi);
    }

    public static void clearNotification(Context context)
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
        
        final Intent intent = new Intent(context, Launcher.class);
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
    
    public static void showChangesHistory(Context context)
    {
        LayoutInflater inflater = LayoutInflater.from(context);

        View alertDialogView = inflater.inflate(R.layout.history_view, null);

        WebView webView = (WebView) alertDialogView.findViewById(R.id.DialogWebView);
        webView.getSettings().setDefaultFontSize(10);
        webView.loadUrl("file:///android_asset/history.html");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(Utils.getString(R.string.MainSettings_HistoryName));
        builder.setView(alertDialogView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }

    public static void writeStringToFileCache(String fileName, String data) throws IOException
    {
        FileWriter fileWriter = null;
        try
        {
            FileCache fileCache = new FileCache(LepraDroidApplication.getInstance());
            File file = fileCache.getFile(fileName);
            fileWriter = new FileWriter(file); 
            fileWriter.write(data);
        }
        finally
        {
            if(fileWriter != null)
                fileWriter.close();
        }
    }
    
    public static String readStringFromFileCache(String fileName) throws IOException
    {
        FileReader fileReader = null;
        
        try
        {
            FileCache fileCache = new FileCache(LepraDroidApplication.getInstance());
            File file = fileCache.getFile(fileName);
            StringBuilder text = new StringBuilder();
            fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String line;

            while ((line = br.readLine()) != null)
            {
                if(text.length() != 0)
                    text.append('\n');
                text.append(line);
            }

            return text.toString();
        }
        catch (Exception e) 
        {
            return "";
        }
        finally
        {
            if(fileReader != null)
                fileReader.close();
        }
    }
    
    public static int getCommentLevelIndicatorLength()
    {
        if(commentLevelIndicatorLength == 0)
            commentLevelIndicatorLength = LepraDroidApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.standard_padding);
        
        return commentLevelIndicatorLength;
    }
    
    public static boolean isContainExtraTagsForWebView(String text)
    {
        return text.contains("<img");
    }
    
    public static boolean isAlreadyInStuff(UUID stuffId, String pid)
    {
        List<BaseItem> posts = ServerWorker.Instance().getPostsById(stuffId, false);
        synchronized (posts)
        {
            for(BaseItem post : posts)
            {
                if(((Post)post).getLepraId().equals(pid))
                    return true;
            }
        }

        return false;
    }

    public static long dirSize(File dir)
    {
        if(dir == null || !dir.exists()) return 0;

        long result = 0;
        for (File file : dir.listFiles())
        {
            if(file.isDirectory())
            {
                result += dirSize(file);
            } else
            {
                result += file.length();
            }
        }
        return result;
    }

    public static double convertBytesToMb(long bytes)
    {
        return bytes / 1024d / 1024d;
    }
}