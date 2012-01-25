package com.home.lepradroid;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.commons.Commons.PostSourceType;
import com.home.lepradroid.interfaces.PostsUpdateListener;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.GetPostsTask;
import com.home.lepradroid.tasks.TaskWrapper;

public class PostsScreen extends BaseActivity implements PostsUpdateListener
{
    private ListView list;
    private PostsAdapter adapter;
    private PostSourceType type;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.posts_view);
        
        type = PostSourceType.valueOf(getIntent().getExtras().getString("type"));
        
        init();
        
        if(SettingsWorker.Instance().IsLogoned())
        {
        	pushNewTask(new TaskWrapper(this, new GetPostsTask(type), "Загружаем посты..."));
        }
    }

    private void init()
    {
        list = (ListView) findViewById(R.id.posts_list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            public void onItemClick(AdapterView<?> arg, View arg1, int arg2,
                    long position)
            {
                Intent intent = new Intent(PostsScreen.this, PostScreen.class);
                intent.putExtra("position", position);
                intent.putExtra("type", type.toString());
                startActivity(intent); 
            }
        });
    }

    public void OnPostsUpdate(PostSourceType type)
    {
    	if(this.type != type) return;
    	
        if(adapter == null)
        {
            adapter = new PostsAdapter(this, R.layout.post_row_view, ServerWorker.Instance().getPostsByType(type));

            list.setAdapter(adapter);
        }
        else
        {
            adapter.notifyDataSetChanged();
        }
    }
}
