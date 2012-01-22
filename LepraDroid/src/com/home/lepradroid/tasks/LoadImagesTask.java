package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.util.Pair;

import com.home.lepradroid.interfaces.PostsUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Logger;

public class LoadImagesTask extends BaseTask
{
    static final Class<?>[] argsClasses = new Class[0];
    static Method method;
    static 
    {
        try
        {
            method = PostsUpdateListener.class.getMethod("OnPostsUpdate", argsClasses);    
        }
        catch (Throwable t) 
        {           
            Logger.e(t);
        }        
    }
    
    @SuppressWarnings("unchecked")
    private void notrifyViews()
    {
        final List<PostsUpdateListener> listeners = ListenersWorker.Instance().getListeners(PostsUpdateListener.class);
        final Object args[] = new Object[0];
        
        for(PostsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (method, args)));
        }
    }

    @Override
    protected Throwable doInBackground(Void... arg0)
    {
        try
        {
            int i = 0;
            ArrayList<Post> posts = ServerWorker.Instance().getPosts();
            for (Post post : posts)
            {
                if(!TextUtils.isEmpty(post.ImageUrl))
                {
                    try
                    {
                        post.dw = ServerWorker.Instance().getImage("http://src.sencha.io/80/80/" + post.ImageUrl);
                    }
                    catch (Exception e)
                    {
                        Logger.e(e);
                    }
                }
                
                if(i%5 == 0)
                {
                    notrifyViews();
                }
                
                i++;
            } 
            
            notrifyViews();
        }
        catch (Throwable t)
        {
            setException(t);
        }

        return e;
    }

}
