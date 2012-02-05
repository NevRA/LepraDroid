package com.home.lepradroid.tasks;

import java.util.ArrayList;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Logger;

public class GetMainPagesTask extends BaseTask
{
    private ArrayList<BaseTask> tasks = new ArrayList<BaseTask>();
    
    @Override
    public void finish()
    {
        for (BaseTask asyncTask : tasks)
        {
            asyncTask.finish();
        }
        super.finish();
    }
    
    @Override
    protected Throwable doInBackground(Void... arg0)
    {
        final long startTime = System.nanoTime();
        
        try
        {
            if (SettingsWorker.Instance().loadVoteWeight() == 0)
            {
                tasks.add((BaseTask) new GetAuthorTask(SettingsWorker.Instance().loadUserName()).execute());
                tasks.get(0).get();
            }
            
            tasks.add((BaseTask) new GetPostsTask(Commons.MAIN_POSTS_ID, Commons.SITE_URL).execute());
            tasks.add((BaseTask) new GetBlogsTask().execute());
            //tasks.add((BaseTask) new GetPostsTask(Commons.FAVORITE_POSTS_ID, Commons.FAVORITES_URL, true).execute());
            //tasks.add((BaseTask) new GetPostsTask(Commons.MYSTUFF_POSTS_ID, Commons.MY_STUFF_URL, true).execute());
            for (BaseTask asyncTask : tasks)
            {
                try
                {
                    final Throwable t = asyncTask.get();
                    if(t != null)
                        setException(t);
                }
                catch (Throwable t)
                {
                    setException(t);
                }
            } 
        }
        catch (Exception e)
        {
            setException(e);
        }
        
        Logger.d("GetAllTask time:" + Long.toString(System.nanoTime() - startTime));    
                
        return e;
    }

}
