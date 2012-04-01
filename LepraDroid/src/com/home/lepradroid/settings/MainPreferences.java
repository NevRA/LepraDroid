package com.home.lepradroid.settings;

import com.home.lepradroid.AuthorScreen;
import com.home.lepradroid.R;
import com.home.lepradroid.utils.Utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

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
                LayoutInflater inflater = LayoutInflater.from(MainPreferences.this);

                View alertDialogView = inflater.inflate(R.layout.history_view, null);

                WebView webView = (WebView) alertDialogView.findViewById(R.id.DialogWebView);
                webView.getSettings().setDefaultFontSize(10);
                webView.loadUrl("file:///android_asset/history.html");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainPreferences.this);
                builder.setView(alertDialogView);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() 
                {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                
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
