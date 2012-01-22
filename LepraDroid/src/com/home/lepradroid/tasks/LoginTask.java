package com.home.lepradroid.tasks;

import org.apache.http.Header;

import com.home.lepradroid.R;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Utils;

public class LoginTask extends BaseTask
{
    private String login; 
    private String password; 
    private String captcha;
    
    public LoginTask(String login, String password, String captcha)
    {
        this.login = login;
        this.password = password;
        this.captcha = captcha;
    }
    
    @Override
    protected Throwable doInBackground(Void... arg0)
    {
        try
        {
            boolean logoned = false;
            Header[] cookies = ServerWorker.Instance().login(Commons.LOGON_PAGE_URL, login, password, captcha, ServerWorker.Instance().getLoginCode());
            for(Header cookie : cookies)
            {
                if(cookie.getValue().contains("lepro.save=1"))
                {
                    SettingsWorker.Instance().saveCookies(cookies);
                    logoned = true;
                }
            }
            
            if(!logoned)
                throw new Exception(Utils.getString(R.string.Login_Failed));
        }
        catch (Throwable t)
        {
            setException(t);
        }
        
        return e;
    }
}
