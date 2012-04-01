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
import com.home.lepradroid.objects.Author;
import com.home.lepradroid.objects.Comment;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.objects.RateItem;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Logger;
import com.home.lepradroid.utils.Utils;

public class RateItemTask extends BaseTask
{
    private UUID groupId;
    private UUID postId;
    private UUID commentId;
    private RateType type;
    private String wtf;
    private String id;
    private RateValueType valueType;

    static final Class<?>[] argsClassesOnPostRateUpdate = new Class[4];
    static final Class<?>[] argsClassesOnCommentRateUpdate = new Class[4];
    static final Class<?>[] argsClassesOnAuthorRateUpdate = new Class[2];
    static Method methodOnPostRateUpdate;
    static Method methodOnCommentRateUpdate;
    static Method methodOnAuthorRateUpdate;

    static
    {
        try
        {
            argsClassesOnPostRateUpdate[0] = UUID.class;
            argsClassesOnPostRateUpdate[1] = UUID.class;
            argsClassesOnPostRateUpdate[2] = int.class;
            argsClassesOnPostRateUpdate[3] = boolean.class;
            
            argsClassesOnCommentRateUpdate[0] = UUID.class;
            argsClassesOnCommentRateUpdate[1] = UUID.class;
            argsClassesOnCommentRateUpdate[2] = UUID.class;
            argsClassesOnCommentRateUpdate[3] = boolean.class;
            
            argsClassesOnAuthorRateUpdate[0] = String.class;
            argsClassesOnAuthorRateUpdate[1] = boolean.class;
            
            methodOnPostRateUpdate = ItemRateUpdateListener.class.getMethod(
                    "OnPostRateUpdate", argsClassesOnPostRateUpdate);
            
            methodOnCommentRateUpdate = ItemRateUpdateListener.class.getMethod(
                    "OnCommentRateUpdate", argsClassesOnCommentRateUpdate);
            
            methodOnAuthorRateUpdate = ItemRateUpdateListener.class.getMethod(
                    "OnAuthorRateUpdate", argsClassesOnAuthorRateUpdate);
        }
        catch (Throwable t)
        {
            Logger.e(t);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void notifyOnAuthorRateUpdate(String userId, boolean successful)
    {
        final List<ItemRateUpdateListener> listeners = ListenersWorker
                .Instance().getListeners(ItemRateUpdateListener.class);
        final Object args[] = new Object[2];
        args[0] = id;
        args[1] = successful;

        for (ItemRateUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(
                    listener, new Pair<Method, Object[]>(
                            methodOnAuthorRateUpdate, args)));
        }
    }

    @SuppressWarnings("unchecked")
    public void notifyOnPostRateUpdate(boolean successful, int newRating)
    {
        final List<ItemRateUpdateListener> listeners = ListenersWorker
                .Instance().getListeners(ItemRateUpdateListener.class);
        final Object args[] = new Object[4];
        args[0] = groupId;
        args[1] = postId;
        args[2] = newRating;
        args[3] = successful;

        for (ItemRateUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(
                    listener, new Pair<Method, Object[]>(
                            methodOnPostRateUpdate, args)));
        }
    }
    
    @SuppressWarnings("unchecked")
    public void notifyOnCommentRateUpdate(boolean successful)
    {
        final List<ItemRateUpdateListener> listeners = ListenersWorker
                .Instance().getListeners(ItemRateUpdateListener.class);
        final Object args[] = new Object[4];
        args[0] = groupId;
        args[1] = postId;
        args[2] = commentId;
        args[3] = successful;

        for (ItemRateUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(
                    listener, new Pair<Method, Object[]>(
                            methodOnCommentRateUpdate, args)));
        }
    }

    public RateItemTask(UUID groupId, UUID postId, RateValueType valueType)
    {
        this.groupId = groupId;
        this.postId = postId;
        this.type = RateType.POST;
        this.wtf = SettingsWorker.Instance().loadVoteWtf();
        this.valueType = valueType;
        this.id = ((Post)ServerWorker.Instance().getPostById(groupId, postId)).Pid;
    }
    
    public RateItemTask(UUID groupId, UUID postId, UUID commentId, RateValueType valueType)
    {
        this.groupId = groupId;
        this.postId = postId;
        this.type = RateType.COMMENT;
        this.wtf = SettingsWorker.Instance().loadVoteWtf();
        this.valueType = valueType;
        this.id = ((Post)ServerWorker.Instance().getPostById(groupId, postId)).Pid;
        this.commentId = commentId;
    }

    public RateItemTask(RateType type, String wtf, String id,
            RateValueType valueType)
    {
        this.type = type;
        this.wtf = wtf;
        this.id = id;
        this.valueType = valueType;
    }

    @Override
    protected Throwable doInBackground(Void... params)
    {
        try
        {
            String response = ""; 

            switch (type)
            {
            case POST:
                response = ServerWorker.Instance().rateItem(type, wtf, "", id,
                        valueType,
                        valueType == RateValueType.MINUS ? "-1" : "1");
                break;
            case COMMENT:
                Comment comment = (Comment)ServerWorker.Instance().getComment(groupId, postId, commentId);
                response = ServerWorker.Instance().rateItem(type, wtf, comment.Pid, id,
                        valueType,
                        valueType == RateValueType.MINUS ? "-1" : "1");
                break;
            case KARMA:
                response = ServerWorker.Instance()
                        .rateItem(type, wtf, id, "", valueType,
                                valueType == RateValueType.MINUS ? "3" : "1");
                response = ServerWorker.Instance()
                        .rateItem(type, wtf, id, "", valueType,
                                valueType == RateValueType.MINUS ? "4" : "2");
                break;
            default:
                break;
            }

            if (Utils.isIntNumber(response)
                    || groupId.equals(Commons.FAVORITE_POSTS_ID)
                    || groupId.equals(Commons.MYSTUFF_POSTS_ID))
            {
                RateItem item = null;

                switch (type)
                {
                case POST:
                    item = ServerWorker.Instance().getPostById(groupId, postId);
                    item.Rating = (groupId.equals(Commons.FAVORITE_POSTS_ID) || groupId
                            .equals(Commons.MYSTUFF_POSTS_ID)) ? item.Rating
                            : Integer.valueOf(response);
                    break;
                case COMMENT:
                    item = ServerWorker.Instance().getComment(groupId, postId, commentId);
                    item.Rating = Integer.valueOf(response);
                    break;
                case KARMA:
                    item = ServerWorker.Instance().getAuthorById(id);
                    Throwable res = new GetAuthorTask(((Author)item).UserName, true).execute().get();
                    if(res != null)
                        throw res;
                    break;
                default:
                    break;
                }

                switch (valueType)
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
                
                notifyOnRate(true, item.Rating);
            }
            else
                notifyOnRate(false, 0);
        }
        catch (Throwable e)
        {
            setException(e);
            
            notifyOnRate(false, 0);
        }

        return e;
    }
    
    private void notifyOnRate(boolean successful, int newRating)
    {
        if(type == RateType.KARMA) 
            notifyOnAuthorRateUpdate(id, successful);
        if(type == RateType.POST)
            notifyOnPostRateUpdate(successful, newRating);
        else
            notifyOnCommentRateUpdate(successful);
    }
}
