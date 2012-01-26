package com.home.lepradroid;

import java.util.ArrayList;
import java.util.List;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.commons.Commons.PostSourceType;
import com.home.lepradroid.interfaces.LoginListener;
import com.home.lepradroid.interfaces.LogoutListener;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.GetPostsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitleProvider;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
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
        
        LayoutInflater inflater = LayoutInflater.from(this);
        ArrayList<View> mPages = new ArrayList<View>();

        View mJokesRuPage = inflater.inflate(R.layout.logon_view, null);
        mJokesRuPage.setTag("Test 1");
        
        View mJokesRuPage2 = inflater.inflate(R.layout.logon_view, null);
        mJokesRuPage2.setTag("Test 2");
        
        View mJokesRuPage3 = inflater.inflate(R.layout.logon_view, null);
        mJokesRuPage3.setTag("Test 3");
        
        View mJokesRuPage4 = inflater.inflate(R.layout.logon_view, null);
        mJokesRuPage4.setTag("Test 4ertet");
        
        View mJokesRuPage5 = inflater.inflate(R.layout.logon_view, null);
        mJokesRuPage5.setTag("Test 5");
        
        View mJokesRuPage6 = inflater.inflate(R.layout.logon_view, null);
        mJokesRuPage6.setTag("Test 6");
        
        mPages.add(mJokesRuPage);
        mPages.add(mJokesRuPage2);
        mPages.add(mJokesRuPage3);
        mPages.add(mJokesRuPage4);
        mPages.add(mJokesRuPage5);
        mPages.add(mJokesRuPage6);
        
        MainPageAdapter adapter = new MainPageAdapter(mPages);
        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(adapter);
        mPager.setCurrentItem(0);

        TabPageIndicator mTitleIndicator = (TabPageIndicator) findViewById(R.id.indicator);
        mTitleIndicator.setViewPager(mPager);
        mTitleIndicator.setCurrentItem(0);
        
        /*createTabs();
        
        if(!SettingsWorker.Instance().IsLogoned())
            showLogonScreen();*/
    }
    
    public class MainPageAdapter extends PagerAdapter implements TitleProvider {

        private List<View> mPages;

        public MainPageAdapter(List<View> pPages) {
                mPages = pPages;
        }

        @Override
        public Object instantiateItem(View pCollection, int pPosition) {
                View view = mPages.get(pPosition);
                ((ViewPager) pCollection).addView(view, 0);
                return view;
        }

        @Override
        public void destroyItem(View pCollection, int pPosition, Object pView) {
                ((ViewPager) pCollection).removeView((View) pView);
        }

        @Override
        public int getCount() {
                return mPages.size();
        }

        @Override
        public boolean isViewFromObject(View pView, Object pObject) {
                return pView.equals(pObject);
        }

        @Override
        public void finishUpdate(View pView) {
        }

        @Override
        public void restoreState(Parcelable pParcelable, ClassLoader pLoader) {
        }

        @Override
        public Parcelable saveState() {
                return null;
        }

        @Override
        public void startUpdate(View pView) {
        }

        /**
         * For TitleProvider
         */
        public String getTitle(int pPosition) {
                return (String) (mPages.get(pPosition).getTag());
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