package com.home.lepradroid.interfaces;

import java.util.UUID;

public interface CommentsUpdateListener extends UpdateListener
{
    void OnCommentsUpdateBegin(UUID id); 
    void OnCommentsUpdateFirstEntries(UUID id); 
    void OnCommentsUpdateFinished(UUID id); 
}
