package com.home.lepradroid;


import android.os.Bundle;
import android.widget.ListView;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.interfaces.PostsUpdateListener;
import com.home.lepradroid.serverworker.ServerWorker;

public class PostsScreen extends BaseActivity implements PostsUpdateListener
{
    private ListView list;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.posts_view);
        
        init();
    }

    private void init()
    {
        list = (ListView) findViewById(R.id.posts_list);
    }

    public void OnPostsUpdate()
    {
        final PostsAdapter adapter = new PostsAdapter(this, R.layout.post_row_view, ServerWorker.Instance().getPosts());

        list.setAdapter(adapter);
    }
}
