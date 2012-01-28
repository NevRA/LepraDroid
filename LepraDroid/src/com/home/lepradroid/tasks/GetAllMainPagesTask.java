package com.home.lepradroid.tasks;

import java.util.ArrayList;

import com.home.lepradroid.commons.Commons.PostSourceType;

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
        tasks.add((BaseTask) new GetPostsTask(PostSourceType.MAIN, true).execute());
        tasks.add((BaseTask) new GetPostsTask(PostSourceType.MYSTUFF, true).execute());
        for (BaseTask asyncTask : tasks)
        {
            try
            {
                Throwable t = asyncTask.get();
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
