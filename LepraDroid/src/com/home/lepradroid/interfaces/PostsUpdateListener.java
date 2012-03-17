package com.home.lepradroid.interfaces;

import java.util.UUID;

public interface PostsUpdateListener extends UpdateListener
{
    void OnPostsUpdateBegin(UUID id, int page); 
    void OnPostsUpdate(UUID id, int page); 
    void OnPostsUpdateFinished(UUID id, int page, boolean successful); 
}
