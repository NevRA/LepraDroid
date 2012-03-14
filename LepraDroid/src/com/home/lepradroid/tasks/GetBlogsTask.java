package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class GetBlogsTask extends BaseTask
{
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
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    @SuppressWarnings("unchecked")
    public void notifyAboutBlogsUpdateBegin()
    {
        final List<BlogsUpdateListener> listeners = ListenersWorker.Instance().getListeners(BlogsUpdateListener.class);
        final Object args[] = new Object[0];

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
        args[0] = true;

        for (BlogsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]>(methodOnBlogsUpdate, args)));
        }
    }

    @Override
    protected Throwable doInBackground(Void... arg0)
    {
        final long startTime = System.nanoTime();
        final ArrayList<BaseItem> items = new ArrayList<BaseItem>();
        final ArrayList<String> urls = new ArrayList<String>();

        try
        {
            int num = -1;

            ServerWorker.Instance().clearPostsById(Commons.BLOGS_POSTS_ID);
            notifyAboutBlogsUpdateBegin();

            final String html = ServerWorker.Instance().getContent(Commons.BLOGS_URL);
            final String blogRow = "<tr class=\"jj_row";
            getSubBlogs(html, items, urls);

            int currentPos = 0;
            boolean lastElement = false;

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

                final Element element = Jsoup.parse(html.substring(start, end));

                Blog blog = new Blog();

                Elements logos = element.getElementsByClass("jj_logo");
                if (!logos.isEmpty())
                {
                    Element logo = logos.first();

                    Elements url = logo.getElementsByTag("a");
                    if (!url.isEmpty())
                        blog.Url = url.attr("href");

                    Elements images = logo.getElementsByTag("img");
                    if (!images.isEmpty())
                        blog.ImageUrl = "http://src.sencha.io/80/80/" + images.first().attr("src");
                }

                Elements title = element.getElementsByTag("h5");
                if (!title.isEmpty())
                    blog.Text = title.first().text();

                Elements author = element.getElementsByClass("jj_creator");
                if (!author.isEmpty())
                {
                    blog.Author = author.first().getElementsByTag("a").first().text();
                    blog.Signature = author.first().text();
                }

                Elements stat = element.getElementsByClass("jj_stat_table");
                if (!stat.isEmpty())
                {
                    Elements div = stat.first().getElementsByTag("div");
                    if (div.size() >= 3)
                    {
                        // TODO text from resources 
                        blog.Stat = "<b>" + div.get(0).text() + "</b>" + " постов / " + "<b>" + div.get(1).text() + "</b>" + " комментариев / " + "<b>" + div.get(2).text() + "</b>" + " подписчиков";
                    }
                }
                if (!urls.contains(blog.Url))
                {
                    urls.add(blog.Url);
                    items.add(blog);
                }

                if (num % 5 == 0 || lastElement)
                {
                    ServerWorker.Instance().addNewPosts(Commons.BLOGS_POSTS_ID, items);
                    notifyAboutBlogsUpdate();

                    items.clear();
                }
            }
            while (lastElement == false);
        } catch (Throwable t)
        {
            setException(t);
        } finally
        {
            if (!items.isEmpty())
                ServerWorker.Instance().addNewPosts(Commons.BLOGS_POSTS_ID, items);
            notifyAboutBlogsUpdate();

            Logger.d("GetBlogsTask time:" + Long.toString(System.nanoTime() - startTime));
        }

        return e;
    }


    private void getSubBlogs(final String html, List<BaseItem> items, List<String> urls)
    {

        final String subBlogRowStart = "<div class=\"subs_loaded hidden\">";
        final String subBlogRowEnd = "<div class=\"js-subs_container\">";
        int start = html.indexOf(subBlogRowStart);
        int end = html.indexOf(subBlogRowEnd);
        Elements subDivs = Jsoup.parse(html.substring(start, end)).getElementsByClass("sub");

        Iterator<Element> i = subDivs.iterator();
        while (i.hasNext())
        {

            Element div = i.next();
            Blog blog = new Blog();

            Elements url = div.getElementsByTag("a");
            if (!url.isEmpty())
                blog.Url = url.attr("href");

            Elements images = div.getElementsByTag("img");
            if (!images.isEmpty())
                blog.ImageUrl = "http://src.sencha.io/80/80/" + images.first().attr("src");


            Elements title = div.getElementsByTag("h5");
            if (!title.isEmpty())
                blog.Text = title.first().text();


            Elements author = div.getElementsByClass("creator");
            if (!author.isEmpty())
                blog.Signature = author.first().getElementsByTag("a").first().text();

            if (!urls.contains(blog.Url))
            {
                urls.add(blog.Url);
                items.add(blog);
            }

        }
    }

}
