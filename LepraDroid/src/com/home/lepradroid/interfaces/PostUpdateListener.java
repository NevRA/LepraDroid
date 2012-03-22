package com.home.lepradroid.interfaces;

import java.util.UUID;

public interface PostUpdateListener extends UpdateListener
{
    void OnPostUpdateFinished(UUID postId, boolean successful); 
}
