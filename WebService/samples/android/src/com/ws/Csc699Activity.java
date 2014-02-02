package com.ws;

import java.io.IOException;
import java.io.InputStream;
//import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
//import android.widget.Button;
//import android.widget.Button;
import android.widget.EditText;

public class Csc699Activity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
       /* final Button btnGetUsers = (Button) findViewById(R.id.btnGetUsers);
        btnGetUsers.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		System.out.println("onclicklistener hit");
        	}
        });*/
    }
    
    /**
     * Get a list of the users as XML.
     * @param view
     */
    public void getUsers(View view) {
        // "/share-space/users" GET
    	System.out.println("getUsers hit");
    	
    	new WSGetRequest().execute("http://10.0.2.2:8080/share-space/users");
    }
    
    /**
     * Add a user to the web service.
     * @param view
     */
    public void addUser(View view) {
        // "/share-space/users" POST
    	System.out.println("addUser hit");
    	
    	new WSPostRequest().execute("http://10.0.2.2:8080/share-space/users");
    }
    
    /**
     * Get a list of the files as XML.
     * @param view
     */
    public void getFiles(View view) {
        // "/share-space/files" GET
    	System.out.println("getFiles hit");
    	
    	new WSGetRequest().execute("http://10.0.2.2:8080/share-space/files");
    }
    
    /**
     * Perform an HTTP GET Request to a resource and display results.
     * @author mod
     *
     */
    private class WSGetRequest extends AsyncTask <String, Void, String> {
    	
    	/**
    	 * Convert the HttpEntity to string output.
    	 * @param entity
    	 * @return
    	 * @throws IllegalStateException
    	 * @throws IOException
    	 */
    	protected String getASCIIContentFromEntity(HttpEntity entity) 
    			throws IllegalStateException, IOException {
    		InputStream in = entity.getContent();
    		StringBuffer out = new StringBuffer();
    		
    		int n = 1;    		
    		while (n>0) {
    			byte[] b = new byte[4096];
    			n =  in.read(b);
    		
    			if (n>0) { out.append(new String(b, 0, n)); }
    		}
    		return out.toString();
    	}
    	
    	/**
    	 * Make the HTTP request to the resource.
    	 */
    	@Override
    	protected String doInBackground(String... urls) {
    		System.out.println("doInBackground hit");
    		
    		HttpClient httpClient = new DefaultHttpClient();
    		HttpContext localContext = new BasicHttpContext();
    		HttpGet httpGet = new HttpGet(urls[0]);
    		
    		httpGet.addHeader("Authorization", "Basic admin:password");
    		httpGet.addHeader("Content-Type", "application/xml");
    		String text = null;
    			
    		try {
    			HttpResponse response = httpClient.execute(httpGet, localContext);
    			HttpEntity entity = response.getEntity();
    			text = getASCIIContentFromEntity(entity);
    		} catch (Exception e) {
    			return e.getLocalizedMessage();
    		}
    		return text;
    	}
    		
    	/**
    	 * Display the results of the HTTP request in the EditText.	
    	 */
    	protected void onPostExecute(String results) {
    		if (results!=null) {
    		EditText et = (EditText)findViewById(R.id.getResponse);
    		et.setText(results);
    			System.out.println("results: " + results);   			
    		} else {
    			System.out.println("results were null");
    		}
    	}
    }
    
    /**
     * Perform an HTTP POST Request to a resource and display results.
     * @author mod
     *
     */
    private class WSPostRequest extends AsyncTask <String, Void, String> {
    	
    	/**
    	 * Convert the HttpEntity to string output.
    	 * @param entity
    	 * @return
    	 * @throws IllegalStateException
    	 * @throws IOException
    	 */
    	protected String getASCIIContentFromEntity(HttpEntity entity) 
    			throws IllegalStateException, IOException {
    		InputStream in = entity.getContent();
    		StringBuffer out = new StringBuffer();
    		
    		int n = 1;    		
    		while (n>0) {
    			byte[] b = new byte[4096];
    			n =  in.read(b);
    		
    			if (n>0) { out.append(new String(b, 0, n)); }
    		}
    		return out.toString();
    	}
    	
    	/**
    	 * Make the HTTP request to the resource.
    	 */
    	@Override
    	protected String doInBackground(String... params) {
    		System.out.println("doInBackground hit");
    		
    		HttpClient httpClient = new DefaultHttpClient();
    		HttpContext localContext = new BasicHttpContext();
    		//HttpGet httpGet = new HttpGet(urls[0]);
    		HttpPost httpPost = new HttpPost(params[0]);
    		FileBody bin = new FileBody(file); 
    		
    		httpPost.addHeader("Authorization", "Basic admin:password");
    		//httpPost.addHeader("Content-Type", "application/xml");
    		
    		String text = null;
    			
    		try {
    			HttpResponse response = httpClient.execute(httpPost, localContext);
    			HttpEntity entity = response.getEntity();
    			text = getASCIIContentFromEntity(entity);
    		} catch (Exception e) {
    			return e.getLocalizedMessage();
    		}
    		return text;
    	}
    		
    	/**
    	 * Display the results of the HTTP request in the EditText.	
    	 */
    	protected void onPostExecute(String results) {
    		if (results!=null) {
    		EditText et = (EditText)findViewById(R.id.getResponse);
    		et.setText(results);
    			System.out.println("results: " + results);   			
    		} else {
    			System.out.println("results were null");
    		}
    	}
    }
}