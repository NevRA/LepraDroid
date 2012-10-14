package com.home.lepradroid.interfaces;

import java.util.UUID;

public interface CommentsUpdateListener extends UpdateListener
{
    void OnCommentsUpdateBegin(UUID postId);
    void OnCommentsUpdateFirstEntries(UUID postId, int count, int totalCount, int commentToSelect);
    void OnCommentsUpdate(UUID postId, int count);
    void OnCommentsUpdateFinished(UUID postId, int commentToSelect);
}
