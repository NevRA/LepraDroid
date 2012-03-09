package com.home.lepradroid.objects;

import java.util.UUID;

public class BaseItem extends RateItem
{
    public UUID     Id                  = UUID.randomUUID();
    public String   Url                 = "";
    public String   Text                = "";
    public String   ImageUrl            = "";
    public String   Author              = "";
    public String   Signature           = "";
}
