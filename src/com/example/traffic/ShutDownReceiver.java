package com.example.traffic;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;

/**
 * 
 */
public class ShutDownReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{	
		try 
		{
			SQLiteDatabase db =  context.openOrCreateDatabase("test.db",  Context.MODE_PRIVATE, null);  
			Cursor cursor = db.rawQuery("SELECT * FROM traffic WHERE id = ?", new String[]{String.valueOf(1)});
			
			int uid = 10029;
			while (cursor.moveToNext()) 
			{  
				uid = cursor.getInt(cursor.getColumnIndex("uid"));			
				break;
			}
			
			Cursor c = db.rawQuery("SELECT * FROM traffic WHERE uid = ?", new String[]{String.valueOf(uid)});
			
			long since_boot = TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);
			if(since_boot < 0)
				since_boot = 0;
			long last_total = 0;		
			long flag = -1;
			long wifi_1 = -1;	
			long wifi_total = 0;
			
			while (c.moveToNext()) 
			{  				
				last_total = c.getInt(c.getColumnIndex("last_total"));			
				flag = c.getInt(c.getColumnIndex("flag"));
				wifi_1 = c.getInt(c.getColumnIndex("wifi_1"));
				wifi_total = c.getInt(c.getColumnIndex("wifi_total"));
				break;
			}
			
			if(flag == 0)
			{
				ContentValues cv = new ContentValues();  
				cv.put("last_total", since_boot + last_total);  
				
				db.update("traffic", cv, "uid = ?", new String[]{String.valueOf(uid)});
			}
			else
			{
				ContentValues cv = new ContentValues();  
				if(since_boot - wifi_1 > 0)
				{
					cv.put("wifi_total", wifi_total + since_boot - wifi_1);  
				}
				else 
				{
					cv.put("wifi_total", wifi_total + 0);  
				}
				cv.put("wifi_1", 0);
				cv.put("wifi_2", -1);
				cv.put("since_boot", 0);
				cv.put("last_total", since_boot + last_total); 				
				
				db.update("traffic", cv, "uid = ?", new String[]{String.valueOf(uid)});
			}
		} 
		catch (Exception e) 
		{
			
		}
	}
}
