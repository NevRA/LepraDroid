package com.home.lepradroid.objects;

import java.util.UUID;

public class BaseItem
{
    public UUID     Id                  = UUID.randomUUID();
    public Integer  Rating              = 0;
    public String   Url                 = "";
    public String   Text                = "";
    public String   ImageUrl            = "";
    public String   Author              = "";
    public String   Signature           = "";
    public Boolean  PlusVoted           = false;
    public Boolean  MinusVoted          = false;
}
