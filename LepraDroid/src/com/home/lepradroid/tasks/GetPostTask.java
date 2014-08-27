package com.home.lepradroid.tasks;

import android.text.TextUtils;
import android.util.Pair;
import com.home.lepradroid.interfaces.PostUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Logger;
import com.home.lepradroid.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

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
        isImagesEnabled = Utils.isImagesEnabled();
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
            Post post = new Post(groupId);
            post.setId(postId);

            String html = ServerWorker.Instance().getContent(url);
            String postOrd = "<div class=\"post ";
            String endStr = "js-comments";

            int start = html.indexOf(postOrd, 0);
            int end = html.indexOf(endStr, start);

            String postHtml = Utils.replaceBadHtmlTags(html.substring(start, end));
            Element content = Jsoup.parse(postHtml);

            Element element = content.getElementsByClass("dti").first();
            
            Elements images = element.getElementsByTag("img");
            for (Element image : images)
            {
                String src = image.attr("src");

                if(isImagesEnabled)
                {
                    if(TextUtils.isEmpty(post.getImageUrl()))
                        post.setImageUrl(image.attr("src"));

                    if(!image.parent().tag().getName().equalsIgnoreCase("a"))
                        image.wrap("<a href=" + "\"" + src + "\"></a>");

                    image.removeAttr("width");
                    image.removeAttr("height");
                }
                else
                    image.remove();
            }
            
            post.setHtml(Utils.wrapLepraTags(element));

            Elements rating = content.getElementsByClass("vote_result");
            if(!rating.isEmpty())
                post.setRating(Short.valueOf(rating.first().text()));
            else
                post.setVoteDisabled(true);
            
            post.setPlusVoted(postHtml.contains("vote_button vote_button_plus vote_voted"));
            post.setMinusVoted(postHtml.contains("vote_button vote_button_minus vote_voted"));

            post.setUrl(url);
            post.setLepraId(url.split(post.isInbox() ? "inbox/" : "comments/")[1]);

            Elements author = content.getElementsByClass("c_user");
            post.setAuthor(author.text());
            post.setSignature(author.parents().first().text());

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
