package com.home.lepradroid.interfaces;

import com.home.lepradroid.objects.Author;

public interface AuthorUpdateListener extends UpdateListener
{
    void OnAuthorUpdateBegin(String userName);
    void OnAuthorUpdate(String userName, Author data);
}
