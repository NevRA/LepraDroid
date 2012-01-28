package com.home.lepradroid;

import java.util.UUID;

import android.os.Bundle;
import android.webkit.WebView;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;

public class PostScreen extends BaseActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_view);
        
        UUID groupId = UUID.fromString(getIntent().getExtras().getString("groupId"));
        UUID id = UUID.fromString(getIntent().getExtras().getString("id"));
        
        BaseItem item = ServerWorker.Instance().getPostById(groupId, id);
        if(item == null)
            finish();
        
        WebView webView = (WebView) findViewById(R.id.webview);
        
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
        webView.loadData(header + ((Post)item).Html, "text/html", "UTF-8");
    }
}
