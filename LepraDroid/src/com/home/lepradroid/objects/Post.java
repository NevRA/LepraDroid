package com.home.lepradroid.objects;

import com.home.lepradroid.commons.Commons;

import java.util.UUID;

public class Post extends BaseItem
{
    private short    totalComments       = -1;
    private short    newComments         = -1;
    private int      voteWeight          = -1;
    private byte[]   lepraId             = null;
    private boolean  golden              = false;
    private boolean  silver              = false;
    private boolean  voteDisabled        = false;
    private boolean  allNew              = false;

    public Post(UUID groupId)
    {
        setGroupId(groupId);
    }
    
    public short getTotalComments()
    {
        return totalComments;
    }
    
    public void setTotalComments(short totalComments)
    {
        this.totalComments = totalComments;
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

    public boolean isAllNew()
    {
        return allNew;
    }

    public void setAllNew(boolean allNew)
    {
        this.allNew = allNew;
    }

    public boolean isGolden()
    {
        return golden;
    }

    public void setGolden(boolean golden)
    {
        this.golden = golden;
    }

    public boolean isSilver()
    {
        return silver;
    }

    public void setSilver(boolean silver)
    {
        this.silver = silver;
    }

    public int getVoteWeight()
    {
        return voteWeight;
    }

    public void setVoteWeight(int voteWeight)
    {
        this.voteWeight = voteWeight;
    }

    public boolean isInbox()
    {
        return getGroupId().equals(Commons.INBOX_POSTS_ID);
    }

    public boolean isMyStuff()
    {
        return getGroupId().equals(Commons.MYSTUFF_POSTS_ID);
    }

    public boolean isFavorite()
    {
        return getGroupId().equals(Commons.FAVORITE_POSTS_ID);
    }

    public boolean isMain()
    {
        return getGroupId().equals(Commons.MAIN_POSTS_ID);
    }
}