package com.dongji.market.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.dongji.market.activity.Login_Activity;
import com.dongji.market.helper.AConstDefine;

public class CommonReceiver extends BroadcastReceiver {

	private Handler handler;

	public CommonReceiver() {
	}

	public CommonReceiver(Handler handler) {
		this.handler = handler;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("com.dongji.market.loginReceiver")) {
			if (intent.getBooleanExtra(AConstDefine.BROADCAST_DIALOG_LOGIN, false)) {
				// Intent tempIntent = new Intent();
				// tempIntent.putExtra(AConstDefine.FLAG_ACTIVITY_BANDR, intent
				// .getIntExtra(AConstDefine.FLAG_ACTIVITY_BANDR,
				// -1));
				// tempIntent.setClass(context, BackupOrRestoreActivity.class);
				// context.startActivity(tempIntent);

				Intent tempIntent = new Intent(AConstDefine.BROADCAST_ACTION_SHOWBANDRLIST);
				tempIntent.putExtra(AConstDefine.FLAG_ACTIVITY_BANDR, intent.getIntExtra(AConstDefine.FLAG_ACTIVITY_BANDR, -1));
				context.sendBroadcast(tempIntent);

			} else if (intent.getBooleanExtra(AConstDefine.LOGIN_STATUS_BROADCAST, false)) {
				Message msg = new Message();
				msg.arg1 = intent.getIntExtra(AConstDefine.LOGIN_STATUS, -10000);
				msg.what = Login_Activity.LOGIN_RESPONSE_STATUS;
				if (handler != null) {
					handler.sendMessageDelayed(msg, 500);
				}
			}

		}
		if (intent.getAction().equals(AConstDefine.GO_HOME_BROADCAST)) {
			if (context != null && !((Activity) context).isFinishing()) {
				((Activity) context).finish();
			}
		}
	}
}
