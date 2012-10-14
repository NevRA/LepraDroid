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
            String userName = SettingsWorker.Instance().loadUserName();

            JSONArray votes = new JSONObject(ServerWorker.Instance().getCommentRating(post.getLepraId(), commentId))
                    .getJSONArray("votes");

            for(int index = 0; index < votes.length(); ++index)
            {
                JSONObject vote = votes.getJSONObject(index);
                String voteUserName = vote.getString("login");
                if(userName.equals(voteUserName))
                {
                    post.setVoteWeight(vote.getInt("attitude"));
                    Logger.d("Vote weigh: " + post.getVoteWeight());

                    break;
                }
            }
        }
        catch (Exception e)
        {
            setException(e);
        }

        return e;
    }
}
