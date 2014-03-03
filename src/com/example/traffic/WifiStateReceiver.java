package com.example.traffic;

import java.util.List;

import android.R.integer;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.net.NetworkInfo;

public class WifiStateReceiver extends BroadcastReceiver 
{
	private int uid = 10029;
	@Override
	public void onReceive(Context context, Intent intent) 
	{	
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
			
			//如果网络状态发生改变，判断wifi是否可用			
			boolean isWifiAvailable = Util.isWifiAvailable(context);			
			if(isWifiAvailable)
			{
				//开启
				//记录当前uid应用的流量.
				long wifi_1 = TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);							
				if(wifi_1 <0 )
					wifi_1 = 0;
				
				ContentValues cv = new ContentValues();  
				cv.put("wifi_1", wifi_1);  
				cv.put("flag", 1);
				
				db.update("traffic", cv, "uid = ?", new String[]{String.valueOf(uid)});
			}
			else
			{
				//如果关闭
				//结余本次wifi过程中 uid应用的 流量
				long wifi_2 = TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);	
				if(wifi_2 < 0)
					wifi_2 = 0;								 				
				
				Cursor c = db.rawQuery("SELECT * FROM traffic WHERE uid = ?", new String[]{String.valueOf(uid)});
				
				long wifi_1 = 0;
				long wifi_total = 0;
				long last_total = 0;
				
				while (c.moveToNext()) 
				{  
					wifi_1 = c.getInt(c.getColumnIndex("wifi_1"));
					wifi_total = c.getInt(c.getColumnIndex("wifi_total"));
					last_total =  c.getInt(c.getColumnIndex("last_total"));
					break;
				}
				
				ContentValues cv = new ContentValues(); 
				
				if(wifi_2 - wifi_1 < 0)
				{
					cv.put("wifi_total", 0 + wifi_total);
				}
				else 
				{
					cv.put("wifi_total", wifi_2 - wifi_1 + wifi_total);
				}
				cv.put("wifi_1", 0);
				cv.put("wifi_2", -1);
				cv.put("since_boot", 0);
				cv.put("total", last_total + wifi_2);
				cv.put("flag", 0);

				db.update("traffic", cv, "uid = ?", new String[]{String.valueOf(uid)});
			}					
		} 
		catch (Exception e) 
		{
			// TODO: handle exception
			//初始化数据库
		}
    }
}
