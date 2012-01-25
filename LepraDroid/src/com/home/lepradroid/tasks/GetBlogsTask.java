package com.home.lepradroid.tasks;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.serverworker.ServerWorker;

public class GetBlogsTask extends BaseTask
{

    @Override
    protected Throwable doInBackground(Void... arg0)
    {
        try
        {
            final String html = ServerWorker.Instance().getContent(Commons.BLOGS_URL);
        }
        catch (Throwable t)
        {
            setException(t);
        }
                
        return e;
    }

}
