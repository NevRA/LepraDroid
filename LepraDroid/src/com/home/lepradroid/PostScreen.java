package com.home.lepradroid;

import java.util.ArrayList;
import java.util.UUID;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.tasks.GetAuthorTask;
import com.home.lepradroid.tasks.GetCommentsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;
import com.viewpagerindicator.TitlePageIndicator;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.select.Elements;

import static com.home.lepradroid.commons.Commons.*;


public class PostScreen extends BaseActivity
{
    private UUID            groupId;
    private UUID            id;
    private String          parentTitle;
    private String          postTitle;

    private PostView        postView;
    private CommentsView    commentsView;
    private AuthorView      authorView;
    private TitlePageIndicator 
                            titleIndicator;
    private BaseItem        post;
    private TabsPageAdapter tabsAdapter;
    private ViewPager       pager;
    private ArrayList<BaseView> 
                            pages = new ArrayList<BaseView>();
    
    private boolean         authorInit = false;
    
    public static final int POST_TAB_NUM = 0;
    public static final int COMMENTS_TAB_NUM = 1;
    public static final int PROFILE_TAB_NUM = 2;
    
    @Override
    protected void onDestroy()
    {
        ServerWorker.Instance().clearCommentsById(id);
        
        if(postView != null)
        {
            postView.OnExit();
            unbindDrawables(postView.contentView.getRootView());
        }
        if(commentsView != null)
        {
            commentsView.OnExit();
            unbindDrawables(commentsView.contentView.getRootView());
        }
        if(authorView != null)
        {
            authorView.OnExit();
            unbindDrawables(authorView.contentView.getRootView());
        }
        
        super.onDestroy();
    }
    
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        menu.add(0, MENU_RELOAD, 0, Utils.getString(R.string.Reload_Menu)).setIcon(R.drawable.ic_reload);
        switch(pager.getCurrentItem())
        {
        case COMMENTS_TAB_NUM:
            menu.add(0, MENU_ADD_COMMENT, 1, Utils.getString(R.string.Comment_Menu)).setIcon(R.drawable.ic_add_comment);
            break;
        default:
            menu.add(0, MENU_LOGOUT, 1, Utils.getString(R.string.Logout_Menu)).setIcon(R.drawable.ic_logout);
            break;
        }
        
        return true;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_base_view);
        
        groupId     = UUID.fromString(getIntent().getExtras().getString("groupId"));
        id          = UUID.fromString(getIntent().getExtras().getString("id"));
        parentTitle = getIntent().getExtras().getString("parentTitle");
        postTitle   = getIntent().getExtras().getString("postTitle");

        createTabs();
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId())
        {
        case MENU_RELOAD:
            switch(pager.getCurrentItem())
            {
            case COMMENTS_TAB_NUM:
                popAllTasksLikeThis(GetCommentsTask.class);
                pushNewTask(new TaskWrapper(null, new GetCommentsTask(groupId, id), Utils.getString(R.string.Posts_Loading_In_Progress)));
                break;
            case PROFILE_TAB_NUM:
                pushNewTask(new TaskWrapper(null, new GetAuthorTask(post.Author), Utils.getString(R.string.Posts_Loading_In_Progress)));
                break;
            default:
                break;
            }
            return true;
        case MENU_ADD_COMMENT:
            addComment();
            return true; 
        }
        return false;
    }
    
    private void addComment()
    {
        final EditText input = new EditText(this);
        
        new AlertDialog.Builder(PostScreen.this)
        .setTitle(Utils.getString(R.string.Add_Comment_Title))
        .setView(input)
        .setPositiveButton(Utils.getString(R.string.yarrr_label), new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int whichButton) 
            {
                final String value = input.getText().toString(); 
            }
        }).setNegativeButton(Utils.getString(android.R.string.cancel), new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int whichButton) 
            {
            }
        }).show();
    }
    
    private void createTabs()
    {
        post = ServerWorker.Instance().getPostById(groupId, id);
        if(post == null) finish();

        postView = new PostView(this, groupId, id);
        postView.setTag(parentTitle + Commons.DELIMETER + Utils.getString(R.string.Post_Tab));
        
        commentsView = new CommentsView(this, groupId, id);
        commentsView.setTag(postTitle + Commons.DELIMETER + Utils.getString(R.string.Comments_Tab));
        
        authorView = new AuthorView(this, post.Author);
        authorView.setTag(postTitle + Commons.DELIMETER +Utils.getString(R.string.Author_Tab));
        
        pages.add(postView);
        pages.add(commentsView);
        pages.add(authorView);
        
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
                        case COMMENTS_TAB_NUM:
                            if(!authorInit)
                            {
                                pushNewTask(new TaskWrapper(null, new GetAuthorTask(post.Author), Utils.getString(R.string.Posts_Loading_In_Progress)));
                                authorInit = true;
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
}
