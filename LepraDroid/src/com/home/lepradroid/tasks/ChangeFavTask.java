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
    private Post                post;
    private StuffOperationType  type;
    
    static final Class<?>[] argsClassesOnChangeFav = new Class[3];
    static Method methodOnChangeFav;
    static
    {
        try
        {
            argsClassesOnChangeFav[0] = UUID.class;
            argsClassesOnChangeFav[1] = StuffOperationType.class;
            argsClassesOnChangeFav[2] = boolean.class;
            methodOnChangeFav = ChangeFavListener.class.getMethod("OnChangeFav", argsClassesOnChangeFav);
        }
        catch (Throwable t)
        {
            Logger.e(t);
        }
    }
    
    public ChangeFavTask(UUID postId, StuffOperationType type)
    {
        post = (Post)ServerWorker.Instance().getPostById(postId);
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public void notifyAboutChangeFav(boolean successful)
    {
        final List<ChangeFavListener> listeners = ListenersWorker.Instance().getListeners(ChangeFavListener.class);
        final Object args[] = new Object[3];
        args[0] = post.getId();
        args[1] = type;
        args[2] = successful;
        
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
            ServerWorker.Instance().postChangeFavs(SettingsWorker.Instance().loadFavWtf(), post.getLepraId(), type);
            new GetPostsTask(Commons.FAVORITE_POSTS_ID, Commons.FAVORITES_URL, Commons.PostsType.MY).execute();
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
