package com.home.lepradroid.tasks;

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
import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Method;
import java.util.List;

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

    private String sid;
    private String uid;

    private String login; 
    private String password; 
    private String captcha;

    public LoginTask(String login, String password, String captcha)
    {
        this.login = login;
        this.password = password;
        this.captcha = captcha;
    }

    private String getCaptchaPublicKey() throws Exception
    {
        final String loginPage = ServerWorker.Instance().getContent(Commons.LOGON_PAGE_URL);
        Document loginPageDocument = Jsoup.parse(loginPage);
        Element captchaElement = loginPageDocument.getElementsByTag("script").get(2);

        String publicKey = captchaElement.data().split(" = ")[1].replace("'","");
        int index = publicKey.indexOf(";");
        return publicKey.substring(0, index);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected Throwable doInBackground(Void... arg0)
    {
        try
        {
            final Pair<String, Header[]> loginInfo = ServerWorker.Instance().login(Commons.AUTH_PAGE_URL, login, password, "", captcha);
            for(Header header : loginInfo.second)
            {
                //lepro.sid=abadb37b85cd113156aea908ede94f77; lepro.uid=46808;
                
                String value = header.getValue();
                if(value.contains(Commons.COOKIE_SID + "="))
                {
                    String[] sidCookie = value.split(";")[0].split("=");
                    if(sidCookie.length > 1)
                        sid = sidCookie[1];
                }
                else if(value.contains(Commons.COOKIE_UID + "="))
                {
                    String[] uidCookie = value.split(";")[0].split("=");
                    if(uidCookie.length > 1)
                        uid = uidCookie[1];
                }
            }
            
            if(TextUtils.isEmpty(sid) || TextUtils.isEmpty(uid))
            {
                String publicKey = getCaptchaPublicKey();
                String challenge = getChallenge(publicKey);

                new GetCaptchaTask(String.format("https://www.google.com/recaptcha/api/image?c=%s", challenge)).execute(null);

                final Document document = Jsoup.parse(loginInfo.first);
                final Elements errors = document.getElementsByClass("error");
                if(errors.size() > 1) // first error about java script
                    throw new Exception(errors.first().text());
                
                throw new Exception(Utils.getString(R.string.Login_Failed));
            }
            
            SettingsWorker.Instance().saveCookies(new Pair<String, String>(sid, uid));
        }
        catch (Throwable t)
        {
            setException(t);
        }
        finally
        {
            final List<LoginListener> listeners = ListenersWorker.Instance().getListeners(LoginListener.class);
            final Object args[] = new Object[1];
                
            args[0] = sid!= null && uid != null;
            
            for(LoginListener listener : listeners)
            {
                publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (method, args)));
            }
        }
        
        return e;
    }

    private String getChallenge(String publicKey) throws Exception
    {
        String response = ServerWorker.Instance().getContent(String.format("https://www.google.com/recaptcha/api/challenge?k=%s&ajax=1", publicKey));
        int first = response.indexOf("'") + 1;
        int second = response.indexOf("'", first);

        return response.substring(first, second);
    }
}
