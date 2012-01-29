package com.home.lepradroid;

import java.util.UUID;

import android.content.Context;
import android.view.LayoutInflater;
import android.webkit.WebView;

import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;

public class PostView extends BaseView
{
    //private Context context;
    private UUID    groupId;
    private UUID    id;
    
    public PostView(Context context, UUID groupId, UUID id)
    {
        super(context);
        
        //this.context = context;
        this.groupId = groupId;
        this.id = id;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null)
        {
            contentView = inflater.inflate(R.layout.post_view, null);
        }

        init();
    }

    private void init()
    {
        BaseItem item = ServerWorker.Instance().getPostById(groupId, id);
        if(item == null)
            return; // TODO some message
        
        WebView webView = (WebView) contentView.findViewById(R.id.webview);
        
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
        webView.loadData(header + ((Post)item).Html, "text/html", "UTF-8");
    }

    @Override
    public void OnExit()
    {
        ListenersWorker.Instance().unregisterListener(this);
    }
}
