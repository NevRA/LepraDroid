package com.home.lepradroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.commons.Commons.RateValueType;
import com.home.lepradroid.interfaces.ExitListener;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Comment;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.RateItemTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.LinksCatcher;
import com.home.lepradroid.utils.Utils;

class CommentsAdapter extends ArrayAdapter<BaseItem> implements ExitListener
{   
    public class ImageResizer
    {
        public String getImageUrl(String src, int num) 
        {
            try
            {
                Comment comment = (Comment)comments.get(num);
                int width = Utils.getWidthForWebView(commentLevelIndicatorLength * comment.Level);
                return "http://src.sencha.io/" + Integer.valueOf(width).toString() + "/" + src;
            }
            catch (Throwable e)
            {
                Log.e(Utils.getString(R.string.app_name), Log.getStackTraceString(e));
            }
            
            return Commons.IMAGE_STUB; 
        }
    }
    
    //private UUID postId;
    private UUID groupId;
    private List<BaseItem>      comments            = Collections.synchronizedList(new ArrayList<BaseItem>());
    private GestureDetector     gestureDetector;
    private int                 commentPos          = -1;
    private boolean             navigationTurnedOn  = true;
    private ListView            listView            = null;
    private int                 commentLevelIndicatorLength
                                                    = 0;
    private LayoutInflater      aInflater           = null;
      
    public CommentsAdapter(ListView parentListView, Context context, final UUID groupId, final UUID postId, int textViewResourceId,
            ArrayList<BaseItem> comments)
    {
        super(context, textViewResourceId, comments);
        this.comments = comments;
        this.listView = parentListView;
        //this.postId = postId;
        this.groupId = groupId;
        
        aInflater = LayoutInflater.from(getContext());
        
        commentLevelIndicatorLength = getContext().getResources().getDimensionPixelSize(R.dimen.comment_level_padding_left);
        
        gestureDetector = new GestureDetector(
                new GestureDetector.SimpleOnGestureListener()
                {
                    @Override
                    public void onLongPress(MotionEvent e)
                    {
                        final Comment item = (Comment)getItem(commentPos);
                        List<String> actions = new ArrayList<String>(0);
                        actions.add(Utils.getString(R.string.CommentAction_Author));
                        actions.add(Utils.getString(R.string.CommentAction_Reply));
                        if(     !item.Author.equalsIgnoreCase(SettingsWorker.Instance().loadUserName()) &&
                                !groupId.equals(Commons.INBOX_POSTS_ID))
                        {
                            if(!item.PlusVoted)
                                actions.add(Utils.getString(R.string.CommentAction_Like));
                            if(!item.MinusVoted)
                                actions.add(Utils.getString(R.string.CommentAction_Dislike));
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Выберите действие");
                        builder.setItems(actions.toArray(new String[actions.size()]), new DialogInterface.OnClickListener() 
                        {
                            public void onClick(DialogInterface dialog, int pos) 
                            {
                                switch (pos)
                                {
                                case 0:
                                    Intent intent = new Intent(getContext(), AuthorScreen.class);
                                    intent.putExtra("username", item.Author);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    LepraDroidApplication.getInstance().startActivity(intent); 
                                    break;
                                case 1:
                                    Utils.addComment(getContext(), groupId, postId, item.Id);
                                    break;
                                case 2:
                                    if(!item.PlusVoted)
                                        new TaskWrapper(null, new RateItemTask(groupId, postId, item.Id, RateValueType.PLUS), "");
                                    else 
                                        new TaskWrapper(null, new RateItemTask(groupId, postId, item.Id, RateValueType.MINUS), "");
                                    break;
                                case 3:
                                    new TaskWrapper(null, new RateItemTask(groupId, postId, item.Id, RateValueType.MINUS), "");
                                    break;
                                default:
                                    break;
                                }
                            }
                        });
                        builder.create().show();
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e)
                    {
                        if(!navigationTurnedOn ) return false;
                        
                        SettingsWorker.Instance().saveCommentClickTipDisabled(true);
                        
                        BaseItem item = getItem(commentPos);
                        if(item == null) return false;
                        
                        Comment comment = (Comment)item;
                        comment.IsExpand = !comment.IsExpand;
                        
                        int visiblePosition = listView.getFirstVisiblePosition();
                        View v = listView.getChildAt(commentPos - visiblePosition);
                        if(v == null) return false;

                        RelativeLayout webViewLayout = (RelativeLayout)v.findViewById(R.id.main);
                        ViewGroup.LayoutParams params = webViewLayout.getLayoutParams();

                        RelativeLayout authorLayout = (RelativeLayout)v.findViewById(R.id.authorLayout);
                        authorLayout.setPadding(authorLayout.getPaddingLeft(), comment.IsExpand ? 0 : getContext().getResources().getDimensionPixelSize(R.dimen.comment_author_layout_padding_top), authorLayout.getPaddingRight(), authorLayout.getPaddingBottom());
                        
                        params.height = comment.IsExpand ? ViewGroup.LayoutParams.FILL_PARENT : 
                           getContext().getResources().getDimensionPixelSize(R.dimen.comment_webview_height);
                        
                        webViewLayout.setLayoutParams(params);
                        
                        return false;
                    }

                    @Override
                    public boolean onDown(MotionEvent e)
                    {
                        return false;
                    }
                });
        gestureDetector.setIsLongpressEnabled(true);
    }
    
    public void OnExit()
    {
        listView = null;
    }
    
    public void setNavigationMode(boolean navigationTurnedOn)
    {
        this.navigationTurnedOn = navigationTurnedOn;
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
        
        this.comments.clear();
        this.comments.addAll(comments);
        
        if(containProgressElement)
            addProgressElement();
    }
    
    public void addProgressElement()
    {
        if(!comments.isEmpty() && comments.get(comments.size() - 1) != null)
            comments.add(null);   
    }
    
    public boolean isContainProgressElement()
    {
        if(!comments.isEmpty() && comments.get(comments.size() - 1) == null)
            return true;
        else
            return false;
    }
    
    public void removeProgressElement()
    {
        if(!comments.isEmpty() && comments.get(comments.size() - 1) == null)
            comments.remove(null);
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) 
    {
        Comment comment = (Comment)getItem(position);
        
        if(comment != null)
        {
            convertView = aInflater.inflate(R.layout.comments_row_view, parent, false);
            
            FrameLayout root = (FrameLayout)convertView.findViewById(R.id.root);
            CommentRootLayout content = (CommentRootLayout)convertView.findViewById(R.id.content);
            int effectiveLevel = Math.min(Commons.MAX_COMMENT_LEVEL, comment.Level);
            content.setLevel(effectiveLevel);
            
            if(effectiveLevel > 0)
            {
                root.setPadding(root.getPaddingLeft() + (effectiveLevel * commentLevelIndicatorLength), root.getPaddingTop(), root.getPaddingRight(), root.getPaddingBottom());
                content.setPadding(content.getPaddingLeft() * 2, content.getPaddingTop(), content.getPaddingRight(), content.getPaddingBottom());
            }

            RelativeLayout webViewLayout = (RelativeLayout)convertView.findViewById(R.id.main);
            ViewGroup.LayoutParams params = webViewLayout.getLayoutParams();
            if(!navigationTurnedOn || comment.IsExpand)
            {
                RelativeLayout authorLayout = (RelativeLayout)convertView.findViewById(R.id.authorLayout);
                authorLayout.setPadding(authorLayout.getPaddingLeft(), 0, authorLayout.getPaddingRight(), authorLayout.getPaddingBottom());
                
                params.height = ViewGroup.LayoutParams.FILL_PARENT; 
                webViewLayout.setLayoutParams(params);
            }
            
            WebView webView = (WebView)convertView.findViewById(R.id.text);
            webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            webView.setWebViewClient(new LinksCatcher());
            WebSettings webSettings = webView.getSettings();
            webSettings.setDefaultFontSize(13);
            webSettings.setJavaScriptEnabled(true);
            Utils.setWebViewFontSize(getContext(), webView);
            String header = Commons.WEBVIEW_COMMENT_HEADER;
            webView.loadDataWithBaseURL("", header + comment.Html, "text/html", "UTF-8", null);
            webView.addJavascriptInterface(new ImageResizer(), "ImageResizer");
            
            TextView author = (TextView)convertView.findViewById(R.id.author);
            author.setText(Html.fromHtml(comment.Signature));
            Utils.setTextViewFontSize(getContext(), author);
            
            TextView rating = (TextView)convertView.findViewById(R.id.rating);
            if(!groupId.equals(Commons.INBOX_POSTS_ID))
            {
                rating.setText(Utils.getRatingStringFromBaseItem(comment));
                Utils.setTextViewFontSize(getContext(), rating);
            }
            else
                rating.setVisibility(View.GONE);

            webView.setOnTouchListener(new View.OnTouchListener() 
            {
                @Override
                public boolean onTouch(View arg0, MotionEvent event)
                {
                    commentPos = position;
                    return gestureDetector.onTouchEvent(event);
                }
            });
            
            if(comment.IsNew)
            {
                webView.setBackgroundColor(0x00000000);
                root.setBackgroundColor(Color.parseColor("#FFE6E6E6"));
                content.setBackgroundColor(Color.parseColor("#FFE6E6E6"));
            }
        }
        else
        {
            convertView = (View) aInflater.inflate(R.layout.footer_view, null);
        }

        return convertView;
    }
}
