package com.home.lepradroid.ui;


import android.os.Bundle;
import android.widget.Button;

import com.home.lepradroid.R;

import roboguice.inject.InjectView;

public class LogonFragmentActivity extends BaseFragmentActivity{
    @InjectView(R.id.yarrr)
    Button signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.logon_view);
    }
}
