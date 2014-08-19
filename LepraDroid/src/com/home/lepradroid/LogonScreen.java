package com.home.lepradroid;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.LoginListener;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.LoginTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;


public class LogonScreen extends BaseActivity implements TextWatcher, LoginListener {
    private Button yarrr;
    private EditText login;
    private EditText password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.logon_view);

        yarrr = (Button) findViewById(R.id.yarrr);
        login = (EditText) findViewById(R.id.login);
        password = (EditText) findViewById(R.id.password);

        init();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(Commons.EXIT_FROM_LOGON_SCREEN_RESULTCODE);

        super.onBackPressed();
    }

    private void updateControls() {
        if (TextUtils.isEmpty(login.getText()) ||
                TextUtils.isEmpty(password.getText()))
            yarrr.setEnabled(false);
        else
            yarrr.setEnabled(true);
    }

    private void init() {
        login.addTextChangedListener(this);
        password.addTextChangedListener(this);

        yarrr.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SettingsWorker.Instance().saveUserName(login.getText().toString());
                pushNewTask(new TaskWrapper(LogonScreen.this, new LoginTask(login.getText().toString(), password.getText().toString()), true, Utils.getString(R.string.Login_In_Progress)));
            }
        });

    }

    public void afterTextChanged(Editable s) {
        updateControls();
    }

    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before,
                              int count) {
    }

    public void OnLogin(boolean successful) {
        if (successful) {
            setResult(Commons.EXIT_FROM_LOGON_SCREEN_AFTER_LOGON_RESULTCODE);
            finish();
        }
    }
}
