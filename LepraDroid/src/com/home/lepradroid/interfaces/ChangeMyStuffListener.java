package com.home.lepradroid.interfaces;

import java.util.UUID;

import com.home.lepradroid.commons.Commons.StuffOperationType;

public interface ChangeMyStuffListener extends UpdateListener
{
    void OnChangeMyStuff(UUID groupId, UUID postId, StuffOperationType type, boolean successful);
}
