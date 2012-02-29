package com.home.lepradroid.settings;


import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Pair;

import com.home.lepradroid.LepraDroidApplication;
import com.home.lepradroid.utils.Logger;

public class SettingsWorker
{
    private static volatile SettingsWorker instance;
    private String cookie                   = "";
    private static final String COOKIES     = "cookies_pref";
    private static final String USER_NAME   = "username_pref";
    private static final String VOTE_WTF    = "votewtf_pref";
    private static final String VOTE_WEIGHT = "voteweight_pref";
    
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
        saveString(COOKIES, "");
    }
    
    public void clearUserInfo() throws Exception
    {
        saveInt(VOTE_WEIGHT, 0);
        saveString(USER_NAME, "");
        saveString(VOTE_WTF, "");
    }
    
    public String loadVoteWtf()
    {
        return loadString(VOTE_WTF);
    }
    
    public void saveVoteWtf(String vote_wtf)
    {
        saveString(VOTE_WTF, vote_wtf);
    }
    
    public Integer loadVoteWeight()
    {
        return loadInt(VOTE_WEIGHT);
    }
    
    public void saveVoteWeight(Integer voteWeight)
    {
        saveInt(VOTE_WEIGHT, voteWeight);
    }
    
    public String loadUserName()
    {
        return loadString(USER_NAME);
    }
    
    public void saveUserName(String userName)
    {
        saveString(USER_NAME, userName);
    }
    
    public void saveCookies(Pair<String, String> auth) throws Exception
    {
        this.cookie = auth.first + ";" + auth.second;
        
        saveString(COOKIES, cookie);
    }
    
    public boolean IsLogoned()
    {
        return (!TextUtils.isEmpty(cookie));
    }
    
    public Pair<String, String> loadCookie() throws Exception
    {
        cookie = loadString(COOKIES);
        if(TextUtils.isEmpty(cookie))
            return null;
        
        final String[] cookies = cookie.split(";");
        return new Pair<String, String>(cookies[0], cookies[1]);
    }
    
    private Integer loadInt(String name)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(LepraDroidApplication.getInstance());
        return sp.getInt(name, 0);
    }
    
    private void saveInt(String name, Integer value)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(LepraDroidApplication.getInstance());
        Editor e = sp.edit();
        e.putInt(name, value);
        e.commit();
    }
    
    private void saveString(String name, String value)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(LepraDroidApplication.getInstance());
        Editor e = sp.edit();
        e.putString(name, value);
        e.commit();
    }
    
    private String loadString(String name)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(LepraDroidApplication.getInstance());
        return sp.getString(name, "");
    }
}
