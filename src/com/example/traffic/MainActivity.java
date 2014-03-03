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
	private int uid = 10029;//Ĭ��UID 10029
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
		//tv_3g.setText("΢���Ѿ�ʹ������ �� ");
		tv_wifi = (TextView)findViewById(R.id.tv_wifi);	
		//tv_wifi.setText("");
		tv_uid = (TextView)findViewById(R.id.tv_uid);	
		tv_uid.setText("΢�ŵ�UID �� ");
		//tv_uid.setMovementMethod(ScrollingMovementMethod.getInstance());
		img = (ImageView)findViewById(R.id.img);
		bt_check = (Button)findViewById(R.id.button_check);
		bt_init = (Button)findViewById(R.id.button_init);
		bt_uid = (Button)findViewById(R.id.button_getUID);
		bt_checkwifi = (Button)findViewById(R.id.button_checkwifi);
		//�־û�����
		sp = this.getSharedPreferences("SP", MODE_PRIVATE);
		editor = sp.edit();
		
		//����ͼƬ
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
		
		//�жϲ���ʾ
		boolean isInit = sp.getBoolean("isInit", false);
		if(isInit)
		{
			checkTraffic(null);	
		}
			
		//�鿴΢�ŵģգɣ�
		bt_uid.setOnClickListener(new View.OnClickListener() {
					
			@Override
			public void onClick(View v) 
			{						
				tv_uid.setText("΢�ŵ�UID �� " + String.valueOf(getUIDOfMM()));
			}
		});
		
		//�鿴��ǰ����
		bt_check.setOnClickListener(new View.OnClickListener() {
				
			@Override
			public void onClick(View v)
			{
				boolean isInit = sp.getBoolean("isInit", false);
				if(!isInit)
				{
					Toast toast = Toast.makeText(getApplicationContext(),
						     "�ף���û��ʼ�����ݿ��أ� ����", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				else 
				{
					checkTraffic(v);
					Toast toast = Toast.makeText(getApplicationContext(),
						     "������ˢ�£�", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}					
			}
		});
		
		//��ʼ�����ݿ�
		bt_init.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub	
				boolean isInit = sp.getBoolean("isInit", false);
				//�ж��Ƿ��Ѿ���ʼ��
				if(isInit)
				{
					Toast toast = Toast.makeText(getApplicationContext(),
						     "�ף��Ѿ���ʼ������Ŷ�� ����", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				else 
				{
					initDatabase();
					editor.putBoolean("isInit", true);
					editor.commit();
					
					Toast toast = Toast.makeText(getApplicationContext(),
						     "��ʼ����������Ҫ�ٵ�����Ŷ ��", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}								
			}
		});	
		
		bt_checkwifi.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				//�ж�wifi�Ƿ�����								
				try 
				{
					boolean isWifiAvailable = Util.isWifiAvailable(MainActivity.this);	
					if(isWifiAvailable)
					{
						Toast toast = Toast.makeText(getApplicationContext(),
							     "wifi ���ã�", Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();						
					}
					else
					{
						Toast toast = Toast.makeText(getApplicationContext(),
							     "wifi �����ã�", Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();	
					}
				} 
				catch (Exception e) 
				{
					// TODO: handle exception
					Toast toast = Toast.makeText(getApplicationContext(),
						     "error��", Toast.LENGTH_LONG);
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
	
	//��ȡ΢�ŵ�UID
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
		
	//��ʼ�����ݿ�
	public void initDatabase()
	{
		SQLiteDatabase db = openOrCreateDatabase("test.db", Context.MODE_PRIVATE, null);  
		db.execSQL("DROP TABLE IF EXISTS traffic");
		//����traffic��
		db.execSQL("CREATE TABLE traffic (id INTEGER PRIMARY KEY AUTOINCREMENT,  uid INTEGER, " +
				"wifi_1 INTEGER, wifi_2 INTEGER, wifi_total INTEGER, last_total INTEGER," +
				"since_boot INTEGER, total INTEGER, flag INTEGER )");
		//��ȡUID
		uid = getUIDOfMM();
		//����������������ֻͳ�����ڿ�ʼ��������֮ǰ�Ĳ���
		long since_boot = TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);	
		//��ֵ����Ϊ-2����Ҫ�ж�
		if(since_boot < 0 )
			since_boot = 0;
		//���ó�ʼֵ
		ContentValues values = new ContentValues();	
		values.put("id", 1);
		values.put("uid", uid);
		values.put("wifi_1", since_boot);
		values.put("wifi_2", -1);
		values.put("wifi_total", since_boot);
		values.put("last_total", 0);
		values.put("since_boot", 0);
		values.put("total", 0);
		
		//�ж�wifi�Ƿ�����
		boolean isWifiAlive = Util.isWifiAvailable(MainActivity.this);
		try 
		{
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
			
		db.insert("traffic", null, values);	
	}
	
	//�����鿴�������൱�ڵ������а�ʱ���㲢��ʾ
	public void checkTraffic(View v)
	{	
		boolean isInit = sp.getBoolean("isInit", false);
		//�ж��Ƿ��Ѿ���ʼ��
		if(!isInit)
		{
			Toast toast = Toast.makeText(getApplicationContext(),
				     "�ף����ݿ⻹û��ʼ���� ��", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();	
			
			return;
		}
		
		//��ȡUID
		uid = getUIDOfMM();
				
		SQLiteDatabase db = openOrCreateDatabase("test.db",  Context.MODE_PRIVATE, null); 
		
		/*���ܹػ�ʱwifi���ţ�����������wifiҲ�Ϳ��ţ���ôreceiver���޷���¼��Ӧwifi����ʱ��wifi_1,�����ڹػ�ʱ
		      ������ֵ����wifi״̬*/
		/*long since_boot_early = 0;
		
		Cursor cur = db.rawQuery("SELECT * FROM traffic WHERE uid = ?", new String[]{String.valueOf(uid)});		
		while (cur.moveToNext()) 
		{  
			since_boot_early = cur.getInt(cur.getColumnIndex("since_boot"));
		}*/
		
		//���������ڵ�����
		long since_boot = TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);	
		if(since_boot < 0)
			since_boot = 0;		
		
		//�����ݿ��ж�ȡ��Ӧ����
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
		
		//���㵽checkΪֹ�ܵ�����
		total = last_total + since_boot;
		
		ContentValues cv = new ContentValues();  	
		cv.put("total", total);
		//�����ܵ�����
		db.update("traffic", cv, "uid= ?", new String[]{String.valueOf(uid)}); 
		
		//3G����
		long shujuTraffic = 0;
		
		//������ǰwifi�ѹر�
		if(flag == 0)
		{
			shujuTraffic = total - wifi_total;			
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
		
		//wifi����
		double wifiTraffic = (double)total - shujuTraffic;
		
		tv_3g.setText(String.format("%.2f", (shujuTraffic/1024.0/1024.0)));
		tv_wifi.setText(String.format("%.2f", (wifiTraffic/1024.0/1024.0)));
	}
}
