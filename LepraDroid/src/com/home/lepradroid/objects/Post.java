package com.home.lepradroid.objects;

import java.io.UnsupportedEncodingException;

public class Post extends BaseItem
{
    private short    totalComments       = -1;
    private short    newComments         = -1;
    private byte[]   pid                 = null;
    private boolean  voteDisabled        = false;
    
    public short getTotalComments()
    {
        return totalComments;
    }
    
    public void setTotalComments(short totalComments)
    {
        this.totalComments = totalComments;
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
    
    public short getNewComments()
    {
        return newComments;
    }
    
    public void setNewComments(short newComments)
    {
        this.newComments = newComments;
    }
    
    public boolean isVoteDisabled()
    {
        return voteDisabled;
    }
    
    public void setVoteDisabled(boolean voteDisabled)
    {
        this.voteDisabled = voteDisabled;
    }
}
