package com.home.lepradroid.settings;

import com.home.lepradroid.AuthorScreen;
import com.home.lepradroid.R;
import com.home.lepradroid.utils.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class MainPreferences extends PreferenceActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.main_preferences_view);
        
        final Preference author = (Preference) getPreferenceManager().findPreference(Utils.getString(R.string.MainSettings_AuthorId));
        author.setOnPreferenceClickListener(new OnPreferenceClickListener() 
        {
            public boolean onPreferenceClick(Preference preference) 
            {
                final Intent intent = new Intent(MainPreferences.this, AuthorScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("username", Utils.getString(R.string.Author_Name));
                startActivity(intent);
                return true;
            }
        });
    }
}
