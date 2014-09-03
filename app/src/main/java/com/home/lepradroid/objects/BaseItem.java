package com.home.lepradroid.objects;

import com.home.lepradroid.utils.Utils;

import java.util.UUID;

public class BaseItem extends RateItem
{
    private UUID     id                  = UUID.randomUUID();
    private UUID     groupId             = null;
    private byte[]   html                = null;
    private byte[]   url                 = null;
    private byte[]   imageUrl            = null;
    private byte[]   author              = null;
    private byte[]   signature           = null;
    
    public UUID getId()
    {
        return id;
    }
    
    public void setId(UUID id)
    {
        this.id = id;
    }

    public UUID getGroupId()
    {
        return groupId;
    }

    public void setGroupId(UUID groupId)
    {
        this.groupId = groupId;
    }
    
    public void setHtml(String html)
    {
        this.html = html.getBytes();
    }
    
    public String getHtml()
    {
        if(html == null) return "";
        return new String(html);
    }

    public String getText()
    {
        return Utils.html2text(getHtml());
    }
    
    public String getUrl()
    {
        if(url == null) return "";
        return new String(url);
    }
    
    public void setUrl(String url)
    {
        this.url = url.getBytes();
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
    
    public String getAuthor()
    {
        if(author == null) return "";
        return new String(author);
    }
    
    public void setAuthor(String author)
    {
        this.author = author.getBytes();
    }
    
    public String getSignature()
    {
        if(signature == null) return "";
        return new String(signature);
    }
    
    public void setSignature(String signaturehor)
    {
        this.signature = signaturehor.getBytes();
    }
}
