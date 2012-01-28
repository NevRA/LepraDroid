package com.home.lepradroid.interfaces;

import com.home.lepradroid.commons.Commons.PostSourceType;

public interface PostsUpdateListener extends UpdateListener
{
    void OnPostsUpdateBegin(); 
    void OnPostsUpdate(PostSourceType type, boolean haveNewRecords); 
}
