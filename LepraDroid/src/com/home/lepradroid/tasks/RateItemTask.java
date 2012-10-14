package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import android.util.Pair;

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
    private Post post;
    private UUID commentId;
    private RateType type;
    private String wtf;
    private String userId;
    private RateValueType valueType;

    static final Class<?>[] argsClassesOnPostRateUpdate = new Class[3];
    static final Class<?>[] argsClassesOnCommentRateUpdate = new Class[3];
    static final Class<?>[] argsClassesOnAuthorRateUpdate = new Class[2];
    static Method methodOnPostRateUpdate;
    static Method methodOnCommentRateUpdate;
    static Method methodOnAuthorRateUpdate;

    static
    {
        try
        {
            argsClassesOnPostRateUpdate[0] = UUID.class;
            argsClassesOnPostRateUpdate[1] = int.class;
            argsClassesOnPostRateUpdate[2] = boolean.class;
            
            argsClassesOnCommentRateUpdate[0] = UUID.class;
            argsClassesOnCommentRateUpdate[1] = UUID.class;
            argsClassesOnCommentRateUpdate[2] = boolean.class;
            
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
    public void notifyOnAuthorRateUpdate(boolean successful)
    {
        final List<ItemRateUpdateListener> listeners = ListenersWorker
                .Instance().getListeners(ItemRateUpdateListener.class);
        final Object args[] = new Object[2];
        args[0] = userId;
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
        final Object args[] = new Object[3];
        args[0] = post.getId();
        args[1] = newRating;
        args[2] = successful;

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
        final Object args[] = new Object[3];
        args[0] = post.getId();
        args[1] = commentId;
        args[2] = successful;

        for (ItemRateUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(
                    listener, new Pair<Method, Object[]>(
                            methodOnCommentRateUpdate, args)));
        }
    }

    public RateItemTask(UUID postId, RateValueType valueType)
    {
        post = (Post)ServerWorker.Instance().getPostById(postId);
        this.type = RateType.POST;
        this.wtf = SettingsWorker.Instance().loadVoteWtf();
        this.valueType = valueType;
    }
    
    public RateItemTask(UUID postId, UUID commentId, RateValueType valueType)
    {
        post = (Post)ServerWorker.Instance().getPostById(postId);
        this.type = RateType.COMMENT;
        this.wtf = SettingsWorker.Instance().loadVoteWtf();
        this.valueType = valueType;
        this.commentId = commentId;
    }

    public RateItemTask(String userId, RateValueType valueType)
    {
        this.type = RateType.KARMA;
        this.wtf = SettingsWorker.Instance().loadVoteKarmaWtf();
        this.userId = userId;
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
                response = ServerWorker.Instance().rateItemRequest(type, wtf, "", post.getLepraId(),
                        valueType,
                        valueType == RateValueType.MINUS ? "-1" : "1");
                break;
            case COMMENT:
                Comment comment = (Comment)ServerWorker.Instance().getComment(post.getId(), commentId);
                response = ServerWorker.Instance().rateItemRequest(type, wtf, comment.getLepraId(), post.getLepraId(),
                        valueType,
                        valueType == RateValueType.MINUS ? "-1" : "1");
                if(!post.isMain())
                {
                    if(post.getVoteWeight() == -1)
                    {
                        new SetVoteWeightTask(post.getId(), comment.getLepraId())
                            .execute()
                                .get();
                    }
                }
                break;
            case KARMA:
                /*response = */ServerWorker.Instance()
                        .rateItemRequest(type, wtf, userId, "", valueType,
                                valueType == RateValueType.MINUS ? "3" : "1");
                response = ServerWorker.Instance()
                        .rateItemRequest(type, wtf, userId, "", valueType,
                                valueType == RateValueType.MINUS ? "4" : "2");
                break;
            default:
                break;
            }

            if (    Utils.isIntNumber(response)
                    || post.isFavorite()
                    || post.isMyStuff())
            {
                RateItem item = null;

                switch (type)
                {
                case POST:
                    item = post;
                    item.setRating((
                            post.isFavorite() ||
                            post.isMyStuff()) ?
                                item.getRating() :
                                Short.valueOf(response));
                    break;
                case COMMENT:
                    item = ServerWorker.Instance().getComment(post.getId(), commentId);
                    item.setRating(Short.valueOf(response));
                    break;
                case KARMA:
                    item = ServerWorker.Instance().getAuthorById(userId);
                    Throwable res = new GetAuthorTask(((Author)item).getUserName(), true).execute().get();
                    if(res != null)
                        throw res;
                    break;
                default:
                    break;
                }

                switch (valueType)
                {
                case MINUS:
                    item.setPlusVoted(false);
                    item.setMinusVoted(true);
                    break;
                case PLUS:
                    item.setPlusVoted(true);
                    item.setMinusVoted(false);
                    break;
                default:
                    break;
                }
                
                notifyOnRate(true, item.getRating());
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
            notifyOnAuthorRateUpdate(successful);
        if(type == RateType.POST)
            notifyOnPostRateUpdate(successful, newRating);
        if(type == RateType.COMMENT)
            notifyOnCommentRateUpdate(successful);
    }
}
