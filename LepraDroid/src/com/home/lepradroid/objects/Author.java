package com.home.lepradroid.objects;

import java.io.UnsupportedEncodingException;

public class Author extends RateItem
{
    private byte[]   id                  = null;
    private byte[]   name                = null;
    private byte[]   imageUrl            = null;
    private byte[]   ego                 = null;
    private byte[]   userName            = null;
    
    public String getId()
    {
        try
        {
            if(id == null) return "";
            return new String(id, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
        }
        
        return "";
    }
    
    public void setId(String id)
    {
        this.id = id.getBytes();
    }
    
    public String getName()
    {
        try
        {
            if(name == null) return "";
            return new String(name, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
        }
        
        return "";
    }
    
    public void setName(String name)
    {
        this.name = name.getBytes();
    }
    
    public String getImageUrl()
    {
        try
        {
            if(imageUrl == null) return "";
            return new String(imageUrl, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
        }
        
        return "";
    }
    
    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl.getBytes();
    }
    
    public String getEgo()
    {
        try
        {
            if(ego == null) return "";
            return new String(ego, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
        }
        
        return "";
    }
    
    public void setEgo(String ego)
    {
        this.ego = ego.getBytes();
    }
    
    public String getUserName()
    {
        try
        {
            if(userName == null) return "";
            return new String(userName, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
        }
        
        return "";
    }
    
    public void setUserName(String userName)
    {
        this.userName = userName.getBytes();
    }
}
