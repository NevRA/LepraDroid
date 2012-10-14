package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import android.util.Pair;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.commons.Commons.StuffOperationType;
import com.home.lepradroid.interfaces.ChangeMyStuffListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Logger;

public class ChangeMyStuffTask extends BaseTask
{
    private Post                post;
    private StuffOperationType  type;
    
    static final Class<?>[] argsClassesOnChangeMyStuff = new Class[3];
    static Method methodOnChangeMyStuff;
    static
    {
        try
        {
            argsClassesOnChangeMyStuff[0] = UUID.class;
            argsClassesOnChangeMyStuff[1] = StuffOperationType.class;
            argsClassesOnChangeMyStuff[2] = boolean.class;
            methodOnChangeMyStuff = ChangeMyStuffListener.class.getMethod("OnChangeMyStuff", argsClassesOnChangeMyStuff);
        }
        catch (Throwable t)
        {
            Logger.e(t);
        }
    }
    
    public ChangeMyStuffTask(UUID postId, StuffOperationType type)
    {
        this.type = type;
        post = (Post)ServerWorker.Instance().getPostById(postId);
    }

    @SuppressWarnings("unchecked")
    public void notifyAboutChangeMyStuff(boolean successful)
    {
        final List<ChangeMyStuffListener> listeners = ListenersWorker.Instance().getListeners(ChangeMyStuffListener.class);
        final Object args[] = new Object[3];
        args[0] = post.getId();
        args[1] = type;
        args[2] = successful;
        
        for(ChangeMyStuffListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnChangeMyStuff, args)));
        }
    }
    
    
    @Override
    protected Throwable doInBackground(Void... params)
    {
        try
        {
            ServerWorker.Instance().postChangeMyStuff(SettingsWorker.Instance().loadStuffWtf(), post.getLepraId(), type);
            new GetPostsTask(Commons.MYSTUFF_POSTS_ID, Commons.MY_STUFF_URL, Commons.PostsType.MY).execute();
            notifyAboutChangeMyStuff(true);
        }
        catch (Exception e)
        {
            notifyAboutChangeMyStuff(false);
            setException(e);
        }
        
        return e;
    }
}
