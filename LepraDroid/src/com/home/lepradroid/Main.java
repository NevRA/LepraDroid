package com.home.lepradroid;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.interfaces.LoginListener;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.GetPostsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Main extends BaseActivity implements LoginListener
{
    private TabHost tabHost;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        createTabs();
        
        if(!SettingsWorker.Instance().IsLogoned())
        {
            Intent intent = new Intent(this, LogonScreen.class);
            startActivity(intent); 
        }
        else
        {
            pushNewTask(new TaskWrapper(this, new GetPostsTask(), "Загружаем посты..."));
        }
    }
    
    private void createTabs()
    {
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(getLocalActivityManager());
        
        TabSpec posts = tabHost.newTabSpec("posts");
        TabSpec blogs = tabHost.newTabSpec("blogs");
        TabSpec mystuff = tabHost.newTabSpec("mystuff");
        
        Intent intent = new Intent(this, PostsScreen.class);
        posts.setIndicator(Utils.getString(R.string.Posts_Tab), null).setContent(
                intent);
        
        intent = new Intent(this, BlogsScreen.class);
        blogs.setIndicator(Utils.getString(R.string.Blogs_Tab), null).setContent(
                intent);
        
        intent = new Intent(this, MyStuffScreen.class);
        mystuff.setIndicator(Utils.getString(R.string.MyStuff_Tab), null).setContent(
                intent);
                
        tabHost.addTab(posts);
        tabHost.addTab(blogs);
        tabHost.addTab(mystuff);
    }

    public void OnLogin(boolean successful)
    {
        if(successful)
        {
            pushNewTask(new TaskWrapper(this, new GetPostsTask(), "Загружаем посты..."));
        }
    }
}