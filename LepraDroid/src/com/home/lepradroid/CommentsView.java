package com.home.lepradroid;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.AddedCommentUpdateListener;
import com.home.lepradroid.interfaces.CommentsUpdateListener;
import com.home.lepradroid.interfaces.ItemRateUpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Comment;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Utils;

public class CommentsView extends BaseView implements CommentsUpdateListener,
        AddedCommentUpdateListener, ItemRateUpdateListener
{
    private BaseActivity    context;
    private UUID            groupId;
    private UUID            postId;
    private ListView        list;
    private ProgressBar     progress;
    private CommentsAdapter adapter;
    private LinearLayout    buttons;
    private Button          up;
    private Button          down;
    private boolean         receivedLastElements 
                                    = false;
    private boolean         shownLastElements 
                                    = false;
    private boolean         waitingNextRecord 
                                    = false;
    private boolean         navigationTurnedOn
                                    = false;
    private ArrayList<BaseItem> 
                            newComments 
                                    = new ArrayList<BaseItem>();

    public CommentsView(BaseActivity context, UUID groupId, UUID id)
    {
        super(context);

        this.context = context;
        this.groupId = groupId;
        this.postId = id;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null)
        {
            contentView = inflater.inflate(R.layout.comments_view, null);
            init();
        }
    }
    
    public void setNavigationMode(boolean navigationTurnedOn)
    {
        this.navigationTurnedOn = navigationTurnedOn;
        buttons.setVisibility(navigationTurnedOn ? View.VISIBLE : View.GONE);
        adapter.setNavigationMode(navigationTurnedOn);
        adapter.notifyDataSetChanged();
    }

    private void init()
    {
        Post post = (Post) ServerWorker.Instance().getPostById(groupId, postId);
        if (post == null)
            return;

        list = (ListView) contentView.findViewById(R.id.list);
        progress = (ProgressBar) contentView.findViewById(R.id.progress);
        buttons = (LinearLayout) contentView.findViewById(R.id.buttons);
        up = (Button) contentView.findViewById(R.id.up);
        down = (Button) contentView.findViewById(R.id.down);

        up.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                goToPrevNewComment();
            }
        });

        down.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                goToNextNewComment();
            }
        });

        TextView tooManyComments = (TextView) contentView
                .findViewById(R.id.too_many_comments);

        adapter = new CommentsAdapter(list, context, groupId, post.Id,
                R.layout.comments_row_view, new ArrayList<BaseItem>());
        list.setAdapter(adapter);

        list.setOnScrollListener(new OnScrollListener()
        {
            public void onScrollStateChanged(AbsListView arg0, int arg1)
            {
            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, final int totalItemCount)
            {
                if (visibleItemCount > 0
                        && firstVisibleItem != 0
                        && firstVisibleItem + visibleItemCount == totalItemCount)
                {
                    if (!receivedLastElements)
                    {
                        adapter.addProgressElement();
                        updateAdapter();
                    }
                    else if (!shownLastElements)
                    {
                        adapter.removeProgressElement();
                        updateAdapter();
                        shownLastElements = true;
                    }
                }
            }
        });
        
        list.setFadingEdgeLength(0);

        if (post.TotalComments > Commons.MAX_COMMENTS_COUNT)
        {
            progress.setVisibility(View.GONE);
            buttons.setVisibility(View.GONE);
            tooManyComments.setVisibility(View.VISIBLE);
            tooManyComments.setText(String.format(
                    Utils.getString(R.string.Too_Many_Comments),
                    Commons.MAX_COMMENTS_COUNT));
        }
    }

    private void goToPrevNewComment()
    {
        int prevPosition = ServerWorker.Instance().getPrevNewCommentPosition(
                groupId, postId, list.getFirstVisiblePosition());
        if (prevPosition != -1)
        {
            list.setSelection(prevPosition);
            waitingNextRecord = false;
        }
    }

    private void goToNextNewComment()
    {
        waitingNextRecord = false;

        final int nextPosition = ServerWorker.Instance()
                .getNextNewCommentPosition(groupId, postId,
                        list.getFirstVisiblePosition());
        if (nextPosition != -1
                && list.getLastVisiblePosition() != list.getCount() - 1)
        {
            if(nextPosition >= list.getCount())
                updateAdapter();
            
            list.post(new Runnable()
            {
                @Override
                public void run()
                {
                    list.setSelection(nextPosition);
                }
            });         
        }
        else
        {
            if (receivedLastElements)
                Toast.makeText(context, Utils.getString(R.string.No_New_Comments),
                        Toast.LENGTH_LONG).show();
            else
            {
                Toast.makeText(context, Utils.getString(R.string.Searching_For_New_Comments),
                        Toast.LENGTH_LONG).show();
                
                waitingNextRecord = true;
            }
        }
    }

    public boolean isReceivedLastElement()
    {
        return receivedLastElements;
    }

    @Override
    public void OnExit()
    {
        context = null;
        adapter.clear();
        adapter.OnExit();
        ListenersWorker.Instance().unregisterListener(this);
    }

    @Override
    public void OnCommentsUpdateBegin(UUID groupId, UUID postId)
    {
        if (!this.postId.equals(postId))
            return;

        receivedLastElements = false;
        shownLastElements = false;

        progress.setVisibility(View.VISIBLE);
        progress.setIndeterminate(true);
        list.setVisibility(View.GONE);
        buttons.setVisibility(View.GONE);

        updateAdapter();
    }
    
    
    private void updateAdapter()
    {
        updateAdapter(false);
    }

    private void updateAdapter(boolean forceUpdate)
    {
        synchronized (this)
        {
            boolean addedOwnerComment = false;
            int index = -1;

            for (int pos = newComments.size() - 1; pos >= 0; pos--)
            {
                final Comment comment = (Comment) newComments.get(pos);
                if (TextUtils.isEmpty(comment.ParentPid)
                        && (!receivedLastElements || !shownLastElements || newComments
                                .isEmpty()))
                {
                    continue;
                }

                index = ServerWorker.Instance().addNewComment(groupId, postId,
                        newComments.get(pos));
                newComments.remove(pos);

                addedOwnerComment = true;
            }

            ArrayList<BaseItem> comments = ServerWorker.Instance().getComments(
                    groupId, postId);

            if (comments.size() != adapter.getCount() || forceUpdate)
            {
                adapter.updateData(comments);
                adapter.notifyDataSetChanged();
            }

            if (addedOwnerComment)
                list.setSelection(index);
        }
    }

    @Override
    public void OnCommentsUpdateFirstEntries(UUID groupId, UUID postId)
    {
        if (!this.postId.equals(postId))
            return;

        if (progress.getVisibility() == View.VISIBLE)
        {
            progress.setVisibility(View.GONE);
            progress.setIndeterminate(false);
            list.setVisibility(View.VISIBLE);
            if(navigationTurnedOn)
                buttons.setVisibility(View.VISIBLE);
        }

        updateAdapter();

    }

    @Override
    public void OnCommentsUpdateFinished(UUID groupId, UUID postId)
    {
        if (!this.postId.equals(postId))
            return;

        receivedLastElements = true;
        if (adapter.isEmpty() || waitingNextRecord)
        {
            shownLastElements = true;
            OnCommentsUpdateFirstEntries(groupId, postId);

            if (waitingNextRecord)
                goToNextNewComment();
        }
    }

    @Override
    public void OnAddedCommentUpdate(UUID id, Comment newComment)
    {
        if (!this.postId.equals(id))
            return;

        newComments.add(newComment);

        Toast.makeText(context, Utils.getString(R.string.Added_Comment),
                Toast.LENGTH_LONG).show();

        if (receivedLastElements)
            updateAdapter();
    }

    @Override
    public void OnCommentsUpdate(UUID groupId, UUID postId)
    {
        if (!this.postId.equals(postId))
            return;
        
        if(     waitingNextRecord &&
                ServerWorker.Instance().getNextNewCommentPosition(groupId, postId, list.getFirstVisiblePosition()) != -1)
        {
            updateAdapter();
            goToNextNewComment();
        }
    }

    @Override
    public void OnPostRateUpdate(UUID groupId, UUID postId, int newRating,
            boolean successful)
    {
    }
    
    private void updateCommentRating(UUID groupId, UUID postId, UUID commentId)
    {
        Comment comment = (Comment) ServerWorker.Instance().getComment(groupId, postId, commentId);
        if(comment != null)
        {
            int visiblePosition = list.getFirstVisiblePosition();
            View v = list.getChildAt(comment.Num - visiblePosition);
            TextView rating = (TextView) v.findViewById(R.id.rating);
            rating.setText(Utils.getRatingStringFromBaseItem(comment));
            Toast.makeText(context, Utils.getString(R.string.Rated_Item_Without_New_Rating), Toast.LENGTH_LONG).show(); 
        }
    }

    @Override
    public void OnCommentRateUpdate(UUID groupId, UUID postId, UUID commentId,
            boolean successful)
    {
        if (!this.groupId.equals(groupId) || !this.postId.equals(postId))
            return;
        
        if(successful)
            updateCommentRating(groupId, postId, commentId);
    }

    @Override
    public void OnAuthorRateUpdate(String userId, boolean successful)
    {  
    }
}
