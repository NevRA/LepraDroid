package com.home.lepradroid.commons;

import java.util.UUID;

public class Commons
{
    public static final String PREFIX_URL       = "https:";
    public static final String SITE_URL         = PREFIX_URL + "//leprosorium.ru/";
    public static final String LOGON_PAGE_URL   = SITE_URL + "login/";
    public static final String CAPTCHA_URL      = SITE_URL + "captchaa/";
    public static final String AUTH_PAGE_URL    = SITE_URL + "ajax/auth/login/";
    public static final String MY_STUFF_URL     = SITE_URL + "ajax/interest/moar/";
    public static final String FAVORITES_URL    = SITE_URL + "ajax/favourites/list/";
    public static final String INBOX_URL        = SITE_URL + "ajax/inbox/moar/";
    public static final String AUTHOR_POSTS_URL = SITE_URL + "users/%s/posts/";
    public static final String POST_COMMENT_URL        
                                                = SITE_URL + "commctl/";    
    public static final String BLOGS_URL        = SITE_URL + "ajax/blogs/top/";
    public static final String POST_VOTE_URL    = SITE_URL + "ajax/vote/post/";
    public static final String COMMENT_VOTE_URL = SITE_URL + "ajax/vote/comment/";
    public static final String KARMA_VOTE_URL   = SITE_URL + "ajax/user/karma/vote/";
    public static final String THRESHOLD_URL    = SITE_URL + "/threshold/";
    public static final String MYCTL_IN_URL     = SITE_URL + "/ajax/interest/in/";
    public static final String MYCTL_OUT_URL    = SITE_URL + "/ajax/interest/out/";
    public static final String FAVSCTL_IN_URL   = SITE_URL + "/ajax/favourites/in/";
    public static final String FAVSCTL_OUT_URL  = SITE_URL + "/ajax/favourites/out/";
    public static final String ADD_INBOX_URL    = SITE_URL + "my/inbox/write/";
    public static final String COMMENT_RATING_URL
                                                = SITE_URL + "votesctl/";


    public static final String APP_TAG          = "LepraDroid";
    
    public static final String COOKIE_SID       = "sid";
    public static final String COOKIE_UID       = "uid";

    public static final Integer EXIT_FROM_LOGON_SCREEN_RESULTCODE
                                                = 0;
    
    public static final Integer EXIT_FROM_LOGON_SCREEN_AFTER_LOGON_RESULTCODE 
                                                = 1;
    
    public static final Float POST_PREVIEW_ICON_SIZE
                                                = 59f;
    
    public static final Integer WEBVIEW_DEFAULT_FONT_SIZE
                                                = 13;
    
    public static final Integer MAX_BLOG_HEADER_LENGTH  
                                                = 20;
    
    public static final Integer MAX_COMMENT_LEVEL  
                                                = 10;
    
    public static final Integer NOTIFICATION_ID  
                                                = 31337;
    
    public static final UUID MAIN_POSTS_ID      = UUID.randomUUID();
    public static final UUID MYSTUFF_POSTS_ID   = UUID.randomUUID();
    public static final UUID BLOGS_POSTS_ID     = UUID.randomUUID();
    public static final UUID FAVORITE_POSTS_ID  = UUID.randomUUID();
    public static final UUID INBOX_POSTS_ID     = UUID.randomUUID();
    public static final UUID OTHER_POSTS_ID     = UUID.randomUUID();
    
    public static final String IMAGE_STUB       = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAAMSURBVBhXY/j//z8ABf4C/qc1gYQAAAAASUVORK5CYII%3D";
    
    public static final String WEBVIEW_HEADER   
                                                = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><script type=\"text/javascript\">function getData(src, level){return ImagesWorker.getData(src, level)}</script>";
    
    public enum RateType
    {
        POST,
        COMMENT,
        KARMA
    }
    
    public enum RateValueType
    {
        MINUS,
        PLUS
    }
    
    public enum StuffOperationType
    {
        ADD,
        REMOVE
    }

    public enum PostsType
    {
        MAIN,
        MY,
        CUSTOM,
        USER
    }
}
