package com.home.lepradroid;

import android.app.Application;
import android.content.Context;

import roboguice.RoboGuice;

public class LepraDroidApplication extends Application {
    private static Context context;

    public static Context getInstance() {
        return context;
    }

    public void onCreate() {
        context = this;

        RoboGuice.setBaseApplicationInjector(this, RoboGuice.DEFAULT_STAGE,
                RoboGuice.newDefaultRoboModule(this), new Configurator());
    }
}
