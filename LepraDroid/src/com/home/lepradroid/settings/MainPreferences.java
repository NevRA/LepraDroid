package com.home.lepradroid.settings;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import com.home.lepradroid.AuthorScreen;
import com.home.lepradroid.R;
import com.home.lepradroid.utils.Utils;

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
        
        final Preference history = (Preference) getPreferenceManager().findPreference(Utils.getString(R.string.MainSettings_HistoryId));
        history.setOnPreferenceClickListener(new OnPreferenceClickListener() 
        {
            public boolean onPreferenceClick(Preference preference) 
            {
                Utils.showChangesHistory(MainPreferences.this);
                
                return true;
            }
        });
        
        final Preference version = (Preference) getPreferenceManager().findPreference(Utils.getString(R.string.MainSettings_VersionId));
        try
        {
            version.setSummary(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        }
        catch (NameNotFoundException e)
        {
        }
    }
}
