package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.home.lepradroid.settings.SettingsWorker;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Pair;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.BlogsUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Blog;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Logger;
import com.home.lepradroid.utils.Utils;

public class GetBlogsTask extends BaseTask
{
    static final Class<?>[] argsClassesOnBlogsUpdateBegin = new Class[1];
    static final Class<?>[] argsClassesOnBlogsUpdate = new Class[1];
    static final Class<?>[] argsClassesOnBlogsUpdateFinished = new Class[2];
    
    static Method methodOnBlogsUpdateBegin;
    static Method methodOnBlogsUpdate;
    static Method methodOnBlogsUpdateFinished;
    
    private boolean refresh = true;
    private int page = 0;
    
    public GetBlogsTask()
    {
        refresh = true;
    }
    
    public GetBlogsTask(int page)
    {       
        this.refresh = false;
        this.page = page;
    }

    static
    {
        try
        {
            argsClassesOnBlogsUpdateBegin[0] = int.class;
            methodOnBlogsUpdateBegin = BlogsUpdateListener.class.getMethod("OnBlogsUpdateBegin", argsClassesOnBlogsUpdateBegin);
            
            argsClassesOnBlogsUpdate[0] = int.class;
            methodOnBlogsUpdate = BlogsUpdateListener.class.getMethod("OnBlogsUpdate", argsClassesOnBlogsUpdate);
            
            argsClassesOnBlogsUpdateFinished[0] = int.class;
            argsClassesOnBlogsUpdateFinished[1] = boolean.class;
            methodOnBlogsUpdateFinished = BlogsUpdateListener.class.getMethod("OnBlogsUpdateFinished", argsClassesOnBlogsUpdateFinished);            
        } 
        catch (Throwable t) 
        {
            Logger.e(t);
        }
    }

    @SuppressWarnings("unchecked")
    public void notifyAboutBlogsUpdateBegin()
    {
        final List<BlogsUpdateListener> listeners = ListenersWorker.Instance().getListeners(BlogsUpdateListener.class);
        final Object args[] = new Object[1];
        args[0] = page;

        for (BlogsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]>(methodOnBlogsUpdateBegin, args)));
        }
    }

    @SuppressWarnings("unchecked")
    public void notifyAboutBlogsUpdate()
    {
        final List<BlogsUpdateListener> listeners = ListenersWorker.Instance().getListeners(BlogsUpdateListener.class);
        final Object args[] = new Object[1];
        args[0] = page;

        for (BlogsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]>(methodOnBlogsUpdate, args)));
        }
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutBlogsUpdateFinished(boolean successful)
    {
        final List<BlogsUpdateListener> listeners = ListenersWorker.Instance().getListeners(BlogsUpdateListener.class);
        final Object args[] = new Object[2];
        args[0] = page;
        args[1] = successful;

        for (BlogsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]>(methodOnBlogsUpdateFinished, args)));
        }
    }

    @Override
    protected Throwable doInBackground(Void... arg0)
    {
        final long startTime = System.nanoTime();
        final ArrayList<BaseItem> items = new ArrayList<BaseItem>();

        try
        {
            int num = -1;
            
            if(refresh)
                ServerWorker.Instance().clearPostsById(Commons.BLOGS_POSTS_ID);
            
            notifyAboutBlogsUpdateBegin();

            String body = String.format("sort=0&csrf_token=%s", SettingsWorker.Instance().loadCsrfToke());
            String html = ServerWorker.Instance().postRequest(Commons.BLOGS_URL, body);
            html = html.substring(html.indexOf("\"template\":"));

            if(refresh)
            {
                // TODO
                // getSubBlogs(html, items);
            }

            final String logoPattern = "background-image:url(";
            final String blogRow = "<div class=\\\"b-list_item\\\"";
            int currentPos = 0;
            boolean lastElement = false;
            boolean receivedBlogs = false;

            do
            {
                if (isCancelled()) break;

                num++;

                int start = html.indexOf(blogRow, currentPos);
                int end = html.indexOf(blogRow, start + 100);

                if (end == -1)
                {
                    end = html.length();
                    lastElement = true;
                }

                currentPos = end;

                final Element content = Jsoup.parse(StringEscapeUtils.unescapeJava(html.substring(start, end)));
                if(page == 0 && lastElement)
                {
                    ServerWorker.Instance().addPostPagesCount(Commons.BLOGS_POSTS_ID, 1000);
                }

                Blog blog = new Blog();

                String logo = content.getElementsByClass("b-list_item_logo").first().attr("style");
                int begin = logo.indexOf(logoPattern);
                logo = logo.substring(begin + logoPattern.length(), logo.indexOf(")", begin));

                String blogUrl = content.getElementsByClass("b-list_item_blog_url").first().attr("href");
                blog.setUrl(Commons.PREFIX_URL + blogUrl + "/");
                blog.setImageUrl(logo);

                Elements title = content.getElementsByTag("h5");
                if (!title.isEmpty())
                    blog.setHtml(title.first().html());

                Elements author = content.getElementsByClass("b-list_item_blog_creator");
                if (!author.isEmpty())
                {
                    blog.setAuthor(author.first().getElementsByTag("a").first().text());
                    blog.setSignature(author.first().text());
                }

                Elements stat = content.getElementsByClass("b-list_item_blog_stats");
                if (!stat.isEmpty())
                {
                    Elements div = stat.first().getElementsByTag("div");
                    if (div.size() >= 3)
                    {
                        blog.setStat("<b>" + div.get(1).text() + "</b>" + " постов / " + "<b>" + div.get(2).text() + "</b>" + " комментариев / " + "<b>" + div.get(3).text() + "</b>" + " подписчиков");
                    }
                }
                
                items.add(blog);
                
                if(isCancelled()) break;

                if (num % 5 == 0 || lastElement)
                {
                    receivedBlogs = true;
                    ServerWorker.Instance().addNewPosts(Commons.BLOGS_POSTS_ID, items);
                    notifyAboutBlogsUpdate();

                    items.clear();
                }
            }
            while (!lastElement);
            
            if(!isCancelled()) 
            {
                if (!items.isEmpty())
                {
                    ServerWorker.Instance().addNewPosts(Commons.BLOGS_POSTS_ID, items);
                    notifyAboutBlogsUpdateFinished(true);
                }
                else           
                    notifyAboutBlogsUpdateFinished(receivedBlogs);
            }
        } 
        catch (Throwable t)
        {
            setException(t);
            notifyAboutBlogsUpdateFinished(false);
        } 
        finally
        {
            Logger.d("GetBlogsTask time:" + Long.toString(System.nanoTime() - startTime));
        }

        return e;
    }

    private void getSubBlogs(final String html, List<BaseItem> items)
    {
        final String subBlogRowStart = "<div class=\"subs_loaded hidden\">";
        final String subBlogRowEnd = "<div class=\"js-subs_container\">";
        int start = html.indexOf(subBlogRowStart);
        int end = html.indexOf(subBlogRowEnd);
        Elements subDivs = Jsoup.parse(html.substring(start, end))
                .getElementsByClass("sub");

        for (Element div : subDivs)
        {
            Blog blog = new Blog();

            Elements url = div.getElementsByTag("a");
            if (!url.isEmpty())
                blog.setUrl(url.attr("href"));

            Elements images = div.getElementsByTag("img");
            if (!images.isEmpty())
                blog.setImageUrl(images.first().attr("src"));

            Elements title = div.getElementsByTag("h5");
            if (!title.isEmpty())
                blog.setHtml(title.first().html());

            Elements author = div.getElementsByClass("creator");
            if (!author.isEmpty())
                blog.setSignature("создатель - "
                        + author.first().getElementsByTag("a").first().text());

            blog.setStat("<b>Лепро-Навигация</b>");
            
            items.add(blog);
        }
    }
}
