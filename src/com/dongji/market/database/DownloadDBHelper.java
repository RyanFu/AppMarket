package com.dongji.market.database;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.dongji.market.download.DownloadEntity;

public class DownloadDBHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "dongji_market_download_db.db";
	private static final String DOWNLOAD_TABLE = "downloadTable";

	private Context context;

	private DatabaseHelper dbHelper;

	public DownloadDBHelper(Context context) {
		this.context = context;
		dbHelper = new DatabaseHelper(this.context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table if not exists " + DOWNLOAD_TABLE + "(_id INTEGER primary key autoincrement, " + DownloadDBConstDefine.DOWNLOAD_APPNAME + " TEXT, " + DownloadDBConstDefine.DOWNLOAD_APPID + " INTEGER, " + "" + DownloadDBConstDefine.DOWNLOAD_CATEGOTY + " INTEGER, " + DownloadDBConstDefine.DOWNLOAD_CURRENT_POSITION + " LONG, " + DownloadDBConstDefine.DOWNLOAD_FILE_LENGTH + " LONG, " + "" + DownloadDBConstDefine.DOWNLOAD_PACKAGENAME + " TEXT, " + DownloadDBConstDefine.DOWNLOAD_VERSION_CODE + " Integer, " + DownloadDBConstDefine.DOWNLOAD_APKURL + " TEXT, " + DownloadDBConstDefine.DOWNLOAD_ICON_URL + " TEXT, " + DownloadDBConstDefine.DOWNLOAD_VERSION_NAME + " TEXT, " + "" + DownloadDBConstDefine.DOWNLOAD_TYPE + " Integer, " + DownloadDBConstDefine.DOWNLOAD_STATUS
					+ " Integer, " + DownloadDBConstDefine.HEAVY + " Integer);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DOWNLOAD_TABLE);
			onCreate(db);
		}

	}

	/**
	 * 将下载对象存入数据库
	 * 
	 * @param entity
	 */
	public void addOrUpdateDownload(DownloadEntity entity) {
		SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
		Cursor cursor = null;
		try {
			ContentValues values = new ContentValues();
			values.put(DownloadDBConstDefine.DOWNLOAD_APPNAME, entity.appName);
			values.put(DownloadDBConstDefine.DOWNLOAD_APPID, entity.appId);
			values.put(DownloadDBConstDefine.DOWNLOAD_CATEGOTY, entity.category);
			values.put(DownloadDBConstDefine.DOWNLOAD_CURRENT_POSITION, entity.currentPosition);
			values.put(DownloadDBConstDefine.DOWNLOAD_FILE_LENGTH, entity.fileLength);
			values.put(DownloadDBConstDefine.DOWNLOAD_PACKAGENAME, entity.packageName);
			values.put(DownloadDBConstDefine.DOWNLOAD_VERSION_CODE, entity.versionCode);
			values.put(DownloadDBConstDefine.DOWNLOAD_APKURL, entity.url);
			values.put(DownloadDBConstDefine.DOWNLOAD_ICON_URL, entity.iconUrl);
			values.put(DownloadDBConstDefine.DOWNLOAD_VERSION_NAME, entity.versionName);
			values.put(DownloadDBConstDefine.DOWNLOAD_TYPE, entity.downloadType);
			values.put(DownloadDBConstDefine.DOWNLOAD_STATUS, entity.getStatus());
			values.put(DownloadDBConstDefine.HEAVY, entity.heavy);
			cursor = sqLiteDatabase.query(DOWNLOAD_TABLE, null, DownloadDBConstDefine.DOWNLOAD_APPID + "=? AND " + DownloadDBConstDefine.DOWNLOAD_CATEGOTY + "=?", new String[] { String.valueOf(entity.appId), String.valueOf(entity.category) }, null, null, null);
			if (cursor.getCount() > 0) {
				sqLiteDatabase.update(DOWNLOAD_TABLE, values, DownloadDBConstDefine.DOWNLOAD_APPID + "=? AND " + DownloadDBConstDefine.DOWNLOAD_CATEGOTY + "=?", new String[] { String.valueOf(entity.appId), String.valueOf(entity.category) });
			} else {
				sqLiteDatabase.insert(DOWNLOAD_TABLE, null, values);
			}
		} catch (SQLiteException e) {
			System.out.println("addDownload error:" + e);
		} finally {
			close(sqLiteDatabase);
		}
	}

	/**
	 * 删除下载记录
	 * 
	 * @param entity
	 * @return
	 */
	public boolean deleteDownloadEntity(DownloadEntity entity) {
		SQLiteDatabase sqLiteDatabase = null;
		try {
			sqLiteDatabase = dbHelper.getWritableDatabase();
			int result = sqLiteDatabase.delete(DOWNLOAD_TABLE, DownloadDBConstDefine.DOWNLOAD_APPID + "=? AND " + DownloadDBConstDefine.DOWNLOAD_CATEGOTY + "=?", new String[] { String.valueOf(entity.appId), String.valueOf(entity.category) });
			return result > 0;
		} catch (SQLiteException e) {
			System.out.println("deleteDownloadEntity error:" + e);
		} finally {
			close(sqLiteDatabase);
		}
		return false;
	}

	/**
	 * 获取所有下载应用(包括下载、更新、待安装)
	 * 
	 * @param downloadList
	 * @return
	 */
	public List<DownloadEntity> getAllDownloadEntity(List<DownloadEntity> downloadList) {
		SQLiteDatabase sqLiteDatabase = null;
		Cursor cursor = null;
		try {
			sqLiteDatabase = dbHelper.getReadableDatabase();
			cursor = sqLiteDatabase.query(DOWNLOAD_TABLE, null, null, null, null, null, null);
			if (cursor.getCount() > 0) {
				boolean hasHistoryDownload = downloadList.size() > 0; // 是否有之前的下载数据
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					DownloadEntity entity = new DownloadEntity();
					entity.appId = cursor.getInt(cursor.getColumnIndex(DownloadDBConstDefine.DOWNLOAD_APPID));
					entity.category = cursor.getInt(cursor.getColumnIndex(DownloadDBConstDefine.DOWNLOAD_CATEGOTY));
					boolean exists = false;
					if (hasHistoryDownload) {// 已有下载数据
						exists = hasDownloadEntity(downloadList, entity);// 是否已包含
					}
					if (!exists) {// 还未包含
						entity.appName = cursor.getString(cursor.getColumnIndex(DownloadDBConstDefine.DOWNLOAD_APPNAME));
						entity.currentPosition = cursor.getLong(cursor.getColumnIndex(DownloadDBConstDefine.DOWNLOAD_CURRENT_POSITION));
						entity.fileLength = cursor.getLong(cursor.getColumnIndex(DownloadDBConstDefine.DOWNLOAD_FILE_LENGTH));
						entity.packageName = cursor.getString(cursor.getColumnIndex(DownloadDBConstDefine.DOWNLOAD_PACKAGENAME));
						entity.versionCode = cursor.getInt(cursor.getColumnIndex(DownloadDBConstDefine.DOWNLOAD_VERSION_CODE));
						entity.url = cursor.getString(cursor.getColumnIndex(DownloadDBConstDefine.DOWNLOAD_APKURL));
						entity.iconUrl = cursor.getString(cursor.getColumnIndex(DownloadDBConstDefine.DOWNLOAD_ICON_URL));
						entity.versionName = cursor.getString(cursor.getColumnIndex(DownloadDBConstDefine.DOWNLOAD_VERSION_NAME));
						entity.downloadType = cursor.getInt(cursor.getColumnIndex(DownloadDBConstDefine.DOWNLOAD_TYPE));
						int status = cursor.getInt(cursor.getColumnIndex(DownloadDBConstDefine.DOWNLOAD_STATUS));
						entity.setStatus(status);
						entity.heavy = cursor.getInt(cursor.getColumnIndex(DownloadDBConstDefine.HEAVY));
						downloadList.add(entity);
					}
					cursor.moveToNext();
				}
			}
		} catch (SQLiteException e) {

		} finally {
			close(sqLiteDatabase, cursor);
		}
		return downloadList;
	}

	/**
	 * 判断此应用是否存在于下载队列
	 * 
	 * @param downloadList
	 * @param entity
	 * @return
	 */
	private boolean hasDownloadEntity(List<DownloadEntity> downloadList, DownloadEntity entity) {
		for (int i = 0; i < downloadList.size(); i++) {
			DownloadEntity e = downloadList.get(i);
			if (entity.appId == e.appId && entity.category == e.category) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 关闭 sqlitedatabase 数据库和 cursor 游标
	 * 
	 * @param sqLiteDatabase
	 * @param cursor
	 */
	private synchronized void close(SQLiteDatabase sqLiteDatabase, Cursor cursor) {
		try {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		} catch (SQLiteException e) {
			System.out.println("close cursor error:" + e);
		}
		close(sqLiteDatabase);
	}

	/**
	 * 关闭 sqlitedatabase 数据库
	 * 
	 * @param sqLiteDatabase
	 */
	private synchronized void close(SQLiteDatabase sqLiteDatabase) {
		try {
			if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
				sqLiteDatabase.close();
			}
		} catch (SQLiteException e) {
			System.out.println("close sqLiteDatabase error:" + e);
		}
	}

	private interface DownloadDBConstDefine {
		final static String DOWNLOAD_APPNAME = "appname";
		final static String DOWNLOAD_APPID = "appId";
		static final String DOWNLOAD_CATEGOTY = "category";
		static final String DOWNLOAD_CURRENT_POSITION = "currentPostion";
		static final String DOWNLOAD_FILE_LENGTH = "fileLength";
		static final String DOWNLOAD_PACKAGENAME = "packageName";
		static final String DOWNLOAD_VERSION_CODE = "versionCode";
		static final String DOWNLOAD_APKURL = "apkUrl";
		static final String DOWNLOAD_ICON_URL = "iconUrl";
		static final String DOWNLOAD_VERSION_NAME = "versionName";
		static final String DOWNLOAD_TYPE = "downloadType";
		static final String DOWNLOAD_STATUS = "status";
		static final String HEAVY = "heavy";
	}
}
