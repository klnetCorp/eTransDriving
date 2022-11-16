package kr.co.klnet.aos.etransdriving.trans.gps.push;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.lbsok.framework.network.nio.channel.ISocketChannel;
import com.lbsok.framework.network.nio.channel.SocketChannelFactory;

import java.io.IOException;

import kr.co.klnet.aos.etransdriving.BuildConfig;
import kr.co.klnet.aos.etransdriving.EtransDrivingApp;
import kr.co.klnet.aos.etransdriving.R;
import kr.co.klnet.aos.etransdriving.trans.gps.common.AppCommon;
import kr.co.klnet.aos.etransdriving.util.CommonUtil;

/**
 * 주기 보고 서비스<br>
 * 데몬 형식으로 동작한다
 */
public class PeriodReportService extends Service
{
	private final static String TAG = "PeriodReportService";
	private static ISocketChannel mInfSocketChannel;
	public final static String  ServiceName = "kr.co.klnet.aos.etransdriving.trans.gps.push.PeriodReportService";
	/**
	 * @uml.property  name="mClsPeriodReportHandler"
	 * @uml.associationEnd  
	 */
	private PeriodReportHandler mClsPeriodReportHandler; // Non Blocking IO Handler
	/**
	 * @uml.property  name="mNmNotificationMgr"
	 * @uml.associationEnd  
	 */
	private NotificationManager mNmNotificationMgr; // Notification Manager

	/**
	 * 상태바 알림 서비스 매니저 생성
	 */
	private void initServiceControl()
	{
		try
		{
			//2018.12.05
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

//				String channelId = "etruckbankPush";
//				String channelName = "etruckbankPush";
				String channelId = getString(R.string.default_notification_channel_id);
				String channelName = getString(R.string.default_notification_channel_name);

				int importance = mNmNotificationMgr.IMPORTANCE_HIGH;
				mNmNotificationMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

				NotificationChannel mChannel = new NotificationChannel(
						channelId, channelName, importance);

				mNmNotificationMgr.createNotificationChannel(mChannel);

				NotificationCompat.Builder builder;
				builder = new NotificationCompat.Builder(this, channelId);

				NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
				style.bigText("이트랜스 드라이빙이 실행중입니다.");
				style.setBigContentTitle(null);
				style.setSummaryText("서비스 동작중");
//				builder.setContentText("이트랜스 드라이빙");
//				builder.setContentTitle("이트랜스 드라이빙이 실행중입니다.");
				builder.setOngoing(true);
				builder.setStyle(style);
				builder.setWhen(0);
				builder.setShowWhen(false);

				builder.setSmallIcon(R.mipmap.ic_launcher);
				builder.setNumber(0);

				Notification notification = builder.build();
//						.setContentTitle("이트랜스 드라이빙") // required
//						.setContentText("이트랜스 드라이빙이 실행중입니다.")  // required
//						.setAutoCancel(true) // 알림 터치시 반응 후 삭제/
//						.setSmallIcon(R.mipmap.ic_launcher)
//						.setNumber(0)
//						.build();

				startForeground(1, notification);

			} else {
				mNmNotificationMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			}


			// 소켓채널 팩토리에서 인스턴스를 얻는다.
			SocketChannelFactory scfFactory = SocketChannelFactory.getInstance();
			// 0 : Plain, 1 : SSL
			mInfSocketChannel = scfFactory.getSocketChannel(AppCommon.SOCKET_CHANNEL_TYPE_PLAIN);
			
			
			mClsPeriodReportHandler = new PeriodReportHandler(this, mInfSocketChannel, getText(R.string.strLBSEngineIP).toString(),
					Integer.parseInt(getText(R.string.strLBSEnginePort).toString()));
			
//			mClsPeriodReportHandler.setEventCode(AppCommon.DEF_EVENT_CODE_PERIOD_REPORT);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		if(!isReportServiceRunning(this))
			runService();

		unregisterRestartAlarm();

	}

	public static boolean isReportServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			Log.i("CHK", "service=" + service.service.getClassName());
			if (PeriodReportService.class.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onDestroy()
	{
		Log.i(TAG, "PeriodReportService::onDestroy");

		try {
			stopForeground(true);
			stopSelf();
		}catch(Exception e) {
			e.printStackTrace();
		}

		String isLbsStartYn 	= EtransDrivingApp.getInstance().getIsLbsStartYn();
//		String isLogIn = basicInfo.getPref(this, PrefKey.LOGIN_YN);
		String isLogIn = EtransDrivingApp.getInstance().getAuthKey().length()>0?"Y":"N";

		//Log.d("<<KGW>>", "qwer isLogIn:" +isLogIn);

		if (isLbsStartYn.equals("Y")) //관제시작여부메세지 제어
		{
			if(mNmNotificationMgr!=null) mNmNotificationMgr.cancel(R.string.strGpsTrackServiceStarted);
//			Util.Toast(this, getString(R.string.strGpsTrackServiceStopped));


			//2018.10.21
			//if (!isWorkStart.equals("Y") || !isLogIn.equals("Y")) {
			if (!isLogIn.equals("Y")) {
				if(BuildConfig.DEBUG)
					EtransDrivingApp.getInstance().showToast(getString(R.string.strGpsTrackServiceStopped));
			}
		}
		

		mNmNotificationMgr = null;
		if(mClsPeriodReportHandler != null) {//
			mClsPeriodReportHandler.onStop();
			mClsPeriodReportHandler = null;
		}


//		int isSet = Util.getSharedData(this, "WorkBtnSet", 0);
		
		if (/*isSet == 1 && */
				isLogIn.equals("Y") && isLbsStartYn.equals("Y"))
		{
			registerRestartAlarm();
		}

		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(TAG, "onStartCommand, startId=" + startId);

		return START_STICKY;
	}

	private void runService() {


		//13.01.25 서비스 등록 시 NullPointException 처리
//		saveToFile("PeriodReportService::onStartCommand");

		try
		{
			//String strEventCode = intent.getExtras().get("strEventCode").toString();

			// 이벤트코드전달
			//if (strEventCode.equals("") == false)
			//{
			//	//mClsPeriodReportHandler.setEventCode(strEventCode);
			//}
			initServiceControl();
		}
		catch (NullPointerException ie)
		{
			ie.printStackTrace();
		}

		if(mClsPeriodReportHandler != null)
		{
			mClsPeriodReportHandler.onStart();
		}

	}

	private void registerRestartAlarm()
	{
		Intent intent = new Intent(this, RestartService.class);
		intent.setAction(UICommon.ACTION_RESTART_PERIOD_REPORT);

		long firstTime = SystemClock.elapsedRealtime();

		firstTime += 10 * 1000; // 10초 후에 알람이벤트 발생
		AlarmManager am				= (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent	= PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

		//am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, pendingIntent);
		//2018.10.21
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, pendingIntent);
		} else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, pendingIntent);
		} else {
			am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, pendingIntent);
		}


	}

	private void unregisterRestartAlarm()
	{

		Intent intent = new Intent(this, RestartService.class);
		intent.setAction(UICommon.ACTION_RESTART_PERIOD_REPORT);

		PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(sender);
	}

	/**
	 * 화면 타이틀바에 주기 보고 되는 것을 알림
	 */
//	private void showNotification()
//	{
//		String isLbsStartYn 	= EtransDrivingApp.getInstance().getIsLbsStartYn(); //관제실행여부
//
//		if (isLbsStartYn.equals("Y")) //관제시작여부
//		{
//			CharSequence csText = getText(R.string.strGpsTrackServiceStarted);
//			Notification notification = new Notification(android.R.drawable.stat_sys_upload, csText, System.currentTimeMillis());
//	//		notification.flags = Notification.FLAG_ONGOING_EVENT;
//
//			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MOB_00.class), 0);
//			notification.setLatestEventInfo(this, getText(R.string.service_name), csText, contentIntent);
//			mNmNotificationMgr.notify(R.string.strGpsTrackServiceStarted, notification);
//		}
//	}


	/**
	 * 통지를 등록한다.
	 * @param 		context - 호출하는 Activity 자신
	 * @param		intent - 등록된 통지를 클릭했을 때,
	 * 						구동될 Activity 정보
	 * @param		noticeId - 통지에 부여할 고유번호
	 * @param		iconId - 통지에 표시될 아이콘의 리소스 아이디
	 * @param		ticker - 통지가 등록될 시에 안테나 영역에
	 * 						일시적으로 보여질 메시지
	 * @param		title - 통지 영역에 표시될 제목
	 * @param		message - 통지 영역에 보여질 내용
	 */
	public static void addNotification(Context context, Intent intent, int noticeId, int iconId, String ticker, String title, String message) {

		//2018.12.01
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

//			String channelId = "etruckbankPush";
//			String channelName = "etruckbankPush";
			String channelId = context.getString(R.string.default_notification_channel_id);
			String channelName = context.getString(R.string.default_notification_channel_name);

			int importance = NotificationManager.IMPORTANCE_HIGH;
			NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			NotificationChannel mChannel = new NotificationChannel(
					channelId, channelName, importance);

			nm.createNotificationChannel(mChannel);

			NotificationCompat.Builder builder =
					new NotificationCompat.Builder(context.getApplicationContext(), channelId);

			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

			int requestID = (int) System.currentTimeMillis();

			PendingIntent pendingIntent
					= PendingIntent.getActivity(context.getApplicationContext()
					, requestID
					, intent
					, PendingIntent.FLAG_UPDATE_CURRENT);

			builder.setContentTitle(title) // required
					.setContentText(message)  // required
					.setAutoCancel(true) // 알림 터치시 반응 후 삭제/
					.setSmallIcon(iconId)
					.setNumber(0)
					.setContentIntent(pendingIntent);

			nm.notify(noticeId, builder.build());

		} else {

			// 통지 객체를 생성
			// (표시 아이콘, 티커메시지, 현재 시간)
			//Notification noti = new Notification(iconId, ticker, System.currentTimeMillis());

			//noti.flags		|= Notification.FLAG_AUTO_CANCEL;
			//noti.number = 1; //메세지 카운터

			// App을 구동하기 위한 PendingIntent의 생성
			PendingIntent pintent = PendingIntent.getActivity(context, 0, intent, 0);

			// 통지의 상세 정보 지정
			// (출처Aactivity, 제목, 내용, 선택시 구동할 App정보)
			Notification noti = CommonUtil.createNotification(context, pintent, title, message, iconId, ticker);
			noti.flags		|= Notification.FLAG_AUTO_CANCEL;
			//noti.setLatestEventInfo(context, title, message, pintent);

			// 시스템에 통지 정보를 등록
			NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			//nm.cancel(noticeId);
			nm.cancelAll();
			nm.notify(noticeId, noti);

		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			//오레오


		}


	}
	
	
	public static void removeNotification(Context context, int noticeId) {
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//		nm.cancel(noticeId);
		nm.cancelAll();
	}	
	
	//TODO
//	String CUR_DATE = "%s:%s:%s:%s:%s:%s";
//	private String getCurTime()
//	{
//		Calendar curDate = Calendar.getInstance(Locale.KOREAN);
//
//		String year		= String.valueOf(curDate.get(Calendar.YEAR));
//		String month	= String.valueOf(curDate.get(Calendar.MONTH) + 1);
//		String date		= String.valueOf(curDate.get(Calendar.DATE));
//		String hour		= String.valueOf(curDate.get(Calendar.HOUR_OF_DAY));
//		String minute	= String.valueOf(curDate.get(Calendar.MINUTE));
//		String second	= String.valueOf(curDate.get(Calendar.SECOND));
//
//		return String.format(CUR_DATE, year, month, date, hour, minute, second);
//	}
//
//	private void saveToFile(String seq)
//	{
//		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "mob_log");
//		if(!file.exists())
//		{
//			file.mkdirs();
//		}
//
//		try
//		{
//			File files = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mob_log/lbs_log.txt");
//			long fSize = files.length();
//			RandomAccessFile f = new RandomAccessFile(files, "rw");
//			f.seek(fSize);
//
//			String sHeader = "[" + getCurTime() + "] " + seq + "\r\n";
//			f.write(sHeader.getBytes());
//			f.close();
//		}
//		catch(IOException e)
//		{
//			e.printStackTrace();
//		}
//	}
}