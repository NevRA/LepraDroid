package com.home.lepradroid.commons;

public class Commons
{
    public static final String SITE_URL = "http://leprosorium.ru/";
    public static final String LOGON_PAGE_URL = SITE_URL + "login/";
    public static final String CAPTCHA_URL = SITE_URL + "captchaa/";
    public static final String MY_STUFF_URL = SITE_URL + "my/";
    
    public static final String APP_TAG = "LepraDroid";
    
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final int EXIT_FROM_LOGON_SCREEN_RESULTCODE = 0;
    public static final int EXIT_FROM_LOGON_SCREEN_AFTER_LOGON_RESULTCODE = 1;
    
    public enum PostSourceType
    {
        MAIN, BLOGS, MYSTUFF
    }
}
