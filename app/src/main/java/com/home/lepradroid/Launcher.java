package com.home.lepradroid;

import android.content.Intent;
import android.os.Bundle;

import com.google.inject.Inject;
import com.home.lepradroid.interfaces.ISettingsManager;
import com.home.lepradroid.ui.LogonFragmentActivity;

import roboguice.activity.RoboActivity;

public class Launcher extends RoboActivity {
    @Inject
    ISettingsManager settingsManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(Launcher.this,
                settingsManager.isAuthorized() ?
                        Main.class :
                        LogonFragmentActivity.class);
        startActivity(intent);
    }
}
