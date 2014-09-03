package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.List;

import android.util.Pair;

import com.home.lepradroid.interfaces.ThresholdUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Logger;

public class PostMainThresholdTask extends BaseTask
{
    static final Class<?>[] argsClassesOnThresholdUpdate = new Class[1];
    static Method methodOnThresholdUpdate;
    static
    {
        try
        {
            argsClassesOnThresholdUpdate[0] = boolean.class;
            methodOnThresholdUpdate = ThresholdUpdateListener.class.getMethod("OnThresholdUpdate", argsClassesOnThresholdUpdate);
        }
        catch (Throwable t)
        {
            Logger.e(t);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutThresholdUpdate(boolean successful)
    {
        final List<ThresholdUpdateListener> listeners = ListenersWorker.Instance().getListeners(ThresholdUpdateListener.class);
        final Object args[] = new Object[1];
        args[0] = successful;
        
        for(ThresholdUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnThresholdUpdate, args)));
        }
    }
    
    @Override
    protected Throwable doInBackground(Void... params)
    {
        try
        {
            ServerWorker.Instance().postMainTresholdRequest(SettingsWorker.Instance().loadMainThreshold());
            notifyAboutThresholdUpdate(true);
        }
        catch (Throwable t)
        {
            notifyAboutThresholdUpdate(false);
            setException(t);
        }

        return e;
    }
}
