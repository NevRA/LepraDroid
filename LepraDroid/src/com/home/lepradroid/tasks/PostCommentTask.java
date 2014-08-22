package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import com.home.lepradroid.settings.SettingsWorker;
import org.json.JSONObject;

import android.util.Pair;

import com.home.lepradroid.interfaces.AddedCommentUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Comment;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Logger;

public class PostCommentTask extends BaseTask
{
    private String wtf;
    private String replyTo;
    private String pid;
    private String comment;
    private UUID   id;
    private short  level;
    
    static final Class<?>[] argsClassesOnAddedCommentUpdate = new Class[2];
    static Method methodOnAddedCommentUpdate;
    static 
    {
        try
        {
            argsClassesOnAddedCommentUpdate[0] = UUID.class;
            argsClassesOnAddedCommentUpdate[1] = Comment.class;
            methodOnAddedCommentUpdate = AddedCommentUpdateListener.class.getMethod("OnAddedCommentUpdate", argsClassesOnAddedCommentUpdate);
        }
        catch (Throwable t) 
        {           
            Logger.e(t);
        }        
    }
    
    public PostCommentTask(UUID id, String wtf, String replyTo, String pid, short level, String comment)
    {
        this.id = id;
        this.wtf = wtf;
        this.replyTo = replyTo;
        this.pid = pid;
        this.comment = comment;
        this.level = level;
    }
    
    @SuppressWarnings("unchecked")
    public void notifyOnAddedCommentUpdate(Comment comment)
    {
        final List<AddedCommentUpdateListener> listeners = ListenersWorker.Instance().getListeners(AddedCommentUpdateListener.class);
        final Object args[] = new Object[2];
        args[0] = id;
        args[1] = comment;
        
        for(AddedCommentUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnAddedCommentUpdate, args)));
        }
    }

    @Override
    protected Throwable doInBackground(Void... params)
    {
        try
        {
            final JSONObject json = new JSONObject(ServerWorker.Instance().postCommentRequest( replyTo, pid, comment)).getJSONObject("comment");
            final JSONObject user = json.getJSONObject("user");
            final Comment comment = new Comment();
            comment.setLevel(level);
            comment.setAuthor(SettingsWorker.Instance().loadUserName());
            comment.setParentLepraId(replyTo);
            comment.setLepraId(json.getString("id"));
            comment.setHtml(this.comment.replace("\n","<br/>"));

            comment.setSignature((user.getString("gender").equals("male") ? "Написал" : "Написала") + " " +
                    "<b>" + "<font color=\"#3270FF\">" + SettingsWorker.Instance().loadUserName() + "</font>" + "</b>" + " сейчас");
            
            notifyOnAddedCommentUpdate(comment);
        }
        catch (Exception e)
        {
            setException(e);
        }
        return e;
    }
}