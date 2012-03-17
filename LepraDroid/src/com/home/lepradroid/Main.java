package com.home.lepradroid;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.LoginListener;
import com.home.lepradroid.interfaces.LogoutListener;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.GetAuthorTask;
import com.home.lepradroid.tasks.GetBlogsTask;
import com.home.lepradroid.tasks.GetMainPagesTask;
import com.home.lepradroid.tasks.GetPostsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;
import com.viewpagerindicator.TitlePageIndicator;

public class Main extends BaseActivity implements LoginListener, LogoutListener
{
    private PostsScreen mainPosts;
    private BlogsScreen blogsPosts;
    private PostsScreen favoritePosts;
    private PostsScreen myStuffPosts;
    private PostsScreen inboxPosts;
    private AuthorView  profile;
    private TitlePageIndicator titleIndicator;
    private TabsPageAdapter tabsAdapter;
    private ViewPager pager;
    private boolean blogsInit                   = false;
    private boolean favoriteInit                = false;
    //private boolean myStuffInit                 = false;
    private boolean inboxInit                   = false;
    private boolean profileInit                 = false;
    
    private ArrayList<BaseView> pages           = new ArrayList<BaseView>();
    
    public static final int MAIN_TAB_NUM        = 0;
    public static final int BLOGS_TAB_NUM       = 3;
    public static final int FAVORITE_TAB_NUM    = 4;
    public static final int MYSTUFF_TAB_NUM     = 1;
    public static final int INBOX_TAB_NUM       = 2;
    public static final int PROFILE_TAB_NUM     = 5;
    
    
    
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
            pushNewTask(new TaskWrapper(null, new GetMainPagesTask(), Utils.getString(R.string.Posts_Loading_In_Progress)));
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
                pushNewTask(new TaskWrapper(null, new GetPostsTask(Commons.MAIN_POSTS_ID, Commons.SITE_URL), Utils.getString(R.string.Posts_Loading_In_Progress)));
                break;
            case BLOGS_TAB_NUM:
                pushNewTask(new TaskWrapper(null, new GetBlogsTask(), Utils.getString(R.string.Posts_Loading_In_Progress)));
                break;
            case FAVORITE_TAB_NUM:
                pushNewTask(new TaskWrapper(null, new GetPostsTask(Commons.FAVORITE_POSTS_ID, Commons.FAVORITES_URL), Utils.getString(R.string.Posts_Loading_In_Progress)));
                break;
            case MYSTUFF_TAB_NUM:
                pushNewTask(new TaskWrapper(null, new GetPostsTask(Commons.MYSTUFF_POSTS_ID, Commons.MY_STUFF_URL), Utils.getString(R.string.Posts_Loading_In_Progress)));
                break;
            case INBOX_TAB_NUM:
                pushNewTask(new TaskWrapper(null, new GetPostsTask(Commons.INBOX_POSTS_ID, Commons.INBOX_URL), Utils.getString(R.string.Posts_Loading_In_Progress)));
                break;
            case PROFILE_TAB_NUM:
                pushNewTask(new TaskWrapper(null, new GetAuthorTask(SettingsWorker.Instance().loadUserName()), Utils.getString(R.string.Posts_Loading_In_Progress)));
                break;
            }
            
            return true;
        }
        return false;
    }
    
    private void createTabs()
    {
        mainPosts = new PostsScreen(this, Commons.MAIN_POSTS_ID, Commons.SITE_URL, Utils.getString(R.string.Posts_Tab));
        mainPosts.setTag(Utils.getString(R.string.Posts_Tab));
        
        blogsPosts = new BlogsScreen(this);
        blogsPosts.setTag(Utils.getString(R.string.Blogs_Tab));
        
        favoritePosts = new PostsScreen(this, Commons.FAVORITE_POSTS_ID, Commons.FAVORITES_URL, Utils.getString(R.string.Favorites_Tab));
        favoritePosts.setTag(Utils.getString(R.string.Favorites_Tab));
        
        myStuffPosts = new PostsScreen(this, Commons.MYSTUFF_POSTS_ID, Commons.MY_STUFF_URL, Utils.getString(R.string.MyStuff_Tab));
        myStuffPosts.setTag(Utils.getString(R.string.MyStuff_Tab));
        
        inboxPosts = new PostsScreen(this, Commons.INBOX_POSTS_ID, Commons.INBOX_URL,  Utils.getString(R.string.Inbox_Tab));
        inboxPosts.setTag(Utils.getString(R.string.Inbox_Tab));
        
        profile = new AuthorView(this, SettingsWorker.Instance().loadUserName());
        profile.setTag(Utils.getString(R.string.Profile_Tab));
        
        pages.add(mainPosts);
        pages.add(myStuffPosts);
        pages.add(inboxPosts);
        pages.add(blogsPosts);
        pages.add(favoritePosts);
        pages.add(profile);
        
        tabsAdapter = new TabsPageAdapter(this, pages);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(tabsAdapter);
        pager.setCurrentItem(0);

        titleIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        titleIndicator.setViewPager(pager);
        titleIndicator.setCurrentItem(0);
        
        titleIndicator
                .setOnPageChangeListener(new ViewPager.OnPageChangeListener()
                {
                    @Override
                    public void onPageSelected(int position)
                    {
                        switch(position)
                        {
                        case BLOGS_TAB_NUM:
                            if(!favoriteInit)
                            {
                                pushNewTask(new TaskWrapper(null, new GetPostsTask(Commons.FAVORITE_POSTS_ID, Commons.FAVORITES_URL), Utils.getString(R.string.Posts_Loading_In_Progress)));
                                favoriteInit = true;
                            }
                            break;
                        case FAVORITE_TAB_NUM:
                            if(!profileInit)
                            {
                                pushNewTask(new TaskWrapper(null, new GetAuthorTask(SettingsWorker.Instance().loadUserName()), Utils.getString(R.string.Posts_Loading_In_Progress)));
                                profileInit = true;
                            }
                            break;
                        case MYSTUFF_TAB_NUM:
                            if(!inboxInit)
                            {
                                pushNewTask(new TaskWrapper(null, new GetPostsTask(Commons.INBOX_POSTS_ID, Commons.INBOX_URL), Utils.getString(R.string.Posts_Loading_In_Progress)));
                                inboxInit = true;
                            }
                            break;
                        case INBOX_TAB_NUM:
                            if(!blogsInit)
                            {
                                pushNewTask(new TaskWrapper(null, new GetBlogsTask(), Utils.getString(R.string.Posts_Loading_In_Progress)));
                                blogsInit = true;
                            }
                            break;
                        }
                    }

                    @Override
                    public void onPageScrolled(int position,
                            float positionOffset, int positionOffsetPixels)
                    {
                    }

                    @Override
                    public void onPageScrollStateChanged(int state)
                    {
                    }
                });
    }

    public void OnLogin(boolean successful)
    {
        if(successful)
        {
            profile.setUserName(SettingsWorker.Instance().loadUserName());
            pushNewTask(new TaskWrapper(null, new GetMainPagesTask(), Utils.getString(R.string.Posts_Loading_In_Progress)));
        }
    }
    
    private void cleanInitState()
    {
        favoriteInit = false;
        //myStuffInit = false;
        inboxInit = false;
        profileInit = false;
        blogsInit = false;
    }
    
    public void OnLogout()
    {
        detachAllTasks();
        Utils.clearData();
        Utils.clearLogonInfo();
        cleanInitState();
        showLogonScreen();
        titleIndicator.setCurrentItem(0);
    }
    
    private void showLogonScreen()
    {
        Intent intent = new Intent(this, LogonScreen.class);
        startActivityForResult(intent, 0);
    }
}