/*
 * ASReferral Class
 * 
 * Copyright (C) 2013 Red Line Labs, Inc.
 * By Justin Butler (justin@redline-labs.com)
 */

package com.appspin.android.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class ASReferral {
	
	private static String APPSPIN_API_ENDPOINT = "http://api.appsp.in/";
	
	private static String APPSPIN_APP_NAME = "ExampleApp";			// YOUR APP NAME GOES HERE
	private static String APPSPIN_APP_ID = "4";						// YOUR APP ID GOES HERE (FROM APP DASHBOARD)
	private static String APPSPIN_DEVELOPER_TOKEN = Constants.key;  // YOUR DEVELOPER TOKEN GOES HERE (FROM MAIN MENU)
	
	private Activity context;
	private String campaignInfoURL;
	
	// Constructor which accepts activity context of creator 
	public ASReferral (Activity activity) {
		context = activity;
	}
	
    public void checkForOffers() {
    	if (context != null)  {
    		// async check for campaign data
    		new AppSpinTask().execute(new String[] {
    				APPSPIN_APP_ID, 
    				APPSPIN_DEVELOPER_TOKEN
    		});
    	}
    	
    	return;
    }
	
    /*
     * This simple example uses a system dialog to present the offer
     * Modify to display a modal container with a more stylized / branded appearance 
     */
	public void presentOffer() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle("Earn rewards!");
 
		String message = "Would you like to earn rewards by sharing "; // YOUR OFFER MESSAGE GOES HERE
		message += APPSPIN_APP_NAME + "?";
		
		alertDialogBuilder
			.setMessage(message)
			.setCancelable(false)
			.setPositiveButton("Yes, please!", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					/*
					 * If your app already has the user's contact info, you can generate affiliate
					 * links on the fly via the API's "generate" endpoint
					 * TODO: link to API docs
					 */
					launchReward();
				}
			})
			.setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
			    	context = null;
				}
			})
			.setNeutralButton("Learn more", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					launchReward();
				}
			});
			
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
				
		return;
	}
	
    private void launchReward() {
    	if (campaignInfoURL != null) {
    		/*
    		 * Launch browser to display campaign details
    		 * 
    		 * If you want to display the details natively, you make make
    		 * a request for JSON data via the API's "info" endpoint
    		 * TODO: link to API docs
    		 */
    		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(campaignInfoURL));
    		context.startActivity(browserIntent);
    	}
    	
    	// drop reference to help with GC
    	context = null;
    	
    	return;
    }
    
	private class AppSpinTask extends AsyncTask<String, Void, String> {
    	@Override
        protected String doInBackground(String... param) {
        	String result;
            HttpClient httpclient = new DefaultHttpClient();
            
            String url = APPSPIN_API_ENDPOINT + "campaigns?";
            
            url += "app_id=" + param[0] + "&";
            url += "token=" + param[1];

        	HttpGet get = new HttpGet(url);

        	try {
                HttpResponse response = httpclient.execute(get);
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    String responseString = out.toString();
                    result = responseString;
            	} else {
            		response.getEntity().getContent().close();
            		throw new IOException(statusLine.getReasonPhrase());
            	}
        	} catch (IOException e) { 
        		e.printStackTrace();
        		result = "{\"error\":\"Error: Server data is not available\"}";
            }
        	
            return result;
        }
    	@Override
        protected void onPostExecute(String result) {
            requestStateChanged(result);
        }
    }
	
	private void requestStateChanged(String result) {
    	String error = null;
    	String url = null;

		try {
			JSONObject json = new JSONObject(result);
			if (json.has("error")) {
				error = json.getString("error");
			} else {
				JSONObject data = json.getJSONObject("data");
				JSONArray campaigns = data.getJSONArray("campaigns");
				if (campaigns.length() > 0) {
					url = campaigns.getJSONObject(0).getString("info_link");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			error = "Error: Server data has invalid format";
		}
    	
    	if (error!=null) {
    		Log.e("AppSpin", error);
    		// Toast.makeText(context.getApplicationContext(), error, Toast.LENGTH_SHORT).show();
    		context = null;
    	} else {
    		if (url!=null) {
    			if (!url.startsWith("http://") && !url.startsWith("https://")) {
        			url = "http://" + url;
        		}
    			
    			campaignInfoURL = url;
    			presentOffer();
    		} else {
    			String message = "Sorry! There are currently no offers available.";
    			Log.i("AppSpin", message);
    			// Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    			context = null;
    		}
    	}
    	    	
    	return;
    }
}
