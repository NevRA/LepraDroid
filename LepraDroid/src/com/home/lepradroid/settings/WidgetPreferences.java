package com.home.lepradroid.settings;

import com.home.lepradroid.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class WidgetPreferences extends PreferenceActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.widget_preferences_view);
    }
}
