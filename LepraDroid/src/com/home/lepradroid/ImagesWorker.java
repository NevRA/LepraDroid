package com.home.lepradroid;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.utils.Utils;

public class ImagesWorker
{
    public String getData(String src, String id, int level) 
    {
        try
        {
            return "http://src.sencha.io/" + Utils.getWidthForWebView(Utils.getCommentLevelIndicatorLength() * level) + "/" + src;
            
            /*String url = "http://src.sencha.io/data/" + width + "/" + src;
            
            String cachedData = Utils.readStringFromFileCache(url);
            if(!TextUtils.isEmpty(cachedData))
                return cachedData;

            String data = ServerWorker.Instance().getContent(url, false);
            if(!TextUtils.isEmpty(data))
            {
                Utils.writeStringToFileCache(url, data);
                return data;
            }*/
        }
        catch (Throwable t)
        {
            // TODO: handle exception
        }
        
        return Commons.IMAGE_STUB;
    }
}
