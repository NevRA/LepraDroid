package com.home.lepradroid.tasks;

import android.text.TextUtils;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class PostInboxTask extends BaseTask
{
    private String userName;
    private String message;

    public PostInboxTask(String userName, String message)
    {
        this.userName = userName;
        this.message = message;
    }

    @Override
    protected Throwable doInBackground(Void... params)
    {
        try
        {
             if(TextUtils.isEmpty(SettingsWorker.Instance().loadInboxWtf()))
             {
                 String html = ServerWorker.Instance().getContent(Commons.ADD_INBOX_URL);
                 Element element = Jsoup.parse(html).getElementsByAttributeValue("name", "wtf").get(1);
                 SettingsWorker.Instance().saveInboxWtf(element.attr("value"));
             }

             ServerWorker.Instance().postInboxRequest(SettingsWorker.Instance().loadInboxWtf(), userName, message);
        }
        catch (Exception e)
        {
            setException(e);
        }
        return e;
    }
}