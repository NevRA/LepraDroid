package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Pair;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.BlogsUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Blog;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Logger;
import com.home.lepradroid.utils.Utils;

public class GetBlogsTask extends BaseTask
{
    private LoadImagesTask loadImagesTask;
    private boolean includeImages;
    
    static final Class<?>[] argsClassesOnBlogsUpdate = new Class[1];
    static final Class<?>[] argsClassesOnBlogsUpdateBegin = new Class[0];
    static Method methodOnBlogsUpdate;
    static Method methodOnBlogsUpdateBegin;
    static 
    {
        try
        {
            argsClassesOnBlogsUpdate[0] = boolean.class;
            methodOnBlogsUpdate = BlogsUpdateListener.class.getMethod("OnBlogsUpdate", argsClassesOnBlogsUpdate); 
            
            methodOnBlogsUpdateBegin = BlogsUpdateListener.class.getMethod("OnBlogsUpdateBegin", argsClassesOnBlogsUpdateBegin); 
        }
        catch (Throwable t) 
        {           
            Logger.e(t);
        }        
    }
    
    public GetBlogsTask(boolean includeImages)
    {
        this.includeImages = includeImages;
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutBlogsUpdateBegin()
    {
        final List<BlogsUpdateListener> listeners = ListenersWorker.Instance().getListeners(BlogsUpdateListener.class);
        final Object args[] = new Object[0];
        
        for(BlogsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnBlogsUpdateBegin, args)));
        }
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutBlogsUpdate()
    {
        final List<BlogsUpdateListener> listeners = ListenersWorker.Instance().getListeners(BlogsUpdateListener.class);
        final Object args[] = new Object[1];
        args[0] = true;
        
        for(BlogsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnBlogsUpdate, args)));
        }
    }
    
    @Override
    protected Throwable doInBackground(Void... arg0)
    {
        final long startTime = System.nanoTime();
        
        try
        {
            notifyAboutBlogsUpdateBegin();
            
            final String html = ServerWorker.Instance().getContent(Commons.BLOGS_URL); 
            final Document document = Jsoup.parse(html);
            final Elements blogs = document.getElementsByClass("jj_general");
            for (@SuppressWarnings("rawtypes")
            Iterator iterator = blogs.iterator(); iterator.hasNext();)
            {
                Element element = (Element) iterator.next();
                Blog blog = new Blog();
                
                Elements images = element.getElementsByTag("img");
                if(!images.isEmpty())
                    blog.ImageUrl = images.first().attr("src");

                Elements title = element.getElementsByTag("h5");
                if(!title.isEmpty())
                    blog.Text = title.first().text();
                
                Elements author = element.getElementsByClass("jj_creator");
                if(!author.isEmpty())
                {
                    blog.Author = author.first().getElementsByTag("a").first().text();
                    blog.Signature = author.first().text();
                }
                
                ServerWorker.Instance().addNewBlog(blog);
            }
            
            if(includeImages)
            {
                
            }
        }
        catch (Throwable t)
        {
            setException(t);
        }
        finally
        {
            notifyAboutBlogsUpdate();
            
            Logger.d("GetBlogsTask time:" + Long.toString(System.nanoTime() - startTime));
        }
                
        return e;
    }

}
