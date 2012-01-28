package com.home.lepradroid.interfaces;

import java.util.UUID;

public interface PostsUpdateListener extends UpdateListener
{
    void OnPostsUpdateBegin(UUID id); 
    void OnPostsUpdate(UUID id, boolean haveNewRecords); 
}
