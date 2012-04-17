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

import com.home.lepradroid.LepraDroidApplication;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.PostUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Logger;
import com.home.lepradroid.utils.Utils;

public class GetPostTask extends BaseTask
{
    private UUID    groupId;
    private UUID    postId;
    private String  url;
    private boolean isImagesEnabled = true;

    static final Class<?>[] argsClassesOnPostUpdateFinished = new Class[2];
    static Method methodOnPostUpdateFinished;
    static
    {
        try
        {
            argsClassesOnPostUpdateFinished[0] = UUID.class;
            argsClassesOnPostUpdateFinished[1] = boolean.class;
            methodOnPostUpdateFinished = PostUpdateListener.class.getMethod("OnPostUpdateFinished", argsClassesOnPostUpdateFinished);
        }
        catch (Throwable t)
        {
            Logger.e(t);
        }
    }

    public GetPostTask(UUID groupId, UUID postId, String url)
    {
        this.groupId = groupId;
        this.postId = postId;
        this.url = url;
        isImagesEnabled = Utils.isImagesEnabled(LepraDroidApplication.getInstance());
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutPostUpdateFinished(boolean successful)
    {
        final List<PostUpdateListener> listeners = ListenersWorker.Instance().getListeners(PostUpdateListener.class);
        final Object args[] = new Object[2];
        args[0] = postId;
        args[1] = successful;
        
        for(PostUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnPostUpdateFinished, args)));
        }
    }

    @Override
    protected Throwable doInBackground(Void... params)
    {
        long startTime = System.nanoTime();
               
        try
        {
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

            Post post = new Post();
            post.Id = postId;
            
            
            Elements images = element.getElementsByTag("img");
            int imageNum = 0;
            List<Pair<String, String>> imgs = new ArrayList<Pair<String, String>>();
            for (Element image : images)
            {
                String src = image.attr("src");
                if(isImagesEnabled && !TextUtils.isEmpty(src))
                {
                    if(TextUtils.isEmpty(post.ImageUrl))
                        post.ImageUrl = "http://src.sencha.io/80/80/" + image.attr("src");
                    
                    String id = "img" + Integer.valueOf(imageNum).toString();
                    
                    if(!image.parent().tag().getName().equalsIgnoreCase("a"))
                        image.wrap("<a href=" + "\"" + src + "\"></a>");
                   
                    image.removeAttr("width");
                    image.removeAttr("height");
                    image.removeAttr("src");
                    image.removeAttr("id");
                    
                    image.attributes().put("id", id);
                    image.attributes().put("src", Commons.IMAGE_STUB);
                    image.attributes().put("onLoad", "getSrcData(\"" + id + "\", \"" + src + "\", " + Integer.valueOf(0).toString() + ");");
                    imgs.add(new Pair<String, String>(id, src));
                    
                    imageNum++;
                }
                else
                    image.remove();
            }
            
            post.Html = Utils.getImagesStub(imgs, 0) + element.html();

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

            ServerWorker.Instance().addNewPost(groupId, post);
            
            notifyAboutPostUpdateFinished(true);
        }
        catch (Throwable t)
        {
            setException(t);
            notifyAboutPostUpdateFinished(false);
        }
        finally
        {
            Logger.d("GetPostTask time:" + Long.toString(System.nanoTime() - startTime));
        }
        
        return e;
    }
}
