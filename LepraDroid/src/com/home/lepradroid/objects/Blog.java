package com.home.lepradroid.objects;

public class Blog extends BaseItem
{
    private byte[] stat = null;

    public String getStat()
    {
        if(stat == null) return "";
        return new String(stat);
    }
    
    public void setStat(String stat)
    {
        this.stat = stat.getBytes();
    }
}
