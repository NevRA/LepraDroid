package com.home.lepradroid;

import com.home.commons.Commons;
import com.home.serverworker.ServerWorker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;


public class LogonScreen extends Activity
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
