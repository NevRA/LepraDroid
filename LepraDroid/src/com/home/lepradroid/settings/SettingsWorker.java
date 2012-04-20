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
    private static final String UNREAD_COUNTER = "unread_counter_pref";
    private static final String COOKIES        = "cookies_pref";
    private static final String USER_NAME      = "username_pref";
    private static final String COMMENT_WTF    = "commentwtf_pref";
    private static final String VOTE_WTF       = "votewtf_pref";
    private static final String VOTE_WEIGHT    = "voteweight_pref";
    private static final String VOTE_KARMA_WTF = "votekarmawtf_pref";
    private static final String VERSION        = "version_pref";
    private static final String MAIN_THRESHOLD = "threshold_pref";

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
        saveInt(UNREAD_COUNTER, 0);
        saveString(MAIN_THRESHOLD, "");
        saveString(VERSION, "");
        saveString(USER_NAME, "");
        saveString(COMMENT_WTF, "");
        saveString(VOTE_WTF, "");
        saveString(VOTE_KARMA_WTF, "");
    }
    
    public String loadCommentWtf()
    {
        return loadString(COMMENT_WTF);
    }
    
    public void saveCommentWtf(String comment_wtf)
    {
        saveString(COMMENT_WTF, comment_wtf);
    }
    
    public String loadVoteWtf()
    {
        return loadString(VOTE_WTF);
    }
    
    public void saveVoteWtf(String vote_wtf)
    {
        saveString(VOTE_WTF, vote_wtf);
    }

    public String loadVoteKarmaWtf()
    {
        return loadString(VOTE_KARMA_WTF);
    }

    public void saveVoteKarmaWtf(String vote_karma_wtf)
    {
        saveString(VOTE_KARMA_WTF, vote_karma_wtf);
    }
    
    public String loadMainThreshold()
    {
        return loadString(MAIN_THRESHOLD);
    }
    
    public void saveMainThreshold(String threshold)
    {
        saveString(MAIN_THRESHOLD, threshold);
    }
    
    public Integer loadUnreadCounter()
    {
        return loadInt(UNREAD_COUNTER);
    }
    
    public void saveUnreadCounter(Integer counter)
    {
        saveInt(UNREAD_COUNTER, counter);
    }

    public Integer loadVoteWeight()
    {
        return loadInt(VOTE_WEIGHT);
    }
    
    public void saveVoteWeight(Integer voteWeight)
    {
        saveInt(VOTE_WEIGHT, voteWeight);
    }
    
    public String loadVersion()
    {
        return loadString(VERSION);
    }
    
    public void saveVersion(String version)
    {
        saveString(VERSION, version);
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
        saveString(COOKIES, auth.first + ";" + auth.second);
    }
    
    public boolean IsLogoned() throws Exception
    {
        return (loadCookie() != null);
    }
    
    public Pair<String, String> loadCookie() throws Exception
    {
        String cookie = loadString(COOKIES);
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
    
    /*private void saveBoolean(String name, Boolean value)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(LepraDroidApplication.getInstance());
        Editor e = sp.edit();
        e.putBoolean(name, value);
        e.commit();
    }
    
    private Boolean loadBoolean(String name)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(LepraDroidApplication.getInstance());
        return sp.getBoolean(name, false);
    }*/
}
