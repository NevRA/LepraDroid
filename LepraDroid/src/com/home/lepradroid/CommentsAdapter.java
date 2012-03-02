package com.home.lepradroid;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.drawable.PictureDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
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
    private UUID postId;
    private UUID groupId;
    private ArrayList<BaseItem> comments = new ArrayList<BaseItem>();
            
    public CommentsAdapter(Context context, UUID groupId, UUID postId, int textViewResourceId,
            ArrayList<BaseItem> comments)
    {
        super(context, textViewResourceId, comments);
        this.comments = comments;
        this.postId = postId;
        this.groupId = groupId;
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
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        final Comment comment = (Comment)getItem(position);
        LayoutInflater aInflater = LayoutInflater.from(getContext());
        
        if(comment != null)
        {
            convertView = aInflater.inflate(R.layout.comments_row_view, parent, false);
            
            if(comment.IsNew)
            {
                RelativeLayout border = (RelativeLayout)convertView.findViewById(R.id.root);
                Picture picture = new Picture();
                Canvas canvas = picture.beginRecording(15, 15);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

                paint.setStrokeWidth(2);
                paint.setColor(android.graphics.Color.RED);     
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setAntiAlias(true);

                Point point1_draw = new Point(0, 0);       
                Point point2_draw = new Point(15, 0);   
                Point point3_draw = new Point(0, 15);

                Path path = new Path();
                path.setFillType(Path.FillType.EVEN_ODD);
                path.moveTo(point1_draw.x,point1_draw.y);
                path.lineTo(point2_draw.x,point2_draw.y);
                path.lineTo(point3_draw.x,point3_draw.y);
                path.lineTo(point1_draw.x,point1_draw.y);
                path.close();

                canvas.drawPath(path, paint);
                picture.endRecording();
                
                PictureDrawable drawable = new PictureDrawable(picture);
                border.setBackgroundDrawable(drawable);
                
                //border.setBackgroundResource(R.drawable.new_comment_border);
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
            
            webView.setOnLongClickListener(new OnLongClickListener() 
            {
                public boolean onLongClick(View v) 
                {
                    Utils.addComment(getContext(), groupId, postId, comment.Id);
                    return true;
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
