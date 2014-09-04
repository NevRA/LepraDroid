package com.home.lepradroid.ui;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import com.home.lepradroid.interfaces.ISettingsManager;

public class BaseFragmentActivity extends RoboSherlockFragmentActivity {
    @Inject
    protected ISettingsManager settingsManager;
}