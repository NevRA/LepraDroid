package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.http.Header;

import android.text.TextUtils;
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
            String cookie = "";
            Header[] headers = ServerWorker.Instance().login(Commons.LOGON_PAGE_URL, login, password, captcha, ServerWorker.Instance().getLoginCode());
            for(Header header : headers)
            {
                //lepro.sid=abadb37b85cd113156aea908ede94f77; lepro.uid=46808;
                
                String value = header.getValue();
                if(value.contains("lepro.save=1"))
                    logoned = true;
                else if(value.contains("lepro.sid="))
                    cookie += value.split(";")[0] + ";";
                else if(value.contains("lepro.uid="))
                    cookie += value.split(";")[0] + ";";
            }
            
            if(!logoned || TextUtils.isEmpty(cookie))
                throw new Exception(Utils.getString(R.string.Login_Failed));
            
            SettingsWorker.Instance().saveCookies(cookie);
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
