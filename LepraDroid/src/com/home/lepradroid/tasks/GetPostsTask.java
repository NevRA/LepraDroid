package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.text.TextUtils;
import android.util.Pair;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.PostsUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Logger;

public class GetPostsTask extends BaseTask
{
    private UUID groupId;
    private String url;
    
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
    
    public GetPostsTask(UUID groupId, String url)
    {       
        this.groupId = groupId;
        this.url = url;
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
        final ArrayList<BaseItem> items = new ArrayList<BaseItem>();
               
        try
        {
            int num = -1;
            
            ServerWorker.Instance().clearPostsById(groupId);
            
            notifyAboutPostsUpdateBegin();
            
            String html = ServerWorker.Instance().getContent(url);
            final String postOrd = "<div class=\"post ord";
            int currentPos = 0;

            boolean lastElement = false;
            
            do
            {
                if(isCancelled()) break;
                
                num++;
                
                int start = html.indexOf(postOrd, currentPos);
                int end = html.indexOf(postOrd, start + 100);
                
                if(end == -1)
                {
                    end = html.length();
                    lastElement = true;
                }
                
                currentPos = end;
                
                html = html.replaceAll("(&#150;|&#151;)", "-");
                
                final String postHtml = html.substring(start, end);
                final Element content = Jsoup.parse(postHtml);
                final Element element = content.getElementsByClass("dt").first();

                String text = element.text();
                Post post = new Post();
                post.Text = TextUtils.isEmpty(text) ? "..." : text;
                post.Html = element.html();
                
                Elements images = element.getElementsByTag("img");
                if(!images.isEmpty())
                {
                    post.ImageUrl = "http://src.sencha.io/80/80/" + images.first().attr("src");
                    
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

                Elements rating = content.getElementsByTag("em");
                post.Rating = Integer.valueOf(rating.first().text());
                
                post.PlusVoted = postHtml.contains("class=\"plus voted\"");
                post.MinusVoted = postHtml.contains("class=\"minus voted\"");
                
                Elements author = content.getElementsByClass("p");
                if(!author.isEmpty())
                {
                    Elements span = author.first().getElementsByTag("span");
                    Elements a = span.first().getElementsByTag("a");
                    String url = a.first().attr("href");
                    if(url.contains("http"))
                        post.Url = url;
                    else
                        post.Url = Commons.SITE_URL + url;
                    
                    post.Pid = post.Url.split("comments/")[1];
                    
                    if(a.size() == 2)
                    {
                        post.TotalComments = Integer.valueOf(a.get(0).text().split(" ")[0]);
                        post.NewComments = Integer.valueOf(a.get(1).text().split(" ")[0]);
                    }
                    else
                    {
                        if(!a.get(0).text().equals("комментировать"))
                            post.TotalComments = Integer.valueOf(a.get(0).text().split(" ")[0]);
                    }
                    
                    post.Author = author.first().getElementsByTag("a").first().text();
                    post.Signature = author.first().text().split("\\|")[0].replace(post.Author, "<b>" + post.Author + "</b>");
                }
                
                items.add(post);
                if(num%5 == 0 || lastElement)
                {
                    ServerWorker.Instance().addNewPosts(groupId, items);
                    notifyAboutPostsUpdate();
                    
                    items.clear();
                }
            }
            while (lastElement == false);       
        }
        catch (Throwable t)
        {
            setException(t);
        }
        finally
        {
            if(!items.isEmpty())
                ServerWorker.Instance().addNewPosts(groupId, items);
            
            notifyAboutPostsUpdate();
            
            Logger.d("GetPostsTask time:" + Long.toString(System.nanoTime() - startTime));
        }
        
        return e;
    }
}
