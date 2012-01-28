package com.home.lepradroid.serverworker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.home.lepradroid.commons.Commons.PostSourceType;
import com.home.lepradroid.objects.Blog;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Logger;

public class ServerWorker
{
    private String loginCode = "";
    private static volatile ServerWorker instance;
    private ClientConnectionManager connectionManager;
    private HttpParams connectionParameters;
    private CookieStore cookieStore;
    private HttpClient client;
    private ArrayList<Post> mainPosts = new ArrayList<Post>();
    private ArrayList<Post> myStuffPosts = new ArrayList<Post>();
    private ArrayList<Blog> blogs = new ArrayList<Blog>();
    
    private ServerWorker() 
    {
        init();
    }

    public static ServerWorker Instance()
    {
        if(instance == null)
        {
            synchronized (ServerWorker.class)
            {
                if(instance == null)
                {
                    instance = new ServerWorker();
                }
            }
        }
        
        return instance;
    }
    
    private void init()
    {
        connectionParameters = new BasicHttpParams();
        connectionParameters.setBooleanParameter("http.protocol.expect-continue", false);
        connectionParameters.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(20));
        connectionParameters.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
        connectionParameters.setBooleanParameter(HttpConnectionParams.STALE_CONNECTION_CHECK,
                false);
        HttpProtocolParams.setVersion(connectionParameters, HttpVersion.HTTP_1_1);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        connectionManager = new ThreadSafeClientConnManager(connectionParameters, registry);
        cookieStore = new BasicCookieStore();
        client = new DefaultHttpClient(connectionManager, connectionParameters);
    }
    
    public void clearSessionInfo() throws Exception
    {
        cookieStore.clear();
        SettingsWorker.Instance().clearCookies();
    }
    
    public String getContent(String url) throws ClientProtocolException, IOException
    {
        final HttpGet httpGet = new HttpGet(url);

        final HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        
        try
        {
            String cookie = SettingsWorker.Instance().loadCookie();
            if(!TextUtils.isEmpty(cookie))
                httpGet.addHeader("Cookie", cookie);
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
        
        final HttpResponse response = client.execute(httpGet, localContext);
        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }
    
    public Header[] login(String url, String login, String password, String captcha, String loginCode) throws ClientProtocolException, IOException
    { 
        final HttpPost httpGet = new HttpPost(url);
        String str = String.format("user=%s&pass=%s&captcha=%s&logincode=%s&save=1&x=30&y=7", login, password, captcha, loginCode);
        
        final StringEntity se = new StringEntity(str, HTTP.UTF_8);
        httpGet.setHeader("Content-Type","application/x-www-form-urlencoded");
        httpGet.setEntity(se);
        
        final HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        
        final HttpResponse response = client.execute(httpGet, localContext);
        
        return response.getAllHeaders();
    }
    
    public Drawable getImage(String url) throws ClientProtocolException, IOException
    {
        final HttpGet httpGet = new HttpGet(url);

        final HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        
        final HttpResponse response = client.execute(httpGet, localContext);
        
        final byte[] array = EntityUtils.toByteArray(response.getEntity());
  
        return Drawable.createFromStream(new ByteArrayInputStream(array), "src");
    }

    public String getLoginCode()
    {
        return loginCode;
    }

    public void setLoginCode(String loginCode)
    {
        this.loginCode = loginCode;
    }
    
    public ArrayList<Post> getPostsByType(PostSourceType type)
    {
        switch (type)
        {
        case MAIN:
            return mainPosts;
        case MYSTUFF:
            return myStuffPosts;
        default:
            break;
        }
        
        return new ArrayList<Post>(0);
    }
    
    
    public void addNewPost(Post post)
    {
        final ArrayList<Post> posts = getPostsByType(post.Type);

        synchronized (posts)
        {
            posts.add(post);
        }
    }
    
    public ArrayList<Blog> getBlogs()
    {
        synchronized (blogs)
        {
            return blogs;
        }
    }
    
    public void addNewBlog(Blog blog)
    {
        synchronized (blogs)
        {
            blogs.add(blog);
        }
    }
    
    public void clearBlogs()
    {
        synchronized (blogs)
        {
            blogs.clear();
        }
    }
    
    public void clearPostsByType(PostSourceType type)
    {
        switch (type)
        {
        case MAIN:
            synchronized (mainPosts)
            {
                mainPosts.clear();
            }
            break;
        case MYSTUFF:
            synchronized (myStuffPosts)
            {
                myStuffPosts.clear();
            }
            break;
        default:
            break;
        }
    }
    
    public void clearPosts()
    {
        synchronized (mainPosts)
        {
            mainPosts.clear();
        }
        
        synchronized (myStuffPosts)
        {
            myStuffPosts.clear();
        }
    }
}
