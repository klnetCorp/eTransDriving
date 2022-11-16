package kr.co.klnet.aos.etransdriving.trans.gps.push;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.lbsok.framework.network.nio.channel.ISocketChannel;
import com.lbsok.framework.network.nio.channel.SocketChannelFactory;

import kr.co.klnet.aos.etransdriving.EtransDrivingApp;
import kr.co.klnet.aos.etransdriving.R;
import kr.co.klnet.aos.etransdriving.trans.gps.common.AppCommon;
import kr.co.klnet.aos.etransdriving.util.CommonUtil;


public class ReportInterface
{
	private static ReportInterface mObject = null;

	private static final String ACTION_STOP = "com.example.MyForegroundService.ACTION_STOP";

	IntentFilter mIfConFilter;
	Activity activity;
	Context mContext;

	/**
	 * ReportInterface 인스턴스 반환
	 * @return
	 */
	public static ReportInterface inst()
	{
		if (mObject == null)
		{
			mObject = new ReportInterface();
		}

		return mObject;
	}

	/**
	 * 네트워크 상태에 대한 브로드 캐스트 리시버 등록
	 */
	public final BroadcastReceiver mConReceiver = new BroadcastReceiver()
	{
		Context mContext;

		@Override
		public void onReceive(Context context, Intent intent)
		{
			mContext = context;
			activity = (Activity) mContext;

			if (isOnline(context) == false)
			{
				CommonUtil.showAlertDialogYes(activity
						, context.getString(R.string.strNetworkError)
						, context.getString(R.string.strNetworkErrorPopUp)
						, new AlertDialog.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
			}
		}
	};

	/**
	 * 네트워크 상태 검사
	 * @param context
	 */
	private void CheckNetworkStatus(Context context)
	{
		if (isOnline(context) == false)
		{
			CommonUtil.showAlertDialogYes(activity
					, context.getString(R.string.strNetworkError)
					, context.getString(R.string.strNetworkErrorPopUp)
					, new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
		}


	}

	public void Destory(Context context)
	{
		// context.unregisterReceiver(mConReceiver);
		ReportInterface periodreport = ReportInterface.inst();
		periodreport.doPeriodReportService(context, false);
	}

	/**
	 * 즉시 보고 소켓 생성
	 * @param
	 * @param
	 */
	public void doDirectSendPacket(Context _context, String _evtCode)
	{
		/*
		// 소켓채널 팩토리에서 인스턴스를 얻는다.
		SocketChannelFactory scfFactory = SocketChannelFactory.getInstance();
		ISocketChannel infSocketChannel;
		@SuppressWarnings("unused")
		DirectSendHandler handler = null;

		try
		{
			// 채널 모드 - 0:Plain, 1:SSL
			infSocketChannel = scfFactory.getSocketChannel(AppCommon.SOCKET_CHANNEL_TYPE_PLAIN);
			handler = new DirectSendHandler(_context, _evtCode, infSocketChannel, _context.getText(R.string.strLBSEngineIP).toString(),
					Integer.parseInt(_context.getText(R.string.strLBSEnginePort).toString()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			handler = null;
		}
		*/

	}

	/**
	 * Periodic Report 서비스를 시작/중지한다.
	 * 
	 * @param context
	 *            Context
	 * @param bStart
	 *            서비스 시작/중지 여부
	 */
	public void doPeriodReportService(Context context, boolean bStart)
	{

		Intent intent = new Intent(context, PeriodReportService.class);
		intent.putExtra("strEventCode", AppCommon.DEF_EVENT_CODE_PERIOD_REPORT);
		if (bStart /*== true /* && EtransDrivingApp.getInstance().getIsLbsStartYn().equals("Y") */) {
			String Vehicle_Id = EtransDrivingApp.getInstance().getVehicleId();
			if (Vehicle_Id != null && Vehicle_Id.length() != 0) {
				EtransDrivingApp.getInstance().setCarCd(Vehicle_Id);
			}

			if(!PeriodReportService.isReportServiceRunning(context)) {
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
					context.startForegroundService(intent);
				} else {
					context.startService(intent);
				}
			}
		} else {
			if(PeriodReportService.isReportServiceRunning(context)) {
				context.stopService(intent);
			}

		}
	}


	/**
	 * 관제 서버에 보낼 데이터 초기화
	 * @param context
	 */
	public void init(Context context)
	{
		String strCreationPeroid;
		String strReportPeroid;
		String strCarCode;
		String strCarrierId;


		activity = (Activity) context;
		mContext = context;

		// ---------------------------------------------------------------------
		// 애뮬레이터에서는 NIO Socket 이용시 에러발생으로 인하여 아래처럼 세팅한다.
		java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
		java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
		// ---------------------------------------------------------------------

		CheckNetworkStatus(context);

		// 생성주기
		strCreationPeroid = EtransDrivingApp.getInstance().getCreationPeroid();
		if (strCreationPeroid.equals("") || strCreationPeroid.length() == 0 || strCreationPeroid == null)
		{
			strCreationPeroid = "180"; // 180초
			EtransDrivingApp.getInstance().setCreationPeroid(strCreationPeroid);
		}

		// 보고주기
		strReportPeroid = EtransDrivingApp.getInstance().getReportPeroid();
		if (strReportPeroid.equals("") || strReportPeroid.length() == 0 || strReportPeroid == null)
		{
			strReportPeroid = "180"; // 180초
			EtransDrivingApp.getInstance().setReportPeroid(strReportPeroid);
		}

		// CARRIER ID 확인
		strCarrierId = EtransDrivingApp.getInstance().getCarrierId();
		// CARRIER ID는 NULL이 될 수 있다. 서버에서 LOCATION 기록시 이 값을 CHECK하지 않도록 해야 함....
		if (strCarrierId.equals("") || strCarrierId.length() == 0 || strCarrierId == null)
		{
			strCarrierId = "0"; // 0번
			EtransDrivingApp.getInstance().setCarrierId(strCarrierId);
		}

		// CAR CODE 확인
		strCarCode = EtransDrivingApp.getInstance().getCarCd();
		if (strCarCode.equals("") || strCarCode.length() == 0 || strCarCode == null)
		{
			strCarCode = "0"; // 차량 번호
			EtransDrivingApp.getInstance().setCarCd(strCarCode);
		}
//
//		// 단말기번호
//		EtransDrivingApp.getInstance().setMobileId(LibSystemSharedValue.inst().CTN);
//
//		//태블릿처리 모바일 처리2018.10.30
//		//설정된 모바일번호랑 비교하기..
//		String strPhoneNumber = Util.getPhoneNumber(mContext);
//		if (strPhoneNumber.length() > 0) {
//			if (!strPhoneNumber.equals(LibSystemSharedValue.inst().CTN)) {
//				EtransDrivingApp.getInstance().setMobileId(strPhoneNumber);
//			}
//		}
//
//
//
//		// 맥어드레스
//		EtransDrivingApp.getInstance().setMacAddress(LibSystemSharedValue.inst().MAC);
	}

	/**
	 * 현재 네트워크 상태 검사
	 * @param context
	 * @return
	 */
	private boolean isOnline(Context context)
	{
		ConnectivityManager cmMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo mobile = cmMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wifi = cmMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo wimax = cmMgr.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
		
		boolean isMobile = false;
		boolean isWifi = false;
		boolean isWimax = false;
		
		try 
		{
			isMobile = mobile.isConnected();
		} 
		catch (Exception e) { }
		
		try 
		{
			isWifi = wifi.isConnected();
		} 
		catch (Exception e) { }
		
		try 
		{
			isWimax = wimax.isConnected();
		} 
		catch (Exception e) { }
		
		if(isMobile || isWifi || isWimax) 
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * 관제 서버에 즉시 전송
	 * @param context
	 * @param strEventCode
	 */
	public void sendButtonCode(Context context, String strEventCode)
	{
		// 프로퍼런스 변수에 저장
		EtransDrivingApp.getInstance().setEventCode(strEventCode);
		// 전송처리
		doDirectSendPacket(context, strEventCode);
	}

	/**
	 * Push 서비스를 실행한다.
	 * 
	 * @param context
	 *            Context
	 * @param bRestart
	 *            재시작을 하는 것인지를 나타낸다.
	 */
	public void startPushService(Context context, boolean bRestart)
	{
/*

		try
		{
			Intent intent = new Intent(context, LibPushService.class);

			if (bRestart == true)
			{
				intent.setAction(LibServiceDefine.ACTION_PUSH_RECONNECT);

				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
					context.startForegroundService(intent);
				} else {
					context.startService(intent);
				}


			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

*/
	}

	/**
	 * Push 서비스를 중지한다.
	 * 
	 * @param context
	 *            Context
	 * @param bStop
	 *            서비스 중지 여부
	 */
	public void stopPushService(Context context, boolean bStop)
	{
/*
		try
		{
			Intent intent = new Intent(context, LibPushService.class);

			if (bStop == true)
			{
				intent.setAction(LibServiceDefine.ACTION_PUSH_LEAVE);

                //2018.12.07
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
					context.startForegroundService(intent);
				} else {
					context.startService(intent);
				}


			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
*/
	}

	
	/**
	 * 로그인 한 적이 있으면 Push Agent를 실행시킨다.
	 * @param context
	 */
	public static void startPushAgent(Context context)
	{
		ReportInterface.inst().startPushService(context, true);
	}



}