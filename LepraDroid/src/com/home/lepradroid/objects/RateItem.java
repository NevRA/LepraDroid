package com.home.lepradroid.objects;

public class RateItem
{
    private int      rating              = 0;
    private boolean  plusVoted           = false;
    private boolean  minusVoted          = false;
    
    public int getRating()
    {
        return rating;
    }
    
    public void setRating(int rating)
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
