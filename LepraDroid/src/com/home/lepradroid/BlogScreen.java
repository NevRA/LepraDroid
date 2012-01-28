package com.home.lepradroid;

import java.util.UUID;

import android.os.Bundle;
import android.widget.RelativeLayout;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.tasks.GetPostsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;

public class BlogScreen extends BaseActivity
{
    private UUID groupId;
    private UUID id;
    private PostsScreen postsScreen;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blog_view);
        
        try
        {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
            
            groupId = UUID.fromString(getIntent().getExtras().getString("groupId"));
            id = UUID.fromString(getIntent().getExtras().getString("id"));
            
            postsScreen = new PostsScreen(this, id);
            layout.addView(postsScreen.contentView);
            
            BaseItem item = ServerWorker.Instance().getPostById(groupId, id);
            if(item != null)
                pushNewTask(new TaskWrapper(null, new GetPostsTask(id, item.Url, true), Utils.getString(R.string.Posts_Loading_In_Progress)));
        }
        catch (Exception e)
        {
            Utils.showError(this, e);
        }
    }
    
    @Override
    protected void onDestroy()
    {
        if(postsScreen != null)
            postsScreen.OnExit();
        super.onDestroy();
    }
}
