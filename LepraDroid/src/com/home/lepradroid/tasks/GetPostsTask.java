package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.text.TextUtils;
import android.util.Pair;

import com.home.lepradroid.LepraDroidApplication;
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

public class GetPostsTask extends BaseTask
{
    private UUID groupId;
    private String url;
    private boolean refresh = true;
    private boolean receivedPosts = false;
    private boolean isCustomBlogPosts = false;
    private int page = 0;
    private boolean isImagesEnabled = true;
    
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
    
    public GetPostsTask(UUID groupId, String url)
    {
        this.groupId = groupId;
        this.url = url;
        isCustomBlogPosts = Utils.isCustomBlogPosts(groupId);
        isImagesEnabled = Utils.isImagesEnabled(LepraDroidApplication.getInstance());
    }
    
    public GetPostsTask(UUID groupId, String url, int page, boolean refresh)
    {       
        this.groupId = groupId;
        this.url = url;
        this.refresh = refresh;
        this.page = page;
        isCustomBlogPosts = Utils.isCustomBlogPosts(groupId);
        isImagesEnabled = Utils.isImagesEnabled(LepraDroidApplication.getInstance());
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
            
            String html = ServerWorker.Instance().getContent(url + ((groupId.equals(Commons.MAIN_POSTS_ID) || isCustomBlogPosts ) ? "pages/" + Integer.toString(page + 1) : ""));
            final String postOrd = "<div class=\"post ord";
            int currentPos = 0;

            boolean lastElement = false;
            
            do
            {
                if(isCancelled()) break;
                
                num++;
                
                int start = html.indexOf(postOrd, currentPos);
                int end = html.indexOf(postOrd, start + 300);
                
                if(     start == -1 && 
                        html.indexOf("<title>Лепрозорий: вход</title>") != -1)
                {
                    new LogoutTask().execute();
                    cancel(false);
                    break;
                }
                
                if(     num == 0 && 
                        groupId.equals(Commons.MAIN_POSTS_ID) &&
                        page == 0)
                {
                    String header = html.substring(0, start);
                    Element content = Jsoup.parse(header);
                    
                    Element filter = content.getElementById("js-showonindex"); 
                    SettingsWorker.Instance().saveMainThreshold(Integer.valueOf(filter.attr("value"))); 
                    
                    Element vote = content.getElementById("content_left_inner");
                    Element script = vote.getElementsByTag("script").first();
                    
                    Pattern pattern = Pattern.compile("wtf_vote = '(.+)'");
                    Matcher matcher = pattern.matcher(script.data());
                    if(matcher.find())
                        SettingsWorker.Instance().saveVoteWtf(matcher.group(1));
                }
                
                if(end == -1)
                {
                    end = html.length();
                    lastElement = true;
                }
                
                currentPos = end;
                
                String postHtml = Utils.replaceBadHtmlTags(html.substring(start, end));
                Element content = Jsoup.parse(postHtml);
                
                if(page == 0 && lastElement && (groupId.equals(Commons.MAIN_POSTS_ID) || isCustomBlogPosts))
                {
                    Element element = content.getElementById("total_pages");
                    ServerWorker.Instance().addPostPagesCount(groupId, element == null ? 0 : Integer.valueOf(element.getElementsByTag("strong").first().text()));
                }
                
                Post post = new Post();
                
                Element element = content.getElementsByClass("dt").first();
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
                        
                        imgs.add(new Pair<String, String>(id, src));
                        
                        image.removeAttr("width");
                        image.removeAttr("height");
                        image.removeAttr("src");
                        image.removeAttr("id");
                        
                        image.attributes().put("id", id);
                        image.attributes().put("src", Commons.IMAGE_STUB);
                        
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
                    
                    if(groupId.equals(Commons.INBOX_POSTS_ID))
                        post.Pid = post.Url.split("inbox/")[1];
                    else
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
                
                if(isCancelled()) break;
                
                items.add(post);
                if(num%5 == 0 || lastElement)
                {
                    receivedPosts = true;
                    ServerWorker.Instance().addNewPosts(groupId, items);
                    notifyAboutPostsUpdate();
                    
                    items.clear();
                }
            }
            while (lastElement == false); 
            
            if(!isCancelled())
            {
                if(!items.isEmpty())
                {
                    ServerWorker.Instance().addNewPosts(groupId, items);
                    notifyAboutPostsUpdateFinished(true);
                }
                else
                    notifyAboutPostsUpdateFinished(receivedPosts);
            }
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
