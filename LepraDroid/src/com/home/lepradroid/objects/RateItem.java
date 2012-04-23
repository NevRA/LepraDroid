package com.home.lepradroid.objects;

public class RateItem
{
    private short    rating              = 0;
    private boolean  plusVoted           = false;
    private boolean  minusVoted          = false;
    
    public short getRating()
    {
        return rating;
    }
    
    public void setRating(short rating)
    {
        this.rating = rating;
    }
    
    public boolean isPlusVoted()
    {
        return plusVoted;
    }
    
    public void setPlusVoted(boolean plusVoted)
    {
        this.plusVoted = plusVoted;
    }
    
    public boolean isMinusVoted()
    {
        return minusVoted;
    }
    
    public void setMinusVoted(boolean minusVoted)
    {
        this.minusVoted = minusVoted;
    }
}
