package com.home.lepradroid;

import android.os.Bundle;
import android.webkit.WebView;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.serverworker.ServerWorker;

public class PostScreen extends BaseActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_view);
        
        long position = getIntent().getExtras().getLong("position", 0);
        
        WebView webView = (WebView) findViewById(R.id.webview);
        
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
        webView.loadData(header + ServerWorker.Instance().getPosts().get((int)position).Html, "text/html", "UTF-8");
    }
}
