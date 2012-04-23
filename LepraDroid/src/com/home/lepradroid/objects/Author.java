package com.home.lepradroid.objects;

public class Author extends RateItem
{
    private byte[]   id                  = null;
    private byte[]   name                = null;
    private byte[]   imageUrl            = null;
    private byte[]   ego                 = null;
    private byte[]   userName            = null;
    
    public String getId()
    {
        if(id == null) return "";
        return new String(id);
    }
    
    public void setId(String id)
    {
        this.id = id.getBytes();
    }
    
    public String getName()
    {
        if(name == null) return "";
        return new String(name);
    }
    
    public void setName(String name)
    {
        this.name = name.getBytes();
    }
    
    public String getImageUrl()
    {
        if(imageUrl == null) return "";
        return new String(imageUrl);
    }
    
    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl.getBytes();
    }
    
    public String getEgo()
    {
        if(ego == null) return "";
        return new String(ego);
    }
    
    public void setEgo(String ego)
    {
        this.ego = ego.getBytes();
    }
    
    public String getUserName()
    {
        if(userName == null) return "";
        return new String(userName);
    }
    
    public void setUserName(String userName)
    {
        this.userName = userName.getBytes();
    }
}
