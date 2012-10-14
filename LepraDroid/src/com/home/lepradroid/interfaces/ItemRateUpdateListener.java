package com.home.lepradroid.interfaces;

import java.util.UUID;

public interface ItemRateUpdateListener extends UpdateListener
{
    void OnPostRateUpdate(UUID postId, int newRating, boolean successful);
    void OnCommentRateUpdate(UUID postId, UUID commentId, boolean successful);
    void OnAuthorRateUpdate(String userId, boolean successful);
}
