package com.home.lepradroid.objects;

import java.util.UUID;

import android.graphics.drawable.Drawable;

public class BaseItem
{
    public UUID Id = UUID.randomUUID();
    public String Url = "";
    public String Text = "";
    public String ImageUrl = "";
    public Drawable dw;
    public String Author = "";
}
