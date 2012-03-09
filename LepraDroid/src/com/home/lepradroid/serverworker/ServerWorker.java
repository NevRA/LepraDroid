package com.home.lepradroid.serverworker;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.home.lepradroid.objects.Author;
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

import android.text.TextUtils;
import android.util.Pair;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.commons.Commons.RateType;
import com.home.lepradroid.commons.Commons.RateValueType;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Comment;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Logger;

public class ServerWorker
{
    private String loginCode = "";
    private static volatile ServerWorker instance;
    private ClientConnectionManager connectionManager;
    private HttpParams connectionParameters;
    private Map<UUID, ArrayList<BaseItem>> posts = new HashMap<UUID, ArrayList<BaseItem>>();
    private List<Author> authors = new ArrayList<Author>();
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
        String str = String.format("user=%s&pass=%s&captcha=%s&logincode=%s&save=1", URLEncoder.encode(login), URLEncoder.encode(password), captcha, loginCode);
        
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
    
    public String rateItem(RateType type, String wtf, String id, RateValueType value) throws ClientProtocolException, IOException {

        HttpPost httpPost = null;
        String str = "";
        switch (type) {

            case POST:
                httpPost = new HttpPost(Commons.ITEM_VOTE_URL);
                str = String.format("type=1&wtf=%s&id=p%s&value=%s", wtf, id, value == RateValueType.MINUS ? "-1" : "1");
                break;

            case COMMENT:
                break;

            case KARMA:
                httpPost = new HttpPost(Commons.KARMA_VOTE_URL);
                str = String.format("wtf=%s&u_id=%s&value=%s", wtf, id, value == RateValueType.MINUS ? "4" : "2");
            default:
                break;
        }

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
        write.lock();
        try
        {
            ArrayList<BaseItem> items = posts.get(groupId);
            if(items == null)
            {
                items = new ArrayList<BaseItem>(0);

                posts.put(groupId, items);
            }
            if(clone)
                return cloneList(items);
            else
                return items;
        }
        finally
        {
            write.unlock();
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
    
    public BaseItem getComment(UUID groupId, UUID postId, UUID commentId)
    {
        read.lock();
        try
        {
            ArrayList<BaseItem> items = getComments(groupId, postId);
            
            for(BaseItem item : items)
            {
                if(item.Id.equals(commentId))
                    return item;
            }
        }
        finally
        {
            read.unlock();
        }

        return null;
    }
    
    public int getPrevNewCommentPosition(UUID groupId, UUID postId, int prevCommentNewPosition)
    {
        read.lock();
        try
        {
            ArrayList<BaseItem> comments = this.comments.get(postId);
            if(comments != null)
            {
                for(int pos = prevCommentNewPosition - 1; pos >= 0 && prevCommentNewPosition < comments.size(); --pos)
                {
                    if(((Comment)comments.get(pos)).IsNew)
                        return pos;
                }
            }
        }
        finally
        {
            read.unlock();
        }
        
        return -1;
    }
    
    public int getNextNewCommentPosition(UUID groupId, UUID postId, int prevCommentNewPosition)
    {
        read.lock();
        try
        {
            ArrayList<BaseItem> comments = this.comments.get(postId);
            if(comments != null)
            {
                for(int pos = prevCommentNewPosition + 1; pos < comments.size(); ++pos)
                {
                    if(((Comment)comments.get(pos)).IsNew)
                        return pos;
                }
            }
        }
        finally
        {
            read.unlock();
        }
        
        return -1;
    }
    
    public ArrayList<BaseItem> getComments(UUID groupId, UUID postId)
    {
        read.lock();
        try
        {
            if(comments.containsKey(postId))
            {
                return cloneList(comments.get(postId));
            }
        }
        finally
        {
            read.unlock();
        }
        
        return new ArrayList<BaseItem>();
    }
    
    public int addNewComment(UUID groupId, UUID id, BaseItem item)
    {
        write.lock();
        try
        {
            if(!comments.containsKey(id))
            {
                ArrayList<BaseItem> targetList = new ArrayList<BaseItem>();
                comments.put(id, targetList);
            }
            
            Comment comment = (Comment)item;
            ArrayList<BaseItem> comments = this.comments.get(id);
            
            if(TextUtils.isEmpty(comment.ParentPid))
            {
                comments.add(item);
                return comments.size() - 1;
            }
            else
            {
                for(int pos = 0; pos < comments.size(); ++pos)
                {
                    Comment parentComment = (Comment)comments.get(pos);
                    if(parentComment.Pid.equals(comment.ParentPid))
                    {
                        comments.add(pos + 1, item);
                        return pos + 1;
                    }
                }
            }
        }
        finally
        {
            write.unlock();
        }
        
        return -1;
    }
    
    public void addNewComments(UUID groupId, UUID id, ArrayList<BaseItem> items)
    {    
        for(BaseItem item : items)
        {
            addNewComment(groupId, id, item);
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

    public void addNewAuthor(Author author)
    {
        write.lock();
        try
        {
            authors.add(author);
        }
        finally
        {
            write.unlock();
        }
    }

    public Author getAuthorById(String u_id)
    {
        read.lock();
        try
        {
            for (Author a: authors)
            {
                if (a.Id.equals(u_id)) return a;
            }
        }
        finally
        {
            read.unlock();
        }

        return null;

    }

    public Author getAuthorByName(String name)
    {
        read.lock();
        try
        {
            for (Author a: authors)
            {
                if (a.UserName.equals(name)) return a;
            }
        }
        finally
        {
            read.unlock();
        }

        return null;

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
