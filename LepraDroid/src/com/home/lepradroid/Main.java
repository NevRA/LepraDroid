package com.home.lepradroid;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.commons.Commons.PostSourceType;
import com.home.lepradroid.interfaces.LoginListener;
import com.home.lepradroid.interfaces.LogoutListener;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.GetPostsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Main extends BaseActivity implements LoginListener, LogoutListener
{
    private TabHost tabHost;
    
	@Override
	protected void onDestroy() 
	{
		Utils.clearData();
		super.onDestroy();
	}
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        createTabs();
        
        if(!SettingsWorker.Instance().IsLogoned())
            showLogonScreen();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Commons.EXIT_FROM_LOGON_SCREEN_RESULTCODE) 
        {
            this.finish();
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
        intent.putExtra("type", PostSourceType.MAIN.toString());
        posts.setIndicator(Utils.getString(R.string.Posts_Tab), getResources().getDrawable(R.drawable.ic_main_tab)).setContent(
                intent);
        
        intent = new Intent(this, BlogsScreen.class);
        blogs.setIndicator(Utils.getString(R.string.Blogs_Tab), getResources().getDrawable(R.drawable.ic_blogs_tab)).setContent(
                intent);
        
        intent = new Intent(this, PostsScreen.class);
        intent.putExtra("type", PostSourceType.MYSTUFF.toString());
        mystuff.setIndicator(Utils.getString(R.string.MyStuff_Tab), getResources().getDrawable(R.drawable.ic_mystuff_tab)).setContent(
                intent);
                
        tabHost.addTab(posts);
        tabHost.addTab(blogs);
        tabHost.addTab(mystuff);
    }

    public void OnLogin(boolean successful)
    {
        if(successful)
        {
            pushNewTask(new TaskWrapper(this, new GetPostsTask(PostSourceType.MAIN), Utils.getString(R.string.Posts_Loading_In_Progress)));
        }
    }
    
    public void OnLogout()
    {
        Utils.clearData();
        Utils.clearLogonInfo();
        showLogonScreen();
        tabHost.setCurrentTab(0);
    }
    
    private void showLogonScreen()
    {
        Intent intent = new Intent(this, LogonScreen.class);
        startActivityForResult(intent, 0);
    }
}