package com.hiuzhong.baselib.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class WebUtil {
	public static final int TIMEOUT_NET_READ= 10000;
	public static final int TIMEOUT_NET_CONNECT=10000;

	public static interface ImageLoadCallback{
		public void imageLoad(Bitmap map, String url);
	}
	public static class ImageLoadPositonCallback{
		public Handler handler;
		public int msgWhat;
		public ImageLoadPositonCallback(Handler handler,int what){
			this.handler = handler;
			this.msgWhat = what;
		}
		public void imageLoad(Bitmap map,int position,String url){
			Message ms = new Message();
			ms.what=msgWhat;
			Bundle b =new Bundle();
			b.putParcelable("img", map);
			b.putInt("p", position);
			ms.setData(b);
			handler.sendMessage(ms);
		}
	} 
	
	public static Bitmap getImageFromUrl(String url){
		InputStream in =null;
		try {
			URL u = new URL(url);
			in = u.openStream();
			Bitmap bm = BitmapFactory.decodeStream(in);
			return bm;
		} catch ( Exception e) {
			e.printStackTrace();
		}finally{
			if(in!= null){
				try {
					in.close();
				} catch (IOException e) { }
			}
		}
		return null;
	}
	
	public static void navigateToWebView(Context context,Class targetWebViewClass, String url){
		Intent intents = new Intent(context,targetWebViewClass);
		intents.putExtra("url", url);
		context.startActivity(intents);
	}
	
	public static String redTextFromUrl(String url,String method ,String charSet){
		InputStream in=null;
		try {
			URL u = new URL(url);
			HttpURLConnection con = (HttpURLConnection) u.openConnection();
			con.setRequestMethod(method);
			con.setDoInput(true);
			con.setRequestProperty("Cahrset", charSet);
			con.setRequestProperty("Content-type", "text/html");
			con.setReadTimeout(TIMEOUT_NET_READ);
			con.setConnectTimeout(TIMEOUT_NET_CONNECT);
			in =con.getInputStream();
			BufferedReader buf = new BufferedReader( new InputStreamReader(in, charSet) );
			 String line = null;
		        StringBuilder sb = new StringBuilder();
		        while ((line = buf.readLine()) != null) {
		            sb.append(line);
		            sb.append("\n");
		        }
			return sb.toString();
		} catch (IOException e) {
			 
		}finally{
			if(in != null){
				try {
					in.close();
				} catch (IOException e) { }
			}
		}
		return null;
	}
	
	public static JSONObject getJSonFromUrl(String url,String method,String charSet){
		String jsonText = redTextFromUrl(url, method, charSet);
		if(jsonText != null){
			JSONTokener jsonParser = new JSONTokener(jsonText);  
			try {
				return  (JSONObject) jsonParser.nextValue();
			} catch (JSONException e) {
				 
			}  
		}
		return null;
	}
	
	public static boolean isNetworkConnected(Context context) {  
		       if (context != null) {  
		         ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
		                  .getSystemService(Context.CONNECTIVITY_SERVICE);  
		          NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
		          if (mNetworkInfo != null) {  
		              return mNetworkInfo.isAvailable();  
		          }  
		      }  
		      return false;  
		  }  
	
	public static void asyncImageLoade(final String url,final ImageLoadCallback callBack){
		new Thread(){

			@Override
			public void run() {
				callBack.imageLoad(getImageFromUrl(url),url);
			}
			
		}.start();
	}
	public static void asyncImageLoade(final String url,final int position,final ImageLoadPositonCallback callBack){
		new Thread(){
			
			@Override
			public void run() {
				callBack.imageLoad(getImageFromUrl(url),position,url);
			}
			
		}.start();
	}

	public static void toWebView(Context cnt,Class targetWebClass ,String url){
		Intent intent = new Intent(cnt, targetWebClass);
		intent.putExtra("url", url);
		cnt.startActivity(intent);
	}
	


}
