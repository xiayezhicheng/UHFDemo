package com.wanghao.uhfdemo;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.iflytek.speech.ErrorCode;
import com.iflytek.speech.ISpeechModule;
import com.iflytek.speech.InitListener;
import com.iflytek.speech.SpeechConstant;
import com.iflytek.speech.SpeechSynthesizer;
import com.iflytek.speech.SynthesizerListener;
import com.jiuray.Function;
import com.jiuray.ModuleControl;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class IdentifyTag extends FragmentActivity{

	private PagerSlidingTabStrip tabs;
	private ViewPager pager;
	private MyPagerAdapter adapter;
	private LinearLayout call_play_info;
	private SpeechSynthesizer mTts;
	private SharedPreferences mSharedPreferences;
	static ArrayList<String> tagList;
	private MyApp myApp;
	private Function fun = new Function();
	private ModuleControl moduleControl = new ModuleControl();
	private ImageView btn_play_start;
	private ImageView btn_play_cancel;
	public static String SPEAKER = "speaker";
	private static final int PLAY_START = 1001;
	private static final int PLAY_PAUSE = 1002;
	private static final int PLAY_RESUME = 1003;
	private static final int PLAY_STOP = 1004;
	private MyHandler myHandler;
	private SQLiteDatabase database;
	private String play_info;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.identify_tag);
		
		initView();
		initData();
	}
	private void initView() {
		myApp = (MyApp)getApplication();
		call_play_info = (LinearLayout)findViewById(R.id.call_play_info);
		tabs = (PagerSlidingTabStrip)findViewById(R.id.tabs);
		tabs.setIndicatorColor(Color.parseColor("#3498db"));
		pager = (ViewPager)findViewById(R.id.pager);
		btn_play_start = (ImageView)findViewById(R.id.btn_play_start);
		btn_play_cancel = (ImageView)findViewById(R.id.btn_play_cancel);
		mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, Activity.MODE_PRIVATE);
		tagList = new ArrayList<String>();
		myHandler = new MyHandler();
		database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH+"/"+DBManager.DB_NAME, null);
		
		
		OnClickListener playListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(v.getId()){
					case R.id.call_play_info:
						PlayInfoFragment dialog = new PlayInfoFragment();
						dialog.show(getSupportFragmentManager(), "QuickContactFragment");
						break;
					case R.id.btn_play_start:
						if(myApp.isHasPlayed()==false){
							setParam();
							mTts.startSpeaking(myApp.getPlay_content(), mTtsListener);
							btn_play_start.setImageDrawable(getResources().getDrawable(R.drawable.btn_play_stop));
							myApp.setHasPlayed(true);
							myApp.setPlaying(true);
						}else{
							if(myApp.isPlaying()==true){
								mTts.pauseSpeaking(mTtsListener);
								btn_play_start.setImageDrawable(getResources().getDrawable(R.drawable.btn_play_start));
								myApp.setPlaying(false);
							}else{
								mTts.resumeSpeaking(mTtsListener);
								btn_play_start.setImageDrawable(getResources().getDrawable(R.drawable.btn_play_stop));
								myApp.setPlaying(true);
							}
						}
						break;
					case R.id.btn_play_cancel:
						if(myApp.isHasPlayed()==true){
							mTts.stopSpeaking(mTtsListener);
							btn_play_start.setImageDrawable(getResources().getDrawable(R.drawable.btn_play_start));
							myApp.setHasPlayed(false);
							myApp.setPlaying(false);
						}
						break;
				}
			}
		};
		
		call_play_info.setOnClickListener(playListener);
		btn_play_start.setOnClickListener(playListener);
		btn_play_cancel.setOnClickListener(playListener);
		
		// 初始化合成对象
		mTts = new SpeechSynthesizer(this, mTtsInitListener);
	}
	
	private void initData() {
		adapter = new MyPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);
		tabs.setViewPager(pager);
		new TagThread().start();
	}
	
	/**
     * 初期化监听。
     */
	private InitListener mTtsInitListener = new InitListener(){

		@Override
		public void onInit(ISpeechModule arg0, int code) {
			if(code==ErrorCode.SUCCESS){
				btn_play_start.setEnabled(true);
			}
		}
		
	};


	/**
	 * 参数设置
	 * @param param
	 * @return 
	 */
	private void setParam() {
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, mSharedPreferences.getString("engine_preference", "local"));
		if(mSharedPreferences.getString("engine_preference", "local").equalsIgnoreCase("local")){
			mTts.setParameter(SpeechSynthesizer.VOICE_NAME,
					mSharedPreferences.getString("role_cn_preference", "xiaoyan"));
		}else{
			mTts.setParameter(SpeechSynthesizer.VOICE_NAME,
					mSharedPreferences.getString("role_cn_preference", "xiaoyan")); 
		}
		mTts.setParameter(SpeechSynthesizer.SPEED,
				mSharedPreferences.getString("speed_preference", "60"));
		
		mTts.setParameter(SpeechSynthesizer.PITCH,
				mSharedPreferences.getString("pitch_preference", "55"));
		
		mTts.setParameter(SpeechSynthesizer.VOLUME,
				mSharedPreferences.getString("volume_preference", "100"));
	}
	
	class TagThread extends Thread{
		HashMap<String, String> map;
		public void run()
		{
			byte[] bLenUii = new byte[1];
			byte[] bUii = new byte[255];
			
			while(myApp.isLoopFlag())
			{
				if(moduleControl.UhfReadInventory(bLenUii, bUii))
				{
					String tagUii = fun.bytesToHexString(bUii, bLenUii[0]);
					
					boolean bool = false;
					int index = -1;
					try {
						bool = !("".equals(tagUii));
					} catch (Exception e) {
						Log.v("BreakPoint", "异常："+e.getMessage());
						e.printStackTrace();
					}
					if(myApp.isHasPlayed()==false)
					{
						if(bool){
							
							index = checkIsExist(tagUii,tagList);
							
							if(index == -1)
							{
								tagList.add(tagUii);
								Cursor cursor = database.rawQuery("select * from tagdemo where epc= '"+tagUii+"'",null);
								if(cursor.moveToFirst()){
									String content = cursor.getString(cursor.getColumnIndex("content"));
									String warn = cursor.getString(cursor.getColumnIndex("warn"));
									if("".equals(warn)){
										play_info = content;
									}else{
										play_info = content+"。特别提醒："+warn;
									}
									myApp.setPlay_content(play_info);
									
									setParam();
									mTts.startSpeaking(myApp.getPlay_content(), mTtsListener);
								}
								cursor.close();
							}
						} 
					}
			
				}
			}
		}
	}
	
	SynthesizerListener mTtsListener = new SynthesizerListener.Stub() {
		@Override
		public void onBufferProgress(int arg0) throws RemoteException {
		}

		@Override
		public void onCompleted(int errorCode) throws RemoteException {
			if(errorCode==0){
				Message message = myHandler.obtainMessage();
				message.what = PLAY_STOP;
				myHandler.sendMessage(message);
			}
		}

		@Override
		public void onSpeakBegin() throws RemoteException {
			Message message = myHandler.obtainMessage();
			message.what = PLAY_START;
			myHandler.sendMessage(message);
		}

		@Override
		public void onSpeakPaused() throws RemoteException {
		}

		@Override
		public void onSpeakProgress(int arg0) throws RemoteException {
		}

		@Override
		public void onSpeakResumed() throws RemoteException {
		}
	};
	
	public int checkIsExist(String uiiStr, ArrayList<String> tagList)
	{
		int existFlag = -1;
		String tempStr = "";
		for(int i=0;i<tagList.size();i++)
		{
			tempStr = tagList.get(i);
			
			if(uiiStr != "" && uiiStr.equals(tempStr))
			{
				existFlag = i;
			}
		}
		return existFlag;
	}
	private class MyHandler extends Handler{

		@Override
		public void handleMessage(Message msg) {
			if(msg.what==PLAY_START){
				btn_play_start.setImageDrawable(getResources().getDrawable(R.drawable.btn_play_stop));
				myApp.setHasPlayed(true);
				myApp.setPlaying(true);
			}
			if(msg.what==PLAY_STOP){
				btn_play_start.setImageDrawable(getResources().getDrawable(R.drawable.btn_play_start));
				myApp.setHasPlayed(false);
				myApp.setPlaying(false);
			}
		}
		
	}
	
   @Override
    protected void onDestroy() {
        super.onDestroy();
        mTts.stopSpeaking(mTtsListener);
        // 退出时释放连接
        mTts.destory();
        database.close();
    }
}
