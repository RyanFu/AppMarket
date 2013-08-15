package com.dongji.market.activity;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.myjson.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.dongji.market.R;
import com.dongji.market.adapter.ImageGalleryAdapter;
import com.dongji.market.adapter.ListBaseAdapter;
import com.dongji.market.adapter.ListMultiTemplateAdapter;
import com.dongji.market.adapter.ListSingleTemplateAdapter;
import com.dongji.market.download.AConstDefine;
import com.dongji.market.helper.AndroidUtils;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.ChannelListInfo;
import com.dongji.market.pojo.NavigationInfo;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.widget.ScrollListView;

/**
 * 编辑推荐
 * @author zhangkai
 *
 */
public class ChoicenessActivity extends BaseActivity implements OnItemClickListener, OnScrollListener, ScrollListView.OnScrollTouchListener {
	private ImageGalleryAdapter mGalleryAdapter;
	private static final int EVENT_ROTATE = 1;
	private static final int EVENT_REQUEST_BANNER_DATA = 2;
	private static final int EVENT_REQUEST_APPLIST_DATA = 3;
	private static final int EVENT_REQUEST_GAMELIST_DATA = 4;
	private static final int EVENT_NO_NETWORK_ERROR = 5;
	private static final int EVENT_REQUEST_DATA_ERROR = 6;
	private static final int EVENT_REFRENSH_DATA = 8;
	private static final int EVENT_ADD_ADAPTER_DATA = 9;
	private static final int EVENT_REQUEST_NAVIGATION = 10;
	private static final int EVENT_DEFOULT_IMAGE = 11;
	private static final int EVENT_COLLECT_DEVICE_INFO = 12;
	
	private static final int SCROLL_DVALUE = 1;
	
	private static final long ROTATE_TIME = 2000L;
	private Gallery mImageGallery;
	private MyHandler mHandler;
	private List<ApkItem> bannerList;
	private FrameLayout mHeaderView;
	
	private ScrollListView mAppListView;
	
	private ScrollListView mGameListView;
	
	private View mLoadingView;
	private View mLoadingProgressBar;
	private TextView mLoadingTextView;
	
	private List<ApkItem> apps;
	private List<ApkItem> games;
	
	private boolean isSingleRow=true;
	private boolean isLoading;
	private boolean isAppClicked=true;
	private boolean isRequestDelay;
	
	private ListMultiTemplateAdapter mAppMultiAdapter;
	private ListSingleTemplateAdapter mAppSingleAdapter;
	
	private ListMultiTemplateAdapter mGameMultiAdapter;
	private ListSingleTemplateAdapter mGameSingleAdapter;
	
	private int currentAppPage;
	private int currentGamePage;
	
	private DataManager dataManager;
	
	private Context context;
	
	private boolean isFirstResume;
	
	private NavigationInfo navigationInfo;
	
	private boolean hasAppData = true;
	private boolean hasGameData = true;
	
	private int responseResult;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_template);
		context=this;
		initLoadingView();
		initHandler();
		initData();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		sendMessage();
		if(isFirstResume) {
			if(mHandler!=null) {
				if(mHandler.hasMessages(EVENT_REFRENSH_DATA)) {
					mHandler.removeMessages(EVENT_REFRENSH_DATA);
				}
				mHandler.sendEmptyMessage(EVENT_REFRENSH_DATA);
			}
		}
		isFirstResume=true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		removeMessage();
//		mHandler.sendEmptyMessageDelayed(EVENT_DEFOULT_IMAGE, 1500L);
	}
	
	private void initView() {
		if(mHeaderView==null) {
			initHeaderView();
		}
		initAppListView();
	}
	
	/**
	 * 初始化应用列表
	 */
	private void initAppListView() {
		mAppListView=(ScrollListView)findViewById(R.id.applistview);
		mAppListView.addHeaderView(mHeaderView, null, false);
		if(isSingleRow) {
			mAppSingleAdapter=new ListSingleTemplateAdapter(this, apps, isRemoteImage);
			mAppListView.setAdapter(mAppSingleAdapter);
		}else {
			mAppMultiAdapter=new ListMultiTemplateAdapter(this, apps);
			mAppListView.setAdapter(mAppMultiAdapter);
		}
		if(isSingleRow) {
			mAppListView.setOnItemClickListener(this);
		}
//		mAppListView.setOnScrollListener(this);
		mAppListView.setOnScrollTouchListener(this);
		mAppListView.setVisibility(View.VISIBLE);
//		getParentActivity().setListViewSlide(mAppListView);
	}
	
	private void initData() {
		dataManager=DataManager.newInstance();
//		Bundle bundle=getIntent().getExtras();
//		if(bundle!=null) {
//			navigationInfo=bundle.getParcelable("navigation");
//			if(navigationInfo!=null) {
				mHandler.sendEmptyMessage(EVENT_REQUEST_BANNER_DATA);
//			}else {
//				mHandler.sendEmptyMessage(EVENT_REQUEST_NAVIGATION);
//			}
//		}
	}
	
	private void initLoadingView() {
		
		mLoadingView=findViewById(R.id.loadinglayout);
		mLoadingProgressBar=findViewById(R.id.loading_progressbar);
		mLoadingTextView=(TextView)findViewById(R.id.loading_textview);
		mLoadingView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(mLoadingProgressBar.getVisibility()==View.GONE) {
					setPreLoading();
//					if(navigationInfo==null) {
//						mHandler.sendEmptyMessage(EVENT_REQUEST_NAVIGATION);
//					}else {
						if(isAppClicked && hasAppData) {
							mHandler.sendEmptyMessage(EVENT_REQUEST_BANNER_DATA);
						}else if(!isAppClicked && hasGameData) {
							mHandler.sendEmptyMessage(EVENT_REQUEST_BANNER_DATA);
						}
//					}
				}
				return false;
			}
		});
	}
	
	private void initGameListView() {
		mGameListView=(ScrollListView)findViewById(R.id.gamelistview);
		if(mHeaderView==null) {
			initHeaderView();
		}
		mGameListView.addHeaderView(mHeaderView, null, false);
		if(isSingleRow) {
			mGameSingleAdapter=new ListSingleTemplateAdapter(this, games, isRemoteImage);
			mGameListView.setAdapter(mGameSingleAdapter);
		}else {
			mGameMultiAdapter=new ListMultiTemplateAdapter(this, games);
			mGameListView.setAdapter(mGameMultiAdapter);
		}
		if(isSingleRow) {
			mGameListView.setOnItemClickListener(this);
		}
//		mGameListView.setOnScrollListener(this);
		if(mAppListView!=null) {
			mAppListView.setVisibility(View.GONE);
		}
		mGameListView.setOnScrollTouchListener(this);
		mGameListView.setVisibility(View.VISIBLE);
//		getParentActivity().setListViewSlide(mGameListView);
	}
	
	private void initHeaderView() {
		mHeaderView=(FrameLayout)LayoutInflater.from(this).inflate(R.layout.layout_choiceness_header, null);
//		mHeaderView=(FrameLayout)findViewById(R.id.choiceness_header);
//		int margin=initHeaderIndicateMargin();
//		mHeaderView.addView(getIndicateImageView(true, margin));
//		mHeaderView.addView(getIndicateImageView(false, margin));
		mImageGallery=(Gallery)mHeaderView.findViewById(R.id.choicenessgallery);
		mGalleryAdapter=new ImageGalleryAdapter(this, bannerList);
		mImageGallery.setAdapter(mGalleryAdapter);
		int count=bannerList.size();
		if(count>0) {
			int i=(Integer.MAX_VALUE/2);
			int num=i%count;
			if(num!=0) {
				i-=num;
			}
			mImageGallery.setSelection(i);
		}
		initSwitchIdResources(count, mHeaderView);
		mImageGallery.setOnItemSelectedListener(new ItemSelectedListener(count));
		mImageGallery.setOnItemClickListener(this);
		mImageGallery.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						mHandler.sendEmptyMessageDelayed(EVENT_ROTATE, ROTATE_TIME);
						break;
						default:
							mHandler.removeMessages(EVENT_ROTATE);
							break;
						
					
				}  
				return false;
			}
		});
		getParentActivity().setInterceptRange(mImageGallery);
	}
	
	private void initHandler() {
		HandlerThread mHandlerThread=new HandlerThread("handler");
		mHandlerThread.start();
		mHandler=new MyHandler(mHandlerThread.getLooper());
		mHandler.sendEmptyMessage(EVENT_COLLECT_DEVICE_INFO);
	}
	
	private boolean requestBannerData() {
		try {
			bannerList=dataManager.getBanners();
			if(bannerList!=null && bannerList.size()>0)
				return true;
		/*} catch (IOException e) {
			System.out.println(e);
			if(!AndroidUtils.isNetworkAvailable(context)) {
				mHandler.sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
			}else {
				mHandler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
			}*/
		} catch (JSONException e) {
			System.out.println(e);
			mHandler.sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
		}
		return false;
	}
	
	private void setDisplayVisible() {
		mLoadingView.setVisibility(View.GONE);
		if(isAppClicked) {
			if (mAppListView != null) {
				mAppListView.setVisibility(View.VISIBLE);
			}
			if(mGameListView!=null) {
				mGameListView.setVisibility(View.GONE);
			}
		}else {
			if(mGameListView!=null) {
				mGameListView.setVisibility(View.VISIBLE);
			}
			if (mAppListView != null) {
				mAppListView.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		Intent intent=new Intent(context, ApkDetailActivity.class);
		Bundle bundle=new Bundle();
		switch(parent.getId()) {
			case R.id.applistview:
				ApkItem item=mAppSingleAdapter.getApkItemByPosition(position-1);
				System.out.println(item.appName+", "+item.status);
				bundle.putParcelable("apkItem", item);
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
				break;
			case R.id.gamelistview:
				bundle.putParcelable("apkItem", mGameSingleAdapter.getApkItemByPosition(position-1));;
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
				break;
			case R.id.choicenessgallery:
				intent=new Intent(this, ApkDetailActivity.class);
				position=position%bannerList.size();
				bundle.putParcelable("apkItem", bannerList.get(position));
				intent.putExtras(bundle);
				startActivity(intent);
				break;
		}
	}
	
	public boolean moveN(Gallery g) {
		try {
			Method method = g.getClass().getDeclaredMethod("moveNext",
					new Class[] {});
			method.setAccessible(true);
			return (Boolean) method.invoke(g, null);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}  
	
	private class MyHandler extends Handler {
		public MyHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case EVENT_ROTATE:
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mImageGallery.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
//							moveN(mImageGallery);
							
							sendEmptyMessageDelayed(EVENT_ROTATE, ROTATE_TIME);
						}
					});
					break;
				case EVENT_REQUEST_BANNER_DATA:
					if(requestBannerData()) {
						if(isAppClicked) {
							sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA);
						}else {
							sendEmptyMessage(EVENT_REQUEST_GAMELIST_DATA);
						}
					}else {
						if(!AndroidUtils.isNetworkAvailable(context)) {
							sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
						}else {
							sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
						}
					}
					break;
				case EVENT_REQUEST_APPLIST_DATA:
					try {
//						apps=dataManager.getApps(context, navigationInfo, true);
						apps=dataManager.getApps(context, DataManager.EDITOR_RECOMMEND_ID, true);
						System.out.println("apps ===> " + apps);
						apps=setApkStatus(apps);
					} catch (JSONException e) {
						System.out.println(e);
						sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
						break;
					}
					if(apps!=null && apps.size()>0) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if(currentAppPage==0) {
									currentAppPage=1;
									initView();
									mLoadingView.setVisibility(View.GONE);
									if(!mHandler.hasMessages(EVENT_ROTATE)) {
										ChoicenessActivity.this.sendMessage();
									}
//									sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA);
//									isLoading=true;
								}else {
									getParentActivity().onProgressBarDone();
									if(isRequestDelay) {
										addAdapterData();
										sendEmptyMessage(EVENT_REQUEST_APPLIST_DATA);
										isRequestDelay=false;
									}
									currentAppPage++;
									isLoading=false;
								}
							}
						});
					}else {
						isLoading=false;
						if(!AndroidUtils.isNetworkAvailable(context)) {
							sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
						}else {
							sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
						}
					}
					break;
				case EVENT_REQUEST_GAMELIST_DATA:
					try {
//						games=dataManager.getApps(context, navigationInfo, false);
						games=dataManager.getApps(context, DataManager.EDITOR_RECOMMEND_ID, false);
						games=setApkStatus(games);
					/*} catch (IOException e) {
						System.out.println(e);
						if(!AndroidUtils.isNetworkAvailable(context)) {
							sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
						}else {
							sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
						}
						break;*/
					} catch (JSONException e) {
						System.out.println(e);
						sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
						break;
					}
					if(games!=null && games.size()>0) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if(currentGamePage==0) {
									currentGamePage=1;
									initGameListView();
									mLoadingView.setVisibility(View.GONE);
									if(!mHandler.hasMessages(EVENT_ROTATE)) {
										ChoicenessActivity.this.sendMessage();
									}
//									sendEmptyMessage(EVENT_REQUEST_GAMELIST_DATA);
//									isLoading=true;
								}else {
									getParentActivity().onProgressBarDone();
									if(isRequestDelay) {
										addAdapterData();
										sendEmptyMessage(EVENT_REQUEST_GAMELIST_DATA);
										isRequestDelay=false;
									}
									currentGamePage++;
									isLoading=false;
								}
							}
						});
					}else {
						isLoading=false;
						if(!AndroidUtils.isNetworkAvailable(context)) {
							sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
						}else {
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
				case EVENT_REFRENSH_DATA:
					/*System.out.println("setdefault iamge:"+mHandler.hasMessages(EVENT_DEFOULT_IMAGE));
					if(hasMessages(EVENT_DEFOULT_IMAGE)) {
						removeMessages(EVENT_DEFOULT_IMAGE);
					}*/
					refreshData();
					if (responseResult != 1) {
						mHandler.sendEmptyMessage(EVENT_COLLECT_DEVICE_INFO);
					}
					break;
				case EVENT_ADD_ADAPTER_DATA:
					addAdapterData();
					break;
				case EVENT_REQUEST_NAVIGATION:
//					requestNavigationData();
					if(navigationInfo!=null) {
						sendEmptyMessage(EVENT_REQUEST_BANNER_DATA);
					}else {
						if(!AndroidUtils.isNetworkAvailable(context)) {
							sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
						}else {
							sendEmptyMessage(EVENT_REQUEST_DATA_ERROR);
						}
					}
					break;
				case EVENT_DEFOULT_IMAGE:
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							setDefaultImage();
						}
					});
					break;
				case EVENT_COLLECT_DEVICE_INFO:
					try {
						if (AndroidUtils.isNetworkAvailable(context)) {
							responseResult = DataManager.newInstance().collectLocalData(context);
						} else {
							sendEmptyMessage(EVENT_NO_NETWORK_ERROR);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
			}
		}
	}
	
/*	private void requestNavigationData() {
		DataManager dataManager=DataManager.newInstance();
		try {
			ArrayList<NavigationInfo> navigationList=dataManager.getNavigationList();
			if(navigationList!=null && navigationList.size()>0) {
				getParentActivity().setNavigationList(navigationList);
				navigationInfo=navigationList.get(0);
			}
		}catch(JSONException e) {
			System.out.println("choiceness request navigation error!");
		}
	}*/
	
	/**
	 * 数据请求错误处理
	 * @param rId
	 * @param rId2
	 */
	private void setErrorMessage(final int rId, final int rId2) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getParentActivity().stopProgressBar();
				isLoading=false;
				if(mLoadingView.getVisibility()==View.VISIBLE) {
					mLoadingProgressBar.setVisibility(View.GONE);
					mLoadingTextView.setText(rId);
				}else {
					AndroidUtils.showToast(context, rId2);
				}
			}
		});
	}
	
	private void setPreLoading() {
		mLoadingView.setVisibility(View.VISIBLE);
		mLoadingProgressBar.setVisibility(View.VISIBLE);
		mLoadingTextView.setText(R.string.loading_txt);
	}
	
	private void sendMessage() {
		removeMessage();
		if(mHandler!=null && mGalleryAdapter!=null) {
			mHandler.sendEmptyMessageDelayed(EVENT_ROTATE, ROTATE_TIME);
		}
	}
	
	private void removeMessage() {
		if(mHandler!=null && mHandler.hasMessages(EVENT_ROTATE)) {
			mHandler.removeMessages(EVENT_ROTATE);
		}
	}
	
	private void setDefaultImage() {
		if(mImageGallery!=null) {
			mGalleryAdapter.setDisplayNotify(false);
		}
		if(mAppSingleAdapter!=null) {
			mAppSingleAdapter.setDisplayNotify(false);
		}
		if(mGameSingleAdapter!=null) {
			mGameSingleAdapter.setDisplayNotify(false);
		}
	}
	
	/**
	 * 指示按钮
	 */
	private LinearLayout mSwithBtnContainer;
	/**
	 * 初始化海报指示标
	 * @param count
	 */
	private void initSwitchIdResources(int count, View mHeaderView) {
		mSwithBtnContainer = (LinearLayout) mHeaderView.findViewById(R.id.switcherbtn_container);

		ImageView localImageView = null;
		int num=AndroidUtils.dip2px(this, 10.0f);
		SwitchBtnClickListener localSwitchBtnClickListener = new SwitchBtnClickListener();
		LinearLayout.LayoutParams localLayoutParams = new LinearLayout.LayoutParams(
				num, num);
		localLayoutParams.leftMargin = 5;
		localLayoutParams.rightMargin = 5;

		for (int j = 0; j < count; j++) {
			localImageView = new ImageView(this);
			localImageView.setLayoutParams(localLayoutParams);
			localImageView
					.setBackgroundResource(R.drawable.selector_image_switcher);
			localImageView.setOnClickListener(localSwitchBtnClickListener);
			mSwithBtnContainer.addView(localImageView);
		}
	}

	/**
	 * 画廊点击联动指示按钮
	 * @author 
	 *
	 */
	public class ItemSelectedListener implements OnItemSelectedListener {

		int mCount;

		public ItemSelectedListener(int count) {
			super();
			mCount = count;
		}

		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view,
				int position, long id) {
			setSelectedSwitchBtn(position % mCount);
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {

		}
	}

	/**
	 * 切换按钮的点击事件响应
	 * @author Administrator
	 *
	 */
	public class SwitchBtnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			int position = mSwithBtnContainer.indexOfChild(v);
			setSelectedSwitchBtn(position);
			setSelectedGalleryImg(position);
		}
	}

	/**
	 * 设置聚焦指示按钮
	 */
	private ImageView mSelectedSwitchButton;
	private void setSelectedSwitchBtn(int paramInt) {
		if (this.mSelectedSwitchButton != null)
			this.mSelectedSwitchButton.setSelected(false);
		ImageView localImageView = (ImageView) this.mSwithBtnContainer
				.getChildAt(paramInt);
		this.mSelectedSwitchButton = localImageView;
		this.mSelectedSwitchButton.setSelected(true);
	}

	/**
	 * 设置聚焦画廊海报图片
	 * @param position
	 */
	private void setSelectedGalleryImg(int position) {
		if (mImageGallery != null) {
			if(position==Integer.MAX_VALUE-1) {
				int count=bannerList==null?0:bannerList.size();
				if(count>0) {
					int i=(Integer.MAX_VALUE/2);
					int num=i%count;
					if(num!=0) {
						i-=num;
					}
					mImageGallery.setSelection(i);
				}
			}else {
				mImageGallery.setSelection(position);
			}			
		}
	}
	
	private void addAdapterRunUiThread(final ListBaseAdapter mAdapter, final List<ApkItem> items) {
		mAdapter.addList(items);
		int what=0;
		if(isAppClicked) {
			what=EVENT_REQUEST_APPLIST_DATA;
		}else {
			what=EVENT_REQUEST_GAMELIST_DATA;
		}
		mHandler.sendEmptyMessage(what);
	}
	
	private void addAdapterData() {
		if(isAppClicked) {
			apps=setApkStatus(apps);
			if(isSingleRow) {
				addAdapterRunUiThread(mAppSingleAdapter, apps);
			}else {
				addAdapterRunUiThread(mAppMultiAdapter, apps);
			}
			apps.clear();
		}else {
			games=setApkStatus(games);
			if(isSingleRow) {
				addAdapterRunUiThread(mGameSingleAdapter, games);
			}else {
				addAdapterRunUiThread(mGameMultiAdapter, games);
			}
			games.clear();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		System.out.println("firstVisibleItem:"+firstVisibleItem+", visibleItemCount:"+visibleItemCount+", totalItemCount:"+totalItemCount);
		/*if (!isLoading
				&& firstVisibleItem + visibleItemCount >= totalItemCount
						- SCROLL_DVALUE) {
			isLoading=true;
			getParentActivity().showProgressBar();
			addAdapterData();
			getParentActivity().stopProgressBar();
		} else if (isLoading && !isRequestDelay
				&& firstVisibleItem + visibleItemCount >= totalItemCount
						- SCROLL_DVALUE) {
			isRequestDelay = true;
			getParentActivity().showProgressBar();
			System.out.println("request data delay!");
		}*/
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}
	
	private void displayLoading() {
		mLoadingView.setVisibility(View.VISIBLE);
		setPreLoading();
		if(mAppListView!=null) {
			mAppListView.setVisibility(View.GONE);
		}
		if(mGameListView!=null) {
			mGameListView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onAppClick() {
		if(!isAppClicked) {
			isAppClicked=true;
			getParentActivity().progressBarGone();
//			if(navigationInfo!=null) {
				if(currentAppPage==0) {
					displayLoading();
					mHandler.sendEmptyMessage(EVENT_REQUEST_BANNER_DATA);
				}else {
					setDisplayVisible();
					mHandler.sendEmptyMessage(EVENT_REFRENSH_DATA);
				}
//			}else {
//				mHandler.sendEmptyMessage(EVENT_REQUEST_NAVIGATION);
//			}
		}
	}

	@Override
	public void onGameClick() {
		if(isAppClicked) {
			isAppClicked=false;
			getParentActivity().progressBarGone();
//			if(navigationInfo!=null) {
				if(currentGamePage==0) {
					displayLoading();
					mHandler.sendEmptyMessage(EVENT_REQUEST_BANNER_DATA);
				}else {
					setDisplayVisible();
					mHandler.sendEmptyMessage(EVENT_REFRENSH_DATA);
				}
//			}else {
//				mHandler.sendEmptyMessage(EVENT_REQUEST_NAVIGATION);
//			}
		}
	}

	@Override
	public boolean isAppClicked() {
		return isAppClicked;
	}

	@Override
	public void onItemClick(ChannelListInfo info) {
	}

	@Override
	public void onAppStatusChange(boolean isCancel, String packageName, int versionCode) {
		if(mAppListView!=null && mAppListView.getAdapter()!=null) {
			if(isSingleRow) {
				mAppSingleAdapter.changeApkStatusByAppId(isCancel, packageName, versionCode);
			}else {
				mAppMultiAdapter.changeApkStatusByAppId(isCancel, packageName, versionCode);
			}
		}
		if(mGameListView!=null && mGameListView.getAdapter()!=null) {
			if(isSingleRow) {
				mGameSingleAdapter.changeApkStatusByAppId(isCancel, packageName, versionCode);
			}else {
				mGameMultiAdapter.changeApkStatusByAppId(isCancel, packageName, versionCode);
			}
		}
	}
	
	@Override
	public void onAppInstallOrUninstallDone(int status, PackageInfo info) {
		if(mAppListView!=null && mAppListView.getAdapter()!=null) {
			if(isSingleRow) {
				mAppSingleAdapter.changeApkStatusByPackageInfo(status, info);
			}else {
				mAppMultiAdapter.changeApkStatusByPackageInfo(status, info);
			}
		}
		if(mGameListView!=null && mGameListView.getAdapter()!=null) {
			if(isSingleRow) {
				mGameSingleAdapter.changeApkStatusByPackageInfo(status, info);
			}else {
				mGameMultiAdapter.changeApkStatusByPackageInfo(status, info);
			}
		}
	}
	
	private void notifyListData(final ListBaseAdapter mAdapter) {
//		initDownloadAndUpdateData();
		setApkStatus(mAdapter.getItemList());
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(mGalleryAdapter!=null) {
					mGalleryAdapter.setDisplayNotify(isRemoteImage);
				}
				mAdapter.setDisplayNotify(isRemoteImage);
			}
		});
	}
	
	private void refreshData() {
		if(mAppListView!=null && mAppListView.getAdapter()!=null) {
			if(isSingleRow) {
				notifyListData(mAppSingleAdapter);
			}else {
				notifyListData(mAppMultiAdapter);
			}
		}
		if(mGameListView!=null && mGameListView.getAdapter()!=null) {
			if(isSingleRow) {
				notifyListData(mGameSingleAdapter);
			}else {
				notifyListData(mGameMultiAdapter);
			}
		}
	}

	@Override
	protected void onUpdateDataDone() {
		refreshData();
	}

	@Override
	public void onScrollTouch(int scrollState) {
		switch(scrollState) {
			case ScrollListView.OnScrollTouchListener.SCROLL_BOTTOM:
//				getParentActivity().scrollOperation(true);
				break;
			case ScrollListView.OnScrollTouchListener.SCROLL_TOP:
//				getParentActivity().scrollOperation(false);
				break;
		}
	}
	
	@Override
	public ListView[] getListViews() {
		return new ListView[]{mAppListView, mGameListView};
	}

	@Override
	protected void loadingImage() {
		if(mGalleryAdapter!=null) {
			mGalleryAdapter.setDisplayNotify(isRemoteImage);
		}
		if(mAppSingleAdapter!=null) {
			mAppSingleAdapter.setDisplayNotify(isRemoteImage);
		}
		if(mGameSingleAdapter!=null) {
			mGameSingleAdapter.setDisplayNotify(isRemoteImage);
		}
	}
	
	private int locStep;
	private void listViewFromTop() {
		if(isAppClicked()) {
			if(mAppListView!=null) {
//				if (!mAppListView.isStackFromBottom()) {
//					mAppListView.setStackFromBottom(true);
//				}
//				mAppListView.setStackFromBottom(false);
				locStep = (int) Math.ceil(mAppListView.getFirstVisiblePosition()/AConstDefine.AUTO_SCRLL_TIMES);
				mAppListView.post(appAutoScroll);
			}
		}else {
			if(mGameListView!=null) {
//				if (!mGameListView.isStackFromBottom()) {
//					mGameListView.setStackFromBottom(true);
//				}
//				mGameListView.setStackFromBottom(false);
				locStep = (int) Math.ceil(mGameListView.getFirstVisiblePosition()/AConstDefine.AUTO_SCRLL_TIMES);
				mGameListView.post(gameAutoScroll);
			}
		}
	}
	
	@Override
	public void OnToolBarClick() {
		listViewFromTop();
	}
	
	Runnable appAutoScroll = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mAppListView.getFirstVisiblePosition() > 0) {
				if (mAppListView.getFirstVisiblePosition() < AConstDefine.AUTO_SCRLL_TIMES) {
					mAppListView.setSelection(mAppListView
							.getFirstVisiblePosition() - 1);
				} else {
					mAppListView.setSelection(Math.max(mAppListView.getFirstVisiblePosition() - locStep, 0));
				}
				mAppListView.post(this);
			}
			return;
		}
	};
	
	Runnable gameAutoScroll = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mGameListView.getFirstVisiblePosition() > 0) {
				if (mGameListView.getFirstVisiblePosition() < AConstDefine.AUTO_SCRLL_TIMES) {
					mGameListView.setSelection(mGameListView
							.getFirstVisiblePosition() - 1);
				} else {
					mGameListView.setSelection(Math.max(mGameListView.getFirstVisiblePosition() - locStep, 0));
				}
				// mAppListView.postDelayed(this, 1);
				mGameListView.post(this);
			}
			return;
		}
	};
}
