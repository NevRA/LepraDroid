package com.home.lepradroid;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.TabHost;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.commons.Commons.PostSourceType;
import com.home.lepradroid.interfaces.LoginListener;
import com.home.lepradroid.interfaces.LogoutListener;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.GetPostsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;
import com.viewpagerindicator.TabPageIndicator;

public class Main extends BaseActivity implements LoginListener, LogoutListener
{
    private TabHost tabHost;
    private PostsScreen mainPosts;
    private PostsScreen blogsPosts;
    private PostsScreen myStuffPosts;
    private TabPageIndicator titleIndicator;
    private TabsPageAdapter tabsAdapter;
    private ArrayList<BaseView> pages = new ArrayList<BaseView>();
    
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
        else
        {
            pushNewTask(new TaskWrapper(this, new GetPostsTask(PostSourceType.MAIN), Utils.getString(R.string.Posts_Loading_In_Progress)));
        }
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
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId())
        {
        case MENU_RELOAD:
            pushNewTask(new TaskWrapper(this, new GetPostsTask(PostSourceType.MAIN), Utils.getString(R.string.Posts_Loading_In_Progress)));
            return true;
        }
        return false;
    }
    
    private void createTabs()
    {
        LayoutInflater inflater = LayoutInflater.from(this);

        mainPosts = (PostsScreen)inflater.inflate(R.layout.posts_view, null);
        mainPosts.init(PostSourceType.MAIN, this.getBaseContext());
        mainPosts.setTag(Utils.getString(R.string.Posts_Tab));
        
        blogsPosts = (PostsScreen)inflater.inflate(R.layout.posts_view, null);
        blogsPosts.init(PostSourceType.BLOGS, this);
        blogsPosts.setTag(Utils.getString(R.string.Blogs_Tab));
        
        myStuffPosts = (PostsScreen)inflater.inflate(R.layout.posts_view, null);
        myStuffPosts.init(PostSourceType.MYSTUFF, this);
        myStuffPosts.setTag(Utils.getString(R.string.MyStuff_Tab));
        
        pages.add(mainPosts);
        pages.add(blogsPosts);
        pages.add(myStuffPosts);
        
        tabsAdapter = new TabsPageAdapter(this, pages);
        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(tabsAdapter);
        mPager.setCurrentItem(0);

        titleIndicator = (TabPageIndicator) findViewById(R.id.indicator);
        titleIndicator.setViewPager(mPager);
        titleIndicator.setCurrentItem(0);
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