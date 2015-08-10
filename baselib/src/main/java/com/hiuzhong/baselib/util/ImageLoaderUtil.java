package com.hiuzhong.baselib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
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
			saveBitmapToFile(cnt, fullUrl, result);
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
		Bitmap result = loadFromCache(cnt, fullUrl);
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
			e.printStackTrace();
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
	public static Bitmap loadFromFile(Context cnt, String filePath,int cachMaxTime) {
		File file = new File(cnt.getFilesDir().getAbsolutePath() + "/"
				+ filePath);
		FileInputStream in=null;
		try {
			if (!file.exists() || System.currentTimeMillis() - file.lastModified() > cachMaxTime) {// 空
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

	public static Bitmap loadFromFile(Context cnt, String filePath ) {
		File file = new File(cnt.getFilesDir().getAbsolutePath() + "/"
				+ filePath);
		FileInputStream in=null;
		try {
			if (!file.exists() ) {// 空
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

	public static boolean deletImg(Context cnt,String fileName) {
		File file = new File(cnt.getFilesDir().getAbsolutePath() + "/"
				+ fileName);
		try {
			if (!file.exists() ) {// 空
				return true;
			}
			if(file.isDirectory()){
				return false;
			}
		 	return file.delete();
		} catch (Exception e) {
			Log.e("Asyload Image fail ", e.getMessage());
		}
		return false;
	}


	/**
	 * 获取圆角位图的方法
	 * @param bitmap 需要转化成圆角的位图
	 * @param pixels 圆角的度数，数值越大，圆角越大
	 * @return 处理后的圆角位图
	 */
	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}
}
