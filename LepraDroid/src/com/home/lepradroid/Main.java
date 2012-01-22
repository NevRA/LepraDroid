package com.home.lepradroid;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.interfaces.LoginListener;

import android.content.Intent;
import android.os.Bundle;

public class Main extends BaseActivity implements LoginListener
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Intent intent = new Intent(this, LogonScreen.class);
        startActivity(intent); 
    }

    public void OnLogin(boolean successful)
    {
        if(successful)
        {
            
        }
    }
}