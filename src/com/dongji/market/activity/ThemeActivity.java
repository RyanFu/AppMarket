package com.dongji.market.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.tsz.afinal.FinalBitmap;

import org.json.JSONException;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.helper.DJMarketUtils;
import com.dongji.market.helper.WaterFallCell;
import com.dongji.market.helper.WaterFallItem;
import com.dongji.market.pojo.SubjectInfo;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.widget.LazyScrollView;
import com.dongji.market.widget.LazyScrollView.ScrollListener;

/**
 * 专题页面
 * 
 * @author yvon
 * 
 */
public class ThemeActivity extends BaseActivity implements OnClickListener {
	private final static int EVENT_REQUEST_DATA = 1;
	private static final int EVENT_NO_NETWORK_ERROR = 3;
	private static final int EVENT_REQUEST_DATA_ERROR = 4;
	private static final int COLUMN_COUNT = 2;// 列数
	private static final int MARGIN = 1;
	// 保存图片缓存路径
	public static String CACHE_PATH = Environment.getExternalStorageDirectory().getPath() + "/theme/imgcache/";
	private LinearLayout mContainer;
	private LazyScrollView mScrollView;
	private int mColumnWidth = 0;
	private int mScrollHeight = 0;
	private int mPicCount = 0;
	private FinalBitmap mFinalBitmap;
	// 记录每列的高度
	private int[] mColumHeight;
	private int scroll_height;
	// 每列图片的数量
	private int[] lineCellCount;
	// 存column_count列数组 存放每列每张图的总高度
	// 比如第一列第一张图高90 总高度为90、第一列第2张图高120 总高度为210...
	private HashMap<Integer, Integer>[] cellTotalHeight = null;
	private int[] screenTop;// 每列在屏幕上最顶部的图片索引
	private int[] screenBottom;// 每列在屏幕最底部的图片索引
	private MyHandler handler;
	private View mLoadingView;
	private View mLoadingProgressBar;
	private TextView mLoadingTextView;
	public List<SubjectInfo> subjectInfo;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		initFinalBitmap();
		setContentView(R.layout.activity_theme);
		initLoadingView();
		initHandler();
	}

	private void initFinalBitmap() {
		String rootPath = "";
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			if (Environment.getExternalStorageDirectory().canWrite())
				rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		} else {
			if (Environment.getDownloadCacheDirectory().canWrite()) {
				rootPath = Environment.getDownloadCacheDirectory().getAbsolutePath();
			} else {
				rootPath = Environment.getDataDirectory().getAbsolutePath() + "/data/com.dongji.market";
			}
		}
		rootPath = rootPath + "/theme/";

		File file = new File(rootPath);
		file.mkdirs();
		mFinalBitmap = FinalBitmap.create(this);
	}

	private void initLoadingView() {
		mLoadingView = findViewById(R.id.loadinglayout);
		mLoadingProgressBar = findViewById(R.id.loading_progressbar);
		mLoadingTextView = (TextView) findViewById(R.id.loading_textview);
		mLoadingView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mLoadingProgressBar.getVisibility() == View.GONE) {
					setPreLoading();
					handler.sendEmptyMessage(EVENT_REQUEST_DATA);
				}
				return false;
			}
		});
	}

	private void setPreLoading() {
		mLoadingView.setVisibility(View.VISIBLE);
		mLoadingProgressBar.setVisibility(View.VISIBLE);
		mLoadingTextView.setText(R.string.loading_txt);
	}

	private void initHandler() {
		HandlerThread mHandlerThread = new HandlerThread("HandlerThread");
		mHandlerThread.start();
		handler = new MyHandler(mHandlerThread.getLooper());
		handler.sendEmptyMessage(EVENT_REQUEST_DATA);
	}

	private class MyHandler extends Handler {
		MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case EVENT_REQUEST_DATA:
				try {
					fetchSubjectData();
					if (subjectInfo != null && subjectInfo.size() > 0) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								initView();
								initData(subjectInfo);
							}
						});
					}
				} catch (IOException e) {
					if (!DJMarketUtils.isNetworkAvailable(ThemeActivity.this)) {
						sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					} else {
						sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					}
				} catch (JSONException e) {
					if (!DJMarketUtils.isNetworkAvailable(ThemeActivity.this)) {
						sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
					} else {
						sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
					}
				}
				break;
			case EVENT_NO_NETWORK_ERROR:
				setErrorMessage(R.string.no_network_refresh_msg, R.string.no_network_refresh_msg2);
				break;
			case EVENT_REQUEST_DATA_ERROR:
				setErrorMessage(R.string.request_data_error_msg, R.string.request_data_error_msg2);
				break;
			}
		}

		private void fetchSubjectData() throws IOException, JSONException {
			subjectInfo = DataManager.newInstance().getAllSubject();
		}
	}

	private void initView() {
		this.mContainer = (LinearLayout) findViewById(R.id.like_ll);
		this.mScrollView = (LazyScrollView) findViewById(R.id.like_sv);
		this.mScrollView.setScrollListener(new ScrollListener() {

			@Override
			public void scrollToBottom() {
			}

			@Override
			public void onAutoLoad(int l, int t, int oldl, int oldt) {
				// 获取最外层ScrollView的高度
				scroll_height = mScrollView.getMeasuredHeight();
				// 下面一段代码起到垃圾回收的作用
				if (t > oldt) {// 向下滚动
					for (int k = 0; k < COLUMN_COUNT; k++) {
						// mContainer是个LinearLayout，那三列LinearLayout就是他的三个子控件
						LinearLayout localLinearLayout = (LinearLayout) mContainer.getChildAt(k);
						// 当用户滑动的Y轴大于每列最顶部图片所在的总高度（也就是每列的最顶部图片被隐藏）
						if (screenTop[k] < cellTotalHeight[k].size() + 1) {
							if (cellTotalHeight[k].get(screenTop[k]) != null) {
								if (t > cellTotalHeight[k].get(screenTop[k])) {
									// 为了更好的用户视觉，我决定将每列最顶部图片的上一张图片回收掉
									if (screenTop[k] - 2 > 0) {
										View view = localLinearLayout.getChildAt(screenTop[k] - 2);
										WaterFallCell cell = (WaterFallCell) view.findViewById(R.id.water_fall_item_cell);
//										cell.recycle();
									}
									// 既然顶部图片被隐藏了那么自然而然现在屏幕上的最顶部图片+1
									screenTop[k] += 1;
								}
							}
						}
						// 计算完屏幕上最顶部图片后再计算屏幕上每列最底部图片的索引值
						if (cellTotalHeight[k].get(screenBottom[k]) != null) {
							if (t + mScrollHeight > cellTotalHeight[k].get(screenBottom[k])) {
								if (screenBottom[k] + 1 <= lineCellCount[k]) {
									screenBottom[k] += 1;
								}
							}
						}
					}
				} else {// 向上滚动
					// 向下滚动回收的是顶部的图片，以此推理，向上滑动当然要回收底部图片了
					// 理解了向下滑动的思路，向上滑动就不做累述了
					for (int k = 0; k < COLUMN_COUNT; k++) {
						LinearLayout localLinearLayout = (LinearLayout) mContainer.getChildAt(k);
						if (screenTop[k] > 1) {
							if (cellTotalHeight[k].get(screenTop[k] - 1) > t) {
								screenTop[k] -= 1;
							}
						}
						if (screenBottom[k] != 0) {

							if (cellTotalHeight[k].get(screenBottom[k] - 1) != null) {
								if (cellTotalHeight[k].get(screenBottom[k] - 1) > t + scroll_height) {
									View view = localLinearLayout.getChildAt(screenBottom[k] - 1);
									WaterFallCell cell = (WaterFallCell) view.findViewById(R.id.water_fall_item_cell);
//									cell.recycle();
									screenBottom[k] -= 1;
								}
							}
						}
					}
				}
				if (t == 0) {
					for (int k = 0; k < COLUMN_COUNT; k++) {
						LinearLayout localLinearLayout = (LinearLayout) mContainer.getChildAt(k);
						for (int i = 0; i < 10; i++) {
							LinearLayout rl = (LinearLayout) localLinearLayout.getChildAt(i);
							if (rl != null) {
								WaterFallCell cell = (WaterFallCell) rl.findViewById(R.id.water_fall_item_cell);
								cell.reload();
							}
						}
					}
				}
			}

			@Override
			public void stopScroll(int nowY) {
				Message msg = mHandler.obtainMessage();
				msg.what = WaterFallCell.LOAD_PIC;
				msg.arg1 = nowY;
				mHandler.sendMessage(msg);
			}
		});
		Display display = this.getWindowManager().getDefaultDisplay();
		this.mColumnWidth = display.getWidth() / COLUMN_COUNT;
		this.mScrollHeight = display.getHeight();
		for (int i = 0; i < COLUMN_COUNT; i++) {
			LinearLayout ll = new LinearLayout(this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mColumnWidth, LayoutParams.WRAP_CONTENT);
			params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
			ll.setOrientation(LinearLayout.VERTICAL);
			ll.setLayoutParams(params);
			mContainer.addView(ll);
		}
	}

	private void initData(List<SubjectInfo> subjectInfo) {
		cellTotalHeight = new HashMap[COLUMN_COUNT];
		lineCellCount = new int[COLUMN_COUNT];
		screenTop = new int[COLUMN_COUNT];
		screenBottom = new int[COLUMN_COUNT];
		mColumHeight = new int[COLUMN_COUNT];
		for (int i = 0; i < mColumHeight.length; i++) {
			mColumHeight[i] = 0;
		}
		for (int i = 0; i < COLUMN_COUNT; i++) {
			lineCellCount[i] = 0;
			cellTotalHeight[i] = new HashMap();
		}
		getData(subjectInfo);
	}

	private void getData(List<SubjectInfo> subjectInfo) {
		List<WaterFallItem> list = new ArrayList<WaterFallItem>();
		for (SubjectInfo subject : subjectInfo) {
			String str = subject.subjectIconUrl;
			WaterFallItem item = new WaterFallItem();
			item.img = str.split(",")[0];
			item.imgheight = Integer.parseInt(str.split(",")[1]);
			item.imgwidth = mColumnWidth;
			list.add(item);
		}
		AddItem2Container(list, subjectInfo);
		mScrollView.completeLoad();
	}

	private void AddItem2Container(List<WaterFallItem> list, List<SubjectInfo> subjectInfo) {
		LayoutInflater inflater = LayoutInflater.from(this);
		for (int i = 0; i < list.size(); i++) {
			WaterFallItem item = list.get(i);
			mPicCount++;
			View contentView = inflater.inflate(R.layout.water_fall_item, null);
			WaterFallCell cell = (WaterFallCell) contentView.findViewById(R.id.water_fall_item_cell);
			cell.setTag(subjectInfo.get(i));
			cell.setOnClickListener(this);
			cell.setmFinalBitmap(mFinalBitmap);
			cell.setmColumnWidth(mColumnWidth);
			cell.setmItem(item);
			cell.setmCount(mPicCount);
			cell.setmContentView(contentView);
			cell.setmHandler(mHandler);
			cell.startResize();
		}

		if (mLoadingView.getVisibility() == View.VISIBLE) {
			mLoadingView.setVisibility(View.GONE);
		}
	}

	/**
	 * 数据获取异常处理
	 * 
	 * @param rId
	 */
	private void setErrorMessage(final int rId, final int rId2) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mLoadingView.getVisibility() == View.VISIBLE) {
					mLoadingProgressBar.setVisibility(View.GONE);
					mLoadingTextView.setText(rId);
				} else {
					DJMarketUtils.showToast(ThemeActivity.this, rId2);
				}
			}
		});
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WaterFallCell.ADD_INTO:
				if (msg.obj != null) {
					WaterFallCell cell = (WaterFallCell) msg.obj;
					int minColum = getMinHeightLL();
					cell.setmColumn(minColum);
					LinearLayout ll = (LinearLayout) mContainer.getChildAt(minColum);
					ll.addView(cell.getmContentView());
					cell.setmID(ll.getChildCount());
					mColumHeight[minColum] += cell.getmHeight() + getResources().getDimensionPixelSize(R.dimen.waterfall_cell_padding) + getResources().getDimensionPixelSize(R.dimen.waterfall_cell_padding);
					// minColum列的图片个数++
					lineCellCount[minColum] += 1;
					if (mColumHeight[minColum] <= mScrollHeight) {
						screenTop[minColum] = 1;
					} else if (screenBottom[minColum] == 0) {
						screenBottom[minColum] = lineCellCount[minColum];
					}
					// 记录加上该张图片后，该张图片在该列中的总高度
					cellTotalHeight[minColum].put(lineCellCount[minColum], mColumHeight[minColum]);
					mFinalBitmap.display(cell, cell.getmItem().img);
				}
				break;
			case WaterFallCell.LOAD_PIC:
				for (int i = 0; i < COLUMN_COUNT; i++) {
					LinearLayout localLinearLayout = (LinearLayout) mContainer.getChildAt(i);
					for (int k = screenTop[i] - 1; k < screenBottom[i] + 1; k++) {
						if (k - 1 > 0) {
							View view = localLinearLayout.getChildAt(k - 1);
							WaterFallCell cell = (WaterFallCell) view.findViewById(R.id.water_fall_item_cell);
							cell.reload();
						}
					}
				}
				break;
			}
		}

	};

	/**
	 * 获取最小高度的LL
	 * 
	 * @return
	 */
	private int getMinHeightLL() {
		int m = 0;
		int length = mColumHeight.length;
		for (int i = 0; i < length; i++) {
			if (mColumHeight[i] < mColumHeight[m]) {
				m = i;
			}
		}
		return m;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.water_fall_item_cell:
			SubjectInfo subjectInfo = (SubjectInfo) v.getTag();
			startThemeList(subjectInfo);
			break;
		default:
			break;
		}
	}

	private void startThemeList(SubjectInfo info) {
		Intent intent = new Intent(this, ThemeListActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("subjectInfo", info);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@Override
	public void onAppClick() {

	}

	@Override
	public void onGameClick() {

	}

	@Override
	public boolean isAppClicked() {
		return false;
	}

	@Override
	public void onAppInstallOrUninstallDone(int status, PackageInfo info) {

	}

	@Override
	public void onAppStatusChange(boolean isCancel, String packageName, int versionCode) {

	}

	@Override
	protected void onUpdateDataDone() {

	}

	@Override
	protected void loadingImage() {
	}

}
