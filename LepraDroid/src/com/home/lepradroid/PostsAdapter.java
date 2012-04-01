package com.home.lepradroid;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
            
            TextView textView = (TextView)view.findViewById(R.id.text);
            String text = Utils.html2text(post.Html);
            textView.setText(TextUtils.isEmpty(text) ? "..." : text);
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
            
            ImageView imageView = (ImageView)view.findViewById(R.id.image);
            
            if(!TextUtils.isEmpty(post.ImageUrl))
            {
                imageView.setVisibility(View.VISIBLE);
                
                imageLoader.DisplayImage(post.ImageUrl, imageView);
            }
            
            return view;
        }
        else
        {
            return (View) aInflater.inflate(R.layout.footer_view, null);
        }
    }
}
