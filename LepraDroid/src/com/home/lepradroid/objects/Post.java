package com.home.lepradroid.objects;

import java.util.UUID;

import com.home.lepradroid.commons.Commons.PostSourceType;

import android.graphics.drawable.Drawable;

public class Post
{
    public Post(PostSourceType type)
    {
        Type = type;
    }
    public PostSourceType Type;
    public UUID giud = UUID.randomUUID();
    public String Author = "";
    public String Signature = "";
    public String Comments = "";
    public Integer Rating;
    public String Text = "";
    public String Html = "";
    public String ImageUrl = "";
    public Drawable dw;
}
