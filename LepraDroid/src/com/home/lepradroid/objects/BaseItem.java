package com.home.lepradroid.objects;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class BaseItem extends RateItem
{
    private UUID     id                  = UUID.randomUUID();
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
    
    public void setHtml(String html)
    {
        this.html = html.getBytes();
    }
    
    public String getHtml()
    {
        try
        {
            if(html == null) return "";
            return new String(html, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
        }
        
        return "";
    }
    
    public String getUrl()
    {
        try
        {
            if(url == null) return "";
            return new String(url, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
        }
        
        return "";
    }
    
    public void setUrl(String url)
    {
        this.url = url.getBytes();
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
    
    public String getAuthor()
    {
        try
        {
            if(author == null) return "";
            return new String(author, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
        }
        
        return "";
    }
    
    public void setAuthor(String author)
    {
        this.author = author.getBytes();
    }
    
    public String getSignature()
    {
        try
        {
            if(signature == null) return "";
            return new String(signature, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
        }
        
        return "";
    }
    
    public void setSignature(String signaturehor)
    {
        this.signature = signaturehor.getBytes();
    }
}
