package com.home.lepradroid.objects;

import java.io.UnsupportedEncodingException;

public class Comment extends BaseItem
{
    private byte[]   parentPid           = null;
    private byte[]   pid                 = null;
    private short    level               = 0;
    private short    num                 = 0;
    private boolean  isNew               = false;
    private boolean  isPostAuthor        = false;
    private boolean  isOnlyText          = false;
    
    public String getParentPid()
    {
        try
        {
            if(parentPid == null) return "";
            return new String(parentPid, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
        }
        
        return "";
    }
    
    public void setParentPid(String parentPid)
    {
        this.parentPid = parentPid.getBytes();
    }
    
    public String getPid()
    {
        try
        {
            if(pid == null) return "";
            return new String(pid, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
        }
        
        return "";
    }
    
    public void setPid(String pid)
    {
        this.pid = pid.getBytes();
    }

    public short getLevel()
    {
        return level;
    }

    public void setLevel(short level)
    {
        this.level = level;
    }

    public short getNum()
    {
        return num;
    }

    public void setNum(short num)
    {
        this.num = num;
    }

    public boolean isNew()
    {
        return isNew;
    }

    public void setNew(boolean isNew)
    {
        this.isNew = isNew;
    }

    public boolean isPostAuthor()
    {
        return isPostAuthor;
    }

    public void setPostAuthor(boolean isPostAuthor)
    {
        this.isPostAuthor = isPostAuthor;
    }

    public boolean isOnlyText()
    {
        return isOnlyText;
    }

    public void setOnlyText(boolean isOnlyText)
    {
        this.isOnlyText = isOnlyText;
    }
}
