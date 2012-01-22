package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.http.Header;

import android.util.Pair;

import com.home.lepradroid.R;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.LoginListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Logger;
import com.home.lepradroid.utils.Utils;

public class LoginTask extends BaseTask
{
    static final Class<?>[] argsClasses = new Class[1];
    static Method method;
    static 
    {
        try
        {
            argsClasses[0] = boolean.class;
            method = LoginListener.class.getMethod("OnLogin", argsClasses);    
        }
        catch (Throwable t) 
        {           
            Logger.e(t);
        }        
    }
    
    private String login; 
    private String password; 
    private String captcha;
    private boolean logoned = false;
    
    public LoginTask(String login, String password, String captcha)
    {
        this.login = login;
        this.password = password;
        this.captcha = captcha;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected Throwable doInBackground(Void... arg0)
    {
        try
        {
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
        finally
        {
            final List<LoginListener> listeners = ListenersWorker.Instance().getListeners(LoginListener.class);
            final Object args[] = new Object[1];
                
            args[0] = logoned;
            
            for(LoginListener listener : listeners)
            {
                publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (method, args)));
            }
        }
        
        return e;
    }
}
