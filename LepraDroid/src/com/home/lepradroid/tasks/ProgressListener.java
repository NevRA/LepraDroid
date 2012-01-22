package com.home.lepradroid.tasks;

public interface ProgressListener
{
    void onBeginProgress(String message);
    void onEndProgress(String message);
}
