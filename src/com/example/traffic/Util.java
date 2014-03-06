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
	
	/*�ж���������  * 
	public void checkNetwork()
	{
		String typeName = "";
		ConnectivityManager mConnMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if (mConnMgr != null)
		{
			NetworkInfo activeInfo = mConnMgr.getActiveNetworkInfo(); // ��ȡ�����������Ϣ
			
			if(activeInfo != null)
			{
				typeName = activeInfo.getTypeName();
			}
			else 
			{
				typeName = "��ǰû�л����";
			}
		}											
		else 
		{
			typeName = "��ǰû�л����";
		}
		
		NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);  
		        // ����һ��Notification  
        Notification notification = new Notification();  
        // ������ʾ���ֻ����ϱߵ�״̬����ͼ��  
        notification.icon = R.drawable.ic_launcher;  
        // ����ǰ��notification���ŵ�״̬���ϵ�ʱ����ʾ����  
        notification.tickerText = "����� �� " + typeName;           
        // ���������ʾ  
        notification.defaults=Notification.DEFAULT_SOUND;  
        // audioStreamType��ֵ����AudioManager�е�ֵ�������������ģʽ  
        notification.audioStreamType= android.media.AudioManager.ADJUST_LOWER;  
       
        Intent intent = new Intent(this, MainActivity.class);  
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);  
        // ���״̬����ͼ����ֵ���ʾ��Ϣ����  
        notification.setLatestEventInfo(this, "������ʾ��", "�鿴��ǰ�����", pendingIntent);  
        manager.notify(1, notification);  
	}*/
	
	public static int getUIDOfMM(Context context)
	{
		int uid = -1;
	
		try 
		{
			SQLiteDatabase db =  context.openOrCreateDatabase("test.db",  Context.MODE_PRIVATE, null);  
			Cursor cursor ;
			
			//���ݿ�û�г�ʼ���������쳣
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
     * �����жϷ����Ƿ�����. 
     * @param context 
     * @param className �жϵķ������� 
     * @return true ������ false �������� 
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