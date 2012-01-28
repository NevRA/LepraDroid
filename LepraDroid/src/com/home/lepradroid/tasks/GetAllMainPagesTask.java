package com.home.lepradroid.tasks;

import java.util.ArrayList;

import com.home.lepradroid.commons.Commons;

public class GetAllMainPagesTask extends BaseTask
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
        tasks.add((BaseTask) new GetPostsTask(Commons.MAIN_POSTS_ID, Commons.SITE_URL, true));
        tasks.add((BaseTask) new GetBlogsTask(true));
        tasks.add((BaseTask) new GetPostsTask(Commons.MYSTUFF_POSTS_ID, Commons.MY_STUFF_URL, true));
        for (BaseTask asyncTask : tasks)
        {
            try
            {
                asyncTask.execute();
                final Throwable t = asyncTask.get();
                if(t != null)
                    setException(t);
            }
            catch (Throwable t)
            {
                setException(t);
            }
        }
       
                
        return e;
    }

}
