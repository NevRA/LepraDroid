package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.List;

import android.util.Pair;

import com.home.lepradroid.interfaces.LogoutListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.utils.Logger;

public class LogoutTask extends BaseTask
{
    static final Class<?>[] argsClasses = new Class[0];
    static Method method;
    static 
    {
        try
        {
            method = LogoutListener.class.getMethod("OnLogout", argsClasses);    
        }
        catch (Throwable t) 
        {           
            Logger.e(t);
        }        
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected Throwable doInBackground(Void... arg0)
    {
        final List<LogoutListener> listeners = ListenersWorker.Instance().getListeners(LogoutListener.class);
        final Object args[] = new Object[0];
        
        for(LogoutListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (method, args)));
        }
        
        return null;
    }

}
