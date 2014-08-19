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

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.PostsUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Logger;
import com.home.lepradroid.utils.Utils;

public class GetPostsTask extends BaseTask
{
    private UUID groupId;
    private String url;
    private Commons.PostsType type;
    private boolean refresh = true;
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
    
    public GetPostsTask(UUID groupId, String url, Commons.PostsType type)
    {
        this.groupId = groupId;
        this.url = url;
        this.type = type;
        isImagesEnabled = Utils.isImagesEnabled();
    }
    
    public GetPostsTask(UUID groupId, String url, Commons.PostsType type, int page, boolean refresh)
    {       
        this.groupId = groupId;
        this.url = url;
        this.refresh = refresh;
        this.page = page;
        this.type = type;
        isImagesEnabled = Utils.isImagesEnabled();
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
        long startTime = System.currentTimeMillis();
               
        try
        {
            int num = -1;
            int currentPos = 0;
            int imageSize = Utils.getPostImagePreviewIsPixelsSize();

            boolean lastElement = false;

            String postOrd = "<div class=\"post ";
            String senchaPrefix = String.format("http://src.sencha.io/%d/%d/", imageSize, imageSize);
            String html = ServerWorker.Instance().getContent(url + getPageNum());

            if(refresh)
                ServerWorker.Instance().clearPostsById(groupId);

            notifyAboutPostsUpdateBegin();

            do
            {
                if(isCancelled()) break;
                
                num++;
                
                int start = html.indexOf(postOrd, currentPos);
                int end = html.indexOf(postOrd, start + 300);

                if(start == -1)
                {
                    if(html.contains("<title>Лепрозорий: вход</title>"))
                    {
                        new LogoutTask().execute();
                        cancel(false);
                    }
                    break;
                }
                
                if(     num == 0 && 
                        page == 0)
                {
                    if(groupId.equals(Commons.MAIN_POSTS_ID))
                    {
                        String header = html.substring(0, start);
                        Element content = Jsoup.parse(header);

                        // TODORA
                        //Element filter = content.getElementById("js-showonindex");
                        //SettingsWorker.Instance().saveMainThreshold(filter.attr("value"));
                        
                        /*if(TextUtils.isEmpty(SettingsWorker.Instance().loadVoteWtf()))
                        {
                            Element vote = content.getElementById("content_left_inner");
                            Element script = vote.getElementsByTag("script").first();
                            
                            Pattern pattern = Pattern.compile("wtf_vote = '(.+)'");
                            Matcher matcher = pattern.matcher(script.data());
                            if(matcher.find())
                                SettingsWorker.Instance().saveVoteWtf(matcher.group(1));
                        }
                        
                        if(TextUtils.isEmpty(SettingsWorker.Instance().loadStuffWtf()))
                        {
                            Pattern pattern = Pattern.compile("mythingsHandler.wtf = '(.+)'");
                            Matcher matcher = pattern.matcher(content.html());
                            if(matcher.find())
                                SettingsWorker.Instance().saveStuffWtf(matcher.group(1));
                        }*/
                    }
                    else if(groupId.equals(Commons.MYSTUFF_POSTS_ID))
                    {
                        if(TextUtils.isEmpty(SettingsWorker.Instance().loadFavWtf()))
                        {
                            String header = html.substring(0, start);
                            Element content = Jsoup.parse(header);
                            
                            Element fav = content.getElementsByAttributeValue("name", "fav").first();
                            Element wtf = fav.getElementsByAttributeValue("name", "wtf").first();

                            SettingsWorker.Instance().saveFavWtf(wtf.attr("value"));
                        }
                    }
                }
                
                if(end == -1)
                {
                    lastElement = true;
                    end = html.length();
                }
                
                currentPos = end;
                
                String postHtml = Utils.replaceBadHtmlTags(html.substring(start, end));
                Element content = Jsoup.parse(postHtml);
                
                if(     page == 0 &&
                        lastElement &&
                        (   type == Commons.PostsType.MAIN ||
                            type == Commons.PostsType.CUSTOM ||
                            type == Commons.PostsType.USER
                        ) )
                {
                    Element element = content.getElementById("total_pages");
                    ServerWorker.Instance().addPostPagesCount(groupId, element == null ? 0 : Integer.valueOf(element.getElementsByTag("strong").first().text()));
                }
                
                Post post = new Post(groupId);
                
                Element element = content.getElementsByClass("dt").first();
                Element info = content.getElementsByClass("dd").first();
                Elements images = element.getElementsByTag("img");

                int imageNum = 0;
                List<Pair<String, String>> imgs = new ArrayList<Pair<String, String>>();

                for (Element image : images)
                {
                    String src = image.attr("src");
                    if(isImagesEnabled && !TextUtils.isEmpty(src))
                    {
                        if(TextUtils.isEmpty(post.getImageUrl()))
                        {
                            post.setImageUrl( senchaPrefix + image.attr("src"));
                        }
                        
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
                  
                post.setHtml(Utils.getImagesStub(imgs, 0) + Utils.wrapLepraTags(element));

                Element vote = info.getElementsByClass("vote_result").first();
                if(vote != null)
                    post.setRating(Integer.valueOf(vote.text()));
                else
                    post.setVoteDisabled(true);
                
                post.setPlusVoted(postHtml.contains("class=\"plus voted\""));
                post.setMinusVoted(postHtml.contains("class=\"minus voted\""));
                
                Element author = info.getElementsByClass("ddi").first();
                if(author != null)
                {
                    if(author.getElementsByClass("stars").first() != null)
                        post.setGolden(true);
                    else if(author.getElementsByClass("wasstars").first() != null)
                        post.setSilver(true);

                    Elements span = author.getElementsByClass("b-post_comments_links");
                    Elements a = span.first().getElementsByTag("a");
                    String url = a.first().attr("href");
                    if(url.contains("http"))
                        post.setUrl(url);
                    else
                        post.setUrl(Commons.SITE_URL + url);

                    post.setLepraId(post.getUrl().split(post.isInbox() ? "inbox/" : "comments/")[1]);
                    
                    if(a.size() == 2)
                    {
                        post.setTotalComments(Short.valueOf(a.get(0).text().split(" ")[0]));
                        post.setNewComments(Short.valueOf(a.get(1).text().split(" ")[0]));
                    }
                    else
                    {
                        if(!a.get(0).text().equals("комментировать"))
                            post.setTotalComments(Short.valueOf(a.get(0).text().split(" ")[0]));
                    }

                    String authorText = author.getElementsByTag("a").first().text();
                    post.setAuthor(authorText);
                    post.setSignature(author.text().split("\\|")[0].replace(authorText, "<b>" + authorText + "</b>"));
                }
                
                if(isCancelled()) break;

                ServerWorker.Instance().addNewPost(groupId, post);

                if(     !post.isMain() &&
                        !post.isInbox() &&
                        (post.isPlusVoted() || post.isMinusVoted()))
                {
                    Integer voteWeight = ServerWorker.Instance().getBlogVoteWeight(groupId);
                    if(voteWeight == null)
                    {
                        Throwable t = new SetVoteWeightTask(post.getId())
                                .execute()
                                .get();
                        Logger.e(t);
                    }
                    else
                        post.setVoteWeight(voteWeight);
                }

                if(num % 5 == 0 || lastElement)
                {
                    notifyAboutPostsUpdate();
                }
            }
            while (!lastElement);
            
            if(!isCancelled())
                notifyAboutPostsUpdateFinished(true);
        }
        catch (Throwable t)
        {
            setException(t);
            notifyAboutPostsUpdateFinished(false);
        }
        finally
        {
            Logger.d("GetPostsTask time:" + Long.toString(System.currentTimeMillis() - startTime) + "ms");
        }
        
        return e;
    }

    private String getPageNum()
    {
        if(type == Commons.PostsType.MAIN || type == Commons.PostsType.CUSTOM )
            return "pages/" + Integer.toString(page + 1);
        if(type == Commons.PostsType.USER)
            return Integer.toString(page + 1);

        return "";
    }
}
