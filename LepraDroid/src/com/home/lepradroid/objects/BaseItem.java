package com.home.lepradroid.objects;

import java.util.UUID;

import android.graphics.drawable.Drawable;

public class BaseItem
{
    public UUID id = UUID.randomUUID();
    public String Text = "";
    public String ImageUrl = "";
    public Drawable dw;
    public String Author = "";
}
