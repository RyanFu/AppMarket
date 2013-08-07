package com.dongji.market.download;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ADownloadApkDBHelper extends SQLiteOpenHelper implements
		AConstDefine {

	public final static String FIELD_APKID = "apkId";
	public final static String FIELD_APKNAME = "apkName";
	public final static String FIELD_APKDOWNLOADSIZE = "apkDownloadSize";
	public final static String FIELD_APKTOTALSIZE = "apkTotalSize";
	public final static String FIELD_APKSTATUS = "apkStatus";
	public final static String FIELD_APKURL = "apkUrl";
	public final static String FIELD_APKVERSION = "apkVersion";
	public final static String FIELD_APKVERSIONCODE = "apkVersionCode";
	public final static String FIELD_APKPACKAGENAME = "apkPackageName";
	public final static String FIELD_APKICONURL = "apkIconUrl";
	public final static String FIELD_APKCATEGORY = "category";

	private final static String DATABASE_NAME = "downapkdb";
	private final static int DATABASE_VERSION = 1;
	private final static String TABLE_DOWNLOAD = "apkdownload";
	private final static String TABLE_IGNORE = "appignore";

	public ADownloadApkDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public synchronized List<AErrorApk> selectAllDownloadApkToError() {
		List<AErrorApk> aErrorApks = new ArrayList<AErrorApk>();
		try {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(TABLE_DOWNLOAD,
					new String[] { FIELD_APKPACKAGENAME, FIELD_APKVERSIONCODE,
							FIELD_APKSTATUS }, null, null, null, null, null);

			AErrorApk aErrorApk;
			while (cursor.moveToNext()) {
				aErrorApk = new AErrorApk();
				aErrorApk.apkPackageName = cursor.getString(cursor
						.getColumnIndex(FIELD_APKPACKAGENAME));
				aErrorApk.apkVersionCode = cursor.getInt(cursor
						.getColumnIndex(FIELD_APKVERSIONCODE));
				// aErrorApk.apkId =
				// cursor.getInt(cursor.getColumnIndex(FIELD_APKID));
				aErrorApk.apkStatus = cursor.getInt(cursor
						.getColumnIndex(FIELD_APKSTATUS));
				aErrorApks.add(aErrorApk);
			}
			cursor.close();
			db.close();

		} catch (Exception e) {
			showErrLog(e.toString());
		}
		return aErrorApks;
	}

	/**
	 * 根据包名和版本号查询该记录是否存在
	 * 
	 * @param apkPackageName
	 * @return true:存在该apk false:不存在该apk
	 */
	public synchronized boolean selectApkIsExist(String apkPackageName,
			int apkVersionCode) {
		try {
			SQLiteDatabase db = this.getReadableDatabase();
			String where = FIELD_APKPACKAGENAME + "=? and "
					+ FIELD_APKVERSIONCODE + "=?";
			String[] whereValue = { apkPackageName,
					String.valueOf(apkVersionCode) };
			Cursor cursor = db.query(TABLE_DOWNLOAD, null, where, whereValue,
					null, null, null);
			if (cursor.moveToFirst()) {
				cursor.close();
				db.close();
				return true;
			}
			cursor.close();
			db.close();

		} catch (Exception e) {
			showErrLog(e.toString());
		}
		return false;
	}

	/**
	 * 根据包名和版本号查询该记录是否存在
	 * 
	 * @param apkPackageName
	 * @return true:存在该apk false:不存在该apk
	 */
	public synchronized ADownloadApkItem selectApkByPAndV(
			String apkPackageName, int apkVersionCode) {
		ADownloadApkItem aDownloadApkItem = null;
		try {
			SQLiteDatabase db = this.getReadableDatabase();
			String where = FIELD_APKPACKAGENAME + "=? and "
					+ FIELD_APKVERSIONCODE + "=?";
			String[] whereValue = { apkPackageName,
					String.valueOf(apkVersionCode) };
			Cursor cursor = db.query(TABLE_DOWNLOAD, null, where, whereValue,
					null, null, null);
			if (cursor.moveToFirst()) {
				aDownloadApkItem = getADownloadApkItemFromCursor(cursor);
				cursor.close();
				db.close();
			}
			cursor.close();
			db.close();

		} catch (Exception e) {
			showErrLog(e.toString());
		}
		return aDownloadApkItem;
	}

	/**
	 * 根据apkPackageName删除相应的下载记录
	 * 
	 * @param downloadAPKItem
	 */
	public synchronized void deleteDownloadByPAndV(String apkPackageName,
			int apkVersionCode) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			String where = FIELD_APKPACKAGENAME + "=? and "
					+ FIELD_APKVERSIONCODE + "=?";
			String[] whereValue = { apkPackageName,
					String.valueOf(apkVersionCode) };
			db.delete(TABLE_DOWNLOAD, where, whereValue);
			db.close();
		} catch (Exception e) {
			showErrLog(e.toString());
		}

	}

	/**
	 * 根据apkId更新APK总大小
	 * 
	 * @param apkId
	 * @param totalSize
	 */
	public synchronized void updateTotalSize(String apkPackageName,
			int apkVersionCode, int totalSize) {
		if (null == apkPackageName || apkPackageName.trim().equals("")) {
			showErrLog("updateTotalSize---apkPackageName为空");
			return;
		}
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			String where = FIELD_APKPACKAGENAME + "=? and "
					+ FIELD_APKVERSIONCODE + "=?";
			String[] whereValue = { apkPackageName,
					String.valueOf(apkVersionCode) };
			ContentValues cv = new ContentValues();
			cv.put(FIELD_APKTOTALSIZE, totalSize);
			db.update(TABLE_DOWNLOAD, cv, where, whereValue);
			db.close();
		} catch (Exception e) {
			showErrLog(e.toString());
		}
	}

	/**
	 * 更新下载信息
	 * 
	 * @param downloadingApkList
	 */
	public synchronized void updateADownloadApkItem(
			ADownloadApkItem aDownloadApkItem) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			String where = FIELD_APKPACKAGENAME + "=? and "
					+ FIELD_APKVERSIONCODE + "=?";
			String[] whereValue = { aDownloadApkItem.apkPackageName,
					String.valueOf(aDownloadApkItem.apkVersionCode) };
			ContentValues cv = new ContentValues();
			cv.put(FIELD_APKDOWNLOADSIZE, aDownloadApkItem.apkDownloadSize);
			cv.put(FIELD_APKSTATUS, aDownloadApkItem.apkStatus);
			db.update(TABLE_DOWNLOAD, cv, where, whereValue);
			db.close();
		} catch (Exception e) {
			showErrLog(e.toString());
		}
	}

	/**
	 * 更新列表中的信息
	 * 
	 * @param downloadingApkList
	 */
	public synchronized void updateDownloadList(
			ADownloadApkList downloadingApkList) {
		if (null == downloadingApkList
				|| downloadingApkList.apkList.size() == 0) {
			return;
		}

		try {
			SQLiteDatabase db = this.getWritableDatabase();
			int size = downloadingApkList.apkList.size();
			for (int i = 0; i < size; i++) {
				String where = FIELD_APKPACKAGENAME + "=? and "
						+ FIELD_APKVERSIONCODE + "=?";
				String[] whereValue = {
						downloadingApkList.apkList.get(i).apkPackageName,
						String.valueOf(downloadingApkList.apkList.get(i).apkVersionCode) };

				ContentValues cv = new ContentValues();
				cv.put(FIELD_APKDOWNLOADSIZE,
						downloadingApkList.apkList.get(i).apkDownloadSize);
				cv.put(FIELD_APKSTATUS,
						downloadingApkList.apkList.get(i).apkStatus);
				db.update(TABLE_DOWNLOAD, cv, where, whereValue);
			}
			db.close();
		} catch (Exception e) {
			showErrLog(e.toString());
		}
	}

	/**
	 * 下载完成后对数据库的更新
	 * 
	 * @param apkPackageName
	 * @param apkDownloadSize
	 */
	public synchronized void updateWhileDownloadComplete(String apkPackageName,
			int apkVersionCode, int apkDownloadSize, int apkStatus) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			String where = FIELD_APKPACKAGENAME + "=? and "
					+ FIELD_APKVERSIONCODE + "=?";
			String[] whereValue = { apkPackageName,
					String.valueOf(apkVersionCode) };
			ContentValues cv = new ContentValues();
			cv.put(FIELD_APKDOWNLOADSIZE, apkDownloadSize);
			cv.put(FIELD_APKSTATUS, apkStatus);
			db.update(TABLE_DOWNLOAD, cv, where, whereValue);
			db.close();
		} catch (Exception e) {
			showErrLog(e.toString());
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "Create table if not exists " + TABLE_DOWNLOAD + "("
				+ FIELD_APKID + " integer," + FIELD_APKNAME + " varchar(100),"
				+ FIELD_APKDOWNLOADSIZE + " integer," + FIELD_APKTOTALSIZE
				+ " integer," + FIELD_APKSTATUS + " integer," + FIELD_APKURL
				+ " varchar(300)," + FIELD_APKICONURL + " varchar(300),"
				+ FIELD_APKVERSION + " varchar(100)," + FIELD_APKVERSIONCODE
				+ " integer," + FIELD_APKPACKAGENAME + " varchar(300), "
				+ FIELD_APKCATEGORY + " integer" + ");";
		db.execSQL(sql);
		sql = "Create table if not exists " + TABLE_IGNORE + "("
				+ FIELD_APKNAME + " varchar(100)," + FIELD_APKTOTALSIZE
				+ " integer," + FIELD_APKURL + " varchar(300),"
				+ FIELD_APKICONURL + " varchar(300)," + FIELD_APKVERSION
				+ " varchar(100)," + FIELD_APKVERSIONCODE + " integer,"
				+ FIELD_APKPACKAGENAME + " varchar(300) " + ");";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = " DROP TABLE IF EXISTS " + TABLE_DOWNLOAD;
		db.execSQL(sql);
		sql = " DROP TABLE IF EXISTS " + TABLE_IGNORE;
		db.execSQL(sql);
		onCreate(db);
	}

	/**
	 * 查询忽略更新的应用信息
	 * 
	 * @return 忽略更新的应用的列表
	 */
	public synchronized ADownloadApkList selectAllIgnoreApp() {
		ADownloadApkList aDownloadApkList = new ADownloadApkList();
		try {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(TABLE_IGNORE, null, null, null, null,
					null, null);

			AIgnoreItem aIgnoreItem;
			while (cursor.moveToNext()) {
				aIgnoreItem = new AIgnoreItem();
				aIgnoreItem.apkIconUrl = cursor.getString(cursor
						.getColumnIndex(FIELD_APKICONURL));
				aIgnoreItem.apkName = cursor.getString(cursor
						.getColumnIndex(FIELD_APKNAME));
				aIgnoreItem.apkPackageName = cursor.getString(cursor
						.getColumnIndex(FIELD_APKPACKAGENAME));
				aIgnoreItem.apkTotalSize = cursor.getInt(cursor
						.getColumnIndex(FIELD_APKTOTALSIZE));
				aIgnoreItem.apkVersion = cursor.getString(cursor
						.getColumnIndex(FIELD_APKVERSION));
				aIgnoreItem.apkVersionCode = cursor.getInt(cursor
						.getColumnIndex(FIELD_APKVERSIONCODE));

				aDownloadApkList.ignoreAppList.add(aIgnoreItem);
			}
			cursor.close();
			db.close();

		} catch (Exception e) {
			showErrLog(e.toString());
		}
		return aDownloadApkList;
	}

	/**
	 * 通过状态查询APK信息
	 * 
	 * @param status
	 *            详见AConstDefine中的各状态
	 * @return 符合查询状态的APK列表信息
	 */
	public synchronized ADownloadApkList selectApkByStatus(int[] status) {
		ADownloadApkList downloadAPKList = new ADownloadApkList();
		try {
			SQLiteDatabase db = this.getReadableDatabase();

			String where = FIELD_APKSTATUS + "=?";
			String[] whereValue = new String[status.length];
			whereValue[0] = Integer.toString(status[0]);
			for (int i = 1; i < status.length; i++) {
				where += " or " + FIELD_APKSTATUS + "=?";
				whereValue[i] = Integer.toString(status[i]);
			}
			Cursor cursor = db.query(TABLE_DOWNLOAD, null, where, whereValue,
					null, null, null);

			while (cursor.moveToNext()) {
				downloadAPKList.apkList
						.add(getADownloadApkItemFromCursor(cursor));
			}
			cursor.close();
			db.close();

		} catch (Exception e) {
			showErrLog(e.toString());
		}
		return downloadAPKList;
	}

	public synchronized ADownloadApkList queryAllApkStatus() {
		ADownloadApkList downloadAPKList = new ADownloadApkList();
		try {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(TABLE_DOWNLOAD, null, null, null, null,
					null, null);
			while (cursor.moveToNext()) {
				downloadAPKList.apkList
						.add(getADownloadApkItemFromCursor(cursor));
			}
			cursor.close();
			db.close();

		} catch (Exception e) {
			showErrLog(e.toString());
		}
		return downloadAPKList;
	}

	/**
	 * 查询下载表中符合相应状态的APK的个数
	 * 
	 * @param APK的状态
	 */
	public synchronized int selectCountByStatus(int[] status) {
		int size = 0;
		try {
			SQLiteDatabase db = this.getReadableDatabase();
			String where = FIELD_APKSTATUS + "=?";
			String[] whereValue = new String[status.length];
			whereValue[0] = Integer.toString(status[0]);
			for (int i = 1; i < status.length; i++) {
				where += " or " + FIELD_APKSTATUS + "=?";
				whereValue[i] = Integer.toString(status[i]);
			}
			Cursor cursor = db.query(TABLE_DOWNLOAD, null, where, whereValue,
					null, null, null);
			size = cursor.getCount();
			cursor.close();
			db.close();
		} catch (Exception e) {
			showErrLog(e.toString());
		}
		return size;
	}

	/**
	 * 通过包名查询APKName
	 * 
	 * @param apkPackageName
	 *            APK包名
	 * @return ApkName 如果没有查到返回空
	 */
	public synchronized String selectApkNameByPackageName(String apkPackageName) {
		String apkName = null;
		try {
			if (null == apkPackageName || apkPackageName == "") {
				showErrLog("selectApkIdByPackageName----apkPackageName为空");
				return null;
			}
			SQLiteDatabase db = this.getReadableDatabase();
			String where = FIELD_APKPACKAGENAME + "=?";
			String[] whereValue = { apkPackageName };
			Cursor cursor = db.query(TABLE_DOWNLOAD,
					new String[] { FIELD_APKNAME }, where, whereValue, null,
					null, null);

			if (cursor.moveToFirst()) {
				apkName = cursor.getString(0);
			}
			cursor.close();
			db.close();
		} catch (Exception e) {
			showErrLog(e.toString());
		}

		return apkName;
	}

	public synchronized ADownloadApkItem selectApkByPackageName(
			String packageName) {
		ADownloadApkItem aDownloadApkItem = null;
		try {
			SQLiteDatabase db = this.getReadableDatabase();
			String where = FIELD_APKPACKAGENAME + "=?";
			String[] whereValue = { packageName };
			Cursor cursor = db.query(TABLE_DOWNLOAD, null, where, whereValue,
					null, null, null);
			if (cursor.moveToFirst()) {
				aDownloadApkItem = getADownloadApkItemFromCursor(cursor);
			}
			cursor.close();
			db.close();

		} catch (Exception e) {
			showErrLog(e.toString());
		}
		return aDownloadApkItem;
	}

	/**
	 * 在下载表中添加一条记录
	 * 
	 * @param downloadAPKItem
	 * @return 返回-1即添加失败
	 */
	public synchronized int insertToDownload(ADownloadApkItem downloadAPKItem) {
		long id = -1;
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put(FIELD_APKID, downloadAPKItem.apkId);
			cv.put(FIELD_APKNAME, downloadAPKItem.apkName);
			cv.put(FIELD_APKURL, downloadAPKItem.apkUrl);
			cv.put(FIELD_APKICONURL, downloadAPKItem.apkIconUrl);
			cv.put(FIELD_APKSTATUS, downloadAPKItem.apkStatus);
			cv.put(FIELD_APKDOWNLOADSIZE, downloadAPKItem.apkDownloadSize);
			cv.put(FIELD_APKTOTALSIZE, downloadAPKItem.apkTotalSize);
			cv.put(FIELD_APKPACKAGENAME, downloadAPKItem.apkPackageName);
			cv.put(FIELD_APKVERSION, downloadAPKItem.apkVersion);
			cv.put(FIELD_APKVERSIONCODE, downloadAPKItem.apkVersionCode);
			cv.put(FIELD_APKCATEGORY, downloadAPKItem.category);
			id = db.insert(TABLE_DOWNLOAD, null, cv);
			db.close();

		} catch (Exception e) {
			showErrLog(e.toString());
		}
		return (int) id;
	}

	/**
	 * 在忽略表中添加一条记录
	 * 
	 * @param downloadAPKItem
	 * @return 返回-1即添加失败
	 */
	public synchronized int insertToIgnore(AIgnoreItem aIgnoreItem) {
		long id = -1;
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put(FIELD_APKICONURL, aIgnoreItem.apkIconUrl);
			cv.put(FIELD_APKNAME, aIgnoreItem.apkName);
			cv.put(FIELD_APKPACKAGENAME, aIgnoreItem.apkPackageName);
			cv.put(FIELD_APKURL, aIgnoreItem.apkUrl);
			cv.put(FIELD_APKVERSION, aIgnoreItem.apkVersion);
			cv.put(FIELD_APKVERSIONCODE, aIgnoreItem.apkVersionCode);
			cv.put(FIELD_APKTOTALSIZE, aIgnoreItem.apkTotalSize);
			id = db.insert(TABLE_IGNORE, null, cv);
			db.close();
		} catch (Exception e) {
			showErrLog(e.toString());
		}
		return (int) id;
	}

	/**
	 * 根据包名删除相应的忽略记录
	 * 
	 * @param downloadAPKItem
	 */
	public synchronized void deleteIgnoreByPackageName(String packageName) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			String where = FIELD_APKPACKAGENAME + "=?";
			String[] whereValue = { packageName };
			db.delete(TABLE_IGNORE, where, whereValue);
			db.close();
		} catch (Exception e) {
			showErrLog(e.toString());
		}
	}

	/**
	 * 根据状态删除相应的下载记录
	 * 
	 * @param downloadAPKItem
	 */
	public synchronized void deleteDownloadByApkStatus(int apkStatus) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			String where = FIELD_APKSTATUS + "=?";
			String[] whereValue = { Integer.toString(apkStatus) };
			db.delete(TABLE_DOWNLOAD, where, whereValue);
			db.close();
		} catch (Exception e) {
			showErrLog(e.toString());
		}
	}

	/**
	 * 根据包名删除相应的下载记录
	 * 
	 * @param downloadAPKItem
	 */
	public synchronized void deleteDownloadByApkPackageName(String packageName) {
		try {
			if (null == packageName) {
				showErrLog("delete---packageName为空");
				return;
			}
			SQLiteDatabase db = this.getWritableDatabase();
			String where = FIELD_APKPACKAGENAME + "=?";
			String[] whereValue = { packageName };
			db.delete(TABLE_DOWNLOAD, where, whereValue);
			db.close();
		} catch (Exception e) {
			showErrLog(e.toString());
		}
	}

	private ADownloadApkItem getADownloadApkItemFromCursor(Cursor cursor) {
		ADownloadApkItem aDownloadApkItem = new ADownloadApkItem();
		try {
			aDownloadApkItem.apkId = cursor.getInt(cursor
					.getColumnIndex(FIELD_APKID));
			aDownloadApkItem.apkName = cursor.getString(cursor
					.getColumnIndex(FIELD_APKNAME));
			aDownloadApkItem.apkUrl = cursor.getString(cursor
					.getColumnIndex(FIELD_APKURL));
			aDownloadApkItem.apkIconUrl = cursor.getString(cursor
					.getColumnIndex(FIELD_APKICONURL));
			aDownloadApkItem.apkDownloadSize = cursor.getInt(cursor
					.getColumnIndex(FIELD_APKDOWNLOADSIZE));
			showLog("selectApkByStatus.apkDownloadSize"
					+ aDownloadApkItem.apkDownloadSize);
			aDownloadApkItem.apkTotalSize = cursor.getInt(cursor
					.getColumnIndex(FIELD_APKTOTALSIZE));
			aDownloadApkItem.apkStatus = cursor.getInt(cursor
					.getColumnIndex(FIELD_APKSTATUS));
			aDownloadApkItem.apkVersion = cursor.getString(cursor
					.getColumnIndex(FIELD_APKVERSION));
			aDownloadApkItem.apkVersionCode = cursor.getInt(cursor
					.getColumnIndex(FIELD_APKVERSIONCODE));
			aDownloadApkItem.apkPackageName = cursor.getString(cursor
					.getColumnIndex(FIELD_APKPACKAGENAME));
			aDownloadApkItem.category = cursor.getInt(cursor
					.getColumnIndex(FIELD_APKCATEGORY));
		} catch (Exception e) {
			showErrLog(e.toString());
		}
		return aDownloadApkItem;
	}

	private void showLog(String msg) {
		Log.i("DownloadApkDBHelper", msg);
	}

	private void showErrLog(String msg) {
		Log.e("DownloadApkDBHelper", msg);
	}
}