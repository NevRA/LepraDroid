package com.home.lepradroid;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.interfaces.CaptchaUpdateListener;
import com.home.lepradroid.tasks.GetCaptchaTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;


public class LogonScreen extends BaseActivity implements CaptchaUpdateListener
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.logon_view);
        
        new TaskWrapper(this, new GetCaptchaTask(), Utils.getString(R.string.Captcha_Loading_In_Progress));
    }

    public void OnCaptchaUpdateListener(Drawable dw)
    {
        if(dw == null) return;
        
        try
        {
            ImageView captcha = (ImageView) findViewById(R.id.imageView3);
            
            captcha.setImageDrawable(dw);
        }
        catch (Throwable t)
        {
            Utils.showError(this, t);
        }
    }
}
