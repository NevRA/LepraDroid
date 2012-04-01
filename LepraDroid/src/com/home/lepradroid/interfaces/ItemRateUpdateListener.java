package com.home.lepradroid.interfaces;

import java.util.UUID;

public interface ItemRateUpdateListener extends UpdateListener
{
    void OnPostRateUpdate(UUID groupId, UUID postId, int newRating, boolean successful);
    void OnCommentRateUpdate(UUID groupId, UUID postId, boolean successful);
    void OnAuthorRateUpdate(String userId, boolean successful);
}
