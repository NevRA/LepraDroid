package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.List;

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

            final String idPattern = "userProfileHandler.userNotes.init('";
            final String html = ServerWorker.Instance().getContent(Commons.SITE_URL + "users/" + userName);
            final Document document = Jsoup.parse(html);
            
            data = ServerWorker.Instance().getAuthorByName(userName);
            if (data == null)
                data = new Author();

            data.setUserName(userName);
            final Element userPic = document.getElementsByClass("b-userpic").first();
            final Element image = userPic.getElementsByTag("img").first();
            if(image != null)
            {
                data.setImageUrl(image.attr("src"));
            }
            
            data.setRating(Short.valueOf(document.getElementsByClass("b-karma_value_inner").text()));
            int start = html.indexOf(idPattern);
            data.setId(html.substring(start + idPattern.length(), html.indexOf("')", start)));

            data.setName(document.getElementsByClass("b-user_full_name").first().text());
            data.setEgo(document.getElementsByClass("b-user_residence").first().text());

            Element userStory = document.getElementsByClass("b-user_text").first();
            if(userStory != null)
            {
                Elements images = userStory.getElementsByTag("img");
                for (Element img : images)
                {
                    String src = img.attr("src");
                    if(isImagesEnabled && !TextUtils.isEmpty(src))
                    {
                        if(!img.parent().tag().getName().equalsIgnoreCase("a"))
                            img.wrap("<a href=" + "\"" + src + "\"></a>");

                        img.removeAttr("width");
                        img.removeAttr("height");
                        img.attr("width", "100%");
                    }
                    else
                        img.remove();
                }

                data.setUserStory(Utils.wrapLepraTags(userStory));
            }
            
            if(SettingsWorker.Instance().loadUserName().equals(userName))
            {
                final Elements userStat = document.getElementsByClass("b-user_stat");
                if(userStat.size() > 1)
                {
                    final String text = userStat.get(1).ownText();
                    SettingsWorker.Instance().saveVoteWeight(Integer.valueOf(text.split(" ")[2]));
                }
            }
            else
            {
                data.setMinusVoted(html.contains("b-karma_button b-karma_button__minus b-karma_button__left_minus active"));
                data.setPlusVoted(html.contains("b-karma_button b-karma_button__plus b-karma_button__left_plus active"));
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
