package com.home.lepradroid.utils;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.home.lepradroid.*;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.objects.Blog;
import com.home.lepradroid.serverworker.ServerWorker;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinksCatcher extends WebViewClient
{
    public static final int LINK_POST           = 1;
    public static final int LINK_COMMENT        = 2;
    public static final int LINK_PROFILE        = 3;
    public static final int LINK_BLOG           = 4;
    public static final int LINK_INBOX          = 5;

    private static final String PATTERN_BLOG    = ".*leprosorium.ru/?";
    private static final String PATTERN_POST    = ".*leprosorium.ru/comments/\\d{5,8}(/)?(#new)?";
    private static final String PATTERN_COMMENT = "(.*leprosorium.ru/comments/\\d{5,8}(/)?)#(\\d{5,8})";
    private static final String PATTERN_PROFILE = ".*leprosorium.ru/users/(.*)(/)?";
    private static final String PATTERN_INBOX   = ".*leprosorium.ru/my/inbox/\\d{5,8}(/)?(#new)?";
    
    private static volatile LinksCatcher instance;
    
    private LinksCatcher()
    {
    }
    
    public static LinksCatcher Instance()
    {
        if(instance == null)
        {
            synchronized (LinksCatcher.class)
            {
                if(instance == null)
                {
                    instance = new LinksCatcher();
                }
            }
        }
        
        return instance;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
        Pattern patternPost = Pattern.compile(PATTERN_POST);
        Pattern patternInbox = Pattern.compile(PATTERN_INBOX);
        Pattern patternComment = Pattern.compile(PATTERN_COMMENT);
        Pattern patternProfile = Pattern.compile(PATTERN_PROFILE);
        Pattern patternBlog = Pattern.compile(PATTERN_BLOG);
        Matcher matcher;

        int linkType = -1;

        if ((matcher = patternPost.matcher(url)) != null && matcher.matches())
            linkType = LINK_POST;
        else if ((matcher = patternInbox.matcher(url)) != null && matcher.matches())
            linkType = LINK_INBOX;
        else if ((matcher = patternComment.matcher(url)) != null && matcher.matches())
            linkType = LINK_COMMENT;
        else if ((matcher = patternProfile.matcher(url)) != null && matcher.matches())
            linkType = LINK_PROFILE;
        else if ((matcher = patternBlog.matcher(url)) != null && matcher.matches())
            linkType = LINK_BLOG;

        if (    linkType == LINK_POST ||
                linkType == LINK_INBOX)
        {
            Intent stubIntent = new Intent(LepraDroidApplication.getInstance(),
                    StubScreen.class);
            stubIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            stubIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            stubIntent.putExtra("url", url.replace("http:", "https:"));
            stubIntent.putExtra("type", linkType);
            LepraDroidApplication.getInstance().startActivity(stubIntent);

        }
        else if (linkType == LINK_PROFILE)
        {
            Intent stubIntent = new Intent(LepraDroidApplication.getInstance(),
                    AuthorScreen.class);
            stubIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            stubIntent.putExtra("username", matcher.group(1));
            LepraDroidApplication.getInstance().startActivity(stubIntent);
        }
        else if (linkType == LINK_BLOG)
        {
            Blog blog = new Blog();
            blog.setId(UUID.randomUUID());
            blog.setUrl(matcher.group(0));
            ServerWorker.Instance().addNewPost(Commons.BLOGS_POSTS_ID, blog);

            Intent intent = new Intent(LepraDroidApplication.getInstance(), BlogScreen.class);
            intent.putExtra("groupId", Commons.BLOGS_POSTS_ID.toString());
            intent.putExtra("id", blog.getId().toString());
            String title = blog.getUrl();
            intent.putExtra("title", title.length() > Commons.MAX_BLOG_HEADER_LENGTH ? title.substring(0, Commons.MAX_BLOG_HEADER_LENGTH - 1) + "..." : title);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            LepraDroidApplication.getInstance().startActivity(intent);
        }
        else if (linkType == LINK_COMMENT)
        {
            Intent stubIntent = new Intent(LepraDroidApplication.getInstance(),
                    StubScreen.class);
            stubIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            stubIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            stubIntent.putExtra("url", matcher.group(1));
            stubIntent.putExtra("commentId", matcher.group(2));
            stubIntent.putExtra("type", LINK_COMMENT);
            LepraDroidApplication.getInstance().startActivity(stubIntent);
        }
        else
        {
            try
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                LepraDroidApplication.getInstance().startActivity(intent);
            }
            catch(Exception ignore)
            {
                //TODO
            }
        }

        return true;
    }
}
