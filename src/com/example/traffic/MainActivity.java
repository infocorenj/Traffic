package com.example.traffic;

import java.text.DecimalFormat;
import java.util.List;

import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.TrafficStats;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private TextView tv_3g;
	private TextView tv_wifi;
	private TextView tv_uid;
	private Button bt_init;
	private Button bt_check;	
	private Button bt_uid;
	private Button bt_checkwifi;
	private int uid = 10029;//默认UID 10029
	private SharedPreferences sp;
	private Editor editor;  
	private ImageView img;
	private PackageManager pm;
	private ApplicationInfo appInfo;
	private Drawable appIcon;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tv_3g = (TextView)findViewById(R.id.tv_3g);		
		//tv_3g.setText("微信已经使用流量 ： ");
		tv_wifi = (TextView)findViewById(R.id.tv_wifi);	
		//tv_wifi.setText("");
		tv_uid = (TextView)findViewById(R.id.tv_uid);	
		tv_uid.setText("微信的UID ： ");
		//tv_uid.setMovementMethod(ScrollingMovementMethod.getInstance());
		img = (ImageView)findViewById(R.id.img);
		bt_check = (Button)findViewById(R.id.button_check);
		bt_init = (Button)findViewById(R.id.button_init);
		bt_uid = (Button)findViewById(R.id.button_getUID);
		bt_checkwifi = (Button)findViewById(R.id.button_checkwifi);
		//持久化对象
		sp = this.getSharedPreferences("SP", MODE_PRIVATE);
		editor = sp.edit();
		
		//设置图片
		pm = getPackageManager();
		try 
		{
			appInfo = pm.getApplicationInfo("com.tencent.mm", PackageManager.GET_META_DATA);
			appIcon = pm.getApplicationIcon(appInfo);
			img.setImageDrawable(appIcon);
		} 
		catch (Exception e) 
		{
			// TODO: handle exception
		}
		
		//判断并显示
		boolean isInit = sp.getBoolean("isInit", false);
		if(isInit)
		{
			updateTraffic();
			showTraffic();
			// 开启服务
			if (!Util.isServiceRunning(this,
					"com.example.traffic.trafficService")) {
				startTrafficService();
			}			
		}
		
		
		//查看微信的ＵＩＤ
		bt_uid.setOnClickListener(new View.OnClickListener() {
					
			@Override
			public void onClick(View v) 
			{						
				tv_uid.setText("微信的UID ： " + String.valueOf(getUIDOfMM()));
			}
		});
		
		//查看当前流量
		bt_check.setOnClickListener(new View.OnClickListener() {
				
			@Override
			public void onClick(View v)
			{
				boolean isInit = sp.getBoolean("isInit", false);
				if(!isInit)
				{
					Toast toast = Toast.makeText(getApplicationContext(),
						     "亲，还没初始化数据库呢！ ！！", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				else 
				{
					updateTraffic();
					showTraffic();
					Toast toast = Toast.makeText(getApplicationContext(),
						     "数据已刷新！", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}					
			}
		});
		
		//初始化数据库
		bt_init.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub	
				boolean isInit = sp.getBoolean("isInit", false);
				//判断是否已经初始化
				if(isInit)
				{
					Toast toast = Toast.makeText(getApplicationContext(),
						     "亲，已经初始化过了哦！ ！！", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				else 
				{
					initDatabase();
					editor.putBoolean("isInit", true);													
					editor.commit();											
					
					Toast toast = Toast.makeText(getApplicationContext(),
						     "初始化好啦！不要再点我了哦 ！", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();										
				}								
			}
		});	
		
		bt_checkwifi.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				//判断wifi是否可用								
				try 
				{
					boolean isWifiAvailable = Util.isWifiAvailable(MainActivity.this);	
					if(isWifiAvailable)
					{
						Toast toast = Toast.makeText(getApplicationContext(),
							     "wifi 可用！", Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();	
						
					}
					else
					{
						Toast toast = Toast.makeText(getApplicationContext(),
							     "wifi 不可用！", Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();	
												
					}
				} 
				catch (Exception e) 
				{
					// TODO: handle exception
					Toast toast = Toast.makeText(getApplicationContext(),
						     "error！", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();	
				}
			}
		});
				
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//开启定时统计服务
	public void startTrafficService()
	{
		Intent serviceIntent = new Intent(this, trafficService.class);				
		startService(serviceIntent);	
	}
	
	//获取微信的UID
	public int getUIDOfMM()
	{
		PackageManager  pm = getPackageManager();
		List<PackageInfo> packinfos = pm
                .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES
                        | PackageManager.GET_PERMISSIONS);
		
		int uidOfMM = -1;
		//String info_str = "";			
			
		for (PackageInfo info : packinfos) 
		{
			//info_str += info.packageName + " uid :" + info.applicationInfo.uid  + "\r\n";
			if(info.packageName.contains("com.tencent.mm"))
			{
				uidOfMM = info.applicationInfo.uid;
			}
		}
		
		return uidOfMM;
	}
		
	//初始化数据库
	public void initDatabase()
	{
		SQLiteDatabase db = openOrCreateDatabase("test.db", Context.MODE_PRIVATE, null);  
		db.execSQL("DROP TABLE IF EXISTS traffic");
		//创建traffic表
		db.execSQL("CREATE TABLE traffic (id INTEGER PRIMARY KEY AUTOINCREMENT,  uid INTEGER, " +
				"wifi_1 INTEGER, wifi_2 INTEGER, wifi_total INTEGER, last_total INTEGER," +
				"since_boot INTEGER, total INTEGER, flag INTEGER, shuju_traffic INTEGER )");
		//获取UID
		uid = getUIDOfMM();
		//开机以来的流量；只统计现在开始的流量，之前的不算
		long since_boot = TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);	
		//该值可能为-2，需要判断
		if(since_boot < 0 )
			since_boot = 0;
		//设置初始值
		ContentValues values = new ContentValues();	
		values.put("id", 1);
		values.put("uid", uid);
		values.put("wifi_1", since_boot);
		values.put("wifi_2", -1);
		values.put("wifi_total", since_boot);
		values.put("last_total", 0);
		values.put("since_boot", 0);
		values.put("total", 0);
		
		//判断wifi是否可用	
		try 
		{	
			boolean isWifiAlive = Util.isWifiAvailable(MainActivity.this);		
			if(isWifiAlive)
			{
				values.put("flag", 1);
			}
			else
			{
				values.put("flag", 0);
			}
		} 
		catch (Exception e) 
		{
			// TODO: handle exception
			values.put("flag", 0);
		}
					
		values.put("shuju_traffic", 0);
			
		db.insert("traffic", null, values);	
	}
							
	//更新当前数据
	public void updateTraffic()
	{				
		//获取UID
		uid = getUIDOfMM();
		//打开数据库		
		SQLiteDatabase db = openOrCreateDatabase("test.db",  Context.MODE_PRIVATE, null); 				
		
		//开机到现在的流量
		long since_boot = TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);	
		if(since_boot < 0)
			since_boot = 0;		
		
		//从数据库中读取想应数据
		long total = 0;
		long wifi_1 = 0;		
		long wifi_total = 0;
		long last_total = 0;
		long flag = -1;
		
		Cursor c = db.rawQuery("SELECT * FROM traffic WHERE uid = ?", new String[]{String.valueOf(uid)});
		while (c.moveToNext()) 
		{  
			wifi_1 = c.getInt(c.getColumnIndex("wifi_1"));			
			wifi_total = c.getInt(c.getColumnIndex("wifi_total"));
			last_total = c.getInt(c.getColumnIndex("last_total"));
			flag = c.getInt(c.getColumnIndex("flag"));
			break;
		}
				
		total = last_total + since_boot;
		
		//存入相应的值
		ContentValues cv = new ContentValues();  	
		cv.put("total", total);		
		
		//3G流量
		long shujuTraffic = 0;
		
		//如果当前wifi已关闭
		if(flag == 0)
		{
			shujuTraffic = total - wifi_total;			
		}
		else 
		{
			if(since_boot - wifi_1 < 0)
			{
				shujuTraffic = total - wifi_total;				
				cv.put("wifi_1", 0);								 
			}
			else 
			{
				shujuTraffic = total - wifi_total - (since_boot - wifi_1);
				cv.put("wifi_1", since_boot);
			}		
		}	
		
		cv.put("shuju_traffic", shujuTraffic);
		
		//wifi流量
		double wifiTraffic = (double)total - shujuTraffic;		
		
		cv.put("wifi_total", wifiTraffic);
		
		db.update("traffic", cv, "uid = ?", new String[]{String.valueOf(uid)});
	}
	
	//读取数据库，显示当前流量
	public void showTraffic()
	{
		// 获取UID
		uid = getUIDOfMM();
		// 打开数据库
		SQLiteDatabase db = openOrCreateDatabase("test.db",
				Context.MODE_PRIVATE, null);
		
		// 从数据库中读取想应数据
		long shujuTraffic = 0;
		long wifiTraffic = 0;
		
		Cursor c = db.rawQuery("SELECT * FROM traffic WHERE uid = ?",
				new String[] { String.valueOf(uid) });
		
		while (c.moveToNext())
		{
			shujuTraffic = c.getInt(c.getColumnIndex("shuju_traffic"));
			wifiTraffic = c.getInt(c.getColumnIndex("wifi_total"));			
			break;
		}
		
		tv_3g.setText(String.format("%.2f", (shujuTraffic/1024.0/1024.0)));
		tv_wifi.setText(String.format("%.2f", (wifiTraffic/1024.0/1024.0)));
	}
}
