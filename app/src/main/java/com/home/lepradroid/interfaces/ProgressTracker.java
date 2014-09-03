package com.home.lepradroid.interfaces;

public interface ProgressTracker
{
    void onProgress(String message);
    void onComplete(Throwable e);
}
