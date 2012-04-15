package com.home.lepradroid.commons;

import java.util.UUID;

public class Commons
{
    public static final String SITE_URL         = "http://leprosorium.ru/";
    public static final String LOGON_PAGE_URL   = SITE_URL + "login/";
    public static final String CAPTCHA_URL      = SITE_URL + "captchaa/";
    public static final String MY_STUFF_URL     = SITE_URL + "my/";
    public static final String FAVORITES_URL    = MY_STUFF_URL + "favourites/";
    public static final String INBOX_URL        = MY_STUFF_URL + "inbox/";
    public static final String POST_COMMENT_URL        
                                                = SITE_URL + "commctl/";    
    public static final String BLOGS_URL        = SITE_URL + "underground/";
    public static final String ITEM_VOTE_URL    = SITE_URL + "rate/";
    public static final String KARMA_VOTE_URL   = SITE_URL + "karma/";
    public static final String LEPROPANEL_URL   = SITE_URL + "/api/lepropanel";
    public static final String THRESHOLD_URL    = SITE_URL + "/threshold/";

    public static final String APP_TAG          = "LepraDroid";
    
    public static final String COOKIE_SID       = "lepro.sid";
    public static final String COOKIE_UID       = "lepro.uid";
    
    public static final String PREFS_NAME       = "MyPrefsFile";
    
    public static final Integer EXIT_FROM_LOGON_SCREEN_RESULTCODE 
                                                = 0;
    
    public static final Integer EXIT_FROM_LOGON_SCREEN_AFTER_LOGON_RESULTCODE 
                                                = 1;
    
    public static final Integer POST_PREVIEW_NORMAL_SIZE
                                                = 80;
    
    public static final Integer POST_PREVIEW_BIG_SIZE
                                                = 240;
    
    public static final Integer WEBVIEW_DEFAULT_FONT_SIZE
                                                = 13;
    
    public static final Integer MAX_COMMENTS_COUNT  
                                                = 5000;
    
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

    public static final String DELIMETER        = " / ";
    
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
}
