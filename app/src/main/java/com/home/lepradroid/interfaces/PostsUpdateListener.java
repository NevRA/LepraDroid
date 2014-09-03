package com.home.lepradroid.interfaces;

import java.util.UUID;

public interface PostsUpdateListener extends UpdateListener
{
    void OnPostsUpdateBegin(UUID groupId, int page);
    void OnPostsUpdate(UUID groupId, int page);
    void OnPostsUpdateFinished(UUID groupId, int page, boolean successful);
}
