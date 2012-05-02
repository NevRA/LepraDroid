package com.home.lepradroid.tasks;

import android.text.TextUtils;
import android.util.Pair;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.AddedInboxUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.lang.reflect.Method;
import java.util.List;

public class PostInboxTask extends BaseTask
{
    private String userName;
    private String message;

    static final Class<?>[] argsClassesOnAddedInboxUpdate = new Class[2];
    static Method methodOnAddedInboxUpdate;
    static
    {
        try
        {
            argsClassesOnAddedInboxUpdate[0] = String.class;
            argsClassesOnAddedInboxUpdate[1] = boolean.class;
            methodOnAddedInboxUpdate = AddedInboxUpdateListener.class.getMethod("OnAddedInboxUpdate", argsClassesOnAddedInboxUpdate);
        }
        catch (Throwable t)
        {
            Logger.e(t);
        }
    }

    public PostInboxTask(String userName, String message)
    {
        this.userName = userName;
        this.message = message;
    }

    @SuppressWarnings("unchecked")
    public void notifyOnAddedInboxUpdate(boolean successful)
    {
        final List<AddedInboxUpdateListener> listeners = ListenersWorker.Instance().getListeners(AddedInboxUpdateListener.class);
        final Object args[] = new Object[2];
        args[0] = userName;
        args[1] = successful;

        for(AddedInboxUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnAddedInboxUpdate, args)));
        }
    }

    @Override
    protected Throwable doInBackground(Void... params)
    {
        try
        {
            if(TextUtils.isEmpty(SettingsWorker.Instance().loadInboxWtf()))
            {
                String html = ServerWorker.Instance().getContent(Commons.ADD_INBOX_URL);
                Element element = Jsoup.parse(html).getElementsByAttributeValue("name", "wtf").get(1);
                SettingsWorker.Instance().saveInboxWtf(element.attr("value"));
            }

            ServerWorker.Instance().postInboxRequest(SettingsWorker.Instance().loadInboxWtf(), userName, message);
            new GetPostsTask(Commons.INBOX_POSTS_ID, Commons.INBOX_URL).execute();
            notifyOnAddedInboxUpdate(true);
        }
        catch (Exception e)
        {
            setException(e);
            notifyOnAddedInboxUpdate(false);
        }
        return e;
    }
}