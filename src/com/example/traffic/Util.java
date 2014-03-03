package com.example.traffic;

import android.content.Context;
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
}