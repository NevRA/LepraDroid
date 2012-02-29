package com.home.lepradroid.tasks;

import java.util.UUID;

import com.home.lepradroid.commons.Commons.RateType;
import com.home.lepradroid.commons.Commons.RateValueType;
import com.home.lepradroid.serverworker.ServerWorker;

public class RateItemTask extends BaseTask
{
    private UUID            groupId;
    private UUID            postId;
    private RateType        type;
    private String          wtf;
    private String          id;
    private RateValueType   value;
    
    public RateItemTask(UUID groupId, UUID postId, RateType type, String wtf, String id, RateValueType value)
    {
        this.groupId = groupId;
        this.postId = postId;
        this.type = type;
        this.wtf = wtf;
        this.id = id;
        this.value = value;
    }

    @Override
    protected Throwable doInBackground(Void... params)
    {
        try
        {
            String response = ServerWorker.Instance().rateItem(type, wtf, id, value);
        }
        catch (Exception e)
        {
            setException(e);
        }
        
        return e;
    }

}
