package com.home.lepradroid;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.CommentsUpdateListener;
import com.home.lepradroid.interfaces.ItemRateUpdateListener;
import com.home.lepradroid.interfaces.PostsUpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.tasks.GetPostsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;

public class PostsScreen extends BaseView implements CommentsUpdateListener, PostsUpdateListener, ItemRateUpdateListener
{
    private ListView    list;
    private ProgressBar progress;
    private UUID        groupId;
    private Context     context;
    private String      parentTitle;
    private String      url;
    private int         page = 0;
    private boolean     lastPageLoadedSuccessful = false;

    public PostsAdapter adapter;
    
    
    public PostsScreen(final Context context, final UUID groupId, final String url, String parentTitle)
    {                                                                            
        super(context);
        
        this.context = context;
        this.groupId = groupId;
        this.parentTitle = parentTitle;
        this.url = url;
        
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null)
            contentView = inflater.inflate(R.layout.posts_view, null);
        
        init();
    }

    public void init()
    {      
        list = (ListView) contentView.findViewById(R.id.list);
        progress = (ProgressBar) contentView.findViewById(R.id.progress);
        adapter = new PostsAdapter(context, groupId, R.layout.post_row_view, new ArrayList<BaseItem>());
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg, View arg1, int arg2,
                    long position)
            {
                Object obj = list.getItemAtPosition((int)position);
                if(obj != null && obj instanceof BaseItem)
                {
                    BaseItem item = (BaseItem)obj;
                    Intent intent = new Intent(LepraDroidApplication.getInstance(), PostScreen.class);
                    intent.putExtra("groupId", groupId.toString());
                    intent.putExtra("id", item.getId().toString());
                    intent.putExtra("parentTitle", parentTitle.length() > Commons.MAX_BLOG_HEADER_LENGTH ? parentTitle.substring(0, Commons.MAX_BLOG_HEADER_LENGTH - 1) + "..." : parentTitle);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    LepraDroidApplication.getInstance().startActivity(intent); 
                }
            }
        });
        
        list.setOnScrollListener(new OnScrollListener()
        {
            public void onScrollStateChanged(AbsListView arg0, int arg1)
            {
            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, final int totalItemCount)
            {
                if (    visibleItemCount > 0
                        && firstVisibleItem != 0
                        && firstVisibleItem + visibleItemCount == totalItemCount)
                {
                    if (lastPageLoadedSuccessful && page < ServerWorker.Instance().getPostPagesCount(groupId) - 1)
                    {
                        BaseActivity activity = (BaseActivity) context;
                        activity.popAllTasksLikeThis(GetPostsTask.class);
                        activity.pushNewTask(new TaskWrapper(null, new GetPostsTask(groupId, url, page + 1, false), Utils.getString(R.string.Posts_Loading_In_Progress)));
                        adapter.addProgressElement();
                        adapter.notifyDataSetChanged();
                        lastPageLoadedSuccessful = false;
                    }
                }
            }
        });
    }

    public void OnPostsUpdate(UUID groupId, int page)
    {
        if(!this.groupId.equals(groupId)) return;
    	
    	if(progress.getVisibility() == View.VISIBLE)
        {
            progress.setVisibility(View.GONE);
            progress.setIndeterminate(false);
            list.setVisibility(View.VISIBLE);
        }
    	
    	this.page = page;
    	
        updateAdapter();
    }
    
    @Override
    public void OnPostsUpdateBegin(UUID groupId, int page)
    {
        if(!this.groupId.equals(groupId)) return;
        
        if(page == 0)
        {
            progress.setVisibility(View.VISIBLE);
            progress.setIndeterminate(true);
            list.setVisibility(View.GONE);
        }
        
        this.page = page;
    }
    
    @Override
    public void OnPostsUpdateFinished(UUID id, int page, boolean successful)
    {
        if(!this.groupId.equals(groupId)) return;
        
        this.lastPageLoadedSuccessful = successful;
        
        if(successful)
        {
            OnPostsUpdate(groupId, page);
        }
        else
        {
            adapter.removeProgressElement();
            adapter.notifyDataSetChanged();
        }
    } 

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4)
    {
        
    }
    
    private void updateAdapter()
    {
        synchronized (this)
        {
            adapter.updateData(ServerWorker.Instance().getPostsById(groupId, true));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void OnExit()
    {
        context = null;
        adapter.clear();
        ListenersWorker.Instance().unregisterListener(this);
    }

    @Override
    public void OnPostRateUpdate(UUID groupId, UUID postId, int newRating, boolean successful)
    {
        if(!this.groupId.equals(groupId)) return;
        updateAdapter();
    }

    @Override
    public void OnCommentsUpdateBegin(UUID groupId, UUID postId)
    {
        if(!this.groupId.equals(groupId)) return;
    }

    @Override
    public void OnCommentsUpdateFirstEntries(UUID groupId, UUID postId)
    {
        if(!this.groupId.equals(groupId)) return;
        updateAdapter();
    }

    @Override
    public void OnCommentsUpdateFinished(UUID groupId, UUID postId)
    {
        if(!this.groupId.equals(groupId)) return;
        updateAdapter();
    }

    @Override
    public void OnCommentsUpdate(UUID groupId, UUID postId)
    {
    }

    @Override
    public void OnAuthorRateUpdate(String userId, boolean successful)
    {
    }

    @Override
    public void OnCommentRateUpdate(UUID groupId, UUID postId, UUID commentId,
            boolean successful)
    {
        if(!this.groupId.equals(groupId)) return;
    }
}
