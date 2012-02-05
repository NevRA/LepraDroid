package com.home.lepradroid;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Comment;
import com.home.lepradroid.utils.Utils;

class CommentsAdapter extends ArrayAdapter<BaseItem>
{
    private ArrayList<BaseItem> comments = new ArrayList<BaseItem>();
            
    public CommentsAdapter(Context context, int textViewResourceId,
            ArrayList<BaseItem> comments)
    {
        super(context, textViewResourceId, comments);
        this.comments = comments;
    }

    public int getCount() 
    {
        return comments.size();
    }
    
    public BaseItem getItem(int position) 
    {
        return comments.get(position);
    }
    
    public long getItemId(int position) 
    { 
        return position;
    }
    
    public void updateData(ArrayList<BaseItem> comments)
    {
        this.comments = comments;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        final Comment comment = (Comment)getItem(position);
        
        LayoutInflater aInflater=LayoutInflater.from(getContext());

        View view = aInflater.inflate(R.layout.comments_row_view, parent, false);
        
        WebView webView = (WebView)view.findViewById(R.id.text);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        WebSettings webSettings = webView.getSettings();
        webSettings.setDefaultFontSize(13);
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
        webView.loadData(header + comment.Html, "text/html", "UTF-8");
        
        TextView author = (TextView)view.findViewById(R.id.author);
        author.setText(Html.fromHtml(comment.Signature));
        
        TextView rating = (TextView)view.findViewById(R.id.rating);
        rating.setText(Utils.getRatingStringFromBaseItem(comment));

        return view;
    }
}
