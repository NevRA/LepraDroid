package com.home.lepradroid.listenersworker;

import com.home.lepradroid.interfaces.UpdateListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListenersWorker
{
    private static volatile ListenersWorker instance;
    private final List<UpdateListener> listeners = Collections.synchronizedList(new ArrayList<UpdateListener>());
    
    private ListenersWorker() 
    {
    }

    public static ListenersWorker Instance()
    {
        if(instance == null)
        {
            synchronized (ListenersWorker.class)
            {
                if(instance == null)
                {
                    instance = new ListenersWorker();
                }
            }
        }
        
        return instance;
    }
    
    public void registerListener(UpdateListener listener)
    {
        listeners.add(listener);
    }
    
    public void unregisterListener(UpdateListener listener)
    {
        listeners.remove(listener);
    }
    
    @SuppressWarnings("unchecked")
    public <T> List<T> getListeners(Class<T> type)
    {
        List<T> listenersToReturn = new ArrayList<T>();

        synchronized(listeners)
        {
            for(UpdateListener listener : listeners)
            {
                if( type.isAssignableFrom(listener.getClass()) )
                {
                    listenersToReturn.add((T)listener);
                }
            }
        }
        
        return listenersToReturn;
    }
}
