package com.home.lepradroid.interfaces;

import java.util.UUID;

public interface CommentsUpdateListener extends UpdateListener
{
    void OnCommentsUpdateBegin(UUID groupId, UUID postId); 
    void OnCommentsUpdateFirstEntries(UUID groupId, UUID postId, int count, int totalCount);
    void OnCommentsUpdate(UUID groupId, UUID postId, int count);
    void OnCommentsUpdateFinished(UUID groupId, UUID postId); 
}
