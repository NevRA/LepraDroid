package com.home.lepradroid.interfaces;

public interface BlogsUpdateListener extends UpdateListener
{
    void OnBlogsUpdateBegin(int page); 
    void OnBlogsUpdate(int page); 
    void OnBlogsUpdateFinished(int page, boolean successful); 
}
