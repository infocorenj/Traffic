package com.example.traffic;

import java.text.DecimalFormat;
import java.util.List;

import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.TrafficStats;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private TextView tv;
	private TextView tv_uid;
	private Button bt_init;
	private Button bt_check;	
	private Button bt_uid;
	private int uid = 10029;//默认UID 10029
	private SharedPreferences sp;
	private Editor editor;  
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tv = (TextView)findViewById(R.id.tv);		
		tv.setText("微信已经使用流量 ： ");
		tv_uid = (TextView)findViewById(R.id.tv_uid);	
		tv_uid.setText("微信的UID ： ");
		tv_uid.setMovementMethod(ScrollingMovementMethod.getInstance());
		bt_check = (Button)findViewById(R.id.button_check);
		bt_init = (Button)findViewById(R.id.button_init);
		bt_uid = (Button)findViewById(R.id.button_getUID);
		sp = this.getSharedPreferences("SP", MODE_PRIVATE);
		editor = sp.edit();
		
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
				checkTraffic(v);					
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
				
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
				"since_boot INTEGER, total INTEGER, flag INTEGER )");
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
		values.put("flag", 0);
		
		db.insert("traffic", null, values);	
	}
	
	//点击查看流量；相当于点击排行版时计算并显示
	public void checkTraffic(View v)
	{
		//获取UID
		uid = getUIDOfMM();
				
		SQLiteDatabase db = openOrCreateDatabase("test.db",  Context.MODE_PRIVATE, null); 
		
		/*可能关机时wifi开着，这样开机后wifi也就开着，那么receiver就无法记录相应wifi打开时的wifi_1,所以在关机时
		      用这个值保存wifi状态*/
		long since_boot_early = 0;
		
		Cursor cur = db.rawQuery("SELECT * FROM traffic WHERE uid = ?", new String[]{String.valueOf(uid)});		
		while (cur.moveToNext()) 
		{  
			since_boot_early = cur.getInt(cur.getColumnIndex("since_boot"));
		}
		
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
		
		//计算到check为止总的流量
		total = last_total + since_boot;
		
		ContentValues cv = new ContentValues();  	
		cv.put("total", total);
		//更新总的流量
		db.update("traffic", cv, "uid= ?", new String[]{String.valueOf(uid)}); 
		
		//3G流量
		long shujuTraffic = 0;
		
		//如果当前wifi已关闭
		if(flag == 0 && since_boot_early != -1)
		{
			shujuTraffic = total - wifi_total;
			/*Toast toast = Toast.makeText(getApplicationContext(),
				     "WIFI 已关闭！", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();*/
		}
		else 
		{
			if(since_boot - wifi_1 < 0)
			{
				shujuTraffic = total - wifi_total;
				cv = new ContentValues();
				cv.put("wifi_1", 0);
				
				db.update("traffic", cv, "uid= ?", new String[]{String.valueOf(uid)}); 
			}
			else 
			{
				shujuTraffic = total - wifi_total - (since_boot - wifi_1);
			}		
		}	
		
		//wifi流量
		double wifiTraffic = (double)total - shujuTraffic;
		
		tv.setText("微信已经使用3G流量 ： "+String.valueOf(shujuTraffic/1024) + "KB | "  + 
						String.format("%.2f", (shujuTraffic/1024.0/1024.0))+ "MB \r\n" +
						"微信已经使用wifi流量 ： " + String.valueOf((total-shujuTraffic)/1024) + "KB  | "  +
						String.format("%.2f", (wifiTraffic/1024.0/1024.0))+ "MB");
	}
}
