package com.home.lepradroid.objects;

import java.util.UUID;

import android.graphics.drawable.Drawable;

public class Post
{
    public UUID giud = UUID.randomUUID();
    public String Author = "";
    public String Signature = "";
    public String Time = "";
    public String Text = "";
    public String Html = "";
    public String ImageUrl = "";
    public Drawable dw;
    public int CommentsTotal;
    public int CommentsUnread;
}
