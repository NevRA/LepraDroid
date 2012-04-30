package com.home.lepradroid.utils;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.home.lepradroid.AuthorScreen;
import com.home.lepradroid.ImagesWorker;
import com.home.lepradroid.LepraDroidApplication;
import com.home.lepradroid.StubScreen;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinksCatcher extends WebViewClient
{
    public static final int LINK_POST           = 1;
    public static final int LINK_COMMENT        = 2;
    public static final int LINK_PROFILE        = 3;

    private static final String PATTERN_POST    = "http://.*leprosorium.ru/comments/\\d{5,8}(#new)?";
    private static final String PATTERN_COMMENT = "http://.*leprosorium.ru/comments/\\d{5,8}#\\d{5,8}";
    private static final String PATTERN_PROFILE = "http://leprosorium.ru/users/(.*)";
    
    private static volatile LinksCatcher instance;
    
    private LinksCatcher()
    {
        
    }
    
    public static LinksCatcher Instance()
    {
        if(instance == null)
        {
            synchronized (ImagesWorker.class)
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
        Pattern patternComment = Pattern.compile(PATTERN_COMMENT);
        Pattern patternProfile = Pattern.compile(PATTERN_PROFILE);
        Matcher matcher;

        int linkType = -1;

        if ((matcher = patternPost.matcher(url)) != null && matcher.matches())
            linkType = LINK_POST;

        if ((matcher = patternComment.matcher(url)) != null && matcher.matches())
            // linkType = LINK_COMMENT; // not implemented yet
            linkType = -1;

        if ((matcher = patternProfile.matcher(url)) != null && matcher.matches())
            linkType = LINK_PROFILE;

        if (linkType == LINK_POST)
        {
            Intent stubIntent = new Intent(LepraDroidApplication.getInstance(),
                    StubScreen.class);
            stubIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            stubIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            stubIntent.putExtra("url", url);
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
