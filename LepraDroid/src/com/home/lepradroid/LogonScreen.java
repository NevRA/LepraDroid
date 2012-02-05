package com.home.lepradroid;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.CaptchaUpdateListener;
import com.home.lepradroid.interfaces.LoginListener;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.GetCaptchaTask;
import com.home.lepradroid.tasks.LoginTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;


public class LogonScreen extends BaseActivity implements CaptchaUpdateListener, TextWatcher, LoginListener
{
    private Button          yarrr;
    private EditText        captcha;
    private EditText        login;
    private EditText        password;
    private ProgressBar     progress;
    private ImageView       captchaImage;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.logon_view);
        
        yarrr = (Button)findViewById(R.id.yarrr);
        captchaImage = (ImageView) findViewById(R.id.captcha_image);
        captcha = (EditText)findViewById(R.id.captcha);
        login = (EditText)findViewById(R.id.login);
        password = (EditText)findViewById(R.id.password);
        progress = (ProgressBar)findViewById(R.id.progress);
        
        init();
    }
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return true;
    }
    
    @Override
    public void onBackPressed() 
    {
    	setResult(Commons.EXIT_FROM_LOGON_SCREEN_RESULTCODE);
    	
    	super.onBackPressed();
    }
    
    private void updateControls()
    {
        if(     captcha.getText().length() != 6 || 
                TextUtils.isEmpty(login.getText()) || 
                TextUtils.isEmpty(password.getText()))
            yarrr.setEnabled(false);
        else
            yarrr.setEnabled(true);
    }

    private void init()
    {
        captcha.addTextChangedListener(this);
        login.addTextChangedListener(this);
        password.addTextChangedListener(this);
        
        captchaImage.setOnClickListener( new View.OnClickListener()
        {
            public void onClick(View v)
            {
                updateCaptcha();              
            }
        });
        
        yarrr.setOnClickListener( new View.OnClickListener()
        {
            public void onClick(View v)
            {
                SettingsWorker.Instance().saveUserName(login.getText().toString());
                pushNewTask(new TaskWrapper(LogonScreen.this, new LoginTask(login.getText().toString(), password.getText().toString(), captcha.getText().toString()), true, Utils.getString(R.string.Login_In_Progress)));              
            }
        });
        
        updateCaptcha();
    }
    
    private void updateCaptcha()
    {
        pushNewTask(new TaskWrapper(this, new GetCaptchaTask(), Utils.getString(R.string.Captcha_Loading_In_Progress)));
        captchaImage.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.VISIBLE);
    }
    
    public void afterTextChanged(Editable s)
    {
        updateControls();
    }

    public void beforeTextChanged(CharSequence s, int start, int count,
            int after)
    {
    }

    public void onTextChanged(CharSequence s, int start, int before,
            int count)
    {
    }

    public void OnCaptchaUpdateListener(Drawable dw)
    {
        captchaImage.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        
        if(dw == null) return;
        
        try
        {
            captchaImage.setImageDrawable(dw);
        }
        catch (Throwable t)
        {
            Utils.showError(this, t);
        }
    }

    public void OnLogin(boolean successful)
    {
       if(!successful)
           updateCaptcha();
       else
       {
    	   setResult(Commons.EXIT_FROM_LOGON_SCREEN_AFTER_LOGON_RESULTCODE);
           finish();
       }
    }
}
