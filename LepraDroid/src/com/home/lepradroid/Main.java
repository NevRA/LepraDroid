package com.home.lepradroid;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.LoginListener;
import com.home.lepradroid.interfaces.LogoutListener;
import com.home.lepradroid.interfaces.ThresholdUpdateListener;
import com.home.lepradroid.settings.MainPreferences;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.GetAuthorTask;
import com.home.lepradroid.tasks.GetBlogsTask;
import com.home.lepradroid.tasks.GetMainPagesTask;
import com.home.lepradroid.tasks.GetPostsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.tasks.UpdateBadgeCounterTask;
import com.home.lepradroid.utils.Logger;
import com.home.lepradroid.utils.Utils;
import com.viewpagerindicator.TitlePageIndicator;

public class Main extends BaseActivity implements LoginListener, LogoutListener, ThresholdUpdateListener
{
    private AuthorView profileView;
    private TitlePageIndicator titleIndicator;
    private ViewPager pager;
    private boolean blogsInit                   = false;
    private boolean favoriteInit                = false;
    //private boolean myStuffInit                 = false;
    private boolean inboxInit                   = false;
    private boolean profileInit                 = false;
    private boolean postsInit                   = false;
    
    private ArrayList<BaseView> pages           = new ArrayList<BaseView>();

    private UUID authorPostsGroupId             = UUID.randomUUID();
    
    public static final int MAIN_TAB_NUM        = 0;
    public static final int BLOGS_TAB_NUM       = 3;
    public static final int FAVORITE_TAB_NUM    = 4;
    public static final int MYSTUFF_TAB_NUM     = 1;
    public static final int INBOX_TAB_NUM       = 2;
    public static final int PROFILE_TAB_NUM     = 5;
    public static final int POSTS_TAB_NUM       = 6;

	private void showHistoryOnStartUp()
	{
	    try
        {
	        String oldVersionName = SettingsWorker.Instance().loadVersion();
	        String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
	        if(!oldVersionName.equalsIgnoreCase(versionName))
	        {
	            SettingsWorker.Instance().saveVersion(versionName);
	            Utils.showChangesHistory(this);
	        }
        }
	    catch (Exception e)
	    {
            Logger.e(e);
        }
	}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        new UpdateBadgeCounterTask().execute();
        Utils.removeNotification(this);
        
        try
        {
            createTabs();

            if(!SettingsWorker.Instance().IsLogoned())
                showLogonScreen();
            else
            {
                showHistoryOnStartUp();
                pushNewTask(new TaskWrapper(null, new GetMainPagesTask(), Utils.getString(R.string.Posts_Loading_In_Progress)));
            }
        }
        catch (Exception e)
        {
            Logger.e(e);
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
            int currentItem = pager.getCurrentItem();
            if(currentItem == MAIN_TAB_NUM)
            {
                popAllTasksLikeThis(GetPostsTask.class);
                reloadMain();
            }
            else if(currentItem == BLOGS_TAB_NUM)
            {
                popAllTasksLikeThis(GetBlogsTask.class);
                reloadBlogs();
            }
            else if(currentItem == FAVORITE_TAB_NUM)
                reloadFavorite();
            else if(currentItem == MYSTUFF_TAB_NUM)
                reloadMyStuff();
            else if(currentItem == INBOX_TAB_NUM)
                reloadInbox();
            else if(currentItem == PROFILE_TAB_NUM)
                reloadAuthor();
            else if(currentItem == POSTS_TAB_NUM)
                reloadPosts();
            break;
        case MENU_SETTINGS:
            Intent intent = new Intent(this, MainPreferences.class);
            startActivity(intent);
            break;
        }
        return false;
    }

    private void reloadMain()
    {
        pushNewTask(new TaskWrapper(null, new GetPostsTask(Commons.MAIN_POSTS_ID, Commons.SITE_URL, Commons.PostsType.MAIN), Utils.getString(R.string.Posts_Loading_In_Progress)));
    }

    private void reloadFavorite()
    {
        pushNewTask(new TaskWrapper(null, new GetPostsTask(Commons.FAVORITE_POSTS_ID, Commons.FAVORITES_URL, Commons.PostsType.MY), Utils.getString(R.string.Posts_Loading_In_Progress)));
    }

    private void reloadMyStuff()
    {
        pushNewTask(new TaskWrapper(null, new GetPostsTask(Commons.MYSTUFF_POSTS_ID, Commons.MY_STUFF_URL, Commons.PostsType.MY), Utils.getString(R.string.Posts_Loading_In_Progress)));
    }

    private void reloadAuthor()
    {
        pushNewTask(new TaskWrapper(null, new GetAuthorTask(SettingsWorker.Instance().loadUserName()), Utils.getString(R.string.Posts_Loading_In_Progress)));
    }

    private void createTabs()
    {
        PostsScreen mainPostsView = new PostsScreen(this, Commons.MAIN_POSTS_ID, Commons.SITE_URL, Commons.PostsType.MAIN, Utils.getString(R.string.Posts_Tab));
        mainPostsView.setTag(Utils.getString(R.string.Main_Tab));

        BlogsScreen blogsPostsView = new BlogsScreen(this);
        blogsPostsView.setTag(Utils.getString(R.string.Blogs_Tab));

        PostsScreen favoritePostsView = new PostsScreen(this, Commons.FAVORITE_POSTS_ID, Commons.FAVORITES_URL, Commons.PostsType.MY, Utils.getString(R.string.Favorites_Tab));
        favoritePostsView.setTag(Utils.getString(R.string.Favorites_Tab));

        PostsScreen myStuffPostsView = new PostsScreen(this, Commons.MYSTUFF_POSTS_ID, Commons.MY_STUFF_URL, Commons.PostsType.MY, Utils.getString(R.string.MyStuff_Tab));
        myStuffPostsView.setTag(Utils.getString(R.string.MyStuff_Tab));

        PostsScreen inboxPostsView = new PostsScreen(this, Commons.INBOX_POSTS_ID, Commons.INBOX_URL, Commons.PostsType.MY, Utils.getString(R.string.Inbox_Tab));
        inboxPostsView.setTag(Utils.getString(R.string.Inbox_Tab));
        
        profileView = new AuthorView(this, SettingsWorker.Instance().loadUserName());
        profileView.setTag(Utils.getString(R.string.Profile_Tab));

        PostsScreen authorPostsView = new PostsScreen(
                this,
                authorPostsGroupId,
                String.format(Commons.AUTHOR_POSTS_URL, SettingsWorker.Instance().loadUserName()),
                Commons.PostsType.USER,
                Utils.getString(R.string.AuthorPosts_Tab));
        authorPostsView.setTag(Utils.getString(R.string.Posts_Tab));
        
        pages.add(mainPostsView);
        pages.add(myStuffPostsView);
        pages.add(inboxPostsView);
        pages.add(blogsPostsView);
        pages.add(favoritePostsView);
        pages.add(profileView);
        pages.add(authorPostsView);

        TabsPageAdapter tabsAdapter = new TabsPageAdapter(pages);
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
                                reloadFavorite();
                                favoriteInit = true;
                            }
                            break;
                        case FAVORITE_TAB_NUM:
                            if(!profileInit)
                            {
                                reloadAuthor();
                                profileInit = true;
                            }
                            break;
                        case MYSTUFF_TAB_NUM:
                            if(!inboxInit)
                            {
                                reloadInbox();
                                inboxInit = true;
                            }
                            break;
                        case INBOX_TAB_NUM:
                            if(!blogsInit)
                            {
                                reloadBlogs();
                                blogsInit = true;
                            }
                            break;
                        case POSTS_TAB_NUM:
                            if(!postsInit)
                            {
                                reloadPosts();
                                postsInit = true;
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

    private void reloadBlogs()
    {
        pushNewTask(new TaskWrapper(null, new GetBlogsTask(), Utils.getString(R.string.Posts_Loading_In_Progress)));
    }

    private void reloadInbox()
    {
        pushNewTask(new TaskWrapper(null, new GetPostsTask(Commons.INBOX_POSTS_ID, Commons.INBOX_URL, Commons.PostsType.MY), Utils.getString(R.string.Posts_Loading_In_Progress)));
    }

    private void reloadPosts()
    {
        pushNewTask(new TaskWrapper(null,
                new GetPostsTask(
                        authorPostsGroupId,
                        String.format(Commons.AUTHOR_POSTS_URL, SettingsWorker.Instance().loadUserName()),
                        Commons.PostsType.USER),
                Utils.getString(R.string.Posts_Loading_In_Progress)));
    }

    public void OnLogin(boolean successful)
    {
        if(successful)
        {
            profileView.setUserName(SettingsWorker.Instance().loadUserName());
            pushNewTask(new TaskWrapper(null, new GetMainPagesTask(), Utils.getString(R.string.Posts_Loading_In_Progress)));
            
            new UpdateBadgeCounterTask().execute();
            
            showHistoryOnStartUp();
        }
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();

        menu.add(0, MENU_RELOAD, 0, Utils.getString(R.string.Reload_Menu)).setIcon(R.drawable.ic_reload);
        menu.add(0, MENU_SETTINGS, 0, Utils.getString(R.string.Settings_Menu)).setIcon(R.drawable.ic_settings);
        menu.add(0, MENU_LOGOUT, 0, Utils.getString(R.string.Logout_Menu)).setIcon(R.drawable.ic_logout);
        
        return true;
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
        
        new UpdateBadgeCounterTask().execute();
    }
    
    private void showLogonScreen()
    {
        Intent intent = new Intent(this, LogonScreen.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public void OnThresholdUpdate(boolean successful)
    {
        if(successful)
        {
            popAllTasksLikeThis(GetPostsTask.class);
            reloadMain();
        }
    }
}