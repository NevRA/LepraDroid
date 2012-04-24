package com.home.lepradroid;

import android.text.TextUtils;

import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Utils;

public class ImagesWorker
{
    private static volatile ImagesWorker instance;
    
    final private String sencha = "http://src.sencha.io/";
    
    private ImagesWorker()
    {
        
    }
    
    public static ImagesWorker Instance()
    {
        if(instance == null)
        {
            synchronized (ImagesWorker.class)
            {
                if(instance == null)
                {
                    instance = new ImagesWorker();
                }
            }
        }
        
        return instance;
    }
    
    public String getData(String src, final int level) 
    {
        final String srcWithSize = Utils.getWidthForWebView(Utils.getCommentLevelIndicatorLength() * level) + "/" + src;;
        final String url = sencha + srcWithSize;   
        
        try
        {  
            String cachedData = Utils.readStringFromFileCache(url);
            if(!TextUtils.isEmpty(cachedData))
            {
                return cachedData;
            }
            else
            {
                new Thread(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            String urlForData = sencha + "data/" + srcWithSize;
                            String data = ServerWorker.Instance().getContent(urlForData, false);
                            if(!TextUtils.isEmpty(data))
                            {
                                Utils.writeStringToFileCache(url, data);
                            }
                        }
                        catch (Exception e)
                        {
                            // TODO Auto-generated catch block
                        }
                    }
                }).start();
            }
        }
        catch (Throwable t)
        {
            // TODO: handle exception
        }
        
        return url;
    }
}
