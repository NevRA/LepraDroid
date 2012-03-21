package com.home.lepradroid.tasks;

import android.text.TextUtils;
import android.util.Pair;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.PostsUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Logger;
import com.home.lepradroid.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetPostTask extends BaseTask
{
    private UUID groupId;
    private String url;
    private boolean refresh = true;
    private boolean receivedPosts = false;
    private boolean isCustomBlogPosts = false;
    private int page = 0;

    static final Class<?>[] argsClassesOnPostsUpdateBegin = new Class[2];
    static final Class<?>[] argsClassesOnPostsUpdate = new Class[2];
    static final Class<?>[] argsClassesOnPostsUpdateFinished = new Class[3];

    static Method methodOnPostsUpdateBegin;
    static Method methodOnPostsUpdate;
    static Method methodOnPostsUpdateFinished;
    static
    {
        try
        {
        	argsClassesOnPostsUpdate[0] = UUID.class;
        	argsClassesOnPostsUpdate[1] = int.class;
            methodOnPostsUpdate = PostsUpdateListener.class.getMethod("OnPostsUpdate", argsClassesOnPostsUpdate);

            argsClassesOnPostsUpdateBegin[0] = UUID.class;
            argsClassesOnPostsUpdateBegin[1] = int.class;
            methodOnPostsUpdateBegin = PostsUpdateListener.class.getMethod("OnPostsUpdateBegin", argsClassesOnPostsUpdateBegin);

            argsClassesOnPostsUpdateFinished[0] = UUID.class;
            argsClassesOnPostsUpdateFinished[1] = int.class;
            argsClassesOnPostsUpdateFinished[2] = boolean.class;
            methodOnPostsUpdateFinished = PostsUpdateListener.class.getMethod("OnPostsUpdateFinished", argsClassesOnPostsUpdateFinished);
        }
        catch (Throwable t)
        {
            Logger.e(t);
        }
    }

    public GetPostTask(UUID groupId, String url)
    {
        this.groupId = groupId;
        this.url = url;
        isCustomBlogPosts = Utils.isCustomBlogPosts(groupId);
    }

    public GetPostTask(UUID groupId, String url, int page, boolean refresh)
    {       
        this.groupId = groupId;
        this.url = url;
        this.refresh = refresh;
        this.page = page;
        isCustomBlogPosts = Utils.isCustomBlogPosts(groupId);
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutPostsUpdateBegin()
    {
        final List<PostsUpdateListener> listeners = ListenersWorker.Instance().getListeners(PostsUpdateListener.class);
        final Object args[] = new Object[2];
        args[0] = groupId;
        args[1] = page;
        
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
        args[1] = page;
        
        for(PostsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnPostsUpdate, args)));
        }
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutPostsUpdateFinished(boolean successful)
    {
        final List<PostsUpdateListener> listeners = ListenersWorker.Instance().getListeners(PostsUpdateListener.class);
        final Object args[] = new Object[3];
        args[0] = groupId;
        args[1] = page;
        args[2] = successful;
        
        for(PostsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnPostsUpdateFinished, args)));
        }
    }

    @Override
    protected Throwable doInBackground(Void... params)
    {
        long startTime = System.nanoTime();
        ArrayList<BaseItem> items = new ArrayList<BaseItem>();
               
        try
        {
            int num = -1;
            
            if(refresh)
                ServerWorker.Instance().clearPostsById(groupId);
            
            notifyAboutPostsUpdateBegin();
            
            String html = ServerWorker.Instance().getContent(url);
            String postOrd = "<div class=\"post ord";
            String postOrdGolden = "<div class=\"post golden ord";
            String endStr = "<div id=\"content\"";



                int start = html.indexOf(postOrd, 0);
                if (start == -1)
                    start = html.indexOf(postOrdGolden, 0);
                int end = html.indexOf(endStr, start);


                String postHtml = Utils.replaceBadHtmlTags(html.substring(start, end));
                Element content = Jsoup.parse(postHtml);
                

                Element element = content.getElementsByClass("dt").first();

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

                        String src = image.attr("src");
                        if(!TextUtils.isEmpty(src))
                            post.Html = post.Html.replace(src, "http://src.sencha.io/303/303/" + src);
                    }
                }

                Elements rating = content.getElementsByTag("em");
                if(!rating.isEmpty())
                    post.Rating = Integer.valueOf(rating.first().text());
                else
                    post.voteDisabled = true;
                
                post.PlusVoted = postHtml.contains("class=\"plus voted\"");
                post.MinusVoted = postHtml.contains("class=\"minus voted\"");
                

                post.Url = url;
                post.Pid = post.Url.split("comments/")[1];

                Elements author = content.getElementsByClass("p");
                post.Author = author.first().getElementsByTag("a").get(1).text();
                post.Signature = author.first().text().split("\\|")[0].replace(post.Author, "<b>" + post.Author + "</b>");
                items.add(post);

            
            if(!items.isEmpty())
            {
                ServerWorker.Instance().addNewPosts(groupId, items);
                notifyAboutPostsUpdateFinished(true);
            }
            else
                notifyAboutPostsUpdateFinished(receivedPosts);
        }
        catch (Throwable t)
        {
            setException(t);
            notifyAboutPostsUpdateFinished(false);
        }
        finally
        {
            Logger.d("GetPostsTask time:" + Long.toString(System.nanoTime() - startTime));
        }
        
        return e;
    }
}
