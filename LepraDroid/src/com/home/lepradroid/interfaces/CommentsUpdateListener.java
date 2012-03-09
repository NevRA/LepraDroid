package com.home.lepradroid.interfaces;

import java.util.UUID;

public interface CommentsUpdateListener extends UpdateListener
{
    void OnCommentsUpdateBegin(UUID groupId, UUID postId); 
    void OnCommentsUpdateFirstEntries(UUID groupId, UUID postId); 
    void OnCommentsUpdate(UUID groupId, UUID postId); 
    void OnCommentsUpdateFinished(UUID groupId, UUID postId); 
}
