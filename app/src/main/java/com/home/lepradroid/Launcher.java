package com.home.lepradroid;

import android.content.Intent;
import android.os.Bundle;

import com.google.inject.Inject;
import com.home.lepradroid.interfaces.ISettingsManager;

import roboguice.activity.RoboActivity;

public class Launcher extends RoboActivity {
    @Inject
    ISettingsManager mSettingsManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(Launcher.this,
                mSettingsManager.isAuthorized() ?
                        Main.class :
                        LogonScreen.class);
        startActivity(intent);
    }
}
