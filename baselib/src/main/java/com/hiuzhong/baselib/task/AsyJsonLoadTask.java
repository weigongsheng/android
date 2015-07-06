package com.hiuzhong.baselib.task;

import org.json.JSONObject;

import android.os.AsyncTask;

import com.hiuzhong.baselib.activity.JsonProcessor;
import com.hiuzhong.baselib.util.WebUtil;

public class AsyJsonLoadTask extends AsyncTask<String, Integer, JSONObject> {
	public JsonProcessor processAct;
	public AsyJsonLoadTask(){
		
	}
	public AsyJsonLoadTask(JsonProcessor processAct){
		this.processAct = processAct;
	}
	
	@Override
	protected JSONObject doInBackground(String... params) {
		 return WebUtil.getJSonFromUrl(params[0], "GET", "UTF-8");
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		if(result == null ||result.toString().equals("{}")){
			return ;
		}
		  processAct.processJsonData(result);
	}
	
	

}
