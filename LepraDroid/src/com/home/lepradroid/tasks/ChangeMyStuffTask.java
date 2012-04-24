package com.home.lepradroid.tasks;

import com.home.lepradroid.commons.Commons.StuffOperationType;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;

public class ChangeMyStuffTask extends BaseTask
{
    private String              pid;
    private StuffOperationType  type;
    
    public ChangeMyStuffTask(String pid, StuffOperationType type)
    {
        this.pid = pid;
        this.type = type;
    }
    
    @Override
    protected Throwable doInBackground(Void... params)
    {
        try
        {
            ServerWorker.Instance().postChangeMyStuff(SettingsWorker.Instance().loadStuffWtf(), pid, type);
        }
        catch (Exception e)
        {
            setException(e);
        }
        
        return e;
    }
}
