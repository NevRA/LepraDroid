package com.home.lepradroid.objects;

import com.home.lepradroid.commons.Commons;


public class Comment extends BaseItem
{
    private byte[]   parentLepraId       = null;
    private byte[]   lepraId             = null;
    private short    level               = 0;
    private short    num                 = 0;
    private boolean  isNew               = false;
    private boolean  isPostAuthor        = false;
    private boolean  isOnlyText          = false;
    
    public String getParentLepraId()
    {
        if(parentLepraId == null) return "";
        return new String(parentLepraId);
    }
    
    public void setParentLepraId(String parentLepraId)
    {
        this.parentLepraId = parentLepraId.getBytes();
    }
    
    public String getLepraId()
    {
        if(lepraId == null) return "";
        return new String(lepraId);
    }
    
    public void setLepraId(String lepraId)
    {
        this.lepraId = lepraId.getBytes();
    }

    public short getLevel()
    {
        return level;
    }

    public void setLevel(short level)
    {
        this.level = (short) Math.min(Commons.MAX_COMMENT_LEVEL, level);
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
