package com.home.lepradroid.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.TextUtils;

import com.home.lepradroid.AuthorScreen;
import com.home.lepradroid.R;
import com.home.lepradroid.tasks.PostMainThresholdTask;
import com.home.lepradroid.utils.Utils;

public class MainPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
    @Override
    protected void onDestroy()
    {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
    
    private void updatePreferences()
    {
        /*final ListPreference mainFilter = (ListPreference) getPreferenceManager().findPreference(Utils.getString(R.string.MainSettings_MainFilterId));
        String threshold = SettingsWorker.Instance().loadMainThreshold();
        if(!TextUtils.isEmpty(threshold))
            mainFilter.setValue(threshold);
        mainFilter.setSummary(mainFilter.getEntry());*/
        
        final Preference author = getPreferenceManager().findPreference(Utils.getString(R.string.MainSettings_AuthorId));
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

        final Preference market = getPreferenceManager().findPreference(Utils.getString(R.string.MainSettings_GooglePlayId));
        market.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference preference)
            {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName()));
                startActivity(browserIntent);
                return true;
            }
        });
        
        final Preference history = getPreferenceManager().findPreference(Utils.getString(R.string.MainSettings_HistoryId));
        history.setOnPreferenceClickListener(new OnPreferenceClickListener() 
        {
            public boolean onPreferenceClick(Preference preference) 
            {
                Utils.showChangesHistory(MainPreferences.this);
                
                return true;
            }
        });
        
        final Preference version = getPreferenceManager().findPreference(Utils.getString(R.string.MainSettings_VersionId));
        try
        {
            version.setSummary(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        }
        catch (NameNotFoundException ignored)
        {
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.main_preferences_view);
        
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
        updatePreferences();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key)
    {
        if(key.equals(Utils.getString(R.string.MainSettings_MainFilterId)))
        {
            String value = sharedPreferences.getString(Utils.getString(R.string.MainSettings_MainFilterId), "1");
            if(!value.equals(SettingsWorker.Instance().loadMainThreshold()))
            {
                SettingsWorker.Instance().saveMainThreshold(value);
                updatePreferences();
                new PostMainThresholdTask().execute();
            }
        }
        else if(key.equals(Utils.getString(R.string.MainSettings_FontSizeId)))
        {
            String value = sharedPreferences.getString(Utils.getString(R.string.MainSettings_FontSizeId), "normal");
            Utils.setIsNormalFontSize(value.equals("normal"));
        }
    }
}
