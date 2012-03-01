package com.home.lepradroid;


import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.CommentsUpdateListener;
import com.home.lepradroid.interfaces.ImagesUpdateListener;
import com.home.lepradroid.interfaces.ItemRateUpdateListener;
import com.home.lepradroid.interfaces.PostsUpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.serverworker.ServerWorker;

public class PostsScreen extends BaseView implements CommentsUpdateListener, PostsUpdateListener, ImagesUpdateListener, ItemRateUpdateListener
{
    private ListView    list;
    private ProgressBar progress;
    private UUID        groupId;
    private Context     context;
    private String      parentTitle;

    public PostsAdapter adapter;
    
    
    public PostsScreen(final Context context, final UUID groupId, String parentTitle)
    {                                                                            
        super(context);
        
        this.context = context;
        this.groupId = groupId;
        this.parentTitle = parentTitle;
        
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
                    intent.putExtra("id", item.Id.toString());
                    intent.putExtra("parentTitle", parentTitle.length() > Commons.MAX_BLOG_HEADER_LENGTH ? parentTitle.substring(0, Commons.MAX_BLOG_HEADER_LENGTH - 1) + "..." : parentTitle);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    LepraDroidApplication.getInstance().startActivity(intent); 
                }
            }
        });
    }

    public void OnPostsUpdate(UUID groupId, boolean haveNewRecords)
    {
        if(!this.groupId.equals(groupId)) return;
    	
    	if(progress.getVisibility() == View.VISIBLE)
        {
            progress.setVisibility(View.GONE);
            progress.setIndeterminate(false);
            list.setVisibility(View.VISIBLE);
        }
    	
        updateAdapter();
    }
    
    @Override
    public void OnPostsUpdateBegin(UUID groupId)
    {
        if(!this.groupId.equals(groupId)) return;
        
        progress.setVisibility(View.VISIBLE);
        progress.setIndeterminate(true);
        list.setVisibility(View.GONE);
        
        updateAdapter();
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
    public void OnImagesUpdate(UUID groupId)
    {
        if(!this.groupId.equals(groupId)) return;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void OnExit()
    {
        ListenersWorker.Instance().unregisterListener(this);
    }

    @Override
    public void OnItemRateUpdate(UUID groupId, UUID postId, int newRating, boolean successful)
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
}
