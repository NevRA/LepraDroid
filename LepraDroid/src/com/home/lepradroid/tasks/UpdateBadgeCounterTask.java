package com.home.lepradroid.tasks;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.util.Pair;
import android.widget.RemoteViews;
import com.home.lepradroid.LepraDroidApplication;
import com.home.lepradroid.R;
import com.home.lepradroid.Widget;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Badge;
import com.home.lepradroid.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateBadgeCounterTask extends BaseTask
{
    public static Badge GetItemsCount() throws Exception
    {
        final String html = ServerWorker.Instance().getContent(Commons.SITE_URL + "users/" + SettingsWorker.Instance().loadUserName());
        final Document document = Jsoup.parse(html);
        
        String things = document.getElementById("js-header_nav_my_things").getElementsByTag("i").text();
        String inbox = document.getElementById("js-header_nav_inbox").getElementsByTag("i").text();
        
        return new Badge(getCountFromString(things), getCountFromString(inbox));
    }
    
    private static Pair<Integer, Integer> getCountFromString(String str)
    {
        int first = 0,
            second = 0;
        Pattern counter = Pattern.compile("(\\d+)");
        
        Matcher matcher = counter.matcher(str);
        if(matcher.find())
            first = Integer.valueOf(matcher.group(0));
        
        if(matcher.find())
            second = Integer.valueOf(matcher.group(0));
        
        return new Pair<Integer, Integer>(first, second);
    }
    
    public UpdateBadgeCounterTask()
    {
    }
    
    @Override
    protected Throwable doInBackground(Void... params)
    {
        try
        {
            Badge badge;
            if(SettingsWorker.Instance().IsLogoned())
                badge = GetItemsCount();
            else
                badge = new Badge(new Pair<Integer, Integer>(0, 0), new Pair<Integer, Integer>(0, 0));
            
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(LepraDroidApplication.getInstance());
            RemoteViews remoteViews = new RemoteViews(LepraDroidApplication.getInstance().getPackageName(), R.layout.widget_view);
            Utils.updateWidget(remoteViews, badge);
            ComponentName thisWidget = new ComponentName(LepraDroidApplication.getInstance(), Widget.class);
            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        }
        catch (Exception e)
        {
            setException(e);
        }
        
        return e;
    }
}
