package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.text.TextUtils;
import android.util.Pair;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.CommentsUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Comment;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Logger;

public class GetCommentsTask extends BaseTask
{
    private UUID groupId;
    private UUID id;
    static final Class<?>[] argsClassesOnCommentsUpdate = new Class[1];
    static final Class<?>[] argsClassesOnCommentsUpdateBegin = new Class[1];
    static Method methodOnCommentsUpdate;
    static Method methodOnCommentsUpdateBegin;
    static 
    {
        try
        {
            argsClassesOnCommentsUpdate[0] = UUID.class;
            methodOnCommentsUpdate = CommentsUpdateListener.class.getMethod("OnCommentsUpdate", argsClassesOnCommentsUpdate); 
            
            argsClassesOnCommentsUpdateBegin[0] = UUID.class;
            methodOnCommentsUpdateBegin = CommentsUpdateListener.class.getMethod("OnCommentsUpdateBegin", argsClassesOnCommentsUpdateBegin); 
        }
        catch (Throwable t) 
        {           
            Logger.e(t);
        }        
    }
    
    @Override
    public void finish()
    {
        super.finish();
    }
    
    public GetCommentsTask(UUID groupId, UUID id)
    {
        this.groupId = groupId;
        this.id = id;
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutCommentsUpdateBegin()
    {
        final List<CommentsUpdateListener> listeners = ListenersWorker.Instance().getListeners(CommentsUpdateListener.class);
        final Object args[] = new Object[1];
        args[0] = id;
        
        for(CommentsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnCommentsUpdateBegin, args)));
        }
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutCommentsUpdate()
    {
        final List<CommentsUpdateListener> listeners = ListenersWorker.Instance().getListeners(CommentsUpdateListener.class);
        final Object args[] = new Object[1];
        args[0] = id;
        
        for(CommentsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnCommentsUpdate, args)));
        }
    }
    
    @Override
    protected Throwable doInBackground(Void... arg0)
    {
        ArrayList<BaseItem> items = new ArrayList<BaseItem>();
        
        final long startTime = System.nanoTime();
        
        try
        {
            ServerWorker.Instance().clearCommentsById(id);
            notifyAboutCommentsUpdateBegin();

            BaseItem post = ServerWorker.Instance().getPostById(groupId, id);
            if(post == null)
                return null; // TODO message
            
            final Element root = ServerWorker.Instance().getContent(post.Url); 
            final Element holder = root.getElementById("js-commentsHolder");
            final Elements comments = holder.getElementsByClass("dt");
            for (@SuppressWarnings("rawtypes")
            Iterator iterator = comments.iterator(); iterator.hasNext();)
            {   
                Element element = (Element) iterator.next();
                Comment comment = new Comment();
                comment.Text = element.text();
                comment.Html = element.html();
                
                Elements images = element.getElementsByTag("img");
                if(!images.isEmpty())
                {
                    post.ImageUrl = images.first().attr("src");
                    
                    for (Element image : images)
                    {
                        String width = image.attr("width");
                        if(!TextUtils.isEmpty(width))
                            comment.Html = comment.Html.replace("width=\"" + width + "\"", "");
                        
                        String height = image.attr("height");
                        if(!TextUtils.isEmpty(height))
                            comment.Html = comment.Html.replace("height=\"" + height + "\"", "");
                        
                        comment.Html = comment.Html.replace(image.attr("src"), "http://src.sencha.io/305/305/" + image.attr("src"));
                    }
                }

                Element parent = element.parent();
                Elements author = parent.getElementsByClass("p");
                if(!author.isEmpty())
                {
                    Elements a = author.first().getElementsByTag("a");
                    comment.Url = Commons.SITE_URL + a.first().attr("href");
                    
                    comment.Author = a.get(1).text();
                    comment.Signature = author.first().text().split("\\|")[0].replace(post.Author, "<b>" + post.Author + "</b>");
                }
                
                Elements vote = parent.getElementsByClass("vote");
                if(!vote.isEmpty())
                {
                    Elements rating = vote.first().getElementsByTag("em");
                    comment.Rating = Integer.valueOf(rating.first().text());
                }
                
                items.add(comment);
            }
        }
        catch (Throwable t)
        {
            setException(t);
        }
        finally
        {
            ServerWorker.Instance().addNewComments(groupId, id, items);
            notifyAboutCommentsUpdate();
            
            Logger.d("GetBlogsTask time:" + Long.toString(System.nanoTime() - startTime));
        }
                
        return e;
    }

}
