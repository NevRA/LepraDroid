package com.home.lepradroid.interfaces;

public interface BlogsUpdateListener extends UpdateListener
{
    void OnBlogsUpdateBegin(); 
    void OnBlogsUpdate(boolean haveNewRecords); 
}
