package com.home.lepradroid.tasks;

public interface ProgressTracker
{
    void onProgress(String message);
    void onComplete(Throwable e);
}
