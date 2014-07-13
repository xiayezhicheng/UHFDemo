package com.wanghao.uhfdemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class DBManager {

	private final int BUFFER_SIZE = 8192;
	public static final String DB_NAME = "patrol.db";
	public static final String PACKAGE_NAME = "com.wanghao.uhfdemo";
	public static final String DB_PATH = "/data"
			+Environment.getDataDirectory().getAbsolutePath()
			+"/"+PACKAGE_NAME;
	private SQLiteDatabase database;
	private Context context;
	
	DBManager(Context context){
		this.context = context;
	}
	
	public void openDatabase(){
		this.database = this.openDatabase(DB_PATH + "/" + DB_NAME);
	}
	
	private SQLiteDatabase openDatabase(String dbfile){
		try{
			File file = new File(dbfile);
			if(!(file.exists())){
				InputStream is = this.context.getResources().openRawResource(R.raw.patrol);
				FileOutputStream fos = new FileOutputStream(dbfile);
				byte[] buffer = new byte[BUFFER_SIZE];
				int count = 0;
				while((count = is.read(buffer))>0){
					fos.write(buffer, 0, count);
				}
				fos.flush();
				fos.close();
				is.close();
			}
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
			return db;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public void closeDatabase(){
		this.database.close();
	}
	
}
