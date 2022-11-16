package kr.co.klnet.aos.etransdriving.trans.gps.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.co.klnet.aos.etransdriving.EtransDrivingApp;

//import android.util.Log;

public class RestartService extends BroadcastReceiver
{
	private final static String TAG = "RestartService";
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(TAG, "RestartService::onReceive");
		if(intent.getAction().equals(UICommon.ACTION_RESTART_PERIOD_REPORT))
		{
//			int isSet = Util.getSharedData(context, "WorkBtnSet", 0);
            String isLbsStartYn 	= EtransDrivingApp.getInstance().getIsLbsStartYn();
			
			if (/* isSet == 1 && */isLbsStartYn.equals("Y"))
			{
				Intent i = new Intent(context, PeriodReportService.class);
				//context.startService(i);


				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
					context.startForegroundService(i);
				} else {
					context.startService(i);
				}


			}
		}
		else if(intent.getAction().equals(UICommon.ACTION_RESTART_WORK_REPORT))
		{
			/*
			Intent i = new Intent(context, NotibarWorkService.class);
			//context.startService(i);

			//2018.12.07
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
				context.startForegroundService(i);
			} else {
				context.startService(i);
			}

			 */

		}
	}
}