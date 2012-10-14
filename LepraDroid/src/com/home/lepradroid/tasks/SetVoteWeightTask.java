package com.home.lepradroid.tasks;

import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

public class SetVoteWeightTask extends BaseTask
{
    private Post    post;
    private String  commentId;

    public SetVoteWeightTask(UUID postId)
    {
        post = (Post)ServerWorker.Instance().getPostById(postId);
    }

    public SetVoteWeightTask(UUID postId, String commentId)
    {
        post = (Post)ServerWorker.Instance().getPostById(postId);
        this.commentId = commentId;
    }

    @Override
    protected Throwable doInBackground(Void... params)
    {
        try
        {
            Integer weight = ServerWorker.Instance().getBlogVoteWeight(post.getGroupId());
            if(weight == null)
            {
                JSONArray votes;

                if(commentId != null)
                {
                    votes = new JSONObject(ServerWorker.Instance().getCommentRating(post.getLepraId(), commentId))
                            .getJSONArray("votes");
                }
                else
                {
                    votes = new JSONObject(ServerWorker.Instance().getPostRating(post.getLepraId()))
                            .getJSONArray("votes");
                }

                String userName = SettingsWorker.Instance().loadUserName();

                for(int index = 0; index < votes.length(); ++index)
                {
                    JSONObject vote = votes.getJSONObject(index);
                    String voteUserName = vote.getString("login");
                    if(userName.equals(voteUserName))
                    {
                        weight = vote.getInt("attitude");
                        ServerWorker.Instance().addBlogVoteWeight(post.getGroupId(),weight);

                        break;
                    }
                }
            }

            if(weight != null)
            {
                post.setVoteWeight(weight);
                Logger.d("Vote weigh: " + post.getVoteWeight());
            }
        }
        catch (Exception e)
        {
            setException(e);
        }

        return e;
    }
}
