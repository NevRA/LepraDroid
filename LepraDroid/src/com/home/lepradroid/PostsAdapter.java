package com.home.lepradroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private List<BaseItem>      posts           = Collections.synchronizedList(new ArrayList<BaseItem>());
    private LayoutInflater      aInflater;
    private ImageLoader         imageLoader;
            
    public PostsAdapter(Context context, UUID groupId, int textViewResourceId,
            ArrayList<BaseItem> posts)
    {
        super(context, textViewResourceId, posts);
        this.posts = posts;
        this.groupId = groupId;
        imageLoader = new ImageLoader(LepraDroidApplication.getInstance());
        aInflater = LayoutInflater.from(getContext());
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
        if(!posts.isEmpty() && posts.get(posts.size() - 1) != null)
            posts.add(null);   
    }
    
    public boolean isContainProgressElement()
    {
        if(!posts.isEmpty() && posts.get(posts.size() - 1) == null)
            return true;
        else
            return false;
    }
    
    public void removeProgressElement()
    {
        if(!posts.isEmpty() && posts.get(posts.size() - 1) == null)
            posts.remove(null);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        Post post = (Post)getItem(position);
        
        if(post != null)
        {
            convertView = aInflater.inflate(R.layout.post_row_view, parent, false);
            
            ImageView imageView = (ImageView)convertView.findViewById(R.id.image);
            TextView textView = (TextView)convertView.findViewById(R.id.text);
            
            if(!TextUtils.isEmpty(post.getImageUrl()))
            {
                imageView.setVisibility(View.VISIBLE);
                imageLoader.DisplayImage(post.getImageUrl(), imageView);
            }
            else
            {
                textView.setPadding(0, textView.getPaddingTop(), textView.getPaddingRight(), textView.getPaddingBottom());
            }
            
            String text = post.getText();
            textView.setText(TextUtils.isEmpty(text) ? "..." : text);
            if(!Utils.isNormalFontSize())
            {
                RelativeLayout root = (RelativeLayout)convertView.findViewById(R.id.root);
                root.setPadding(root.getPaddingLeft() * 2, root.getPaddingTop(), root.getPaddingRight() * 2, root.getPaddingBottom() * 2);
                
                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                params.width = params.width * 3;
                params.height = params.height * 3;
                imageView.setLayoutParams(params);
                
                textView.setPadding (textView.getPaddingLeft() * 2, textView.getPaddingTop(), textView.getPaddingRight(), textView.getPaddingBottom());
                textView.setTypeface(null, Typeface.NORMAL);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimensionPixelSize(R.dimen.comment_font_size));
            }
            Utils.setTextViewFontSize(textView);
            
            TextView authorView = (TextView)convertView.findViewById(R.id.author);
            authorView.setText(Html.fromHtml(post.getSignature()));
            Utils.setTextViewFontSize(authorView);
            
            TextView commentsView = (TextView)convertView.findViewById(R.id.comments);
            commentsView.setText(Utils.getCommentsStringFromPost(post));
            Utils.setTextViewFontSize(commentsView);
            
            TextView ratingView = (TextView)convertView.findViewById(R.id.rating);
            Utils.setTextViewFontSize(ratingView);
            if(groupId.equals(Commons.INBOX_POSTS_ID) || post.isVoteDisabled())
                ratingView.setVisibility(View.GONE);
            else
                ratingView.setText(Utils.getRatingStringFromBaseItem(post));
            
            return convertView;
        }
        else
        {
            return (View) aInflater.inflate(R.layout.footer_view, null);
        }
    }
}
