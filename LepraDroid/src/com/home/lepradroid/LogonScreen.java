package com.home.lepradroid;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.serverworker.ServerWorker;

import android.os.Bundle;
import android.widget.ImageView;


public class LogonScreen extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.logon_view);
        
        try
        {
            String res = ServerWorker.Instance().getContent(Commons.LOGON_PAGE_URL);
            int pos = res.indexOf("/captchaa/");
            
            String captchaUrl = Commons.CAPTCHA_URL + res.substring(pos + 10, pos + 10 + 16);
            
            ImageView captcha = (ImageView) findViewById(R.id.imageView3);
            
            captcha.setImageDrawable(ServerWorker.Instance().getCaptcha(captchaUrl));
        }
        catch (Throwable t)
        {
            // TODO Auto-generated catch block
            t.printStackTrace();
        }
    }

}
