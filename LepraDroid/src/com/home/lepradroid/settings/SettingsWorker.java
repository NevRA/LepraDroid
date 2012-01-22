package com.home.lepradroid.settings;

import org.apache.http.Header;

public class SettingsWorker
{
    private static volatile SettingsWorker instance;
    private Header[] cookies = new Header[0];
    
    private SettingsWorker() 
    {
    }

    public static SettingsWorker Instance()
    {
        if(instance == null)
        {
            synchronized (SettingsWorker.class)
            {
                if(instance == null)
                {
                    instance = new SettingsWorker();
                }
            }
        }
        
        return instance;
    }
    
    public void saveCookies(Header[] cookies)
    {
        this.cookies = cookies;
    }
    
    public Header[] loadCookies()
    {
        return cookies;
    }
}
