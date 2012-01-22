package com.home.lepradroid.serverworker;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.graphics.drawable.Drawable;

public class ServerWorker
{
    private static volatile ServerWorker instance;
    private ClientConnectionManager connectionManager;
    private HttpParams connectionParameters;
    private ServerWorker() 
    {
        init();
    }

    public static ServerWorker Instance()
    {
        if(instance == null)
        {
            synchronized (ServerWorker.class)
            {
                if(instance == null)
                {
                    instance = new ServerWorker();
                }
            }
        }
        
        return instance;
    }
    
    private void init()
    {
        connectionParameters = new BasicHttpParams();
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        connectionManager = new ThreadSafeClientConnManager(connectionParameters, registry);
    }
    
    public String getContent(String url) throws ClientProtocolException, IOException
    {
        final HttpGet httpGet = new HttpGet(url);
        final HttpClient client = new DefaultHttpClient(connectionManager, connectionParameters);
        
        final HttpResponse response = client.execute(httpGet);
        
        return EntityUtils.toString(response.getEntity());
    }
    
    public Drawable getImage(String url) throws ClientProtocolException, IOException
    {
        final HttpGet httpGet = new HttpGet(url);
        final HttpClient client = new DefaultHttpClient(connectionManager, connectionParameters);
        
        final HttpResponse response = client.execute(httpGet);
        
        final byte[] array = EntityUtils.toByteArray(response.getEntity());
  
        return Drawable.createFromStream(new ByteArrayInputStream(array), "src");
    }
}
