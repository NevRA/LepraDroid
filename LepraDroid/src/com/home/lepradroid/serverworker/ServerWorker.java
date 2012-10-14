package com.home.lepradroid.serverworker;

import android.text.TextUtils;
import android.util.Pair;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.commons.Commons.RateType;
import com.home.lepradroid.commons.Commons.RateValueType;
import com.home.lepradroid.commons.Commons.StuffOperationType;
import com.home.lepradroid.objects.Author;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Comment;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Utils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;

public class ServerWorker
{
    private String                                  loginCode               = "";
    private static volatile ServerWorker            instance;
    private ClientConnectionManager                 connectionManager;
    private HttpParams                              connectionParameters;

    private final Map<UUID, Integer>                blogVoteWeights         = Collections.synchronizedMap(new HashMap<UUID, Integer>());
    private final Map<UUID, ArrayList<BaseItem>>    posts                   = Collections.synchronizedMap(new HashMap<UUID, ArrayList<BaseItem>>());
    private final Map<String, Author>               authors                 = Collections.synchronizedMap(new HashMap<String, Author>());
    private final Map<UUID, Integer>                postsPagesCount         = Collections.synchronizedMap(new HashMap<UUID, Integer>());
    private final Map<UUID, ArrayList<BaseItem>>    comments                = Collections.synchronizedMap(new HashMap<UUID, ArrayList<BaseItem>>());

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
    
    public HttpEntity getContentEntity(String url, boolean addCookie) throws Exception
    {
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept-Encoding", "gzip");

        if(addCookie)
        {
            final Pair<String, String> cookies = SettingsWorker.Instance().loadCookie();
            if(cookies != null)
                httpGet.addHeader("Cookie", Commons.COOKIE_SID + "=" + cookies.first + ";" + Commons.COOKIE_UID + "=" +cookies.second + ";");
        }

        HttpClient client = new DefaultHttpClient(connectionManager, connectionParameters);
        HttpResponse response = client.execute(httpGet);
        
        Header[] contentEncodings = response.getHeaders("Content-Encoding");
        if(contentEncodings != null)
            for(Header header : contentEncodings)
            {
                if (header.getValue().equalsIgnoreCase("gzip")) 
                {
                    return new Utils.GzipDecompressingEntity(response.getEntity());
                }
            }

        return response.getEntity();
    }
    
    public InputStream getContentStream(String url, boolean addCookies) throws Exception
    {
        return getContentEntity(url, addCookies).getContent();
    }

    public InputStream getContentStream(String url) throws Exception
    {
        return getContentStream(url, true);
    }
    
    public String getContent(String url, boolean addCookies) throws Exception
    {
        return EntityUtils.toString(getContentEntity(url, addCookies), "UTF-8");
    }

    public String getContent(String url) throws Exception
    {
        return getContent(url, true);
    }
    

    public Pair<String, Header[]> login(String url, String login, String password, String captcha, String loginCode) throws IOException
    {
        HttpPost httpGet = new HttpPost(url);
        String str = String.format("user=%s&pass=%s&captcha=%s&logincode=%s&save=1", URLEncoder.encode(login), URLEncoder.encode(password), captcha, loginCode);

        StringEntity se = new StringEntity(str, HTTP.UTF_8);
        httpGet.setHeader("Content-Type","application/x-www-form-urlencoded");
        httpGet.setEntity(se);

        HttpClient client = new DefaultHttpClient(connectionManager, connectionParameters);
        HttpResponse response = client.execute(httpGet);

        return new Pair<String, Header[]>(EntityUtils.toString(response.getEntity(), "UTF-8"), response.getAllHeaders());
    }

    public byte[] getImage(String url) throws IOException
    {
        HttpGet httpGet = new HttpGet(url);

        HttpClient client = new DefaultHttpClient(connectionManager, connectionParameters);
        HttpResponse response = client.execute(httpGet);

        return EntityUtils.toByteArray(response.getEntity());
    }
    
    public String postRequest(String url, String body) throws Exception
    {
        HttpPost httpPost = new HttpPost(url);

        Pair<String, String> cookies = SettingsWorker.Instance().loadCookie();
        if(cookies != null)
            httpPost.addHeader("Cookie", Commons.COOKIE_SID + "=" + cookies.first + ";" + Commons.COOKIE_UID + "=" +cookies.second + ";");

        StringEntity se = new StringEntity(body, HTTP.UTF_8);
        httpPost.setHeader("Content-Type","application/x-www-form-urlencoded");
        httpPost.setEntity(se);

        HttpClient client = new DefaultHttpClient(connectionManager, connectionParameters);
        HttpResponse response = client.execute(httpPost);

        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }

    public String rateItemRequest(RateType type, String wtf, String id, String postId, RateValueType valueType, String value) throws Exception {

        String url = "";
        String body = "";
        switch (type) 
        {
            case POST:
                url = Commons.ITEM_VOTE_URL;
                body = String.format("type=1&wtf=%s&id=p%s&value=%s", wtf, postId, valueType == RateValueType.MINUS ? "-1" : "1");
                break;

            case COMMENT:
                url = Commons.ITEM_VOTE_URL;
                body = String.format("type=0&wtf=%s&id=p%s&post_id=%s&value=%s", wtf, id, postId, valueType == RateValueType.MINUS ? "-1" : "1");
                break;

            case KARMA:
                url = Commons.KARMA_VOTE_URL;
                body = String.format("wtf=%s&u_id=%s&value=%s", wtf, id, value);
            default:
                break;
        }

        return postRequest(url, body);
    }
    
    public void postChangeFavs(String wtf, String lepraPostId, StuffOperationType type) throws Exception
    {
        postRequest(Commons.FAVSCTL_URL, String.format("%s=%s&wtf=%s", (type == StuffOperationType.ADD ? "add" : "del"), lepraPostId, wtf));
    }
    
    public void postChangeMyStuff(String wtf, String lepraPostId, StuffOperationType type) throws Exception
    {
        postRequest(Commons.MYCTL_URL, String.format("%s=%s&wtf=%s", (type == StuffOperationType.ADD ? "add" : "del"), lepraPostId, wtf));
    }
    
    public void postMainTresholdRequest(String value) throws Exception
    {
        postRequest(Commons.THRESHOLD_URL, String.format("showonindex=%s&selected_threshold=all", value));
    }

    public String postInboxRequest(String wtf, String userName, String message) throws Exception
    {
        return postRequest(Commons.ADD_INBOX_URL, String.format("run=01&wtf=%s&whom=%s&comment=%s", wtf, userName, URLEncoder.encode(message)));
    }

    public String postCommentRequest(String wtf, String replyTo, String pid, String comment) throws Exception
    {
        return postRequest(Commons.POST_COMMENT_URL, String.format("wtf=%s&step=1&i=0&replyto=%s&pid=%s&iframe=0&file=&comment=%s", wtf, replyTo, pid, URLEncoder.encode(comment)));
    }

    public String getCommentRating(String postId, String commentId) throws Exception
    {
        return postRequest(Commons.COMMENT_RATING_URL, String.format("id=%s&type=0&post_id=%s", commentId, postId));
    }

    public String getPostRating(String postId) throws Exception
    {
        return postRequest(Commons.COMMENT_RATING_URL, String.format("id=%s&type=1", postId));
    }
    
    /*public int getNewItemsCount() throws Exception
    {
        JSONObject json = new JSONObject(ServerWorker.Instance().getContent(Commons.LEPROPANEL_URL));
        int count = Integer.valueOf(json.getString("inboxunreadcomms"));
        count += Integer.valueOf(json.getString("myunreadcomms"));
        
        return count;
    }*/

    public String getLoginCode()
    {
        return loginCode;
    }

    public void setLoginCode(String loginCode)
    {
        this.loginCode = loginCode;
    }

    public BaseItem getPostById(UUID postId)
    {
        synchronized (posts)
        {
            for(ArrayList<BaseItem> items : posts.values())
            {
                for(BaseItem item : items)
                {
                    if(item.getId().equals(postId))
                        return item;
                }
            }
        }

        return null;
    }

    public ArrayList<BaseItem> getPostsById(UUID groupId, boolean clone)
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

    public static ArrayList<BaseItem> cloneList(ArrayList<BaseItem> list)
    {
        synchronized (list)
        {
            ArrayList<BaseItem> clonedList = new ArrayList<BaseItem>(list.size());
            for (BaseItem item : list)
            {
                clonedList.add(item);
            }
            return clonedList;
        }
    }

    public Integer getBlogVoteWeight(UUID groupId)
    {
        return blogVoteWeights.get(groupId);
    }

    public void addBlogVoteWeight(UUID groupId, int weight)
    {
        blogVoteWeights.put(groupId, weight);
    }

    public BaseItem getComment(UUID postId, UUID commentId)
    {
        ArrayList<BaseItem> items = getComments(postId);

        for(BaseItem item : items)
        {
            if(item.getId().equals(commentId))
                return item;
        }

        return null;
    }

    public int getPrevNewCommentPosition(UUID postId, int prevCommentNewPosition)
    {
        ArrayList<BaseItem> comments = this.comments.get(postId);
        if(comments != null)
        {
            synchronized (comments)
            {
                for(int pos = prevCommentNewPosition - 1; pos >= 0 && prevCommentNewPosition < comments.size(); --pos)
                {
                    if(((Comment)comments.get(pos)).isNew())
                        return pos;
                }
            }
        }

        return -1;
    }

    public int getNextNewCommentPosition(UUID postId, int prevCommentNewPosition)
    {
        ArrayList<BaseItem> comments = this.comments.get(postId);
        if(comments != null)
        {
            synchronized (comments)
            {
                for(int pos = prevCommentNewPosition + 1; pos < comments.size(); ++pos)
                {
                    if(((Comment)comments.get(pos)).isNew())
                        return pos;
                }
            }
        }

        return -1;
    }

    public ArrayList<BaseItem> getComments(UUID postId)
    {
        if(comments.containsKey(postId))
        {
            return cloneList(comments.get(postId));
        }

        return new ArrayList<BaseItem>();
    }

    public int addNewComment(UUID id, BaseItem item)
    {
        if(!comments.containsKey(id))
        {
            ArrayList<BaseItem> targetList = new ArrayList<BaseItem>();
            comments.put(id, targetList);
        }

        Comment comment = (Comment)item;
        ArrayList<BaseItem> comments = this.comments.get(id);

        if(TextUtils.isEmpty(comment.getParentLepraId()))
        {
            comments.add(item);
            return comments.size() - 1;
        }
        else
        {
            synchronized (comments)
            {
                for(int pos = 0; pos < comments.size(); ++pos)
                {
                    Comment parentComment = (Comment)comments.get(pos);
                    if(parentComment.getLepraId().equals(comment.getParentLepraId()))
                    {
                        comments.add(pos + 1, item);
                        return pos + 1;
                    }
                }
            }
        }

        return -1;
    }

    /*public void addNewComments(UUID id, ArrayList<BaseItem> items)
    {
        for(BaseItem item : items)
        {
            addNewComment(id, item);
        }
    }*/

    public void addNewPosts(UUID groupId, ArrayList<BaseItem> newPosts)
    {
        ArrayList<BaseItem> oldPosts = getPostsById(groupId, false);
        for(BaseItem post : newPosts)
        {
            synchronized (oldPosts)
            {
                int pos = 0;
                for(; pos < oldPosts.size(); ++pos)
                    if(oldPosts.get(pos).getUrl().equals(post.getUrl()))
                        break;
                if(pos == oldPosts.size())
                    oldPosts.add(post);
            }
        }
    }
    
    public void addPostPagesCount(UUID groupId, Integer cout)
    {
        postsPagesCount.put(groupId, cout);
    }
    
    public Integer getPostPagesCount(UUID groupId)
    {
        if(postsPagesCount.containsKey(groupId))
            return postsPagesCount.get(groupId);
        else
            return 0;
    }

    public void addNewPost(UUID groupId, BaseItem post)
    {
        getPostsById(groupId, false).add(post);
    }

    public void addNewAuthor(Author author)
    {
        if(authors.containsKey(author.getId()))
        {
            Author orig = authors.get(author.getId());
            orig.setRating(author.getRating());
            orig.setPlusVoted(author.isPlusVoted());
            orig.setMinusVoted(author.isMinusVoted());
        }
        else
            authors.put(author.getId(), author);
    }

    public Author getAuthorById(String id)
    {
        return authors.get(id);
    }

    public Author getAuthorByName(String name)
    {
        synchronized (authors)
        {
            for (Entry<String, Author> a : authors.entrySet())
            {
                if (a.getValue().getUserName().equals(name)) return a.getValue();
            }
        }

        return null;
    }

    public void clearCommentsById(UUID id)
    {
        comments.remove(id);
    }

    public void clearPostsById(UUID groupId)
    {
        getPostsById(groupId, false).clear();
    }

    public void clearComments()
    {
        comments.clear();
    }

    public void clearPosts()
    {
        posts.clear();
    }
}