package com.example.traffic;

import java.util.List;

import android.R.integer;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Util
{
	public static boolean isWifiAvailable(Context context)throws Exception
	{
		boolean isWifiAvailable = false;
		
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
							
		if(wifiNetworkInfo != null && wifiNetworkInfo.isAvailable())
		{
			if(wifiNetworkInfo.isConnected())
			{
				isWifiAvailable = true;
			}
			else
			{
				
			}
		}
		else
		{
			
		}
		
		return isWifiAvailable;
	}
	
	/*判断网络类型  * 
	public void checkNetwork()
	{
		String typeName = "";
		ConnectivityManager mConnMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if (mConnMgr != null)
		{
			NetworkInfo activeInfo = mConnMgr.getActiveNetworkInfo(); // 获取活动网络连接信息
			
			if(activeInfo != null)
			{
				typeName = activeInfo.getTypeName();
			}
			else 
			{
				typeName = "当前没有活动网络";
			}
		}											
		else 
		{
			typeName = "当前没有活动网络";
		}
		
		NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);  
		        // 创建一个Notification  
        Notification notification = new Notification();  
        // 设置显示在手机最上边的状态栏的图标  
        notification.icon = R.drawable.ic_launcher;  
        // 当当前的notification被放到状态栏上的时候，提示内容  
        notification.tickerText = "活动网络 ： " + typeName;           
        // 添加声音提示  
        notification.defaults=Notification.DEFAULT_SOUND;  
        // audioStreamType的值必须AudioManager中的值，代表着响铃的模式  
        notification.audioStreamType= android.media.AudioManager.ADJUST_LOWER;  
       
        Intent intent = new Intent(this, MainActivity.class);  
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);  
        // 点击状态栏的图标出现的提示信息设置  
        notification.setLatestEventInfo(this, "内容提示：", "查看当前活动网络", pendingIntent);  
        manager.notify(1, notification);  
	}*/
	
	public static int getUIDOfMM(Context context)
	{
		int uid = -1;
	
		try 
		{
			SQLiteDatabase db =  context.openOrCreateDatabase("test.db",  Context.MODE_PRIVATE, null);  
			Cursor cursor ;
			
			//数据库没有初始化将会有异常
			try 
			{
				cursor = db.rawQuery("SELECT * FROM traffic WHERE id = ?", new String[]{String.valueOf(1)});
				while (cursor.moveToNext()) 
				{  
					uid = cursor.getInt(cursor.getColumnIndex("uid"));			
					break;
				}
			} 
			catch (Exception e) 
			{
				throw new Exception();
			}
		}
		catch(Exception e) 
		{
			
		}
		
		return uid;
	}
	
	/** 
     * 用来判断服务是否运行. 
     * @param context 
     * @param className 判断的服务名字 
     * @return true 在运行 false 不在运行 
     */
	public static boolean isServiceRunning(Context mContext, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(30);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}
}