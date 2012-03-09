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
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Comment;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.tasks.GetCommentsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;

public class CommentsView extends BaseView implements CommentsUpdateListener,
        AddedCommentUpdateListener
{
    private BaseActivity    context;
    private UUID            groupId;
    private UUID            id;
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
    private ArrayList<BaseItem> 
                            newComments 
                                    = new ArrayList<BaseItem>();

    public CommentsView(BaseActivity context, UUID groupId, UUID id)
    {
        super(context);

        this.context = context;
        this.groupId = groupId;
        this.id = id;

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
        buttons.setVisibility(navigationTurnedOn ? View.VISIBLE : View.GONE);
        adapter.setNavigationMode(navigationTurnedOn);
        adapter.notifyDataSetChanged();
    }

    private void init()
    {
        Post post = (Post) ServerWorker.Instance().getPostById(groupId, id);
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

        adapter = new CommentsAdapter(context, groupId, post.Id,
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

        if (post.TotalComments <= Commons.MAX_COMMENTS_COUNT)
        {
            context.pushNewTask(new TaskWrapper(null, new GetCommentsTask(
                    groupId, id), Utils
                    .getString(R.string.Posts_Loading_In_Progress)));
        }
        else
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
                groupId, id, list.getFirstVisiblePosition());
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
                .getNextNewCommentPosition(groupId, id,
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
            if (shownLastElements)
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
        ListenersWorker.Instance().unregisterListener(this);
    }

    @Override
    public void OnCommentsUpdateBegin(UUID groupId, UUID postId)
    {
        if (!this.id.equals(postId))
            return;

        receivedLastElements = false;
        shownLastElements = false;

        progress.setVisibility(View.VISIBLE);
        progress.setIndeterminate(true);
        list.setVisibility(View.GONE);
        buttons.setVisibility(View.GONE);

        updateAdapter();
    }

    private void updateNavigatingButtons()
    {
        if (ServerWorker.Instance().getNextNewCommentPosition(groupId, id, -1) != -1)
            buttons.setVisibility(View.VISIBLE);
        else
            buttons.setVisibility(View.GONE);
    }

    private void updateAdapter()
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

                index = ServerWorker.Instance().addNewComment(groupId, id,
                        newComments.get(pos));
                newComments.remove(pos);

                addedOwnerComment = true;
            }

            ArrayList<BaseItem> comments = ServerWorker.Instance().getComments(
                    groupId, id);

            if (comments.size() != adapter.getCount())
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
        if (!this.id.equals(postId))
            return;

        if (progress.getVisibility() == View.VISIBLE)
        {
            progress.setVisibility(View.GONE);
            progress.setIndeterminate(false);
            list.setVisibility(View.VISIBLE);
            buttons.setVisibility(View.VISIBLE);
        }

        updateAdapter();

    }

    @Override
    public void OnCommentsUpdateFinished(UUID groupId, UUID postId)
    {
        if (!this.id.equals(postId))
            return;

        receivedLastElements = true;
        if (adapter.isEmpty() || waitingNextRecord)
        {
            shownLastElements = true;
            OnCommentsUpdateFirstEntries(groupId, postId);

            if (waitingNextRecord)
                goToNextNewComment();
        }

        updateNavigatingButtons();
    }

    @Override
    public void OnAddedCommentUpdate(UUID id, Comment newComment)
    {
        if (!this.id.equals(id))
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
        if (!this.id.equals(id))
            return;
        
        if(     waitingNextRecord &&
                ServerWorker.Instance().getNextNewCommentPosition(groupId, id, list.getFirstVisiblePosition()) != -1)
        {
            updateAdapter();
            goToNextNewComment();
        }
    }
}
