package kr.co.klnet.aos.etransdriving.trans.gps.push;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import kr.co.klnet.aos.etransdriving.EtransDrivingApp;
//import com.ssomon.lib.common.SLog;

/**
 * Push Agent로부터 메시지를 수신하기 위한 브로드 캐스트 리시버
 */
public class UIBroadCastReceiver extends BroadcastReceiver
{
	private final static String TAG = "UIBroadCastReceiver";
	public final static String BROADCAST_ACTION_START_REPORT_SERVICE = "kr.co.klnet.aos.etransdriving.start_report_service";
	public final static String BROADCAST_ACTION_CHECK_GPS_ALERT = "kr.co.klnet.aos.etransdriving.check_gps_alert";
	private static OnForcedLogoutListener onForcedLogoutListener;
	
	public interface OnForcedLogoutListener
	{
		public void OnCommand();
	}

	public static void setOnForcedLogoutListener(OnForcedLogoutListener listener) {
		onForcedLogoutListener = listener;
	}
	
	private static OnRehandlingMsgListener onRehandlingMsgListener;
	
	public interface OnRehandlingMsgListener
	{
		public void OnReceiveMsg();
	}

	public static void setOnRehandlingMsgListener(OnRehandlingMsgListener listener) {
		onRehandlingMsgListener = listener;
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = (intent != null) ? intent.getAction() : null;
		Log.i(TAG, "::::: onReceive, action=" + action + " :::::");

		if (BROADCAST_ACTION_START_REPORT_SERVICE.equals(action)
//				||Intent.ACTION_BOOT_COMPLETED.equals(action) //system boot
//				|| Intent.ACTION_USER_PRESENT.equals(action) // lockscreen
//				|| Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action) //when closed the system dialog
//				|| Intent.ACTION_POWER_CONNECTED.equals(action) //connected to charger
//				|| Intent.ACTION_POWER_DISCONNECTED.equals(action) //disconnected from charger
//				|| Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action) //changed airplane mode
////				|| Intent.ACTION_PROVIDER_CHANGED.equals(action)
////				|| Intent.ACTION_SCREEN_OFF.equals(action)
////				|| Intent.ACTION_SCREEN_ON.equals(action)
				)
		{
			/* tom.lee
			// 2012.03.02: 로그인 한 적이 있으면 Push Agent를 실행한다.
			UserPreferences pref = AppContext.getUserPref();
			if (pref.getHasbeenLogin())
			{
				ReportInterface.startPushAgent(context);
			}

			// 2012.05.17: 일과 시작 상태이면 주기보고 서비스 실행
			int isSet = Util.getSharedData(context, "WorkBtnSet", 0);
            String isLbsStartYn 	= AppContext.getUserPref().getIsLbsStartYn();
			BasicInfo basicInfo = BasicInfo.getInstance();
			String isLogIn = basicInfo.getPref(context, PrefKey.LOGIN_YN);
			*/
			String isLogIn = EtransDrivingApp.getInstance().getLoggedIn();
//			if (isSet == 1 && isLogIn.equals("Y") && isLbsStartYn.equals("Y"))
			if (isLogIn!=null && isLogIn.length()>0)
			{
//				ReportInterface.inst().startPeriodReportService(context);
				ReportInterface.inst().doPeriodReportService(context, false);
				ReportInterface.inst().doPeriodReportService(context, true);
			}
		}
/* tom.lee
		else if (LibServiceDefine.ACTION_PUSH.equals(action))
		{
			String notiCmd 	= intent.getStringExtra(LibServiceDefine.EXTRA_NOTI_CMD);
			String notiMsg 	= intent.getStringExtra(LibServiceDefine.EXTRA_NOTI_MSG);
			String senderGb = StringUtil.nullConvert(intent.getStringExtra(AppData.PUSH_SENDER_GB), "P");

			startPopUpActivity(context, notiCmd, notiMsg, senderGb);
		}

		if (action.equals("android.intent.action.BOOT_COMPLETED") || action.equals(NotibarWorkReport.NOTIBAR_REFRESH_ACTION))
		{
			handleNotibarWorkService(context, true);
		}
*/

		//2018.12.01
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			//2018.12.01 park
			//UIBroadCastReceiver receiver = new UIBroadCastReceiver();

			try{
				context.unregisterReceiver(this);
			}
			catch(IllegalArgumentException e) {
				//context.unregisterReceiver(this);
				Log.e("ETRUCK", "context.unregisterReceiver IllegalArgumentException : " + e.getLocalizedMessage());
			}
			catch (Exception e) {
				//context.unregisterReceiver(this);
				Log.e("ETRUCK", "context.unregisterReceiver Exception : " + e.getLocalizedMessage());
			}

		}


	}

	private void handleNotibarWorkService(Context _context, boolean _isStart)
	{
		Log.i(TAG, "::::: handleNotibarWorkService, _isStart=" + _isStart + " :::::");
/*
		if(android.os.Build.VERSION.SDK_INT > 14)
		{
			//Log.i("ETRUCK", "simple work report service start..");
			Intent intent = new Intent(_context, NotibarWorkService.class);
			if (_isStart == true)
			{
				_context.stopService(intent);
				//_context.startService(intent);


				//2018.12.07
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
					try{
						_context.startForegroundService(intent);
					}
					catch(IllegalStateException e) {
						Log.e("ETRUCK", "_context.startForegroundService(intent) IllegalStateException : " + e.getLocalizedMessage());
					}
					catch (Exception e) {
						Log.e("ETRUCK", "_context.startService(intent) Exception : " + e.getLocalizedMessage());
					}
				} else {
					_context.startService(intent);
				}


			}
			else
			{
				_context.stopService(intent);
			}
		}
		else
		{
			Log.e("ETRUCK", "not enough api level. can't started simple work report service.");
		}
 */
	}
	
	/**
	 * Push 수신 후 분석을 위한 Activity로 이동
	 * @param context
	 * @param notiCmd
	 * @param notiMsg
	 */
	private void startPopUpActivity(Context context, String notiCmd, String notiMsg, String senderGb)
	{
/*
//		Log.v("qwer","UIBroadCastReceiver::startPopUpActivity(notiCmd : " + notiCmd + ", notiMsg : " + notiMsg + ")");
		if(Integer.valueOf(notiCmd) == UICommon.CMD_FORCED_LOGOUT_EVENT)
		{
			if(getCurrentActivity(context).equals("Y"))
			{
				Intent intent = new Intent(context, MOB_10.class);
				intent.putExtra("FORCED_LOGUT", "Y");

				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				
				context.startActivity(intent);
				
				onForcedLogoutListener.OnCommand();
			}
		}
		if(Integer.valueOf(notiCmd) == UICommon.CMD_REHANDLING_MESSAGE || Integer.valueOf(notiCmd) == UICommon.CMD_TML_ARRIVAL_MESSAGE)
		{
			if(getCurrentActivity(context).equals("Y") && isScreenOn(context))
			{
				Util.setSharedData(context, "rehandlingPush", notiMsg.substring(notiMsg.indexOf("{"), notiMsg.lastIndexOf("}")+1));
				Util.setSharedData(context, "PushCmd", Integer.valueOf(notiCmd));
				onRehandlingMsgListener.OnReceiveMsg();
			}else
			{
				Util.removeSharedData(context, "rehandlingPush");
				
				Intent intent = new Intent(context, LibPopUpActivity.class);
				intent.setAction(UICommon.INTENT_PUSH_POPUP);
				intent.putExtra(LibServiceDefine.EXTRA_NOTI_CMD, notiCmd);
				intent.putExtra(LibServiceDefine.EXTRA_NOTI_MSG, notiMsg.substring(notiMsg.indexOf("{"), notiMsg.lastIndexOf("}")+1));
				intent.putExtra("isThisAppShowing", getCurrentActivity(context));
				intent.putExtra(AppData.PUSH_SENDER_GB, senderGb);

				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		}
		else
		{
			Intent intent = new Intent(context, LibPopUpActivity.class);
			intent.setAction(UICommon.INTENT_PUSH_POPUP);

			intent.putExtra(LibServiceDefine.EXTRA_NOTI_CMD, notiCmd);
			intent.putExtra(LibServiceDefine.EXTRA_NOTI_MSG, notiMsg);
			intent.putExtra(AppData.PUSH_SENDER_GB, senderGb);

			intent.putExtra("isThisAppShowing", getCurrentActivity(context));


			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}

 */
	}
	
	//현재 화면에 보이는 프로세스 패키지 이름 알아보기
	public String getCurrentActivity(Context context)
	{

		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> taskInfo = am.getRunningTasks(1);
		  
		ComponentName topActivity = taskInfo.get(0).topActivity;
		
		String isYN = topActivity.getPackageName().equals(context.getPackageName()) ? "Y" : "N";

		  
		return isYN ;
	}
	
	public boolean isScreenOn(Context context)
	{
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	    boolean isScreenOn = pm.isScreenOn();
	    return isScreenOn;
	}
}