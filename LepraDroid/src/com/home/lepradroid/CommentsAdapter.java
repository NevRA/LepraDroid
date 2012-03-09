package com.home.lepradroid;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.text.Html;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Comment;
import com.home.lepradroid.utils.Utils;

class CommentsAdapter extends ArrayAdapter<BaseItem>
{   
    //private UUID postId;
    //private UUID groupId;
    private ArrayList<BaseItem> comments            = new ArrayList<BaseItem>();
    private GestureDetector     gestureDetector;
    private int                 commentPos          = -1;
            
    public CommentsAdapter(Context context, final UUID groupId, final UUID postId, int textViewResourceId,
            ArrayList<BaseItem> comments)
    {
        super(context, textViewResourceId, comments);
        this.comments = comments;
        //this.postId = postId;
        //this.groupId = groupId;
        
        gestureDetector = new GestureDetector(
                new GestureDetector.SimpleOnGestureListener()
                {
                    @Override
                    public void onLongPress(MotionEvent e)
                    {
                        Utils.addComment(getContext(), groupId, postId, getItem(commentPos).Id);
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e)
                    {
                        Comment comment = (Comment)getItem(commentPos);
                        comment.IsExpand = !comment.IsExpand;
                        notifyDataSetChanged();
                        return true;
                    }

                    @Override
                    public boolean onDown(MotionEvent e)
                    {
                        return false;
                    }
                });
        gestureDetector.setIsLongpressEnabled(true);
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
        boolean containProgressElement = false;
        containProgressElement = isContainProgressElement();
        
        this.comments = comments;
        
        if(containProgressElement)
            addProgressElement();
    }
    
    public void addProgressElement()
    {
        synchronized(comments)
        {
            if(!comments.isEmpty() && comments.get(comments.size() - 1) != null)
                comments.add(null);   
        }
    }
    
    public boolean isContainProgressElement()
    {
        synchronized(comments)
        {
            if(!comments.isEmpty() && comments.get(comments.size() - 1) == null)
                return true;
            else
                return false;
        }
    }
    
    public void removeProgressElement()
    {
        synchronized(comments)
        {
            if(!comments.isEmpty() && comments.get(comments.size() - 1) == null)
                comments.remove(null);
        }
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) 
    {
        final Comment comment = (Comment)getItem(position);
        LayoutInflater aInflater = LayoutInflater.from(getContext());
        
        if(comment != null)
        {
            convertView = aInflater.inflate(R.layout.comments_row_view, parent, false);
            CommentRootLayout border = (CommentRootLayout)convertView.findViewById(R.id.root);
            border.setIsNew(comment.IsNew);
            
            RelativeLayout webViewLayout = (RelativeLayout)convertView.findViewById(R.id.main);
            ViewGroup.LayoutParams params = webViewLayout.getLayoutParams();
            if(comment.IsExpand)
            {
                params.height = ViewGroup.LayoutParams.FILL_PARENT; 
                webViewLayout.setLayoutParams(params);
            }
            
            WebView webView = (WebView)convertView.findViewById(R.id.text);
            webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            WebSettings webSettings = webView.getSettings();
            webSettings.setDefaultFontSize(13);
            String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
            webView.loadDataWithBaseURL("", header + comment.Html, "text/html", "UTF-8", null);
            
            TextView author = (TextView)convertView.findViewById(R.id.author);
            author.setText(Html.fromHtml(comment.Signature));
            
            TextView rating = (TextView)convertView.findViewById(R.id.rating);
            rating.setText(Utils.getRatingStringFromBaseItem(comment));
             
            webView.setOnTouchListener(new View.OnTouchListener() 
            {
                @Override
                public boolean onTouch(View arg0, MotionEvent event)
                {
                    commentPos = position;
                    return gestureDetector.onTouchEvent(event);
                }
            });
        }
        else
        {
            convertView = (View) aInflater.inflate(R.layout.footer_view, null);
        }

        return convertView;
    }
}
