package com.home.lepradroid;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.CommentsUpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.tasks.GetCommentsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;

public class CommentsView extends BaseView implements CommentsUpdateListener
{
    private BaseActivity    context;
    private UUID            groupId;
    private UUID            id;
    private ListView        list;
    private ProgressBar     progress;
    private CommentsAdapter
                            adapter;
    private boolean         receivedLastElements = false;
    private boolean         shownLastElements = false;
    
    public CommentsView(BaseActivity context, UUID groupId, UUID id)
    {
        super(context);
        
        this.context = context;
        this.groupId = groupId;
        this.id = id;
        
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null)
        {
            contentView = inflater.inflate(R.layout.comments_view, null);
            init();
        }
    }

    private void init()
    {
        Post post = (Post)ServerWorker.Instance().getPostById(groupId, id);
        if(post == null) return;
        
        list = (ListView) contentView.findViewById(R.id.list);
        progress = (ProgressBar) contentView.findViewById(R.id.progress);
        TextView tooManyComments = (TextView) contentView.findViewById(R.id.too_many_comments);
        
        adapter = new CommentsAdapter(context, R.layout.comments_row_view, new ArrayList<BaseItem>());
        list.setAdapter(adapter);
        
        list.setOnScrollListener(new OnScrollListener() 
        {
            public void onScrollStateChanged(AbsListView arg0, int arg1) {}
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, final int totalItemCount) 
            {
                if (visibleItemCount > 0 && firstVisibleItem != 0 && firstVisibleItem + visibleItemCount == totalItemCount) 
                {
                    if(!receivedLastElements)
                    {
                        adapter.addProgressElement();
                        updateAdapter();
                    }
                    else if(!shownLastElements)
                    {
                        adapter.removeProgressElement();
                        updateAdapter();
                        shownLastElements = true;
                    }
                }
            }
        });
        
        if(post.TotalComments <= Commons.MAX_COMMENTS_COUNT)
        {
            context.pushNewTask(new TaskWrapper(null, new GetCommentsTask(groupId, id), Utils.getString(R.string.Posts_Loading_In_Progress)));
        }
        else
        {
            progress.setVisibility(View.GONE);
            tooManyComments.setVisibility(View.VISIBLE);
            
            tooManyComments.setText(String.format(Utils.getString(R.string.Too_Many_Comments), Commons.MAX_COMMENTS_COUNT));
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
    public void OnCommentsUpdateBegin(UUID id)
    {
        if(this.id != id) return;
        
        receivedLastElements = false;
        shownLastElements = false;
        
        progress.setVisibility(View.VISIBLE);
        progress.setIndeterminate(true);
        list.setVisibility(View.GONE);
        
        updateAdapter();
    }

    private void updateAdapter()
    {
        synchronized (this)
        {
            ArrayList<BaseItem> comments = ServerWorker.Instance().getComments(groupId, id);
            if(comments.size() != adapter.getCount())
            {
                adapter.updateData(comments);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void OnCommentsUpdateFirstEntries(UUID id)
    {
        if(this.id != id) return;
        
        if(progress.getVisibility() == View.VISIBLE)
        {
            progress.setVisibility(View.GONE);
            progress.setIndeterminate(false);
            list.setVisibility(View.VISIBLE);
        }
        
        updateAdapter();

    }

    @Override
    public void OnCommentsUpdateFinished(UUID id)
    {
        if(this.id != id) return;
        
        receivedLastElements = true;
        if(adapter.isEmpty())
        {
            shownLastElements = true;
            OnCommentsUpdateFirstEntries(id);
        }
    }
}
