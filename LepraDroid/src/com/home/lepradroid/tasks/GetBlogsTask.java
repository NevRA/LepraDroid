package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Pair;

import com.home.lepradroid.LepraDroidApplication;
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
    private boolean isNormalTextSize = true;
    
    public GetBlogsTask()
    {
        refresh = true;
        isNormalTextSize = Utils.isNormalFontSize(LepraDroidApplication.getInstance());
    }
    
    public GetBlogsTask(int page)
    {       
        this.refresh = false;
        this.page = page;
        isNormalTextSize = Utils.isNormalFontSize(LepraDroidApplication.getInstance());
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
            
            final String html = ServerWorker.Instance().getContent(Commons.BLOGS_URL + "subscribers/" + Integer.valueOf(page + 1));

            if(refresh)
                getSubBlogs(html, items);
            
            final String blogRow = "<tr class=\"jj_row";
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

                final Element content = Jsoup.parse(html.substring(start, end));
                if(page == 0 && lastElement)
                {
                    Element element = content.getElementById("total_pages");
                    ServerWorker.Instance().addPostPagesCount(Commons.BLOGS_POSTS_ID, element == null ? 0 : Integer.valueOf(element.getElementsByTag("strong").first().text()));
                }

                Blog blog = new Blog();

                Elements logos = content.getElementsByClass("jj_logo");
                if (!logos.isEmpty())
                {
                    Element logo = logos.first();

                    Elements url = logo.getElementsByTag("a");
                    if (!url.isEmpty())
                        blog.Url = url.attr("href");

                    Elements images = logo.getElementsByTag("img");
                    if (!images.isEmpty())
                        blog.ImageUrl = "http://src.sencha.io/" + (isNormalTextSize ? + Commons.POST_PREVIEW_NORMAL_SIZE + "/"  + Commons.POST_PREVIEW_NORMAL_SIZE : Commons.POST_PREVIEW_BIG_SIZE + "/"  + Commons.POST_PREVIEW_BIG_SIZE) + "/" + images.first().attr("src");
                }

                Elements title = content.getElementsByTag("h5");
                if (!title.isEmpty())
                    blog.Html = title.first().html();

                Elements author = content.getElementsByClass("jj_creator");
                if (!author.isEmpty())
                {
                    blog.Author = author.first().getElementsByTag("a").first().text();
                    blog.Signature = author.first().text();
                }

                Elements stat = content.getElementsByClass("jj_stat_table");
                if (!stat.isEmpty())
                {
                    Elements div = stat.first().getElementsByTag("div");
                    if (div.size() >= 3)
                    {
                        blog.Stat = "<b>" + div.get(0).text() + "</b>" + " постов / " + "<b>" + div.get(1).text() + "</b>" + " комментариев / " + "<b>" + div.get(2).text() + "</b>" + " подписчиков";
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
            while (lastElement == false);
            
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
                blog.Url = url.attr("href");

            Elements images = div.getElementsByTag("img");
            if (!images.isEmpty())
                blog.ImageUrl = "http://src.sencha.io/80/80/"
                        + images.first().attr("src");

            Elements title = div.getElementsByTag("h5");
            if (!title.isEmpty())
                blog.Html = title.first().html();

            Elements author = div.getElementsByClass("creator");
            if (!author.isEmpty())
                blog.Signature = "создатель - "
                        + author.first().getElementsByTag("a").first().text();

            blog.Stat = "<b>Лепро-Навигация</b>";
            
            items.add(blog);
        }
    }
}
