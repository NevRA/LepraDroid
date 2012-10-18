package com.home.lepradroid;

import com.home.lepradroid.settings.SettingsWorker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Launcher extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        if(!SettingsWorker.Instance().loadIsAppRunning())
        {
            Intent intent = new Intent(Launcher.this, Main.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }   
        
        finish();
    }
}
