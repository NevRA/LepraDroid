package com.home.lepradroid;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.interfaces.CommentsUpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.serverworker.ServerWorker;

public class CommentsView extends BaseView implements CommentsUpdateListener
{
    private Context     context;
    private UUID        groupId;
    private UUID        id;
    private ListView    list;
    private ProgressBar progress;
    private CommentsAdapter
                        adapter;
    
    public CommentsView(Context context, UUID groupId, UUID id)
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
        list = (ListView) contentView.findViewById(R.id.list);
        progress = (ProgressBar) contentView.findViewById(R.id.progress);
        
        adapter = new CommentsAdapter(context, R.layout.comments_row_view, new ArrayList<BaseItem>());
        list.setAdapter(adapter);
    }

    @Override
    public void OnExit()
    {
        adapter.clear();
        ListenersWorker.Instance().unregisterListener(this);
    }

    @Override
    public void OnCommentsUpdateBegin(UUID id)
    {
        if(this.id != id) return;
        
        progress.setVisibility(View.VISIBLE);
        progress.setIndeterminate(true);
        list.setVisibility(View.GONE);
        
        updateAdapter();
    }

    private void updateAdapter()
    {
        synchronized (this)
        {
            adapter.updateData(ServerWorker.Instance().getComments(groupId, id));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void OnCommentsUpdate(UUID id)
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
}
