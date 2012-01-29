package com.home.lepradroid.settings;


import com.home.lepradroid.LepraDroidApplication;
import com.home.lepradroid.utils.Logger;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Pair;

public class SettingsWorker
{
    private static volatile SettingsWorker instance;
    private String cookie = "";
    private static final String COOKIES = "cookies_pref";
    
    private SettingsWorker() 
    {
        try
        {
            loadCookie();
        }
        catch (Exception e)
        {
            Logger.e(e);
        }
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
    
    public void clearCookies() throws Exception
    {
        save(COOKIES, "");
    }
    
    public void saveCookies(Pair<String, String> auth) throws Exception
    {
        this.cookie = auth.first + ";" + auth.second;
        
        save(COOKIES, cookie);
    }
    
    public boolean IsLogoned()
    {
        return (!TextUtils.isEmpty(cookie));
    }
    
    public Pair<String, String> loadCookie() throws Exception
    {
        cookie = load(COOKIES);
        if(TextUtils.isEmpty(cookie))
            return null;
        
        final String[] cookies = cookie.split(";");
        return new Pair<String, String>(cookies[0], cookies[1]);
    }
    
    private void save(String name, String value)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(LepraDroidApplication.getInstance());
        Editor e = sp.edit();
        e.putString(name, value);
        e.commit();
    }
    
    private String load(String name)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(LepraDroidApplication.getInstance());
        return sp.getString(name, "");
    }
}
