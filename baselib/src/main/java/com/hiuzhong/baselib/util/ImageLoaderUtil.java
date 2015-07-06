package com.hiuzhong.baselib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class ImageLoaderUtil {
	/**
	 * 缓存最大生存时间默认一天
	 */
	public static final long MAX_CACHE_TIME =24 * 3600 * 1000L;

	/**
	 * 加载图片数据如果本地有缓存从缓存取否则从网络取,取完之后同步写入到缓存中(文件系统)
	 * @param cnt
	 * @param fullUrl
	 * @return
	 */
	public static Bitmap laodImageWithCache(Context cnt,String fullUrl){
		Bitmap result = loadFromCache(cnt,fullUrl);
		if(result == null){
			result =WebUtil.getImageFromUrl(fullUrl);
			saveBitmapToFile(cnt,fullUrl,result);
		}
		return result;
	}

	/**
	 * 加载图片数据如果本地有缓存从缓存取否则从网络取,取完之后异步写入到缓存中(文件系统)
	 * @param cnt
	 * @param fullUrl
	 * @return
	 */
	public static Bitmap laodImageWithCacheAsySave(final Context cnt,final String fullUrl){
		Bitmap result = loadFromCache(cnt,fullUrl);
		if(result == null){
			result =WebUtil.getImageFromUrl(fullUrl);
			new AsyncTask<Bitmap, Void, Void>() {
				@Override
				protected Void doInBackground(Bitmap... params) {
					saveBitmapToFile(cnt, fullUrl, params[0]);
					return null;
				}
			}.execute(result);
		}
		return result;
	}



	public static void saveBitmapToFile(Context cnt, String url, Bitmap fileMap) {
		String fileName = url.substring(url.lastIndexOf('/') + 1);
		FileOutputStream out;
		try {
			out = new FileOutputStream(cnt.getFileStreamPath(fileName));
			fileMap.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
		} catch (Exception e) {
		}

	}


	public static Bitmap loadFromCache(Context cnt, String url) {
		String fileName = url.substring(url.lastIndexOf('/') + 1);
		File file = new File(cnt.getFilesDir().getAbsolutePath() + "/"
				+ fileName);
		try {
			if (!file.exists() || System.currentTimeMillis() - file.lastModified() > MAX_CACHE_TIME) {// 空
				return null;
			}
			return BitmapFactory.decodeStream(new FileInputStream(file));
		} catch (Exception e) {
			Log.e("Asyload Image fail ", e.getMessage());
		}
		return null;
	}
	public static Bitmap loadFromFile(Context cnt, String filePath) {
		File file = new File(cnt.getFilesDir().getAbsolutePath() + "/"
				+ filePath);
		FileInputStream in=null;
		try {
			if (!file.exists() || System.currentTimeMillis() - file.lastModified() > MAX_CACHE_TIME) {// 空
				return null;
			}
			in = new FileInputStream(file);
				return BitmapFactory.decodeStream(new FileInputStream(file));
		} catch (Exception e) {
			Log.e("Asyload Image fail ", e.getMessage());
		} finally {
			if(in != null){
				try {
					in.close();
				} catch (IOException e) { 	}
			}
		}
		return null;
	}

	public static String parseFilePath(String fullUrl) {
		return null;
	}
}
