package com.home.lepradroid.tasks;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.serverworker.ServerWorker;

public class GetPostsTask extends BaseTask
{

    @Override
    protected Throwable doInBackground(Void... params)
    {
        try
        {
            String html = ServerWorker.Instance().getContent(Commons.SITE_URL);
            Document document = Jsoup.parse(html);
        }
        catch (Throwable t)
        {
            setException(t);
        }
        
        return e;
    }
}
