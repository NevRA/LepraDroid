package com.home.lepradroid.tasks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.text.TextUtils;
import android.util.Pair;

import com.home.lepradroid.LepraDroidApplication;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.CommentsUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Comment;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.FileCache;
import com.home.lepradroid.utils.Logger;

public class GetCommentsTask extends BaseTask
{
    final private int BUFFER_SIZE = 4 * 1024;
    
    private UUID groupId;
    private UUID postId;
    private int commentsCout = 0;
    private String postAuthor = "";
    
    static final Class<?>[] argsClassesOnCommentsUpdateFinished = new Class[2];
    static final Class<?>[] argsClassesOnCommentsUpdateFirstEtries = new Class[2];
    static final Class<?>[] argsClassesOnCommentsUpdateBegin = new Class[2];
    static final Class<?>[] argsClassesOnCommentsUpdate = new Class[2];
    static Method methodOnCommentsUpdateFinished;
    static Method methodOnCommentsUpdateFirstEtries;
    static Method methodOnCommentsUpdateBegin;
    static Method methodOnCommentsUpdate;
    static 
    {
        try
        {
            argsClassesOnCommentsUpdateFinished[0] = UUID.class;
            argsClassesOnCommentsUpdateFinished[1] = UUID.class;
            methodOnCommentsUpdateFinished = CommentsUpdateListener.class.getMethod("OnCommentsUpdateFinished", argsClassesOnCommentsUpdateFinished);
            
            argsClassesOnCommentsUpdateFirstEtries[0] = UUID.class;
            argsClassesOnCommentsUpdateFirstEtries[1] = UUID.class;
            methodOnCommentsUpdateFirstEtries = CommentsUpdateListener.class.getMethod("OnCommentsUpdateFirstEntries", argsClassesOnCommentsUpdateFirstEtries); 
            
            argsClassesOnCommentsUpdateBegin[0] = UUID.class;
            argsClassesOnCommentsUpdateBegin[1] = UUID.class;
            methodOnCommentsUpdateBegin = CommentsUpdateListener.class.getMethod("OnCommentsUpdateBegin", argsClassesOnCommentsUpdateBegin); 
            
            argsClassesOnCommentsUpdate[0] = UUID.class;
            argsClassesOnCommentsUpdate[1] = UUID.class;
            methodOnCommentsUpdate = CommentsUpdateListener.class.getMethod("OnCommentsUpdate", argsClassesOnCommentsUpdate); 
        }
        catch (Throwable t) 
        {           
            Logger.e(t);
        }        
    }
    
    @Override
    public void finish()
    {
        super.finish();
    }
    
    public GetCommentsTask(UUID groupId, UUID postId)
    {
        this.groupId = groupId;
        this.postId = postId;
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutCommentsUpdateBegin()
    {
        final List<CommentsUpdateListener> listeners = ListenersWorker.Instance().getListeners(CommentsUpdateListener.class);
        final Object args[] = new Object[2];
        args[0] = groupId;
        args[1] = postId;
        
        for(CommentsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnCommentsUpdateBegin, args)));
        }
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutFirstCommentsUpdate()
    {
        final List<CommentsUpdateListener> listeners = ListenersWorker.Instance().getListeners(CommentsUpdateListener.class);
        final Object args[] = new Object[2];
        args[0] = groupId;
        args[1] = postId;
        
        for(CommentsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnCommentsUpdateFirstEtries, args)));
        }
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutCommentsUpdateFinished()
    {
        final List<CommentsUpdateListener> listeners = ListenersWorker.Instance().getListeners(CommentsUpdateListener.class);
        final Object args[] = new Object[2];
        args[0] = groupId;
        args[1] = postId;
        
        for(CommentsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnCommentsUpdateFinished, args)));
        }
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutCommentsUpdate()
    {
        final List<CommentsUpdateListener> listeners = ListenersWorker.Instance().getListeners(CommentsUpdateListener.class);
        final Object args[] = new Object[2];
        args[0] = groupId;
        args[1] = postId;
        
        for(CommentsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnCommentsUpdate, args)));
        }
    }
    
    private int readBytesToBuff_WithoutNonLatinCharsAtTheEnd(FileInputStream stream, byte[] buffer) throws IOException
    {
        int len = stream.read(buffer, 0, BUFFER_SIZE / 2);
        
        while(len > 0 && len < BUFFER_SIZE && buffer[len - 1] < 0 )
        {
            int readed = stream.read(buffer, len, 1);
            if(readed == -1) break;
            
            len += readed;
        }
        
        return len;
    }
    
    @Override
    protected Throwable doInBackground(Void... arg0)
    {
        final long startTime = System.nanoTime();
        
        BufferedInputStream stream = null;
        FileInputStream fileStream = null;
        File file = null;
        
        try
        {
            ServerWorker.Instance().clearCommentsById(postId);
            notifyAboutCommentsUpdateBegin();

            Post post = (Post)ServerWorker.Instance().getPostById(groupId, postId);
            if(post == null)
                return null; // TODO message
            
            postAuthor = post.Author;
            
            final String pref = "<div id=\"XXXXXXXX\" ";
            final String postTree = "class=\"post tree";
            
            try 
            {
                // TODO CHANGE TO NORMAL PARSING
                
                stream = new BufferedInputStream(ServerWorker.Instance().getContentStream(post.Url), BUFFER_SIZE);
                file = new FileCache(LepraDroidApplication.getInstance()).getFile("comments");
                FileOutputStream fos = new FileOutputStream(file);
                byte[] chars = new byte[BUFFER_SIZE];
                int len = 0;

                while (len != -1)
                {
                    if(isCancelled()) break;
                    
                    fos.write(chars, 0, len);
                    
                    len = stream.read(chars, 0, BUFFER_SIZE);
                }
                
                fos.close();
                
                post.NewComments = -1;
                
                String pageA = null, pageB = null;
                int start = -1;
                int end = -1;
                String header = "";
                
                fileStream = new FileInputStream(file);
                while(true)
                {
                    if(isCancelled()) break;
                    
                    if((len = readBytesToBuff_WithoutNonLatinCharsAtTheEnd(fileStream, chars)) < 0)
                    {
                        if(start >= 0 && end < 0)
                            parseRecord(pageA); // to read last record
                        
                        break;
                    }
                    
                    if(len == 0)
                        continue;
                    else if(pageA == null)
                    {
                        pageA = new String(chars, 0, len, "UTF-8");
                        if(TextUtils.isEmpty(post.commentsWtf))
                            header += pageA;
                        continue;
                    }
                    else
                    {
                        
                        pageB = new String(chars, 0, len, "UTF-8");
                        if(TextUtils.isEmpty(post.commentsWtf))
                            header += pageB;
                    }
                    
                    if(TextUtils.isEmpty(post.commentsWtf) && !TextUtils.isEmpty(header))
                    {
                        Element content = Jsoup.parse(header);
                        Element commentsForm = content.getElementById("comments-form");
                        if(commentsForm != null)
                        {
                            Elements wtf = commentsForm.getElementsByAttributeValue("name", "wtf");
                            if(!wtf.isEmpty())
                            {
                                post.commentsWtf = wtf.attr("value");
                            }
                            
                            header = null;
                        }
                    }
                    
                    if(TextUtils.isEmpty(post.commentsWtf))
                        continue;
               
                    while(true)
                    {
                        if(isCancelled()) break;
                        
                        String html = pageA + pageB;
    
                        start = start >= 0 ? start : html.indexOf(postTree, 0) - pref.length();
                        if(start < 0)
                        {
                            if(!TextUtils.isEmpty(pageB))
                                pageA = pageB;
    
                            break;
                        }
                        if(start > 0)
                        {
                            html = html.substring(start, html.length());
                            start = 0;
                        }
                        
                        end = html.length() > 500 ? html.indexOf(postTree,  500) - pref.length() : -1;
                        if(end < 0)
                        {
                            pageA = html;
    
                            break;
                        }
                        
                        pageA = html.substring(end, html.length());  
                        pageB = "";
    
                        parseRecord(html.substring(start, end));
                    }
                }
            } 
            finally 
            {
                if(stream != null)
                    stream.close();
                if(fileStream != null)
                    fileStream.close();
                if(file != null)
                    file.delete();
            }
        }
        catch (Throwable t)
        {
            Logger.e(t);
            setException(t);
        }
        finally
        {
            if(!isCancelled())
                notifyAboutCommentsUpdateFinished();
            
            Logger.d("GetCommentsTask time:" + Long.toString(System.nanoTime() - startTime));
        }
   
        return e;
    }
    
    private void parseRecord(String html)
    {
        Element content = Jsoup.parse(html);
        Element element = content.getElementsByClass("dt").first();
        
        Comment comment = new Comment();
        comment.Pid = content.getElementsByTag("div").first().id();
        comment.Text = element.text();
        comment.Html = element.html();
        
        if(element.parent().attr("class").contains("new"))
            comment.IsNew = true;
        
        Elements images = element.getElementsByTag("img");
        if(!images.isEmpty())
        {
            comment.ImageUrl = images.first().attr("src");
            
            for (Element image : images)
            {
                String width = image.attr("width");
                if(!TextUtils.isEmpty(width))
                    comment.Html = comment.Html.replace("width=\"" + width + "\"", "");
                
                String height = image.attr("height");
                if(!TextUtils.isEmpty(height))
                    comment.Html = comment.Html.replace("height=\"" + height + "\"", "");
                
                comment.Html = comment.Html.replace(image.attr("src"), "http://src.sencha.io/305/305/" + image.attr("src"));
            }
        }

        Elements author = content.getElementsByClass("p");
        if(!author.isEmpty())
        {
            Elements a = author.first().getElementsByTag("a");
            comment.Url = Commons.SITE_URL + a.first().attr("href");
            
            comment.Author = a.get(1).text();
            if(postAuthor.equals(comment.Author))
               comment.IsPostAuthor = true;
               
            comment.Signature = author.first().text().split("\\|")[0].replace(comment.Author, "<b>" + (comment.IsPostAuthor ? "<font color=\"red\">" : "") + comment.Author + (comment.IsPostAuthor ? "</font>" : "") + "</b>");
        }
        
        Elements vote = content.getElementsByClass("vote");
        if(!vote.isEmpty())
        {
            Elements rating = vote.first().getElementsByTag("em");
            comment.Rating = Integer.valueOf(rating.first().text());
        }
        
        comment.PlusVoted = html.contains("class=\"plus voted\"");
        comment.MinusVoted = html.contains("class=\"minus voted\"");
                     
        ServerWorker.Instance().addNewComment(groupId, postId, comment);
        
        commentsCout ++;
        
        if(commentsCout == 50)
        {
            notifyAboutFirstCommentsUpdate();
        }
        else if(commentsCout != 0 && 
                commentsCout % 100 == 0)
        {
            notifyAboutCommentsUpdate();
        }
    }
}
