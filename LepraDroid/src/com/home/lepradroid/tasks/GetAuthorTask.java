package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Pair;

import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.AuthorUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Author;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.utils.Logger;

public class GetAuthorTask extends BaseTask
{
    static final Class<?>[] argsClassesOnAuthorUpdate = new Class[2];
    static final Class<?>[] argsClassesOnAuthorUpdateBegin = new Class[1];
    static Method methodOnAuthorUpdate;
    static Method methodOnAuthorUpdateBegin;
    static 
    {
        try
        {
            argsClassesOnAuthorUpdate[0] = String.class;
            argsClassesOnAuthorUpdate[1] = Author.class;
            methodOnAuthorUpdate = AuthorUpdateListener.class.getMethod("OnAuthorUpdate", argsClassesOnAuthorUpdate); 
            
            argsClassesOnAuthorUpdateBegin[0] = String.class;
            methodOnAuthorUpdateBegin = AuthorUpdateListener.class.getMethod("OnAuthorUpdateBegin", argsClassesOnAuthorUpdateBegin); 
        }
        catch (Throwable t) 
        {           
            Logger.e(t);
        }        
    }
    
    private String userName;
    
    public GetAuthorTask(String userName)
    {
        this.userName = userName;
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutAuthorUpdateBegin()
    {
        final List<AuthorUpdateListener> listeners = ListenersWorker.Instance().getListeners(AuthorUpdateListener.class);
        final Object args[] = new Object[1];
        args[0] = userName;
        
        for(AuthorUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnAuthorUpdateBegin, args)));
        }
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutAuthorUpdate(Author data)
    {
        final List<AuthorUpdateListener> listeners = ListenersWorker.Instance().getListeners(AuthorUpdateListener.class);
        final Object args[] = new Object[2];
        args[0] = userName;
        args[1] = data;
        
        for(AuthorUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnAuthorUpdate, args)));
        }
    }
    
    @Override
    protected Throwable doInBackground(Void... params)
    {
        Author data = null;
        try
        {
            notifyAboutAuthorUpdateBegin();
            
            final String html = ServerWorker.Instance().getContent(Commons.SITE_URL + "users/" + userName);
            final Document document = Jsoup.parse(html);
            
            data = new Author();
            
            final Element userPic = document.getElementsByClass("userpic").first();
            final Elements images = userPic.getElementsByTag("img");
            if(!images.isEmpty())
            {
                data.ImageUrl = images.first().attr("src");
            }
            
            final Element userInfo = document.getElementsByClass("userbasicinfo").first();
            data.Id = userInfo.getElementsByClass("vote").first().attr("uid");
            data.Name = userInfo.getElementsByTag("h3").first().text();
            
            data.Ego = document.getElementsByClass("userego").first().text();
            
            Element vote = null;
            if(SettingsWorker.Instance().loadUserName().equals(userName))
            {
                vote = document.getElementById("uservote");
                final Elements userStat = document.getElementsByClass("userstat");
                if(userStat.size() > 1)
                {
                    final String text = userStat.get(1).ownText();
                    SettingsWorker.Instance().saveVoteWeight(Integer.valueOf(text.split(" ")[2]));
                }
            }
            else
                vote  = document.getElementById("js-user_karma");
            
            data.Rating = Integer.valueOf(vote.getElementsByTag("em").text());

            System.out.println("data.Rating = " + data.Rating);

            Element vote1 = userInfo.getElementsByClass("vote1").first();
            Element vote2 = userInfo.getElementsByClass("vote2").first();

            data.MinusVoted = vote1.text().contains("class=\"minus voted\"") &&  vote2.text().contains("class=\"minus voted\"");
            data.PlusVoted  = vote1.text().contains("class=\"plus voted\"") &&  vote2.text().contains("class=\"plus voted\"");

            if (TextUtils.isEmpty(SettingsWorker.Instance().loadVoteKarmaWtf()))
            {
                Element script = userInfo.getElementsByTag("script").first();
                Pattern pattern = Pattern.compile("VoteBlockUser.wtf = \"(.+)\"");
                Matcher matcher = pattern.matcher(script.data());
                if(matcher.find()){
                    SettingsWorker.Instance().saveVoteKarmaWtf(matcher.group(1));
                }
            }


            ServerWorker.Instance().addNewAuthor(data);


            notifyAboutAuthorUpdate(data);
        }
        catch (Exception e)
        {
            setException(e);
        }
        finally
        {
            notifyAboutAuthorUpdate(null);
        }
        
        return e;
    }
}
