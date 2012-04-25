package com.home.lepradroid.interfaces;

import java.util.UUID;

import com.home.lepradroid.commons.Commons.StuffOperationType;

public interface ChangeFavListener extends UpdateListener
{
    void OnChangeFav(UUID groupId, UUID postId, StuffOperationType type, boolean successful);
}
