package com.home.lepradroid.tasks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.FileCache;
import com.home.lepradroid.utils.Logger;
import com.home.lepradroid.utils.Utils;

public class GetCommentsTask extends BaseTask
{
    final private int BUFFER_SIZE = 4 * 1024;

    private Post    post;
    private String  commentToSelectId   = null;
    private int     commentToSelect     = -1;
    private short   commentsCount       = 0;
    private String  postAuthor          = "";
    private String  userName            = "";
    private boolean isImagesEnabled     = true;
    private boolean isCommentWtfLoaded  = false;
    private int     totalBytesReaded    = 0;
    private int     totalBytesParsed    = 0;
    
    static final Pattern patternLevel = Pattern.compile("post tree indent_(\\S*)");
    static final Class<?>[] argsClassesOnCommentsUpdateFinished = new Class[2];
    static final Class<?>[] argsClassesOnCommentsUpdateFirstEtries = new Class[4];
    static final Class<?>[] argsClassesOnCommentsUpdateBegin = new Class[1];
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
            argsClassesOnCommentsUpdateFinished[1] = int.class;
            methodOnCommentsUpdateFinished = CommentsUpdateListener.class.getMethod("OnCommentsUpdateFinished", argsClassesOnCommentsUpdateFinished);
            
            argsClassesOnCommentsUpdateFirstEtries[0] = UUID.class;
            argsClassesOnCommentsUpdateFirstEtries[1] = int.class;
            argsClassesOnCommentsUpdateFirstEtries[2] = int.class;
            argsClassesOnCommentsUpdateFirstEtries[3] = int.class;
            methodOnCommentsUpdateFirstEtries = CommentsUpdateListener.class.getMethod("OnCommentsUpdateFirstEntries", argsClassesOnCommentsUpdateFirstEtries); 
            
            argsClassesOnCommentsUpdateBegin[0] = UUID.class;
            methodOnCommentsUpdateBegin = CommentsUpdateListener.class.getMethod("OnCommentsUpdateBegin", argsClassesOnCommentsUpdateBegin); 
            
            argsClassesOnCommentsUpdate[0] = UUID.class;
            argsClassesOnCommentsUpdate[1] = int.class;
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

    public GetCommentsTask(UUID postId, String commentToSelectId)
    {
        post = (Post)ServerWorker.Instance().getPostById(postId);
        this.commentToSelectId = commentToSelectId;
        isImagesEnabled = Utils.isImagesEnabled();
        isCommentWtfLoaded = !TextUtils.isEmpty(SettingsWorker.Instance().loadCommentWtf());
    }
    
    public GetCommentsTask(UUID postId)
    {
        post = (Post)ServerWorker.Instance().getPostById(postId);
        isImagesEnabled = Utils.isImagesEnabled();
        isCommentWtfLoaded = !TextUtils.isEmpty(SettingsWorker.Instance().loadCommentWtf());
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutCommentsUpdateBegin()
    {
        final List<CommentsUpdateListener> listeners = ListenersWorker.Instance().getListeners(CommentsUpdateListener.class);
        final Object args[] = new Object[1];
        args[0] = post.getId();
        
        for(CommentsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnCommentsUpdateBegin, args)));
        }
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutFirstCommentsUpdate()
    {
        final List<CommentsUpdateListener> listeners = ListenersWorker.Instance().getListeners(CommentsUpdateListener.class);
        final Object args[] = new Object[4];
        args[0] = post.getId();
        args[1] = totalBytesParsed;
        args[2] = totalBytesReaded;
        args[3] = commentToSelect;
        
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
        args[0] = post.getId();
        args[1] = commentToSelect;
        
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
        args[0] = post.getId();
        args[1] = totalBytesParsed;
        
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
    
    private String getCommentWtfAndReturnFirstPageAfterIt(FileInputStream stream) throws Exception
    {  
        int len;
        byte[] chars = new byte[BUFFER_SIZE];
        String header = "";
        while((len = readBytesToBuff_WithoutNonLatinCharsAtTheEnd(stream, chars)) > 0)
        {
            String str = new String(chars, 0, len, "UTF-8"); 
            header += str;
            int pos = header.indexOf("comments-form");
            if(pos > 0)
            {
                header = header.substring(pos - 50);
                Element commentsForm = Jsoup.parse(header).getElementById("comments-form");
                if(commentsForm != null)
                {
                    Elements wtf = commentsForm.getElementsByAttributeValue("name", "wtf");
                    if(!wtf.isEmpty())
                        SettingsWorker.Instance().saveCommentWtf(wtf.attr("value"));
                    else
                        continue;
                    
                    isCommentWtfLoaded = true;
                    return str;
                }
            }
        }
                        
        throw new Exception("Не могу найти заголовок страницы"); // TODO from resources
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
            ServerWorker.Instance().clearCommentsById(post.getId());
            notifyAboutCommentsUpdateBegin();

            userName = SettingsWorker.Instance().loadUserName();
            postAuthor = post.getAuthor();
            
            final String pref = "<div id=\"XXXXXXXX\" ";
            final String postTree = "class=\"post tree";
            
            try 
            {
                // TODO CHANGE TO NORMAL PARSING
                
                stream = new BufferedInputStream(ServerWorker.Instance().getContentStream(post.getUrl()), BUFFER_SIZE);
                file = new FileCache(LepraDroidApplication.getInstance()).getFile(post.getId().toString() + ".comments");
                FileOutputStream fos = new FileOutputStream(file);
                byte[] chars = new byte[BUFFER_SIZE];
                int len = 0;

                while (len != -1)
                {
                    if(isCancelled()) break;
                    
                    fos.write(chars, 0, len);
                    
                    len = stream.read(chars, 0, BUFFER_SIZE);
                    totalBytesReaded += len;
                }
                
                fos.close();
                
                post.setNewComments((short)-1);
                
                String pageA = null, pageB;
                int start = -1;
                int end = -1;                
                fileStream = new FileInputStream(file);
                
                if(!isCommentWtfLoaded)
                    pageA = getCommentWtfAndReturnFirstPageAfterIt(fileStream);
                                
                while(true)
                {
                    if(isCancelled()) break;
                    
                    if((len = readBytesToBuff_WithoutNonLatinCharsAtTheEnd(fileStream, chars)) < 0)
                    {
                        if(start >= 0 && end < 0)
                            parseRecord(pageA); // to read last record
                        
                        break;
                    }

                    totalBytesParsed += len;
                    
                    if(len == 0)
                        continue;
                    else if(pageA == null)
                    {
                        pageA = new String(chars, 0, len, "UTF-8");
                        continue;
                    }
                    else
                    {
                        pageB = new String(chars, 0, len, "UTF-8");
                    }

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
                    if(!file.delete())
                    {
                        //TODO
                    }
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
            
            new UpdateBadgeCounterTask().execute();
            
            Logger.d("GetCommentsTask time:" + Long.toString(System.nanoTime() - startTime));
        }
   
        return e;
    }
    
    private void parseRecord(String html) throws ExecutionException, InterruptedException
    {
        Element content = Jsoup.parse(Utils.replaceBadHtmlTags(html));
        Element element = content.getElementsByClass("dt").first();
        Element root = content.getElementsByTag("div").first();

        Comment comment = new Comment();

        if(root.attr("class").contains("hidden_comment"))
            return;

        if(root.attr("class").contains("new"))
            comment.setNew(true);

        String commentId = root.id();
        comment.setLepraId(commentId);

        if(commentId.equals(commentToSelectId))
            commentToSelect = commentsCount;
        
        Matcher level = patternLevel.matcher(root.className());
        if(level.find())
            comment.setLevel(Short.valueOf(level.group(1)));

        Elements images = element.getElementsByTag("img");
        int imageNum = 0;
        List<Pair<String, String>> imgs = new ArrayList<Pair<String, String>>();
        for (Element image : images)
        {
            String src = image.attr("src");
            if(isImagesEnabled && !TextUtils.isEmpty(src))
            {
                String id = "img" + Integer.valueOf(imageNum).toString();  
                
                if(!image.parent().tag().getName().equalsIgnoreCase("a"))
                    image.wrap("<a href=" + "\"" + src + "\"></a>");
                
                image.removeAttr("width");
                image.removeAttr("height");
                image.removeAttr("src");
                image.removeAttr("id");
                
                image.attributes().put("id", id);
                image.attributes().put("src", Commons.IMAGE_STUB);
                image.attributes().put("onLoad", "getSrcData(\"" + id + "\", \"" + src + "\", " + Integer.valueOf(comment.getLevel()).toString() + ");");
                imgs.add(new Pair<String, String>(id, src));
                
                imageNum++;
            }
            else
                image.remove();
        }
        
        comment.setHtml(Utils.getImagesStub(imgs, comment.getLevel()) + Utils.wrapLepraTags(element));
        if(     imgs.isEmpty() && 
                !Utils.isContainExtraTagsForWebView(comment.getHtml()))
        {
            comment.setOnlyText(true);
        }

        Element authorElement = content.getElementsByClass("p").first();
        if(authorElement != null)
        {
            Elements a = authorElement.getElementsByTag("a");
            comment.setUrl(Commons.SITE_URL + a.first().attr("href"));
            
            String author = a.get(1).text();
            if(postAuthor.equals(author))
               comment.setPostAuthor(true);
            
            String color = "black";
            if(comment.isPostAuthor())
                color = "red";
            else if (author.equals(userName))
                color = "#3270FF";
            
            comment.setAuthor(author);
            comment.setSignature(authorElement.text().split("\\|")[0].replace(author, "<b>" + "<font color=\"" + color + "\">" + author + "</font>" + "</b>"));
        }

        if(!post.isInbox())
        {
            Element vote = content.getElementsByClass("vote").first();
            if(vote != null)
            {
                String voteBody = vote.html();
                comment.setPlusVoted(voteBody.contains("class=\"plus voted\""));
                comment.setMinusVoted(voteBody.contains("class=\"minus voted\""));

                if(     !post.isMain() &&
                        post.getVoteWeight() == -1 &&
                            (comment.isPlusVoted() ||
                             comment.isMinusVoted()))
                {
                    new SetVoteWeightTask(post.getId(), commentId)
                        .execute()
                            .get();
                }

                Element rating = vote.getElementsByTag("em").first();
                comment.setRating(Short.valueOf(rating.text()));
            }
        }
        
        comment.setNum(commentsCount);
                     
        ServerWorker.Instance().addNewComment(post.getId(), comment);
        
        commentsCount++;

        if(commentToSelectId != null)
        {
            if(     commentToSelect != -1 &&
                    commentsCount >= 50 + commentToSelect)
            {
                notifyAboutFirstCommentsUpdate();

                commentToSelectId = null;
                commentToSelect = -1;
            }
        }
        else
        {
            if(commentsCount == 50)
            {
                notifyAboutFirstCommentsUpdate();
            }
            else if(commentsCount != 0 &&
                    commentsCount % 100 == 0)
            {
                notifyAboutCommentsUpdate();
            }
        }
    }
}