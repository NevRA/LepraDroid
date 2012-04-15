package com.home.lepradroid;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.utils.ImageLoader;
import com.home.lepradroid.utils.Utils;

class PostsAdapter extends ArrayAdapter<BaseItem>
{
    private UUID                groupId;
    private ArrayList<BaseItem> posts               = new ArrayList<BaseItem>();
    public ImageLoader          imageLoader;
            
    public PostsAdapter(Context context, UUID groupId, int textViewResourceId,
            ArrayList<BaseItem> posts)
    {
        super(context, textViewResourceId, posts);
        this.posts = posts;
        this.groupId = groupId;
        imageLoader = new ImageLoader(LepraDroidApplication.getInstance());
    }

    public int getCount() 
    {
        return posts.size();
    }
    
    public BaseItem getItem(int position) 
    {
        return posts.get(position);
    }
    
    public long getItemId(int position) 
    { 
        return position;
    }
    
    public void updateData(ArrayList<BaseItem> posts)
    {
        this.posts = posts;
    }
    
    public void addProgressElement()
    {
        synchronized(posts)
        {
            if(!posts.isEmpty() && posts.get(posts.size() - 1) != null)
                posts.add(null);   
        }
    }
    
    public boolean isContainProgressElement()
    {
        synchronized(posts)
        {
            if(!posts.isEmpty() && posts.get(posts.size() - 1) == null)
                return true;
            else
                return false;
        }
    }
    
    public void removeProgressElement()
    {
        synchronized(posts)
        {
            if(!posts.isEmpty() && posts.get(posts.size() - 1) == null)
                posts.remove(null);
        }
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        Post post = (Post)getItem(position);
        LayoutInflater aInflater=LayoutInflater.from(getContext());
        
        if(post != null)
        {
            View view = aInflater.inflate(R.layout.post_row_view, parent, false);
            
            ImageView imageView = (ImageView)view.findViewById(R.id.image);
            TextView textView = (TextView)view.findViewById(R.id.text);
            
            if(!TextUtils.isEmpty(post.ImageUrl))
            {
                imageView.setVisibility(View.VISIBLE);
                imageLoader.DisplayImage(post.ImageUrl, imageView);
            }
            else
            {
                textView.setPadding(0, textView.getPaddingTop(), textView.getPaddingRight(), textView.getPaddingBottom());
            }
            
            String text = Utils.html2text(post.Html);
            textView.setText(TextUtils.isEmpty(text) ? "..." : text);
            if(!Utils.isNormalFontSize(getContext()))
            {
                RelativeLayout root = (RelativeLayout)view.findViewById(R.id.root);
                root.setPadding(root.getPaddingLeft() * 2, root.getPaddingTop(), root.getPaddingRight() * 2, root.getPaddingBottom() * 2);
                
                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                params.width = params.width * 3;
                params.height = params.height * 3;
                imageView.setLayoutParams(params);
                
                textView.setPadding (textView.getPaddingLeft() * 2, textView.getPaddingTop(), textView.getPaddingRight(), textView.getPaddingBottom());
                textView.setTypeface(null, Typeface.NORMAL);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimensionPixelSize(R.dimen.comment_font_size));
            }
            Utils.setTextViewFontSize(getContext(), textView);
            
            TextView authorView = (TextView)view.findViewById(R.id.author);
            authorView.setText(Html.fromHtml(post.Signature));
            Utils.setTextViewFontSize(getContext(), authorView);
            
            TextView commentsView = (TextView)view.findViewById(R.id.comments);
            commentsView.setText(Utils.getCommentsStringFromPost(post));
            Utils.setTextViewFontSize(getContext(), commentsView);
            
            TextView ratingView = (TextView)view.findViewById(R.id.rating);
            Utils.setTextViewFontSize(getContext(), ratingView);
            if(groupId.equals(Commons.INBOX_POSTS_ID) || post.voteDisabled)
                ratingView.setVisibility(View.GONE);
            else
                ratingView.setText(Utils.getRatingStringFromBaseItem(post));
            
            return view;
        }
        else
        {
            return (View) aInflater.inflate(R.layout.footer_view, null);
        }
    }
}
