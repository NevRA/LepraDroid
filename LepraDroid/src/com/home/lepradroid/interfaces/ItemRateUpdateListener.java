package com.home.lepradroid.interfaces;

import java.util.UUID;

public interface ItemRateUpdateListener extends UpdateListener
{
    void OnItemRateUpdate(UUID groupId, UUID postId, int newRating, boolean successful);
    void OnItemRateUpdate(String userId, boolean successful);
}
