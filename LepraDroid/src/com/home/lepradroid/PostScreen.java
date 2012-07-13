package com.home.lepradroid;

import java.util.ArrayList;
import java.util.UUID;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.commons.Commons.StuffOperationType;
import com.home.lepradroid.interfaces.ChangeFavListener;
import com.home.lepradroid.interfaces.ChangeMyStuffListener;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.tasks.*;
import com.home.lepradroid.utils.Utils;
import com.viewpagerindicator.TitlePageIndicator;


public class PostScreen extends BaseActivity implements ChangeMyStuffListener, ChangeFavListener
{
    private UUID            groupId;
    private UUID            postId;
    //private String          parentTitle;
    private UUID            authorPostsGroupId  = UUID.randomUUID();

    private PostView        postView;
    private CommentsView    commentsView;
    private AuthorView      authorView;
    private Post            post;
    private ViewPager       pager;
    private ArrayList<BaseView> 
                            pages               = new ArrayList<BaseView>();
    
    private boolean         authorInit          = false;
    private boolean         postsInit           = false;
    
    private boolean         navigationTurnedOn
                                                = false;
    
    public static final int POST_TAB_NUM        = 0;
    public static final int COMMENTS_TAB_NUM    = 1;
    public static final int PROFILE_TAB_NUM     = 2;
    public static final int POSTS_TAB_NUM       = 3;
    
    @Override
    protected void onDestroy()
    {
        ServerWorker.Instance().clearCommentsById(postId);
        
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
        
        switch(pager.getCurrentItem())
        {
        case POST_TAB_NUM:
            if(Utils.isAlreadyInStuff(Commons.MYSTUFF_POSTS_ID, post.getPid()))
                menu.add(0, MENU_DEL_STUFF, 0, Utils.getString(R.string.Del_Stuff_Menu)).setIcon(R.drawable.ic_del_stuff);
            else
                menu.add(0, MENU_ADD_STUFF, 0, Utils.getString(R.string.Add_Stuff_Menu)).setIcon(R.drawable.ic_add_stuff); 
            if(!groupId.equals(Commons.INBOX_POSTS_ID))
            {
                if(Utils.isAlreadyInStuff(Commons.FAVORITE_POSTS_ID, post.getPid()))
                    menu.add(0, MENU_DEL_FAV, 0, Utils.getString(R.string.Del_Fav_Menu)).setIcon(R.drawable.ic_del_fav);
                else
                    menu.add(0, MENU_ADD_FAV, 0, Utils.getString(R.string.Add_Fav_Menu)).setIcon(R.drawable.ic_add_fav); 
            }
            break;
        case COMMENTS_TAB_NUM:
            menu.add(0, MENU_COMMENT_NAVIGATE, 0, navigationTurnedOn ? Utils.getString(R.string.Turn_Off_Navigation) : Utils.getString(R.string.Turn_On_Navigation)).setIcon(navigationTurnedOn ? R.drawable.ic_comment_navigation_off : R.drawable.ic_comment_navigation);
            menu.add(0, MENU_RELOAD, 1, Utils.getString(R.string.Reload_Menu)).setIcon(R.drawable.ic_reload);
            menu.add(0, MENU_ADD_COMMENT, 2, Utils.getString(R.string.Comment_Menu)).setIcon(R.drawable.ic_add_comment);
            break;
        case PROFILE_TAB_NUM:
            menu.add(0, MENU_RELOAD, 0, Utils.getString(R.string.Reload_Menu)).setIcon(R.drawable.ic_reload);
            menu.add(0, MENU_INBOX, 1, Utils.getString(R.string.Inbox_Menu)).setIcon(R.drawable.ic_inbox);
            break;
        case POSTS_TAB_NUM:
            menu.add(0, MENU_RELOAD, 0, Utils.getString(R.string.Reload_Menu)).setIcon(R.drawable.ic_reload);
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
        postId          = UUID.fromString(getIntent().getExtras().getString("id"));
        //parentTitle = getIntent().getExtras().getString("parentTitle");

        createTabs();
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId())
        {
        case MENU_ADD_STUFF:
            pushNewTask(new TaskWrapper(null, new ChangeMyStuffTask(groupId, postId, StuffOperationType.ADD), null));
            break;
        case MENU_DEL_STUFF:
            pushNewTask(new TaskWrapper(null, new ChangeMyStuffTask(groupId, postId, StuffOperationType.REMOVE), null));
            break;
        case MENU_ADD_FAV:
            pushNewTask(new TaskWrapper(null, new ChangeFavTask(groupId, postId, StuffOperationType.ADD), null));
            break;
        case MENU_DEL_FAV:
            pushNewTask(new TaskWrapper(null, new ChangeFavTask(groupId, postId, StuffOperationType.REMOVE), null));
            break;
        case MENU_RELOAD:
            switch(pager.getCurrentItem())
            {
            case COMMENTS_TAB_NUM:
                popAllTasksLikeThis(GetCommentsTask.class);
                reloadComments();
                break;
            case PROFILE_TAB_NUM:
                reloadAuthor();
                break;
            case POSTS_TAB_NUM:
                reloadPosts();
                break;
            default:
                break;
            }
            break;
        case MENU_ADD_COMMENT:
            addComment();
            break;
        case MENU_INBOX:
            inbox();
            break;
        case MENU_COMMENT_NAVIGATE:
            navigationTurnedOn = !navigationTurnedOn;
            commentsView.setNavigationMode(navigationTurnedOn);
            break;
        }
        return false;
    }

    private void inbox()
    {
        Utils.addInbox(this, post.getAuthor());
    }

    private void addComment()
    {
        Utils.addComment(this, groupId, postId, null);
    }
    
    private void createTabs()
    {
        post = (Post)ServerWorker.Instance().getPostById(groupId, postId);
        if(post == null) {finish(); return;}
        
        navigationTurnedOn = post.getNewComments() > 0;

        postView = new PostView(this, groupId, postId);
        postView.setTag(Utils.getString(R.string.Post_Tab));
        
        commentsView = new CommentsView(this, groupId, postId);
        commentsView.setTag(Utils.getString(R.string.Comments_Tab));
        commentsView.setNavigationMode(navigationTurnedOn);
        
        if(     post.getTotalComments() <= Commons.MAX_COMMENTS_COUNT &&
                Utils.isCommentsLoadingWithPost(this))
        {
            reloadComments();
        }
        
        authorView = new AuthorView(this, post.getAuthor());
        authorView.setTag(Utils.getString(R.string.Author_Tab));

        PostsScreen authorPostsView = new PostsScreen(
                this,
                authorPostsGroupId,
                String.format(Commons.AUTHOR_POSTS_URL, post.getAuthor()),
                Commons.PostsType.USER,
                Utils.getString(R.string.AuthorPosts_Tab));
        authorPostsView.setTag(Utils.getString(R.string.AuthorPosts_Tab));
        
        pages.add(postView);
        pages.add(commentsView);
        pages.add(authorView);
        pages.add(authorPostsView);

        TabsPageAdapter tabsAdapter = new TabsPageAdapter(pages);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(tabsAdapter);
        pager.setCurrentItem(0);

        TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        titleIndicator.setViewPager(pager);
        titleIndicator.setCurrentItem(0);
        
        titleIndicator
                .setOnPageChangeListener(new ViewPager.OnPageChangeListener()
                {
                    @Override
                    public void onPageSelected(int position)
                    {
                        switch (position)
                        {
                            case COMMENTS_TAB_NUM:
                                if (!authorInit)
                                {
                                    reloadAuthor();
                                    authorInit = true;

                                    if (!Utils.isCommentsLoadingWithPost(PostScreen.this))
                                    {
                                        reloadComments();
                                    }
                                }
                                break;
                            case POSTS_TAB_NUM:
                                if (!postsInit)
                                {
                                    reloadPosts();
                                    postsInit = true;
                                }
                                break;
                        }
                    }

                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
                    {
                    }

                    @Override
                    public void onPageScrollStateChanged(int state)
                    {
                    }
                });
    }

    private void reloadPosts()
    {
        pushNewTask(new TaskWrapper(null,
                new GetPostsTask(
                        authorPostsGroupId,
                        String.format(Commons.AUTHOR_POSTS_URL, post.getAuthor()),
                        Commons.PostsType.USER),
                Utils.getString(R.string.Posts_Loading_In_Progress)));
    }

    private void reloadAuthor()
    {
        pushNewTask(new TaskWrapper(null, new GetAuthorTask(post.getAuthor()), Utils.getString(R.string.Posts_Loading_In_Progress)));
    }

    private void reloadComments()
    {
        pushNewTask(new TaskWrapper(null, new GetCommentsTask(groupId, postId), Utils.getString(R.string.Posts_Loading_In_Progress)));
    }

    @Override
    public void OnChangeMyStuff(UUID groupId, UUID postId,
            StuffOperationType type, boolean successful)
    {
        if(!this.groupId.equals(groupId) || !this.postId.equals(postId))
            return;
        
        if(successful)
        {
            Toast.makeText(this, Utils.getString(type == StuffOperationType.ADD ? R.string.Post_Added_To_Stuff : R.string.Post_Removed_From_Stuff), Toast.LENGTH_LONG).show();
            if(groupId.equals(Commons.MYSTUFF_POSTS_ID))
                finish();
        }
    }

    @Override
    public void OnChangeFav(UUID groupId, UUID postId, StuffOperationType type,
            boolean successful)
    {
        if(!this.groupId.equals(groupId) || !this.postId.equals(postId))
            return;
        
        if(successful)
        {
            Toast.makeText(this, Utils.getString(type == StuffOperationType.ADD ? R.string.Post_Added_To_Fav : R.string.Post_Removed_From_Fav), Toast.LENGTH_LONG).show();
            if(groupId.equals(Commons.FAVORITE_POSTS_ID))
                finish();
        }
    }
}
