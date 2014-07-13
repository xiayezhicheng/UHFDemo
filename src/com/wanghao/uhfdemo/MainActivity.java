package com.wanghao.uhfdemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.jiuray.Function;
import com.jiuray.ModuleControl;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity {

	ModuleControl moduleControl = new ModuleControl();
	private BluetoothAdapter mBluetoothAdapter;
	private static ArrayList<String> deviceList = new ArrayList<String>();
	private FButton btn_on_off;
	private FButton btn_identify;
	private MyApp myApp;
	private static boolean connFlag = false;
	private byte flagCrc;
	private byte initQ;
	public DBManager dbManager;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		initView();
	}

	private void initView() {
		myApp = (MyApp)getApplication();
		myApp.setLoopFlag(false);
		dbManager = new DBManager(this);
		dbManager.openDatabase();
		dbManager.closeDatabase();
		
		btn_on_off = (FButton)findViewById(R.id.btn_on_off);
		btn_identify = (FButton)findViewById(R.id.btn_identify);
		btn_on_off.setOnClickListener(btnHandleListener);
		btn_identify.setOnClickListener(btnHandleListener);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter==null){
			Toast.makeText(MainActivity.this, "该设备没有蓝牙", Toast.LENGTH_SHORT).show();
			return;
		}
		if(!mBluetoothAdapter.isEnabled()){
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, 10);
		}
	}

	OnClickListener btnHandleListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()){
				case R.id.btn_on_off:
					if(connFlag==false){
						PairedDevList();
						Intent intent_on_off = new Intent();
						intent_on_off.putExtra("connFlag",connFlag);
						intent_on_off.putStringArrayListExtra("deviceList", deviceList);
						intent_on_off.setClass(MainActivity.this, ConnectDev.class);
						startActivityForResult(intent_on_off, 1);
					}else if(moduleControl.UhfReaderDisconnect()){
						connFlag = false;
						btn_on_off.setText("连接蓝牙");
						btn_on_off.setButtonColor(getResources().getColor(R.color.fbutton_color_peter_river));
						Toast.makeText(getApplicationContext(), "蓝牙连接，断开成功", Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(getApplicationContext(), "蓝牙连接，断开失败", Toast.LENGTH_SHORT).show();
					}
					break;
				case R.id.btn_identify:
					if(connFlag==false){
						Toast.makeText(getApplicationContext(), "请先连接蓝牙", Toast.LENGTH_SHORT).show();
						return;
					}
					if(btn_identify.getText().equals("识别标签")
							&&moduleControl.UhfStartInventory((byte)1, initQ, flagCrc))
					{
						Intent intent_identify = new Intent();
						intent_identify.putExtra("connFlag", connFlag);
						intent_identify.setClass(MainActivity.this,IdentifyTag.class);
						startActivity(intent_identify);
						btn_identify.setText("停止识别");
						btn_identify.setButtonColor(getResources().getColor(R.color.fbutton_color_turquoise));
						myApp.setLoopFlag(true);
						
					}else{
						//停止识别
						myApp.setLoopFlag(false);
						if(moduleControl.UhfStopOperation(flagCrc))
						{
							btn_identify.setText("识别标签");	
							btn_identify.setButtonColor(getResources().getColor(R.color.fbutton_color_emerald));
						}else{
							
							Toast.makeText(MainActivity.this, "停止识别标签失败", 0).show();
						}
					}
					
				break;
			}
		}
	};
	
	private void PairedDevList() {
		deviceList.clear();
		if(!mBluetoothAdapter.isEnabled()){
			//请求打开蓝牙设备
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(intent);
		}
		Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
		
		String deviceStr="";
		if(devices.size()>0){
			for(Iterator<BluetoothDevice> iterator = devices.iterator();iterator.hasNext();){
				BluetoothDevice bluetoothdevice = (BluetoothDevice)iterator.next();
				deviceStr = bluetoothdevice.getName()+"\n"+bluetoothdevice.getAddress();
				deviceList.add(deviceStr);
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==1){
			Bundle extra = data.getExtras();
			if(extra!=null){
				connFlag = extra.getBoolean("connFlag");
				if(connFlag==true){
					btn_on_off.setText("断开蓝牙");
					btn_on_off.setButtonColor(getResources().getColor(R.color.fbutton_color_alizarin));
				}else{
					btn_on_off.setText("连接蓝牙");
					btn_on_off.setButtonColor(getResources().getColor(R.color.fbutton_color_peter_river));
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		exit();
	}
    
	public void exit(){
		new AlertDialog.Builder(this)
		.setTitle("消息")
		.setMessage("确认退出！")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				moduleControl.UhfReaderDisconnect();
				connFlag = false;
				finish();
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		}).show();
	}
}
