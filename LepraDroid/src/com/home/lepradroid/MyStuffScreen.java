package com.home.lepradroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.commons.Commons.PostSourceType;
import com.home.lepradroid.interfaces.MyStuffUpdateListener;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.tasks.GetPostsTask;
import com.home.lepradroid.tasks.TaskWrapper;

public class MyStuffScreen extends BaseActivity implements MyStuffUpdateListener
{
    private ListView list;
    private PostsAdapter adapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.posts_view);
        
        init();
        
        pushNewTask(new TaskWrapper(this, new GetPostsTask(PostSourceType.MYSTUFF), "Загружаем мои вещи..."));
    }

    private void init()
    {
        list = (ListView) findViewById(R.id.posts_list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            public void onItemClick(AdapterView<?> arg, View arg1, int arg2,
                    long position)
            {
                Intent intent = new Intent(MyStuffScreen.this, PostScreen.class);
                intent.putExtra("position", position);
                intent.putExtra("type", PostSourceType.MYSTUFF.toString());
                startActivity(intent); 
            }
        });
    }

    public void OnMyStuffUpdate()
    {
        if(adapter == null)
        {
            adapter = new PostsAdapter(this, R.layout.post_row_view, ServerWorker.Instance().getPosts(PostSourceType.MYSTUFF));

            list.setAdapter(adapter);
        }
        else
        {
            adapter.notifyDataSetChanged();
        }
    }
}
