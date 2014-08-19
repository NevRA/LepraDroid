package com.home.lepradroid.tasks;

import android.graphics.drawable.Drawable;
import android.util.Pair;
import com.home.lepradroid.R;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.CaptchaUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Logger;
import com.home.lepradroid.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Method;
import java.util.List;

public class GetCaptchaTask extends BaseTask
{   
    static final Class<?>[] argsClasses = new Class[1];
    static Method method;
    static 
    {
        try
        {
            argsClasses[0] = Drawable.class;
            method = CaptchaUpdateListener.class.getMethod("OnCaptchaUpdateListener", argsClasses);    
        }
        catch (Throwable t) 
        {           
            Logger.e(t);
        }        
    }
    
    public GetCaptchaTask() 
    {
    }
    
    private Drawable captcha;
        
    @SuppressWarnings("unchecked")
    @Override
    protected Throwable doInBackground(Void... params) 
    {
        try 
        {  
            Logger.d("Started GetCaptchaTask");
  
            final Document document = Jsoup.parse(ServerWorker.Instance().getContent(Commons.LOGON_PAGE_URL), "UTF-8");
            final Element root = document.body(); 
            final Elements elements = root.getElementsByAttributeValue("name", "logincode");
            if(elements.isEmpty())
                throw new Exception(Utils.getString(R.string.Captcha_Not_Found));
            final String loginCode = elements.first().attr("value");
            //ServerWorker.Instance().setLoginCode(loginCode);
              
            captcha = Utils.getImageFromByteArray(ServerWorker.Instance().getImage(Commons.CAPTCHA_URL + loginCode));
        }
        catch (Throwable e) 
        {           
            setException(e);
        }
        finally
        {
            final List<CaptchaUpdateListener> listeners = ListenersWorker.Instance().getListeners(CaptchaUpdateListener.class);
            final Object args[] = new Object[1];
                
            args[0] = captcha;
            
            for(CaptchaUpdateListener listener : listeners)
            {
                publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (method, args)));
            }
        }
        
        Logger.d("Finished GetCaptchaTask");
        
        return e;
    }
}