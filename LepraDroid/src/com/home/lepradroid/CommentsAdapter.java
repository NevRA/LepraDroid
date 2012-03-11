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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.home.lepradroid.interfaces.ExitListener;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Comment;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Utils;

class CommentsAdapter extends ArrayAdapter<BaseItem> implements ExitListener
{   
    //private UUID postId;
    //private UUID groupId;
    private ArrayList<BaseItem> comments            = new ArrayList<BaseItem>();
    private GestureDetector     gestureDetector;
    private int                 commentPos          = -1;
    private boolean             navigationTurnedOn  = true;
    private ListView            listView            = null;
    private int					authorLayoutPaddingTop;
    private int					commentWebViewHeight;
      
    public CommentsAdapter(ListView parentListView, Context context, final UUID groupId, final UUID postId, int textViewResourceId,
            ArrayList<BaseItem> comments)
    {
        super(context, textViewResourceId, comments);
        this.comments = comments;
        this.listView = parentListView;
        //this.postId = postId;
        //this.groupId = groupId;
        
        authorLayoutPaddingTop = context.getResources().getDimensionPixelOffset(R.dimen.comment_author_layout_padding_top);
        commentWebViewHeight = context.getResources().getDimensionPixelOffset(R.dimen.comment_webview_height);
        
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
    
    class CommentViewHolder{
    	CommentRootLayout border;
    	RelativeLayout webViewLayout;
    	RelativeLayout authorLayout;
    	WebView webView;
    	TextView author;
    	TextView rating;
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) 
    {
        Comment comment = (Comment)getItem(position);
        LayoutInflater aInflater = LayoutInflater.from(getContext());
        
        if(comment != null)
        {
        	
			CommentViewHolder holder;
			if (convertView != null && convertView.getTag() != null) {
				holder = (CommentViewHolder) convertView.getTag();
			} else {
				convertView = aInflater.inflate(R.layout.comments_row_view,
						parent, false);
				holder = new CommentViewHolder();
				holder.border = (CommentRootLayout) convertView
						.findViewById(R.id.root);
				holder.webViewLayout = (RelativeLayout) convertView
						.findViewById(R.id.main);
				holder.authorLayout = (RelativeLayout) convertView
						.findViewById(R.id.authorLayout);
				holder.webView = (WebView) convertView.findViewById(R.id.text);
				holder.author = (TextView) convertView
						.findViewById(R.id.author);
				holder.rating = (TextView) convertView
						.findViewById(R.id.rating);
				convertView.setTag(holder);
			}

			holder.border.setIsNew(comment.IsNew);

			ViewGroup.LayoutParams params = holder.webViewLayout
					.getLayoutParams();
			if (!navigationTurnedOn || comment.IsExpand) {
				holder.authorLayout.setPadding(
						holder.authorLayout.getPaddingLeft(), 0,
						holder.authorLayout.getPaddingRight(),
						holder.authorLayout.getPaddingBottom());
				params.height = ViewGroup.LayoutParams.FILL_PARENT;
				holder.webViewLayout.setLayoutParams(params);
			} else {
				holder.authorLayout.setPadding(
						holder.authorLayout.getPaddingLeft(),
						authorLayoutPaddingTop,
						holder.authorLayout.getPaddingRight(),
						holder.authorLayout.getPaddingBottom());
				params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
				holder.webViewLayout.setLayoutParams(params);
			}

			holder.webView
					.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
			WebSettings webSettings = holder.webView.getSettings();
			webSettings.setDefaultFontSize(13);
			String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

			holder.webView.clearView();
			holder.webView.loadDataWithBaseURL("", header + comment.Html,
					"text/html", "UTF-8", null);

			holder.author.setText(Html.fromHtml(comment.Signature));

			holder.rating.setText(Utils.getRatingStringFromBaseItem(comment));

			holder.webView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
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
