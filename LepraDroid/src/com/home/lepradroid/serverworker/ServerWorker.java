package com.home.lepradroid.serverworker;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Pair;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Logger;

public class ServerWorker
{
    private String loginCode = "";
    private static volatile ServerWorker instance;
    private ClientConnectionManager connectionManager;
    private HttpParams connectionParameters;
    private Map<UUID, ArrayList<BaseItem>> posts = new HashMap<UUID, ArrayList<BaseItem>>();
    private ReentrantReadWriteLock readWriteLock =  new ReentrantReadWriteLock();
    private final Lock read  = readWriteLock.readLock();
    private final Lock write = readWriteLock.writeLock();
    private Map<UUID, ArrayList<BaseItem>> comments = new HashMap<UUID, ArrayList<BaseItem>>();
    
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
                true);
        HttpProtocolParams.setVersion(connectionParameters, HttpVersion.HTTP_1_1);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        connectionManager = new ThreadSafeClientConnManager(connectionParameters, registry);
    }
    
    public void clearSessionInfo() throws Exception
    {
        SettingsWorker.Instance().clearCookies();
        SettingsWorker.Instance().clearUserInfo();
    }
    
    public HttpEntity getContentEntity(String url) throws Exception
    {
        final HttpGet httpGet = new HttpGet(url);
        //httpGet.setHeader("charset", "utf-8");
        
        try
        {
            final Pair<String, String> cookies = SettingsWorker.Instance().loadCookie();
            if(cookies != null)
                httpGet.addHeader("Cookie", Commons.COOKIE_SID + "=" + cookies.first + ";" + Commons.COOKIE_UID + "=" +cookies.second + ";"); 
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
        
        final HttpClient client = new DefaultHttpClient(connectionManager, connectionParameters);
        final HttpResponse response = client.execute(httpGet);
        
        return response.getEntity();
    }
    
    public InputStream getContentStream(String url) throws Exception
    {        
        return getContentEntity(url).getContent();
    }
    
    public String getContent(String url) throws Exception
    {        
        return EntityUtils.toString(getContentEntity(url), "UTF-8");
    }
    
    public Pair<String, Header[]> login(String url, String login, String password, String captcha, String loginCode) throws ClientProtocolException, IOException
    { 
        final HttpPost httpGet = new HttpPost(url);
        String str = String.format("user=%s&pass=%s&captcha=%s&logincode=%s&save=1", login, password, captcha, loginCode);
        
        final StringEntity se = new StringEntity(str, HTTP.UTF_8);
        httpGet.setHeader("Content-Type","application/x-www-form-urlencoded");
        httpGet.setEntity(se);
        
        final HttpClient client = new DefaultHttpClient(connectionManager, connectionParameters);
        final HttpResponse response = client.execute(httpGet);
        
        return new Pair<String, Header[]>(EntityUtils.toString(response.getEntity(), "UTF-8"), response.getAllHeaders());
    }
    
    public byte[] getImage(String url) throws ClientProtocolException, IOException
    {
        final HttpGet httpGet = new HttpGet(url);
        
        final HttpClient client = new DefaultHttpClient(connectionManager, connectionParameters);
        final HttpResponse response = client.execute(httpGet);
        
        return EntityUtils.toByteArray(response.getEntity());
    }
    
    public String postComment(String wtf, String replyTo, String pid, String comment) throws ClientProtocolException, IOException
    {
        final HttpPost httpPost = new HttpPost(Commons.POST_COMMENT_URL);
        String str = String.format("wtf=%s&step=1&i=0&replyto=%s&pid=%s&iframe=0&file=&comment=%s", wtf, replyTo, pid, URLEncoder.encode(comment));
        
        try
        {
            final Pair<String, String> cookies = SettingsWorker.Instance().loadCookie();
            if(cookies != null)
                httpPost.addHeader("Cookie", Commons.COOKIE_SID + "=" + cookies.first + ";" + Commons.COOKIE_UID + "=" +cookies.second + ";"); 
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
        
        final StringEntity se = new StringEntity(str, HTTP.UTF_8);
        httpPost.setHeader("Content-Type","application/x-www-form-urlencoded");
        httpPost.setEntity(se);
        
        final HttpClient client = new DefaultHttpClient(connectionManager, connectionParameters);
        final HttpResponse response = client.execute(httpPost);
        
        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }

    public String getLoginCode()
    {
        return loginCode;
    }

    public void setLoginCode(String loginCode)
    {
        this.loginCode = loginCode;
    }
    
    public BaseItem getPostById(UUID groupId, UUID id)
    {
        read.lock();
        try
        {
            ArrayList<BaseItem> items = posts.get(groupId);
            if(items != null)
                for(BaseItem item : items)
                {
                    if(item.Id.equals(id))
                        return item;
                }
        }
        finally
        {
            read.unlock();
        }
        
        return null;
    }
    
    public ArrayList<BaseItem> getPostsById(UUID groupId, boolean clone)
    {
        read.lock();
        try
        {
            ArrayList<BaseItem> items = posts.get(groupId);
            if(items == null)
            {
                items = new ArrayList<BaseItem>(0);
                write.lock();
                try
                {
                    posts.put(groupId, items);
                }
                finally
                {
                    write.unlock();
                }
            }
            if(clone)
                return cloneList(items);
            else
                return items;
        }
        finally
        {
            read.unlock();
        }
    }
    
    public static ArrayList<BaseItem> cloneList(ArrayList<BaseItem> list)
    {
        ArrayList<BaseItem> clonedList = new ArrayList<BaseItem>(list.size());
        for (BaseItem item : list)
        {
            clonedList.add(item);
        }
        return clonedList;
    }
    
    public ArrayList<BaseItem> getComments(UUID groupId, UUID id)
    {
        read.lock();
        try
        {
            BaseItem post = getPostById(groupId, id);
            if(post != null)
            {
                if(comments.containsKey(id))
                {
                    return cloneList(comments.get(id));
                }
            }
        }
        finally
        {
            read.unlock();
        }
        
        return new ArrayList<BaseItem>();
    }
    
    public void addNewComment(UUID groupId, UUID id, BaseItem item)
    {
        write.lock();
        try
        {
            BaseItem post = getPostById(groupId, id);
            if(post != null)
            {
                if(!comments.containsKey(id))
                {
                    ArrayList<BaseItem> targetList = new ArrayList<BaseItem>();
                    comments.put(id, targetList);
                }
                
                comments.get(id).add(item);
                
                return;
            }
        }
        finally
        {
            write.unlock();
        }
    }
    
    public void addNewComments(UUID groupId, UUID id, ArrayList<BaseItem> items)
    {
        write.lock();
        try
        {
            BaseItem post = getPostById(groupId, id);
            if(post != null)
            {
                if(!comments.containsKey(id))
                {
                    ArrayList<BaseItem> targetList = new ArrayList<BaseItem>();
                    comments.put(id, targetList);
                }
                
                comments.get(id).addAll(items);
                
                return;
            }
        }
        finally
        {
            write.unlock();
        }
    }
    
    public void addNewPosts(UUID groupId, ArrayList<BaseItem> posts)
    {
        write.lock();
        try
        {
            getPostsById(groupId, false).addAll(posts);
        }
        finally
        {
            write.unlock();
        }
    }
    
    public void addNewPost(UUID groupId, BaseItem post)
    {
        write.lock();
        try
        {
            getPostsById(groupId, false).add(post);
        }
        finally
        {
            write.unlock();
        }
    }
    
    public void clearCommentsById(UUID id)
    {
        write.lock();
        try
        {
            if(comments.containsKey(id))
                comments.remove(id);
        }
        finally
        {
            write.unlock();
        }
    }
    
    public void clearPostsById(UUID groupId)
    {
        write.lock();
        try
        {
            getPostsById(groupId, false).clear();
        }
        finally
        {
            write.unlock();
        }
    }
    
    public void clearComments()
    {
        write.lock();
        try
        {
            comments.clear();
        }
        finally
        {
            write.unlock();
        }
    }
    
    public void clearPosts()
    {
        write.lock();
        try
        {
            posts.clear();
        }
        finally
        {
            write.unlock();
        }
    }
}
