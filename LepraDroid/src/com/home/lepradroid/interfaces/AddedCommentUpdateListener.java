package com.home.lepradroid.interfaces;

import java.util.UUID;

import com.home.lepradroid.objects.Comment;

public interface AddedCommentUpdateListener extends UpdateListener
{
    void OnAddedCommentUpdate(UUID id, Comment newComment); 
}
