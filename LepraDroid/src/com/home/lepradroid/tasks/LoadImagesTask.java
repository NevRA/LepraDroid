package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.util.Pair;

import com.home.lepradroid.commons.Commons.PostSourceType;
import com.home.lepradroid.interfaces.PostsUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Logger;

public class LoadImagesTask extends BaseTask
{
    private PostSourceType type;
    
    static final Class<?>[] argsClasses = new Class[1];
    static Method methodOnPostsUpdate;
    static 
    {
        try
        {
        	argsClasses[0] = PostSourceType.class;
            methodOnPostsUpdate = PostsUpdateListener.class.getMethod("OnPostsUpdate", argsClasses);    
        }
        catch (Throwable t) 
        {           
            Logger.e(t);
        }        
    }
    
    public LoadImagesTask(PostSourceType type)
    {
        this.type = type;
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutPostsUpdate()
    {
    	if(isCancelled()) return;
    	
        final List<PostsUpdateListener> listeners = ListenersWorker.Instance().getListeners(PostsUpdateListener.class);
        final Object args[] = new Object[1];
        args[0] = type;
        
        for(PostsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnPostsUpdate, args)));
        }
    }

    @Override
    protected Throwable doInBackground(Void... arg0)
    {
        try
        {
            int i = 0;
            ArrayList<Post> posts = ServerWorker.Instance().getPostsByType(type);
            for (Post post : posts)
            {
            	if(isCancelled()) break;
            	
                if(!TextUtils.isEmpty(post.ImageUrl))
                {
                    try
                    {
                        if(post.dw == null)
                            post.dw = ServerWorker.Instance().getImage("http://src.sencha.io/80/80/" + post.ImageUrl);
                    }
                    catch (Exception e)
                    {
                        Logger.e(e);
                    }
                }
                
                if(i%5 == 0)
                {
                	notifyAboutPostsUpdate();
                }
                
                i++;
            } 
            
            notifyAboutPostsUpdate();
        }
        catch (Throwable t)
        {
            setException(t);
        }

        return e;
    }
}
