package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import com.home.lepradroid.utils.Utils;
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
    
    private String  userName;
    private boolean withoutBeginNotify  = false;
    private boolean isImagesEnabled     = true;
    
    public GetAuthorTask(String userName, boolean withoutBeginNotify)
    {
        this.withoutBeginNotify = withoutBeginNotify;
        this.userName = userName;
        this.isImagesEnabled = Utils.isImagesEnabled();
    }
    
    public GetAuthorTask(String userName)
    {
        this.userName = userName;
        this.isImagesEnabled = Utils.isImagesEnabled();
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
        Author data;
        try
        {
            if(!withoutBeginNotify)
                notifyAboutAuthorUpdateBegin();
            
            final String html = ServerWorker.Instance().getContent(Commons.SITE_URL + "users/" + userName);
            final Document document = Jsoup.parse(html);
            
            data = ServerWorker.Instance().getAuthorByName(userName);
            if (data == null)
                data = new Author();


            data.setUserName(userName);
            final Element userPic = document.getElementsByClass("userpic").first();
            final Element image = userPic.getElementsByTag("img").first();
            if(image != null)
            {
                data.setImageUrl(image.attr("src"));
            }
            
            final Element userInfo = document.getElementsByClass("userbasicinfo").first();
            data.setId(userInfo.getElementsByClass("vote").first().attr("uid"));
            data.setName(userInfo.getElementsByTag("h3").first().text());

            data.setEgo(document.getElementsByClass("userego").first().text());

            Element userStory = document.getElementsByClass("userstory").first();
            if(userStory != null)
            {
                Elements images = userStory.getElementsByTag("img");
                int imageNum = 0;
                List<Pair<String, String>> imgs = new ArrayList<Pair<String, String>>();
                for (Element img : images)
                {
                    String src = img.attr("src");
                    if(isImagesEnabled && !TextUtils.isEmpty(src))
                    {
                        String id = "img" + Integer.valueOf(imageNum).toString();

                        if(!img.parent().tag().getName().equalsIgnoreCase("a"))
                            img.wrap("<a href=" + "\"" + src + "\"></a>");

                        img.removeAttr("width");
                        img.removeAttr("height");
                        img.removeAttr("src");
                        img.removeAttr("id");

                        img.attributes().put("id", id);
                        img.attributes().put("src", Commons.IMAGE_STUB);
                        img.attributes().put("onLoad", "getSrcData(\"" + id + "\", \"" + src + "\", " + Integer.valueOf(0).toString() + ");");
                        imgs.add(new Pair<String, String>(id, src));

                        imageNum++;
                    }
                    else
                        img.remove();
                }

                data.setUserStory(Utils.getImagesStub(imgs, 0) + Utils.wrapLepraTags(userStory));
            }
            
            Element vote;
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
            {
                vote  = document.getElementById("js-user_karma");
                
                Element vote1 = userInfo.getElementsByClass("vote1").first();
                Element vote2 = userInfo.getElementsByClass("vote2").first();

                data.setMinusVoted(vote1.getElementsByTag("a").last().attr("class").equals("minus voted") && vote2.getElementsByTag("a").last().attr("class").equals("minus voted"));
                data.setPlusVoted(vote1.getElementsByTag("a").first().attr("class").equals("plus voted") && vote2.getElementsByTag("a").first().attr("class").equals("plus voted"));
                if (TextUtils.isEmpty(SettingsWorker.Instance().loadVoteKarmaWtf()))
                {
                    Element script = userInfo.getElementsByTag("script").first();
                    Pattern pattern = Pattern.compile("VoteBlockUser.wtf = \"(.+)\"");
                    Matcher matcher = pattern.matcher(script.data());
                    if(matcher.find()){
                        SettingsWorker.Instance().saveVoteKarmaWtf(matcher.group(1));
                    }
                }
            }
            
            data.setRating(Short.valueOf(vote.getElementsByTag("em").text()));

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
