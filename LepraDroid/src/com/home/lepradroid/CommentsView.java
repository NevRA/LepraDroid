package com.home.lepradroid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.base.BaseView;
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
    private Post            post;
    private ListView        list;
    private ProgressBar     progress;
    private ProgressBar     commentsProgress;
    private CommentsAdapter adapter;
    private LinearLayout    buttons;
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

    public CommentsView(BaseActivity context, UUID postId)
    {
        super(context);

        this.context = context;
        post = (Post)ServerWorker.Instance().getPostById(postId);

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
    }

    private void init()
    {
        list = (ListView) contentView.findViewById(R.id.list);
        commentsProgress = (ProgressBar) contentView.findViewById(R.id.commentsLoadingProgress);
        progress = (ProgressBar) contentView.findViewById(R.id.progress);
        buttons = (LinearLayout) contentView.findViewById(R.id.buttons);
        Button up = (Button) contentView.findViewById(R.id.up);
        Button down = (Button) contentView.findViewById(R.id.down);

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

        adapter = new CommentsAdapter(context, post, new ArrayList<BaseItem>());
        
        list.setScrollingCacheEnabled(false);
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
    }

    private void goToPrevNewComment()
    {
        int prevPosition = ServerWorker.Instance().getPrevNewCommentPosition(post.getId(), list.getFirstVisiblePosition());
        if (prevPosition != -1)
        {
            selectItem(prevPosition);
            waitingNextRecord = false;
        }
    }

    private void goToNextNewComment()
    {
        waitingNextRecord = false;

        final int nextPosition = ServerWorker.Instance()
                .getNextNewCommentPosition(post.getId(),
                        list.getFirstVisiblePosition());
        if (nextPosition != -1
                && list.getLastVisiblePosition() != list.getCount() - 1)
        {
            if(nextPosition >= list.getCount())
                updateAdapter();
            
            selectItem(nextPosition);
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

    @Override
    public void OnExit()
    {
        context = null;
        adapter.clear();
        adapter.OnExit();
        ListenersWorker.Instance().unregisterListener(this);
    }

    @Override
    public void OnCommentsUpdateBegin(UUID postId)
    {
        if (!this.post.getId().equals(postId))
            return;

        receivedLastElements = false;
        shownLastElements = false;

        commentsProgress.setVisibility(View.GONE);
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

    private void selectItem(final int item)
    {
        if(item >= 0)
            list.post(new Runnable()
            {
                @Override
                public void run()
                {
                    list.setSelection(item);
                }
            });
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
                if (TextUtils.isEmpty(comment.getParentLepraId())
                        && (!receivedLastElements || !shownLastElements || newComments
                                .isEmpty()))
                {
                    continue;
                }

                index = ServerWorker.Instance().addNewComment(post.getId(),
                        newComments.get(pos));
                newComments.remove(pos);

                addedOwnerComment = true;
            }

            List<BaseItem> comments = ServerWorker.Instance().getComments(post.getId());

            if (comments.size() != adapter.getCount() || forceUpdate)
            {
                adapter.updateData(comments);
                adapter.notifyDataSetChanged();
            }

            if (addedOwnerComment)
                selectItem(index);
        }
    }

    @Override
    public void OnCommentsUpdateFirstEntries(UUID postId, int count, int totalCount, int commentToSelect)
    {
        if (!post.getId().equals(postId))
            return;

        progress.setVisibility(View.GONE);
        progress.setIndeterminate(false);
        list.setVisibility(View.VISIBLE);
        if(navigationTurnedOn)
            buttons.setVisibility(View.VISIBLE);

        if(Utils.isCommentsLoadingIndicatorEnabled(LepraDroidApplication.getInstance()))
            commentsProgress.setVisibility(View.VISIBLE);

        commentsProgress.setMax(totalCount);
        commentsProgress.setProgress(count);

        updateAdapter();
        selectItem(commentToSelect);
    }

    @Override
    public void OnCommentsUpdateFinished(UUID postId, int commentToSelect)
    {
        if (!post.getId().equals(postId))
            return;

        receivedLastElements = true;

        progress.setVisibility(View.GONE);
        progress.setIndeterminate(false);
        list.setVisibility(View.VISIBLE);
        if(navigationTurnedOn)
            buttons.setVisibility(View.VISIBLE);
        commentsProgress.setVisibility(View.GONE);

        if (adapter.isEmpty() || waitingNextRecord)
        {
            shownLastElements = true;

            updateAdapter();

            if (waitingNextRecord)
                goToNextNewComment();
        }

        selectItem(commentToSelect);
    }

    @Override
    public void OnAddedCommentUpdate(UUID id, Comment newComment)
    {
        if (!post.getId().equals(id))
            return;

        newComments.add(newComment);

        Toast.makeText(context, Utils.getString(R.string.Added_Comment),
                Toast.LENGTH_LONG).show();

        if (receivedLastElements)
            updateAdapter();
    }

    @Override
    public void OnCommentsUpdate(UUID postId, int count)
    {
        if (!post.getId().equals(postId))
            return;

        commentsProgress.setProgress(count);
        
        if(     waitingNextRecord &&
                ServerWorker.Instance().getNextNewCommentPosition(postId, list.getFirstVisiblePosition()) != -1)
        {
            updateAdapter();
            goToNextNewComment();
        }
    }

    @Override
    public void OnPostRateUpdate(UUID postId, int newRating,
            boolean successful)
    {
    }
    
    private void updateCommentRating(UUID commentId)
    {
        Comment comment = (Comment) ServerWorker.Instance().getComment(post.getId(), commentId);
        if(comment != null)
        {
            int visiblePosition = list.getFirstVisiblePosition();
            View v = list.getChildAt(comment.getNum() - visiblePosition);
            TextView rating = (TextView) v.findViewById(R.id.rating);
            rating.setText(Utils.getRatingStringFromBaseItem(comment, post.getVoteWeight()));
            Toast.makeText(context, Utils.getString(R.string.Rated_Item_Without_New_Rating), Toast.LENGTH_LONG).show(); 
        }
    }

    @Override
    public void OnCommentRateUpdate(UUID postId, UUID commentId,
            boolean successful)
    {
        if (!post.getId().equals(postId))
            return;
        
        if(successful)
            updateCommentRating(commentId);
    }

    @Override
    public void OnAuthorRateUpdate(String userId, boolean successful)
    {  
    }
}