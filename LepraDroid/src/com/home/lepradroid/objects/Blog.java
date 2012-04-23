package com.home.lepradroid.objects;

import java.io.UnsupportedEncodingException;

public class Blog extends BaseItem
{
    private byte[] stat = null;

    public String getStat()
    {
        try
        {
            if(stat == null) return "";
            return new String(stat, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
        }
        
        return "";
    }
    
    public void setStat(String stat)
    {
        this.stat = stat.getBytes();
    }
}
