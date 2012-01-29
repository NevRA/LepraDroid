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

import com.home.lepradroid.R;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.PostsUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Logger;
import com.home.lepradroid.utils.Utils;

public class GetPostsTask extends BaseTask
{
    private UUID groupId;
    private String url;
    private LoadImagesTask loadImagesTask;
    private boolean includeImages;
    
    static final Class<?>[] argsClassesOnPostsUpdate = new Class[2];
    static final Class<?>[] argsClassesOnPostsUpdateBegin = new Class[1];
    static Method methodOnPostsUpdate;
    static Method methodOnPostsUpdateBegin;
    static 
    {
        try
        {
        	argsClassesOnPostsUpdate[0] = UUID.class;
        	argsClassesOnPostsUpdate[1] = boolean.class;
            methodOnPostsUpdate = PostsUpdateListener.class.getMethod("OnPostsUpdate", argsClassesOnPostsUpdate); 
            
            argsClassesOnPostsUpdateBegin[0] = UUID.class;
            methodOnPostsUpdateBegin = PostsUpdateListener.class.getMethod("OnPostsUpdateBegin", argsClassesOnPostsUpdateBegin); 
        }
        catch (Throwable t) 
        {           
            Logger.e(t);
        }        
    }
    
    @Override
	public void finish()
    {
    	if(loadImagesTask != null) loadImagesTask.finish();
    	super.finish();
    }
    
    public GetPostsTask(UUID groupId, String url, boolean includeImages)
    {       
        this.groupId = groupId;
        this.url = url;
        this.includeImages = includeImages;
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutPostsUpdateBegin()
    {
        final List<PostsUpdateListener> listeners = ListenersWorker.Instance().getListeners(PostsUpdateListener.class);
        final Object args[] = new Object[1];
        args[0] = groupId;
        
        for(PostsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnPostsUpdateBegin, args)));
        }
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutPostsUpdate()
    {
        final List<PostsUpdateListener> listeners = ListenersWorker.Instance().getListeners(PostsUpdateListener.class);
        final Object args[] = new Object[2];
        args[0] = groupId;
        args[1] = true; // TODO add new 'load new posts'
        
        
        for(PostsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnPostsUpdate, args)));
        }
    }

    @Override
    protected Throwable doInBackground(Void... params)
    {
        final long startTime = System.nanoTime();
        
        try
        {
            int num = -1;
            
            ServerWorker.Instance().clearPostsById(groupId);
            
            notifyAboutPostsUpdateBegin();
            
            ArrayList<BaseItem> items = new ArrayList<BaseItem>();
            
            final Element root = ServerWorker.Instance().getContent(url); 
            final Element content = root.getElementById("content");
            final Elements posts = content.getElementsByClass("dt");
            for (@SuppressWarnings("rawtypes")
            Iterator iterator = posts.iterator(); iterator.hasNext();)
            {
                num++;
                Element element = (Element) iterator.next();
                String text = element.text();
                Post post = new Post();
                post.Text = TextUtils.isEmpty(text) ? "..." : text;
                post.Html = element.html();
                
                Elements images = element.getElementsByTag("img");
                if(!images.isEmpty())
                {
                    post.ImageUrl = images.first().attr("src");
                    
                    for (Element image : images)
                    {
                        String width = image.attr("width");
                        if(!TextUtils.isEmpty(width))
                            post.Html = post.Html.replace("width=\"" + width + "\"", "");
                        
                        String height = image.attr("height");
                        if(!TextUtils.isEmpty(height))
                            post.Html = post.Html.replace("height=\"" + height + "\"", "");
                        
                        post.Html = post.Html.replace(image.attr("src"), "http://src.sencha.io/303/303/" + image.attr("src"));
                    }
                }
                
                Element authorParent = element.parent();
                if(authorParent != null)
                {
                	Elements rating = authorParent.getElementsByTag("em");
                    post.Rating = Integer.valueOf(rating.first().text());
                    
                    Elements author = authorParent.getElementsByClass("p");
                    if(!author.isEmpty())
                    {
                        Elements span = author.first().getElementsByTag("span");
                        Elements a = span.first().getElementsByTag("a");
                        String url = a.first().attr("href");
                        if(url.contains("http"))
                            post.Url = url;
                        else
                            post.Url = Commons.SITE_URL + url;
                        
                        if(a.size() == 2)
                            post.Comments = a.get(0).text() + " / " + "<b>" + a.get(1).text() + "</b>";
                        else
                            post.Comments = Utils.getString(R.string.No_Comments);
                        
                        post.Author = author.first().getElementsByTag("a").first().text();
                        post.Signature = author.first().text().split("\\|")[0].replace(post.Author, "<b>" + post.Author + "</b>");
                    }
                }
                items.add(post);
                if(num%5 == 0)
                {
                    ServerWorker.Instance().addNewPosts(groupId, items);
                    notifyAboutPostsUpdate();
                    
                    items = new ArrayList<BaseItem>(0);
                }
            }
            
            if(includeImages)
            {
                loadImagesTask = new LoadImagesTask(groupId);
                loadImagesTask.execute();
            }
        }
        catch (Throwable t)
        {
            setException(t);
        }
        finally
        {
            notifyAboutPostsUpdate();
            
            Logger.d("GetPostsTask time:" + Long.toString(System.nanoTime() - startTime));
        }
        
        return e;
    }
}
