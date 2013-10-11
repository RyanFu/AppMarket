package com.dongji.market.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.dongji.market.pojo.SettingConf;

public class MarketDatabase {
	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "dongji_market_db.db";
	private static final String SETTING_TABLE = "setting_table";// 保存设置项
	private static final String SEARCH_HISTORY_TABLE = "search_history_table";// 保存搜索历史表
	private static final String SEARCH_HOTWORDS_TABLE = "search_hotwords_table";// 保存搜索热词表
	private static final String RATING_TABLE = "rating_table";// 评分表

	private Context context;

	public MarketDatabase(Context context) {
		this.context = context;
		dbHelper = new DatabaseHelper(this.context);
	}

	private DatabaseHelper dbHelper;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table if not exists " + SETTING_TABLE + "(_id INTEGER primary key autoincrement, name TEXT, value INTEGER)");
			db.execSQL("create table if not exists " + SEARCH_HISTORY_TABLE + "(_id INTEGER primary key autoincrement, name TEXT)");
			db.execSQL("create table if not exists " + SEARCH_HOTWORDS_TABLE + "(_id INTEGER primary key autoincrement, hotword TEXT)");
			db.execSQL("create table if not exists " + RATING_TABLE + "(_id INTEGER primary key autoincrement, typeid INTEGER, appid INTEGER,rating float);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + SETTING_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + SEARCH_HISTORY_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + SEARCH_HOTWORDS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + RATING_TABLE);
			onCreate(db);
		}

	}

	/**
	 * 释放数据库资源
	 * 
	 * @param sqliteDatabase
	 * @param cursor
	 */
	private void release(SQLiteDatabase sqliteDatabase, Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		if (sqliteDatabase != null && sqliteDatabase.isOpen()) {
			sqliteDatabase.close();
		}
	}

	/**
	 * 设置数据库
	 * 
	 * @author Adrian
	 * 
	 */
	public static class Setting_Service {

		private DatabaseHelper dbHelp;
		private static final String TBNAME = SETTING_TABLE;

		public Setting_Service(Context cxt) {
			dbHelp = new MarketDatabase.DatabaseHelper(cxt);
		}

		/**
		 * 添加设置项
		 * 
		 * @param config
		 */
		public void add(SettingConf config) {
			SQLiteDatabase db = null;
			ContentValues cv = new ContentValues();
			cv.put("name", config.getName());
			cv.put("value", config.getValue());
			try {
				db = dbHelp.getWritableDatabase();
				db.insertOrThrow(TBNAME, null, cv);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, null);
			}
		}

		/**
		 * 删除设置项
		 * 
		 * @param name
		 */
		public void del(String name) {
			SQLiteDatabase db = null;
			try {
				db = dbHelp.getWritableDatabase();
				db.delete(TBNAME, "name = ?", new String[] { name });
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, null);
			}
		}

		/**
		 * 更新设置项
		 * 
		 * @param name
		 * @param value
		 */
		public void update(String name, int value) {
			SQLiteDatabase db = null;
			ContentValues cv = new ContentValues();
			cv.put("value", value);
			try {
				db = dbHelp.getWritableDatabase();
				db.update(TBNAME, cv, "name = ?", new String[] { name });
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, null);
			}
		}

		/**
		 * 查询设置项
		 * 
		 * @param name
		 * @return
		 */
		public int select(String name) {
			SQLiteDatabase db = null;
			int value = 0;
			Cursor cursor = null;
			try {
				db = dbHelp.getReadableDatabase();
				cursor = db.query(TBNAME, new String[] { "value" }, "name = ?", new String[] { name }, null, null, null);
				if (cursor.getCount() == 0) {
					add(new SettingConf(name, 50));
				}
				while (cursor.moveToNext()) {
					value = cursor.getInt(0);
				}
				cursor.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, cursor);
			}
			return value;
		}

		/**
		 * 猎取所有设置项
		 * 
		 * @return
		 */
		public List<SettingConf> getAll() {
			SQLiteDatabase db = null;
			List<SettingConf> data = new ArrayList<SettingConf>();
			SettingConf conf = new SettingConf();
			Cursor cursor = null;
			try {
				db = dbHelp.getReadableDatabase();
				cursor = db.query(TBNAME, null, null, null, null, null, null);
				while (cursor.moveToNext()) {
					conf.set_id(cursor.getInt(0));
					conf.setName(cursor.getString(1));
					conf.setValue(cursor.getInt(2));
					data.add(conf);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, cursor);
			}
			return data;
		}

		/**
		 * 释放数据库资源
		 * 
		 * @param sqliteDatabase
		 * @param cursor
		 */
		private void release(SQLiteDatabase sqliteDatabase, Cursor cursor) {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (sqliteDatabase != null && sqliteDatabase.isOpen()) {
				sqliteDatabase.close();
			}
		}
	}

	/**
	 * 搜索历史管理数据库
	 * 
	 * @author Adrian
	 * 
	 */
	public static class SearchHistory {

		private DatabaseHelper dbHelper;
		private static final String TBNAME = SEARCH_HISTORY_TABLE;

		public SearchHistory(Context cxt) {
			dbHelper = new MarketDatabase.DatabaseHelper(cxt);
		}

		/**
		 * 增加单条历史搜索,设置总条数最大为20
		 * 
		 * @param keyword
		 */
		public void add(String keyword) {
			SQLiteDatabase db = null;
			ContentValues values = new ContentValues();
			values.put("name", keyword);
			try {
				db = dbHelper.getWritableDatabase();
				db.insert(TBNAME, null, values);
				delRepeat(keyword);
				setMax(20);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, null);
			}
		}

		/**
		 * 删除单条搜索历史
		 * 
		 * @param keyword
		 */
		public void del(String keyword) {
			SQLiteDatabase db = null;
			try {
				db = dbHelper.getWritableDatabase();
				db.delete(TBNAME, "name = ?", new String[] { keyword });
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, null);
			}
		}

		/**
		 * 更新搜索历史
		 * 
		 * @param keyword
		 */
		public void update(String keyword) {
			SQLiteDatabase db = null;
			ContentValues values = new ContentValues();
			values.put("name", keyword);
			try {
				db = dbHelper.getWritableDatabase();
				db.update(TBNAME, values, "name = ?", new String[] { keyword });
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, null);
			}
		}

		/**
		 * 检查当前搜索词条是否已存在历史搜索记录中
		 * 
		 * @param keyword
		 * @param db
		 * @return
		 */
		public boolean checkExist(String keyword, SQLiteDatabase db) {
			Cursor cursor = null;
			try {
				cursor = db.query(TBNAME, null, "name = ?", new String[] { keyword }, null, null, null);
				if (cursor.getCount() > 0) {
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, cursor);
			}
			return false;
		}

		/**
		 * 删除历史搜索词条中重复的词条
		 * 
		 * @param keyword
		 */
		public void delRepeat(String keyword) {
			SQLiteDatabase db = null;
			Cursor cursor = null;
			try {
				db = dbHelper.getWritableDatabase();
				cursor = db.query(TBNAME, new String[] { "_id" }, "name = ?", new String[] { keyword }, null, null, null);
				if (cursor.getCount() > 1) {
					if (cursor.moveToFirst()) {
						db.delete(TBNAME, "_id = ?", new String[] { cursor.getInt(0) + "" });
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, cursor);
			}
		}

		/**
		 * 删除所有搜索历史记录
		 */
		public void delAll() {
			SQLiteDatabase db = null;
			try {
				db = dbHelper.getWritableDatabase();
				db.delete(TBNAME, null, null);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, null);
			}
		}

		/**
		 * 设置可保存记录最多条数
		 * 
		 * @param max
		 */
		public void setMax(int max) {
			SQLiteDatabase db = null;
			Cursor cursor = null;
			try {
				db = dbHelper.getReadableDatabase();
				cursor = db.query(TBNAME, null, null, null, null, null, null);
				if (cursor.getCount() > max) {
					if (cursor.moveToFirst()) {
						del(cursor.getString(cursor.getColumnIndex("name")));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, cursor);
			}
		}

		/**
		 * 获取所有搜索历史记录
		 * 
		 * @return
		 */
		public List<String> getAll() {
			List<String> list = new ArrayList<String>();
			SQLiteDatabase db = null;
			Cursor cursor = null;
			try {
				db = dbHelper.getReadableDatabase();
				cursor = db.query(TBNAME, new String[] { "name" }, null, null, null, null, "_id desc");
				while (cursor.moveToNext()) {
					list.add(cursor.getString(0));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, cursor);
			}
			return list;
		}

		/**
		 * 获取搜索历史条目数量
		 * 
		 * @return
		 */
		public int getCount() {
			SQLiteDatabase db = null;
			Cursor cursor = null;
			int count = 0;
			try {
				db = dbHelper.getReadableDatabase();
				cursor = db.query(TBNAME, new String[] { "_id" }, null, null, null, null, null);
				count = cursor.getCount();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, cursor);
			}
			return count;
		}

		/**
		 * 释放数据库资源
		 * 
		 * @param sqliteDatabase
		 * @param cursor
		 */
		private void release(SQLiteDatabase sqliteDatabase, Cursor cursor) {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (sqliteDatabase != null && sqliteDatabase.isOpen()) {
				sqliteDatabase.close();
			}
		}
	}

	/**
	 * 热词管理数据库
	 * 
	 * @author Adrian
	 * 
	 */
	public static class HotwordsService {

		private DatabaseHelper dbHelper;
		private static final String TBNAME = SEARCH_HOTWORDS_TABLE;

		public HotwordsService(Context context) {
			dbHelper = new MarketDatabase.DatabaseHelper(context);
		}

		/**
		 * 添加热词集合
		 * 
		 * @param data
		 */
		public void add(List<String> data) {
			SQLiteDatabase db = null;
			ContentValues values = new ContentValues();
			try {
				db = dbHelper.getWritableDatabase();
				for (String string : data) {
					values.put("hotword", string);
					db.insert(TBNAME, null, values);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, null);
			}
		}

		/**
		 * 删除热词操作
		 */
		public void delAll() {
			SQLiteDatabase db = null;
			try {
				db = dbHelper.getWritableDatabase();
				db.delete(TBNAME, null, null);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, null);
			}
		}

		/**
		 * 更新热词
		 * 
		 * @param data
		 */
		public void update(List<String> data) {
			delAll();
			add(data);
		}

		/**
		 * 猎取所有热词
		 * 
		 * @return
		 */
		public List<String> getAll() {
			SQLiteDatabase db = null;
			List<String> list = new ArrayList<String>();
			Cursor cursor = null;
			try {
				db = dbHelper.getReadableDatabase();
				cursor = db.query(TBNAME, new String[] { "hotword" }, null, null, null, null, null);
				while (cursor.moveToNext()) {
					list.add(cursor.getString(0));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				release(db, cursor);
			}
			return list;
		}

		/**
		 * 释放数据库资源
		 * 
		 * @param sqliteDatabase
		 * @param cursor
		 */
		private void release(SQLiteDatabase sqliteDatabase, Cursor cursor) {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (sqliteDatabase != null && sqliteDatabase.isOpen()) {
				sqliteDatabase.close();
			}
		}
	}

	public boolean addRatingApp(int typeid, int appid, float rating) {
		SQLiteDatabase sqLiteDatabase = null;
		try {
			sqLiteDatabase = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("typeid", typeid);
			values.put("appid", appid);
			values.put("rating", rating);
			long count = sqLiteDatabase.insert(RATING_TABLE, null, values);
			return count > 0;
		} catch (SQLiteException e) {
		} finally {
			release(sqLiteDatabase, null);
		}
		return false;
	}

	public float selectRatingById(int typeid, int appid) {
		SQLiteDatabase sqLiteDatabase = null;
		Cursor cursor = null;
		try {
			sqLiteDatabase = dbHelper.getReadableDatabase();
			cursor = sqLiteDatabase.query(RATING_TABLE, new String[] { "rating" }, "typeid=? and appid=?", new String[] { String.valueOf(typeid), String.valueOf(appid) }, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				float num = cursor.getFloat(cursor.getColumnIndex("rating"));
				return num;
			}
		} catch (SQLiteException e) {
			System.out.println(e);
		} finally {
			release(sqLiteDatabase, cursor);
		}
		return -1;
	}
}
