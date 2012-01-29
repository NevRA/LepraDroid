package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.text.TextUtils;
import android.util.Pair;

import com.home.lepradroid.interfaces.ImagesUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Logger;

public class LoadImagesTask extends BaseTask
{
    private UUID groupId;
    
    static final Class<?>[] argsClasses = new Class[1];
    static Method methodOnImagesUpdate;
    static 
    {
        try
        {
        	argsClasses[0] = UUID.class;
            methodOnImagesUpdate = ImagesUpdateListener.class.getMethod("OnImagesUpdate", argsClasses);    
        }
        catch (Throwable t) 
        {           
            Logger.e(t);
        }        
    }
    
    public LoadImagesTask(UUID groupId)
    {
        this.groupId = groupId;
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutImagesUpdate()
    {
    	if(isCancelled()) return;
    	
        final List<ImagesUpdateListener> listeners = ListenersWorker.Instance().getListeners(ImagesUpdateListener.class);
        final Object args[] = new Object[1];
        args[0] = groupId;
        
        for(ImagesUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnImagesUpdate, args)));
        }
    }

    @Override
    protected Throwable doInBackground(Void... arg0)
    {
        try
        {
            int i = 0;
            ArrayList<BaseItem> posts = ServerWorker.Instance().getPostsById(groupId);
            for (BaseItem post : posts)
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
                	notifyAboutImagesUpdate();
                }
                
                i++;
                
                Thread.sleep(100);
            } 
            
            notifyAboutImagesUpdate();
        }
        catch (Throwable t)
        {
            setException(t);
        }

        return e;
    }
}
