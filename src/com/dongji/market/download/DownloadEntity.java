package com.dongji.market.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.pojo.ApkItem;

/**
 * 用于处理 apk 的下载
 * @author ZhangKai
 */
public class DownloadEntity implements Runnable, DownloadConstDefine, Parcelable {
	public String appName;   // 应用名称
	public int appId;   // 应用id
	public int category;   // 应用分类id
	public long currentPosition;    // 当前下载进度值
	public long fileLength;    // apk文件总大小
	public String packageName;    // 包名
	public int versionCode;   // 版本号
	private int status;   // 下载状态值
	public String url;   // 下载地址
	private static final int REQUEST_TIME_OUT = 10000;   // 请求超时时间
	private static final String REQUEST_METHOD_GET = "GET";   // 请求方式
	public int downloadType;
	public String versionName;
	private int retryNum;
	public String iconUrl;  // 图标地址
	public String installedVersionName;   // 已安装的软件版本名
	public long installedFileLength;   // 已安装的软件大小
	public Drawable installedIcon;   // 已安装的软件图标
	public int installedVersionCode;  // 已安装的软件版本号
	public int heavy;
	
	private boolean isOver=true;
	
	private OnDownloadListener listener;
	
	public DownloadEntity() {
		super();
	}
	
	public DownloadEntity(ApkItem item) {
		this.appName=item.appName;
		this.appId=item.appId;
		this.category=item.category;
		this.fileLength=item.fileSize;
		this.packageName=item.packageName;
		this.versionCode=item.versionCode;
		this.url=item.apkUrl;
		this.versionName=item.version;
		this.iconUrl=item.appIconUrl;
		if(item.status==AConstDefine.STATUS_APK_UNINSTALL) {
			this.downloadType=TYPE_OF_DOWNLOAD;
		}else if(item.status==AConstDefine.STATUS_APK_UNUPDATE) {
			this.downloadType=TYPE_OF_UPDATE;
		}
		this.heavy = item.heavy;
	}

	@Override
	public void run() {
		isOver=false;
//		status = STATUS_OF_DOWNLOADING;
		setStatus(STATUS_OF_DOWNLOADING);
		
		InputStream is=null;
		RandomAccessFile randomAccess=null;
		try {
			is = getInputStream();
			if(is!=null) {
				randomAccess=startDownload(is);
			}
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException "+e);
//			status = STATUS_OF_EXCEPTION;
			status = STATUS_OF_PAUSE;
		} catch(FileNotFoundException e) {
//			status = STATUS_OF_EXCEPTION;
			status = STATUS_OF_PAUSE;
			System.out.println("FileNotFoundException:"+e+", "+getPrepareAbsoluteFilePath());
		} catch (IOException e) {
			System.out.println("IOException:"+e);
//			status = STATUS_OF_EXCEPTION;
			status = STATUS_OF_PAUSE;
		} finally {
			postExecute();
			try{
				if(is!=null) {
					is.close();
				}
				if(randomAccess!=null) {
					randomAccess.close();
				}
			} catch (IOException e) {
				System.out.println("close exception:"+e);
			}
			isOver=true;
//			System.out.println(appName+" over "+isOver);
		}
	}

	/**
	 * 获取链接
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private InputStream getInputStream() throws MalformedURLException, IOException {
		if(!AndroidUtils.isSdcardExists()) {
			return null;
		}else {
			File directory=new File(DOWNLOAD_ROOT_PATH);
			if(!directory.exists()) {
				directory.mkdirs();
			}
		}
		
		boolean isComplete=AndroidUtils.checkFileExists(getPostAbsoluteFilePath());
		if(isComplete) {
			return null;
		}
		boolean exists=AndroidUtils.checkFileExists(getPrepareAbsoluteFilePath());
		// 容错处理：如果此文件被删除，则重新开始下载
		if (currentPosition > 0 && !exists) {
//			System.out.println(appName+" file not exists!");
			currentPosition = 0;
		}
		
//		System.out.println(appName+" start currentPosition:"+currentPosition+", "+Thread.currentThread().getName()+", "+Thread.activeCount());
		
		URL mUrl=new URL(url);
		HttpURLConnection httpConnection=(HttpURLConnection)mUrl.openConnection();
		httpConnection.setConnectTimeout(REQUEST_TIME_OUT);
		httpConnection.setDoInput(true);
		httpConnection.setRequestMethod(REQUEST_METHOD_GET);
		httpConnection.setRequestProperty("Range","bytes="+currentPosition + "-" );   // 断点续传
		InputStream is=httpConnection.getInputStream();
		fileLength = currentPosition + httpConnection.getContentLength();   // 获取下载文件总大小
		return is;
	}
	
	/**
	 * 开始执行下载
	 * @param is
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private RandomAccessFile startDownload(InputStream is) throws FileNotFoundException, IOException {
		RandomAccessFile randomAccessFile=new RandomAccessFile(getPrepareAbsoluteFilePath(), "rw");
		randomAccessFile.seek(currentPosition);
		byte[] data=new byte[1024*4];
//		isRunning=true;
		int num=0;
//		status = STATUS_OF_DOWNLOADING;
		
//		System.out.println("start download "+appName);
		
		int networkType=NetTool.getNetWorkType(DownloadService.mDownloadService);
		boolean useMobileGprs=false;
		if (networkType == 3) {
			useMobileGprs=true;
		}
		while(status==STATUS_OF_DOWNLOADING && (num=is.read(data))!=-1) {
//			System.out.println("thread name:"+Thread.currentThread().getName()+", "+isRunning);
			if(useMobileGprs) {
				if(!DownloadService.mDownloadService.addGprsTraffic(num)) {
					status = STATUS_OF_PAUSE_ON_TRAFFIC_LIMIT;
					break;
				}
			}
			randomAccessFile.write(data, 0, num);
			currentPosition+=num;
		}
		return randomAccessFile;
	}
	
	/**
	 * 下载完成
	 */
	private void postExecute() {
		synchronized (this) {
//			System.out.println(appName+" over "+currentPosition+", "+fileLength+", "+status+", "+Thread.currentThread().getName()+", "+Thread.activeCount());
			if (currentPosition == fileLength) {
//			if (status == STATUS_OF_DOWNLOADING) {
				status = STATUS_OF_COMPLETE;
//				setStatus(STATUS_OF_COMPLETE);
				completeRenameFile();
//			}
			}else if(status==STATUS_OF_EXCEPTION) {
				// retry
				notifyDownloadChange();
			} else if (status == STATUS_OF_PAUSE
					|| status == STATUS_OF_PAUSE_ON_TRAFFIC_LIMIT) {
				notifyDownloadChange();
			}/*else if(!isRunning){
				System.out.println(appName+" post pause!");
				status=STATUS_OF_PAUSE;
			}*/else if(status==STATUS_OF_PAUSE_ON_EXIT_SYSTEM){
				
			}else if(status==STATUS_OF_INITIAL) {
				currentPosition = 0;
				notifyDownloadChange();
			}else {
				status=STATUS_OF_PAUSE;
				System.out.println(appName+" download post error!");
			}
		}
	}
	
	public synchronized boolean canDownload() {
//		if(status!=STATUS_OF_DOWNLOADING && !isRunning) { // && Thread.currentThread().getName().equals("main")status==STATUS_OF_PREPARE &&
		if(status!=STATUS_OF_DOWNLOADING && isOver) {
			return true;
		}
		return false;
	}
	
	public synchronized boolean canPause() {
		if(status==STATUS_OF_DOWNLOADING && status!=STATUS_OF_PAUSE) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean equals(Object o) { 
		if(o==null) {
			return false;
		}else if(o instanceof DownloadEntity){
			DownloadEntity entity=(DownloadEntity)o;
			if(hashCode()==entity.hashCode()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return packageName.hashCode()+versionCode;
	}
	
	/**
	 * 重命名下载文件
	 */
	private void completeRenameFile() {
		File file=new File(getPrepareAbsoluteFilePath());
		File apkFile=new File(getPostAbsoluteFilePath());
		if(file.exists() && !apkFile.exists()) {
			boolean flag=file.renameTo(apkFile);
//			System.out.println(appName+" rename:"+flag+", "+file.getPath()+", "+apkFile.getPath()+", "+status+", "+listener);
			notifyDownloadChange();
		}else if(apkFile.exists()) {
			notifyDownloadChange();
		}
	}
	
	/**
	 * 获取下载前的文件地址
	 * @return
	 */
	private String getPrepareAbsoluteFilePath() {
		String path="";
		path = DOWNLOAD_ROOT_PATH + hashCode() + DOWNLOAD_FILE_PREPARE_SUFFIX;
		return path;
	}
	
	/**
	 * 获取下载完成后的文件地址
	 * @return
	 */
	private String getPostAbsoluteFilePath() {
		String path="";
		if(status==STATUS_OF_COMPLETE) {
			path = DOWNLOAD_ROOT_PATH + hashCode() + DOWNLOAD_FILE_POST_SUFFIX;
		}
		return path;
	}
	
	private void notifyDownloadChange() {
		if(listener!=null) {
			listener.onDownloadStatusChanged(this);
		}
	}
	
	public void setRunning(boolean isRunning) {
//		this.isRunning=isRunning;
	}
	
	/**
	 * 设置下载状态值
	 * @param status
	 */
	public synchronized void setStatus(int status) {
//		synchronized (this) {
			if ((this.currentPosition == this.fileLength && this.currentPosition > 0)
					|| (this.status == STATUS_OF_COMPLETE && this.currentPosition > 0)) {
				this.status = STATUS_OF_COMPLETE;
				return;
			}
			switch(status) {
			case STATUS_OF_INITIAL:
				this.currentPosition = 0;
//				isRunning = true;
				this.status=status;
				break;
			case STATUS_OF_PAUSE:
//				isRunning=false;
				this.status=status;
//				System.out.println(appName+" set pause!");
				break;
			case STATUS_OF_PREPARE:
//				isRunning=false;
				if(isOver) {
//					System.out.println(appName+" set prepare "+isOver);
					this.status=status;
				}
				break;
				default:
					this.status=status;
					break;
			}
//		}
	}
	
	/**
	 * 重置
	 */
	public boolean reset() {
		if(this.status==STATUS_OF_INITIAL && currentPosition==0) {
			return false;
		}
		this.status = STATUS_OF_INITIAL;
		this.currentPosition=0;
		this.isOver=true;
		return true;
	}
	
	/**
	 * 获取下载状态值
	 * @return
	 */
	public synchronized int getStatus() {
		/*synchronized(this) {
			return this.status;
		}*/
		return this.status;
	}
	
	public void setOnDownloadListener(OnDownloadListener listener) {
		if(this.listener==null) {
			this.listener=listener;
		}
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(appName);
		dest.writeInt(appId);
		dest.writeInt(category);
		dest.writeLong(currentPosition);
		dest.writeLong(fileLength);
		dest.writeString(packageName);
		dest.writeInt(versionCode);
		dest.writeInt(status);
		dest.writeString(url);
		dest.writeString(iconUrl);
		dest.writeInt(downloadType);
		dest.writeString(versionName);
		
		dest.writeInt(status);
		dest.writeInt(downloadType);
		
		dest.writeString(installedVersionName);
		dest.writeLong(installedFileLength);
		dest.writeInt(installedVersionCode);
		dest.writeInt(heavy);
	}
	
	public static final Creator<DownloadEntity> CREATOR=new Creator<DownloadEntity>() {
		
		@Override
		public DownloadEntity[] newArray(int size) {
			return new DownloadEntity[size];
		}
		
		@Override
		public DownloadEntity createFromParcel(Parcel source) {
			DownloadEntity entity=new DownloadEntity();
			entity.appName=source.readString();
			entity.appId=source.readInt();
			entity.category=source.readInt();
			entity.currentPosition=source.readLong();
			entity.fileLength=source.readLong();
			entity.packageName=source.readString();
			entity.versionCode=source.readInt();
			entity.status=source.readInt();
			entity.url=source.readString();
			entity.iconUrl=source.readString();
			entity.downloadType=source.readInt();
			entity.versionName=source.readString();
			
			entity.status=source.readInt();
			entity.downloadType=source.readInt();
			
			entity.installedVersionName=source.readString();
			entity.installedFileLength=source.readLong();
			entity.installedVersionCode=source.readInt();
			entity.heavy=source.readInt();
			return entity;
		}
	};
}
