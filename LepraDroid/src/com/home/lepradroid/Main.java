package com.home.lepradroid;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.widget.TabHost;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.commons.Commons.PostSourceType;
import com.home.lepradroid.interfaces.LoginListener;
import com.home.lepradroid.interfaces.LogoutListener;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.GetAllMainPagesTask;
import com.home.lepradroid.tasks.GetPostsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;
import com.viewpagerindicator.TitlePageIndicator;

public class Main extends BaseActivity implements LoginListener, LogoutListener
{
    private TabHost tabHost;
    private PostsScreen mainPosts;
    private PostsScreen blogsPosts;
    private PostsScreen myStuffPosts;
    private TitlePageIndicator titleIndicator;
    private TabsPageAdapter tabsAdapter;
    private ViewPager pager;
    
    public static final int MAIN_TAB_NUM = 0;
    public static final int BLOGS_TAB_NUM = 1;
    public static final int MYSTUFF_TAB_NUM = 2;
    
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
            pushNewTask(new TaskWrapper(null, new GetAllMainPagesTask(), Utils.getString(R.string.Posts_Loading_In_Progress)));
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
            switch(pager.getCurrentItem())
            {
            case MAIN_TAB_NUM:
                pushNewTask(new TaskWrapper(null, new GetPostsTask(PostSourceType.MAIN), Utils.getString(R.string.Posts_Loading_In_Progress)));
                break;
            case BLOGS_TAB_NUM:
                break;
            case MYSTUFF_TAB_NUM:
                pushNewTask(new TaskWrapper(null, new GetPostsTask(PostSourceType.MYSTUFF), Utils.getString(R.string.Posts_Loading_In_Progress)));
                break;
            }
            
            return true;
        }
        return false;
    }
    
    private void createTabs()
    {
        mainPosts = new PostsScreen(this, PostSourceType.MAIN);
        mainPosts.setTag(Utils.getString(R.string.Posts_Tab));
        
        blogsPosts = new PostsScreen(this, PostSourceType.BLOGS);
        blogsPosts.setTag(Utils.getString(R.string.Blogs_Tab));
        
        myStuffPosts = new PostsScreen(this, PostSourceType.MYSTUFF);
        myStuffPosts.setTag(Utils.getString(R.string.MyStuff_Tab));
        
        pages.add(mainPosts);
        pages.add(blogsPosts);
        pages.add(myStuffPosts);
        
        tabsAdapter = new TabsPageAdapter(this, pages);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(tabsAdapter);
        pager.setCurrentItem(0);

        titleIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        titleIndicator.setViewPager(pager);
        titleIndicator.setCurrentItem(0);
    }

    public void OnLogin(boolean successful)
    {
        if(successful)
        {
            pushNewTask(new TaskWrapper(null, new GetAllMainPagesTask(), Utils.getString(R.string.Posts_Loading_In_Progress)));
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