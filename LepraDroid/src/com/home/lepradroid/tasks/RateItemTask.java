package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import android.util.Pair;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.commons.Commons.RateType;
import com.home.lepradroid.commons.Commons.RateValueType;
import com.home.lepradroid.interfaces.ItemRateUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.RateItem;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Logger;
import com.home.lepradroid.utils.Utils;

public class RateItemTask extends BaseTask
{
    private UUID            groupId;
    private UUID            postId;
    private RateType        type;
    private String          wtf;
    private String          id;
    private RateValueType   value;

    static final Class<?>[] argsClassesOnItemRateUpdate = new Class[4];
    static Method methodOnItemRateUpdate;
    static
    {
        try
        {
            argsClassesOnItemRateUpdate[0] = UUID.class;
            argsClassesOnItemRateUpdate[1] = UUID.class;
            argsClassesOnItemRateUpdate[2] = int.class;
            argsClassesOnItemRateUpdate[3] = boolean.class;
            methodOnItemRateUpdate = ItemRateUpdateListener.class.getMethod("OnItemRateUpdate", argsClassesOnItemRateUpdate);
        }
        catch (Throwable t)
        {
            Logger.e(t);
        }
    }

    @SuppressWarnings("unchecked")
    public void notifyOnItemRateUpdate(boolean successful, int newRating)
    {
        final List<ItemRateUpdateListener> listeners = ListenersWorker.Instance().getListeners(ItemRateUpdateListener.class);
        final Object args[] = new Object[4];
        args[0] = groupId;
        args[1] = postId;
        args[2] = newRating;
        args[3] = successful;

        for(ItemRateUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnItemRateUpdate, args)));
        }
    }

    public RateItemTask(UUID groupId, UUID postId, RateType type, String wtf, String id, RateValueType value)
    {
        this.groupId = groupId;
        this.postId = postId;
        this.type = type;
        this.wtf = wtf;
        this.id = id;
        this.value = value;
    }

    public RateItemTask(RateType type, String wtf, String id, RateValueType value)
    {
        this.type = type;
        this.wtf = wtf;
        this.id = id;
        this.value = value;
    }


    @Override
    protected Throwable doInBackground(Void... params)
    {
        try
        {
            String response = ServerWorker.Instance().rateItem(type, wtf, id, value);
            System.out.println("response = " + response);
            if(     Utils.isIntNumber(response) ||
                    groupId.equals(Commons.FAVORITE_POSTS_ID) ||
                    groupId.equals(Commons.MYSTUFF_POSTS_ID))
            {
                RateItem item = null;

                switch (type)
                {
                    case POST:
                        item = ServerWorker.Instance().getPostById(groupId, postId);
                        item.Rating = (groupId.equals(Commons.FAVORITE_POSTS_ID) || groupId.equals(Commons.MYSTUFF_POSTS_ID)) ? item.Rating : Integer.valueOf(response);
                        break;
                    case KARMA:
                        item = ServerWorker.Instance().getAuthorById(id);
                        item.Rating = Integer.valueOf(response);
                        break;
                    default:
                        break;
                }



                switch (value)
                {
                case MINUS:
                    item.PlusVoted = false;
                    item.MinusVoted = true;
                    break;
                case PLUS:
                    item.PlusVoted = true;
                    item.MinusVoted = false;
                    break;
                default:
                    break;
                }
                notifyOnItemRateUpdate(true, item.Rating);
            }
            else
                notifyOnItemRateUpdate(false, 0);

        }
        catch (Exception e)
        {
            setException(e);
            notifyOnItemRateUpdate(false, 0);
        }

        return e;
    }

}
