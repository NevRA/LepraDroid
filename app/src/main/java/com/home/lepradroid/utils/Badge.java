package com.home.lepradroid.utils;

import android.util.Pair;

public class Badge
{
    public Pair<Integer, Integer> things;
    public Pair<Integer, Integer> inbox;
    
    public Badge()
    {
        things = new Pair<Integer, Integer>(0, 0);
        inbox = new Pair<Integer, Integer>(0, 0);
    }
    
    public Badge(Pair<Integer, Integer> things, Pair<Integer, Integer> inbox)
    {
        this.things = things;
        this.inbox = inbox;
    }
    
    public boolean isNoNewItems()
    {
        return things.first == 0 && things.second == 0 && inbox.first == 0 && inbox.second == 0;
    }
    
    public String ToString()
    {
        String first = GetFormattedItem(things);
        String second = GetFormattedItem(inbox);
        return first + "|" + second;
    }
    
    private String GetFormattedItem(Pair<Integer, Integer> item)
    {
        String first = item.first.toString();
        String second = item.second.toString();
        if(item.first != 0 && item.second != 0)
            return first + "/" + second;
        if(item.first != 0)
            return first;
        else
            return second;
    }
}
