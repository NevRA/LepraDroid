package com.home.lepradroid.commons;

import java.util.UUID;

public class Commons
{
    public static final String SITE_URL = "http://leprosorium.ru/";
    public static final String LOGON_PAGE_URL = SITE_URL + "login/";
    public static final String CAPTCHA_URL = SITE_URL + "captchaa/";
    public static final String MY_STUFF_URL = SITE_URL + "my/";
    public static final String FAVORITES_URL = MY_STUFF_URL + "favourites/";
    
    public static final String BLOGS_URL = SITE_URL + "underground/";
    
    public static final String APP_TAG = "LepraDroid";
    
    public static final String COOKIE_SID = "lepro.sid";
    public static final String COOKIE_UID = "lepro.uid";
    
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final int EXIT_FROM_LOGON_SCREEN_RESULTCODE = 0;
    public static final int EXIT_FROM_LOGON_SCREEN_AFTER_LOGON_RESULTCODE = 1;
    
    public static final UUID MAIN_POSTS_ID = UUID.randomUUID();
    public static final UUID MYSTUFF_POSTS_ID = UUID.randomUUID();
    public static final UUID BLOGS_POSTS_ID = UUID.randomUUID();
    public static final UUID FAVORITE_POSTS_ID = UUID.randomUUID();
}
