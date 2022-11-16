package kr.co.klnet.aos.etransdriving.trans.gps.push;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.lbsok.framework.network.nio.channel.IConnectorListener;
import com.lbsok.framework.network.nio.channel.IPacketChannelListener;
import com.lbsok.framework.network.nio.channel.ISocketChannel;
import com.lbsok.framework.network.nio.channel.PacketChannel;
import com.lbsok.framework.network.nio.selector.Connector;
import com.lbsok.framework.network.nio.selector.SelectorThread;
import com.lbsok.framework.timer.DispatcherTimer;
import com.lbsok.framework.timer.ITimerEvent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Calendar;

import kr.co.klnet.aos.etransdriving.BuildConfig;
import kr.co.klnet.aos.etransdriving.EtransDrivingApp;
import kr.co.klnet.aos.etransdriving.json.JsonNetInfo;
import kr.co.klnet.aos.etransdriving.trans.gps.common.AppCommon;
import kr.co.klnet.aos.etransdriving.trans.gps.packet.JGpsInfo;
import kr.co.klnet.aos.etransdriving.trans.gps.packet.PacketController;
import kr.co.klnet.aos.etransdriving.trans.gps.packet.ResponsePacket;
import kr.co.klnet.aos.etransdriving.util.StringUtil;

//2018.11.02

/**
 * 주기 전송 핸들러
 */
public class PeriodReportHandler implements IConnectorListener, IPacketChannelListener
{
	private final static String TAG = "KLNET-LBS";

	private final static int RESPONSE_PACKET_ARRIVED = 100;
	private final static int CONNECTION_ERROR = 101;
	private final static int CONNECTION_ESTABLISHED = 102;
	private final static int CONNECTION_CLOSED = 103;
	private final static int SENT_REPORT_PACKET = 104;

	public int MONITORING_INTERVAL_SECONDS = 5;  //5초
	private Context mContext;
	private boolean				mBoolConnected			= false;
	private int					mIntCreationPeroid		= 0;	
	private int					mIntReportPeroid		= 0;
	private int					mIntCreationTimerCount	= 0;	
	private int					mIntReportTimerCount	= 0;
	private String mStrEventCode			= "";
	private InetSocketAddress mIsaRemoteAddr;
	private SelectorThread mClsSelectorThread;
	private ISocketChannel mInfChannelFactory;
	private PacketChannel mClsPacketChannel;
	private PacketController mClsPacketController;
	private DispatcherTimer mClsVehLocationRefreshTimer;
	private LBSTimer			mLBSTimer = null;
	private int			sentCounter_ = 0;
	public Handler msgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {


			switch(msg.what) {
				case RESPONSE_PACKET_ARRIVED: {
//					if(BuildConfig.DEBUG) {
//						if(msg.obj instanceof String) {
//							EtransDrivingApp.getInstance().showToast((String) msg.obj);
//						}
//					}
				}
				break;
				case CONNECTION_ERROR: {
					if(BuildConfig.DEBUG) {
						EtransDrivingApp.getInstance().showToast("서버 연결 오류");
					}
				}
				break;
				case CONNECTION_ESTABLISHED: {

				}
				break;
				case CONNECTION_CLOSED: {

				}
				break;
			}
		}

		;
	};

	
	private ITimerEvent mInfTimerListener = new ITimerEvent()
	{
		@Override
		public void onTimerEvent(Context ctxContext, DispatcherTimer clsTimer)
		{
			doRefreshPeroidReport();
		}
	};

	public PeriodReportHandler(Context ctxService, ISocketChannel infChannelFactory, String strLBSEngineIP, int intLBSEnginePort) throws Exception
	{
		mContext = ctxService;

		mClsVehLocationRefreshTimer = new DispatcherTimer(ctxService);
		mClsVehLocationRefreshTimer.setTimerEventListener(mInfTimerListener);

		mInfChannelFactory		= infChannelFactory;
		mClsSelectorThread		= new SelectorThread();
		mIsaRemoteAddr			= new InetSocketAddress(strLBSEngineIP, intLBSEnginePort);
		mClsPacketController	= new PacketController(ctxService);
		mStrEventCode			= AppCommon.DEF_EVENT_CODE_PERIOD_REPORT;

		mClsPacketController.startLocationService();
		
		// 생성주기
		mIntCreationPeroid		= EtransDrivingApp.getInstance().getCreationPeroidInt();
		// 보고주기
		mIntReportPeroid		= EtransDrivingApp.getInstance().getReportPeroidInt();

		Log.i(TAG, "collect period=" + mIntCreationPeroid + " seconds, report period=" + mIntReportPeroid + " seconds");
		
	}

	private void connectionClosed()
	{
		mBoolConnected = false;
//		msgHandler.sendEmptyMessage(CONNECTION_CLOSED);
	}

	/**
	 * 서버접속이 완료되면 발생하는 이벤트
	 */
	@Override
	public void connectionEstablished(Connector connector, SocketChannel scChannel)
	{
		try
		{
			scChannel.socket().setReceiveBufferSize(2 * 1024);
			scChannel.socket().setSendBufferSize(2 * 1024);

			this.mClsPacketChannel = new PacketChannel(scChannel, mInfChannelFactory, mClsSelectorThread, new ResponsePacket(), this);
			mBoolConnected = true;

//			msgHandler.sendEmptyMessage(CONNECTION_ESTABLISHED);
			//TODO
//			saveToFile("connectionEstablished");

			// 서버 접속이 완료되면 패킷을 보낸다.
			sendPacket();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 서버접속 실패시 발생하는 이벤트
	 */
	@Override
	public void connectionFailed(Connector connector, Exception cause)
	{
		mBoolConnected = false;
		//TODO
//		saveToFile("connectionFailed");

//		msgHandler.sendEmptyMessage(CONNECTION_ERROR);
		if(BuildConfig.DEBUG) {
			msgHandler.post(new Runnable() {
				@Override
				public void run() {
					EtransDrivingApp.getInstance().showToast("연결 오류");
				}
			});
		}

	}

	/**
	 * 타이머에 의해 5초에 한번씩 주기적으로 발생
	 */
	public void doRefreshPeroidReport()
	{
		int increaseCnt = MONITORING_INTERVAL_SECONDS; //LBS_TIMER 의 주기와 동일하게 맞춰야 함.

		// 생성주기
		mIntCreationPeroid		= EtransDrivingApp.getInstance().getCreationPeroidInt();
		// 보고주기
		mIntReportPeroid		= EtransDrivingApp.getInstance().getReportPeroidInt();

		//보고전생성값 2018.10.21
		int creationGpsPeroid = increaseCnt;

		mIntCreationTimerCount += increaseCnt;
		mIntReportTimerCount += increaseCnt;

		Log.i(TAG, "GPS Monitor[" + this.toString() + " ], collect counter=" +mIntCreationTimerCount +"/" + mIntCreationPeroid
				+  ", report counter="+ mIntReportTimerCount + "/" + mIntReportPeroid);

		if (mIntCreationTimerCount >= (mIntCreationPeroid - creationGpsPeroid) || (mIntCreationTimerCount % 180 == 0) )
		{
            JGpsInfo gpsInfo = JGpsInfo.getInst();
			int size = gpsInfo.getArrayListSize();

			if(size <= 0)
			{
				//query all gps location
				Location gpsLoc = mClsPacketController.getLatestLocation(LocationManager.GPS_PROVIDER);
				Location netLoc = mClsPacketController.getLatestLocation(LocationManager.NETWORK_PROVIDER);
				Location fusedLoc = mClsPacketController.getLatestLocation(JLocationManager.FUSED_PROVIDER);

				if(gpsLoc==null) {
					Log.w(TAG, "    GPS LOCATION is invalid !!!!!");
					mClsPacketController.requestSingleUpdate(LocationManager.GPS_PROVIDER);
				} else {
					gpsInfo.setLocation(gpsLoc);
					Log.i(TAG, "    GPS " + ", lat=" + gpsLoc.getLatitude() + ", lon=" + gpsLoc.getLongitude()
							+ ", speed=" + gpsLoc.getSpeed() + ", dir=" + gpsLoc.getBearing());
				}

				if(netLoc==null) {
					Log.i(TAG, "NETWORK LOCATION is invalid !!!!!");
					Log.i(TAG, "NETWORK LOCATION 수집 재시작");
					mClsPacketController.collectNetworkLocationData();
					mClsPacketController.requestSingleUpdate(LocationManager.NETWORK_PROVIDER);
				}

				if(fusedLoc==null) {
					Log.w(TAG, "    FUSED LOCATION is invalid !!!!!");
					mClsPacketController.requestSingleUpdate(JLocationManager.FUSED_PROVIDER);
				}


				// tom.lee, 이트럭에서 사용하던 workaround 는 주석처리,
				//Log.i(TAG, "NETWORK GPS 수집 시작");
				//mClsPacketController.collectNetworkLocationData();
			}
		}

		// 생성주기 카운터가 현재 세팅되어 생성주기값보다 크면 패킷의 바디부분을 생성하여 누적
		if (mIntCreationTimerCount >= mIntCreationPeroid)
		{
			// 이벤트코드 : 주기보고
			int errCode = mClsPacketController.makePeriodReportGpsData(mStrEventCode);
			if (errCode==0) {
				//success
				Log.i(TAG, "GPS 패킷 저장 성공, 누적 패킷=" +mClsPacketController.getGpsDataSize() +"개");
				EtransDrivingApp.getInstance().debugMessage("GPS 패킷 저장 성공, 누적 패킷=" +mClsPacketController.getGpsDataSize() +"개");
				mIntCreationTimerCount = 0;
			} else {
				String msg = "";
				if(errCode==-1) {
					msg = "GPS 패킷 없음, 저장된 패킷= " +mClsPacketController.getGpsDataSize() + " 개";
				} else {
					msg = "비정상 GPS, 저장된 패킷= " +mClsPacketController.getGpsDataSize() + " 개";
				}
				Log.i(TAG, msg);
				EtransDrivingApp.getInstance().debugMessage(msg);
			}
		}

		// 보고주기라면 서버로 전송
		if (mIntReportTimerCount >= mIntReportPeroid)
		{
			Log.i(TAG, "::::: [" + sentCounter_ + "] GPS 전송 시작, 누적 패킷=" +mClsPacketController.getGpsDataSize() +"개, ==============----------===========>report timer=" +mIntReportTimerCount);
			EtransDrivingApp.getInstance().debugMessage("::::: [" + sentCounter_ + "] GPS 전송 시작, 누적 패킷=" +mClsPacketController.getGpsDataSize() +"개, report timer=" +mIntReportTimerCount);

			if(EtransDrivingApp.getInstance().getGpsStatus()!=1) {
				EtransDrivingApp.getInstance().showToast("[이트랜스드라이빙] GPS 를 사용할 수 없습니다");
			}

			if(mClsPacketController.getGpsDataSize() > 0) {

				final IConnectorListener listener = this;
				// 서버 연결
				new Thread() {
					public void run() {
						Connector clsConnector = null;
						try {
							if (JsonNetInfo.getNetworkInfo(mContext).status != JsonNetInfo.JSON_NET.CONNECTED) {
								mIntReportTimerCount = 0;
								return;
							}

							// 서버에 접속한다. 접속이 성공적으로 완료되면 아래 connectionEstablished() 이벤트가 발생
							clsConnector = new Connector(mClsSelectorThread, mIsaRemoteAddr, listener);
							clsConnector.connect();

							// 연결을 요청보내고 주기보고 타이머 카운터를 초기화한다.
							// 즉 연결이 Establish 된 시점이 아니라 연결이 실패가 나든 성공이 되든 간에 먼저 처리함.
							mIntReportTimerCount = 0;
							sentCounter_++;

						} catch (IOException e) {
							e.printStackTrace();
							mIntReportTimerCount = 0;
						}
					}
				}.start();
			}
			else
			{
				Log.d(TAG, "[" + sentCounter_ + "] GPS 누적 패킷 없음 XXXXXXXXX:::::;" + mIntReportTimerCount);

				EtransDrivingApp.getInstance().debugMessage("[" + sentCounter_ + "] 저장된 GPS 데이터 없음. 리포트 전송하지 않음");
				if (BuildConfig.DEBUG) {
					EtransDrivingApp.getInstance().showToast("[" + sentCounter_ + "] 저장된 GPS 데이터 없음. 리포트 전송하지 않음");
				}
				// mIntReportTimerCount = 0;
			}
		}

	}

	private class LBSTimer extends BroadcastReceiver
	{
		private final static String TAG = "LBSTimer";
		final public String LBS_TIMER = "kr.co.klnet.aos.etransdrinvg.trans.push.TIMER";
		@Override
		public void onReceive(Context context, Intent intent)
		{

			Log.d(TAG, "PeriodReportHandler::LBSTimer::BroadcastReceiver");

			//TODO
//			saveToFile("LBSTimer::onReceive");
			doRefreshPeroidReport();
			context.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
			context.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));

/* //tom.lee, to remark
			//2019.02.22 hckim
			//신규,재설치 등으로 인해 (onNewToken 통해) 신규 FCM Token이 발급되었으나 아직 전화번호 권한을 승인하지 않은 경우를 대비 로그인과 LBSTimer에서 Sync 수행
			//FCM TOKEN에 대해 아직 Sync를 맞춰야 할 상황이라면 어서빨리 서로 do it.
			String tokenSyncSuccessYN = Util.getTokenSyncSuccessYN(context);
			if(!StringUtil.isEmpty(tokenSyncSuccessYN) && tokenSyncSuccessYN.equals("N"))
			{
				//Y가 아직 아니면 모바일 서버로 전송 성공을 못했다는 것이므로 재전송 필요
				Intent _intent = new Intent(context, CommonService.class);
				_intent.setAction("TO_SYNC_FCM_TOKEN_FROM_LBTIMER");
				context.startService(_intent);
			}

*/
			//2018.10.21
			scheduleKeepalivePing(true, 1000);

			try {
				BeaconReportInterface.inst().doBeaconService(context,true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private void scheduleKeepalivePing(boolean isSet, int interval)
		{
			Log.d(TAG, "scheduleKeepalivePing, isSet=" + isSet + ", interval=" + interval);
//			saveToFile("LBSTimer::scheduleKeepalivePing(isSet : " + isSet + ", interval : " + interval + ")");
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.MILLISECOND, interval);

			Intent i = new Intent(LBS_TIMER);

			AlarmManager am = null;
			PendingIntent pendingIntent = null;

			long firstTime = SystemClock.elapsedRealtime();

			int rptInterval = MONITORING_INTERVAL_SECONDS * 1000;

			//생성주기에 따라..
			if(mIntCreationPeroid <= 5) {
				MONITORING_INTERVAL_SECONDS = 5;
				rptInterval = MONITORING_INTERVAL_SECONDS * 1000;
			}

			try {
				am				= (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
				pendingIntent	= PendingIntent.getBroadcast(mContext, 0, i, 0);

				if (isSet == true) {
					//2018.10.21
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
						am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime + rptInterval, pendingIntent);
					} else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
						am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime + rptInterval, pendingIntent);
					} else {
						am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime + rptInterval, pendingIntent);
					}

					mContext.registerReceiver(this, new IntentFilter(LBS_TIMER));
				} else {
					mContext.unregisterReceiver(this);
					am.cancel(pendingIntent);
				}
			} catch(Exception e) {
				mContext.unregisterReceiver(this);
				am.cancel(pendingIntent);
			}
		}
	}

	public void onStart()
	{
		Log.d(TAG, "onStart()");

		mClsPacketController.onStart();

		// BroadCast Receiver를 생성 및 구현하고,
		// Receiver의 onReceiver에서 doRefreshPeroidReport()를 호출
		if (mLBSTimer == null)
		{
			mLBSTimer = new LBSTimer();
			mLBSTimer.scheduleKeepalivePing(true, 1000);

			BeaconReportInterface.inst().doBeaconService(mContext,true);
		}
	}

	public void onStop()
	{
		Log.d(TAG, "onStop()");

		if(mLBSTimer != null) mLBSTimer.scheduleKeepalivePing(false, 0);

		BeaconReportInterface.inst().doBeaconService(mContext,false);

		if(mClsPacketController != null) mClsPacketController.onStop();
		if(mClsVehLocationRefreshTimer != null)  mClsVehLocationRefreshTimer.onStop();

		mClsPacketController = null;
		mClsVehLocationRefreshTimer = null;

		mClsSelectorThread = null;
		mIsaRemoteAddr = null;
		mLBSTimer = null;
	}

	/**
	 * 응답 패킷 수신시 발생하는 이벤트
	 */
	@Override
	public void packetArrived(PacketChannel pc, ByteBuffer bbPacket)
	{
		String obj = "::::: " + "[" + sentCounter_ + "] 서버응답, 응답코드(get(1) : 0x" + String.format("%x" , bbPacket.get(1)) + ")";
		Log.i(TAG, obj);
//		Message msg = msgHandler.obtainMessage(RESPONSE_PACKET_ARRIVED);
//		msg.obj = obj;
//		msgHandler.sendMessage(msg);

//		if(BuildConfig.DEBUG) {
//			msgHandler.post(new Runnable() {
//				@Override
//				public void run() {
//					EtransDrivingApp.getInstance().showToast(obj);
//				}
//			});
//		}

		//TODO
//		saveToFile(String.format("packetArrived : %x" , bbPacket.get(1)));
		if (bbPacket.limit() == 3)
		{
			if (bbPacket.get(1) == 0x30)
			{
				// 성공적으로 전송하였으면 전송데이터 초기화
				mClsPacketController.clearPacket();
			}
		}

		pc.close();
		connectionClosed();
	}

	@Override
	public void packetSent(PacketChannel clsPacketChannel, ByteBuffer pckt)
	{
		Log.d(TAG, "packetSent");
//		msgHandler.sendEmptyMessage(SENT_REPORT_PACKET);
		try
		{
			clsPacketChannel.resumeReading();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean sendPacket()
	{
		Log.d(TAG, "packetSend");
		//TODO
//		saveToFile("sendPacket");
		if (mBoolConnected)
		{
			ByteBuffer bbSentPacket = mClsPacketController.makePacket();
			mClsPacketController.dumpPacket(bbSentPacket);
			mClsPacketChannel.sendPacket(bbSentPacket);

			return true;
		}

		return false;
	}

	public void setEventCode(String strEventCode)
	{		
		mStrEventCode = strEventCode;		
	}

	@Override
	public void socketDisconnected(PacketChannel pc)
	{
		Log.d(TAG, "socketDisconnected");
		connectionClosed();
		pc.close();
		//TODO
//		saveToFile("socketDisconnected");
	}

	@Override
	public void socketException(PacketChannel pc, Exception ex)
	{
		Log.d(TAG, "socketException");
		connectionClosed();
		pc.close();
		//TODO
//		saveToFile("socketException");
	}




}