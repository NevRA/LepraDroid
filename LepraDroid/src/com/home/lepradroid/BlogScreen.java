package com.home.lepradroid;

import java.util.UUID;

import android.os.Bundle;
import android.view.MenuItem;
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
    private BaseItem post;
    
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
            
            post = ServerWorker.Instance().getPostById(groupId, id);
            if(post != null)
                pushNewTask(new TaskWrapper(null, new GetPostsTask(id, post.Url), Utils.getString(R.string.Posts_Loading_In_Progress)));
            else
                finish(); // TODO message
        }
        catch (Exception e)
        {
            Utils.showError(this, e);
        }
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId())
        {
        case MENU_RELOAD:
            pushNewTask(new TaskWrapper(null, new GetPostsTask(id, post.Url), Utils.getString(R.string.Posts_Loading_In_Progress)));
            return true;
        }
        return false;
    }
    
    @Override
    protected void onDestroy()
    {
        if(postsScreen != null)
        {
            postsScreen.OnExit();
            unbindDrawables(postsScreen.contentView);
        }
        super.onDestroy();
    }
}
