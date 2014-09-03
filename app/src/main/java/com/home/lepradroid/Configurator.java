package com.home.lepradroid;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.home.lepradroid.interfaces.ISettingsManager;
import com.home.lepradroid.settings.SettingsManager;

public class Configurator extends AbstractModule {
    @Override
    protected void configure() {
        bind(ISettingsManager.class).to(SettingsManager.class).in(Scopes.SINGLETON);
    }
}