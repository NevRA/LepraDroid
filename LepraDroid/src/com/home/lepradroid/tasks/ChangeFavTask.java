package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import android.util.Pair;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.commons.Commons.StuffOperationType;
import com.home.lepradroid.interfaces.ChangeFavListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Logger;

public class ChangeFavTask extends BaseTask
{
    private String              pid;
    private StuffOperationType  type;
    private UUID                groupId;
    private UUID                postId;
    
    static final Class<?>[] argsClassesOnChangeFav = new Class[4];
    static Method methodOnChangeFav;
    static
    {
        try
        {
            argsClassesOnChangeFav[0] = UUID.class;
            argsClassesOnChangeFav[1] = UUID.class;
            argsClassesOnChangeFav[2] = StuffOperationType.class;
            argsClassesOnChangeFav[3] = boolean.class;
            methodOnChangeFav = ChangeFavListener.class.getMethod("OnChangeFav", argsClassesOnChangeFav);
        }
        catch (Throwable t)
        {
            Logger.e(t);
        }
    }
    
    public ChangeFavTask(UUID groupId, UUID postId, StuffOperationType type)
    {
        this.groupId = groupId;
        this.postId = postId;
        this.type = type;
        
        Post post = (Post)ServerWorker.Instance().getPostById(groupId, postId);
        if(post != null)
            this.pid = post.getPid();
    }

    @SuppressWarnings("unchecked")
    public void notifyAboutChangeFav(boolean successful)
    {
        final List<ChangeFavListener> listeners = ListenersWorker.Instance().getListeners(ChangeFavListener.class);
        final Object args[] = new Object[4];
        args[0] = groupId;
        args[1] = postId;
        args[2] = type;
        args[3] = successful;
        
        for(ChangeFavListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnChangeFav, args)));
        }
    }
    
    
    @Override
    protected Throwable doInBackground(Void... params)
    {
        try
        {
            ServerWorker.Instance().postChangeFavs(SettingsWorker.Instance().loadFavWtf(), pid, type);
            new GetPostsTask(Commons.FAVORITE_POSTS_ID, Commons.FAVORITES_URL).execute();
            notifyAboutChangeFav(true);
        }
        catch (Exception e)
        {
            notifyAboutChangeFav(false);
            setException(e);
        }
        
        return e;
    }
}
