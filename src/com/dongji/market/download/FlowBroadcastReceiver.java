package com.dongji.market.download;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;

import com.dongji.market.R;
import com.dongji.market.widget.CustomNoTitleDialog;
import com.dongji.market.widget.SettingFlowDialog;

public class FlowBroadcastReceiver implements AConstDefine {
	private Activity activity;
	private CustomNoTitleDialog mFlowSettingDialog;

	public FlowBroadcastReceiver(Activity activity) {
		this.activity = activity;
	}

	private MyBroadcastReceiver myBroadcastReceiver;

	public void registerMyReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BROADCAST_ACTION_NOFLOW);
		intentFilter.addAction(CANCELNOFLOWDIALOG);
		myBroadcastReceiver = new MyBroadcastReceiver();
		activity.registerReceiver(myBroadcastReceiver, intentFilter);
		if (!activity.isFinishing()) {
			mFlowSettingDialog = new CustomNoTitleDialog(activity);
			mFlowSettingDialog.setMessage(R.string.dialog_tip_noflow);
			mFlowSettingDialog.setNeutralButton(activity.getString(R.string.dialog_setting), new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mFlowSettingDialog.dismiss();
					SettingFlowDialog settingFlowDialog = new SettingFlowDialog(activity);
					if (settingFlowDialog != null) {

						settingFlowDialog.show();
					}
				}
			});
			mFlowSettingDialog.setNegativeButton(activity.getString(R.string.dialog_pause), new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mFlowSettingDialog.dismiss();
				}
			});
			mFlowSettingDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					activity.sendBroadcast(new Intent(CANCELNOFLOWDIALOG));
				}
			});
		}
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (BROADCAST_ACTION_NOFLOW.equals(intent.getAction())) {
				if (!activity.isFinishing() && mFlowSettingDialog != null) {
					mFlowSettingDialog.show();
				}
			} else if (CANCELNOFLOWDIALOG.equals(intent.getAction())) {
				if (!activity.isFinishing() && mFlowSettingDialog != null) {
					mFlowSettingDialog.dismiss();
				}
			}

		}
	}

	public void unregisterMyReceiver() {
		activity.unregisterReceiver(myBroadcastReceiver);
	}
}
