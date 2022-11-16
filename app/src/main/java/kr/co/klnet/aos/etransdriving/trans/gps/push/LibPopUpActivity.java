package kr.co.klnet.aos.etransdriving.trans.gps.push;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.minew.beacon.BluetoothState;
import com.minew.beacon.MinewBeaconManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import kr.co.klnet.aos.etransdriving.R;
import kr.co.klnet.aos.etransdriving.ui.Popup;
import kr.co.klnet.aos.etransdriving.util.StringUtil;
import kr.co.klnet.aos.etransdriving.util.TTS;

//비콘 2018.11.02


/**
 * Push 메시지 수신 시 내용 분석 및 팝업 Display
 */
public class LibPopUpActivity extends Activity
{
	private PushPopUpLockView	mLockView;
	private Context mContext;
	private Activity mActivity;
	private String title;
	private String parsedMsg;
	private String assignNo;
	private String changePeriod;
	private String MixedNo;
	private int					PushCmd;
	private String eventCode;
	private ReportInterface		report;
	private String PUSHMSG = "메시지";
	private Vibrator mVibrator = null;

	//이것은 의도와 맞지 않게 리스너 이벤트에 전혀 맞지 않음
	//private int                 callState = 0;

	private String pushSeq;

	private final int HANDLER_LOGOUT = 0;

	private final int HANDLER_LOGOUT_SUCCESS = 1;

	private final int HANDLER_START_SUCCESS = 2;

	private MinewBeaconManager mMinewBeaconManager;
	private static final int REQUEST_ENABLE_BT = 2;

	private TTS tts;
	private SoundManager  mSoundManager;
	private TelephonyManager mTelMan = null;



	/**
	 * 팝업 버튼 클릭 시 동작<br> 단순 메시지인 경우 팝업만 사라지고, 배차인 경우 화면 전환이 이루어진다.
	 * @uml.property  name="mClickListener"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	Button.OnClickListener mClickListener = new Button.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			switch (view.getId())
			{
				case R.id.doOK:
					PeriodReportService.removeNotification(mContext, 0);

					if(mLockView != null)
					{
						mLockView.unLockView();
						mLockView = null;
					}

					if(PushCmd == UICommon.CMD_PRIVATE_MESSAGE || PushCmd == UICommon.CMD_TEMPORARY_MESSAGE)
					{
						finish();
//					AppUpgrade appUpgrade = new AppUpgrade(mContext);
//					appUpgrade.doLiveUpdate(true);
					}
					else if (PushCmd == UICommon.CMD_APK_UPDATE)
					{
						finish();
//					AppUpgrade appUpgrade = new AppUpgrade(mContext);
//					appUpgrade.doLiveUpdate(true);
					}
					else
					{

					}

					break;
			}
		}
	};

	/**
	 * Push 팝업 설정 해제
	 */
	@Override
	public void onDestroy()
	{
		if (mLockView != null)
		{
			mLockView.unLockView();
			mLockView = null;
		}

		if(tts != null){
			tts.stop();
			tts.shutdown();
		}

		releaseWakeLock();
		super.onDestroy();
	}

	private  PowerManager.WakeLock wakeLock;

	private void acquireWakeLock(Context context) {
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
						| PowerManager.ACQUIRE_CAUSES_WAKEUP
				, context.getClass().getName());

		if (wakeLock != null) {
			wakeLock.acquire(20000);
		}
	}

	private void releaseWakeLock() {
		if (wakeLock != null) {
			if(wakeLock.isHeld()){
				try {
					wakeLock.release();
				}catch (Exception e){};
			}
			wakeLock = null;
		}
	}

	/**
	 * Push 팝업 설정
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mActivity = this;
		mContext = this;

		//화면캐우는 부분
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				//키잠금해제하기
				//| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				//화면켜기
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		acquireWakeLock(mContext);


		Intent intent = mActivity.getIntent();
		String action = intent.getAction();

		title			= "";
		parsedMsg		= "";
		PushCmd			= 0;
		assignNo		= "";
		changePeriod	= "";
		report			= ReportInterface.inst();

		if (action.equals(UICommon.INTENT_PUSH_POPUP)) {
			String notiMsg = "push 본문~~~~";

			//이트럭뱅크웹에서 푸시 메시지중 TTS 대상이되는
			//푸시 서버로부터 notiMsg  뒷 부분에 #TTS# 구분자가 있는지 확인한다.
			//if(notiMsg != null) {//기존 코딩들도 notiMsg가 null이지 않은 전제라 나도 체크 안함

			boolean enabledTTS = true; //Util.getSharedData(mContext, "Tts_Read", "").equals("Y");
			int soundMode = 0;//0 : 무음, 외외 : R.raw.meassage_arrive 등등등 : 음성 성우  or 또는 TTS 읽기
			String forcedTTSMsg = null;

			if (notiMsg.indexOf("#TTS#") > -1) { //TTS 읽으라고 설정 되어 있는데 마침 메세지에 ttsCommand(#TTS#)가 있다면

				tts = null;

				if(enabledTTS)
					soundMode = 1;

				String[] toCheckNotiMsg = notiMsg.split("#TTS#");//"#TTS#"는 무조건 한번 체크해야 함
				notiMsg = toCheckNotiMsg[0];//#TTS# 좌측 데이터만이 이후단 처리될 메시지임

				if(toCheckNotiMsg.length > 1 && !StringUtil.isEmpty(toCheckNotiMsg[1])) {//따로 읽어줄 TTS 읽을 내용이 있다면
					forcedTTSMsg = toCheckNotiMsg[1];//우측 별도의 TTS 텍스트로
				}

			}

			// Push 메시지 분석 및 팝업 내용 준비
			onSetPopUpData(notiMsg);

				forcedTTSMsg = parsedMsg;//onSetPopUpData 에서 처리된 순수TTS용메시지만 담겨있다

			if (PushCmd == UICommon.CMD_CHANGE_GET_PERIOD || PushCmd == UICommon.CMD_CHANGE_REPORT_PERIOD || PushCmd == UICommon.CMD_CHANGE_GET_FINISH ||
					PushCmd == UICommon.CMD_CHANGE_REPORT_FINISH || PushCmd == UICommon.CMD_GET_GPS_POSITION || PushCmd == UICommon.CMD_CHANGE_GET_REPORT_FINISH ||
					PushCmd == UICommon.CMD_CHANGE_LBS_TRACE_START || PushCmd == UICommon.CMD_CHANGE_LBS_TRACE_STOP || PushCmd == UICommon.CMD_FORCED_LOGOUT_EVENT

					)
			{
				if (mLockView != null)
				{
					mLockView.unLockView();
					mLockView = null;
				}

				finish();
			}
			else
			{
				mLockView = new PushPopUpLockView(mContext, mActivity);
				mLockView.lockView(R.layout.push_popup);

				// 여기서 Class Cast Exception이 자주 발생하는데...
				TextView pushTitle = (TextView) mLockView.findLockViewById(R.id.textTitle);
				TextView pushData = (TextView) mLockView.findLockViewById(R.id.textData);

				pushTitle.setText(title);

				pushData.setText(parsedMsg);
				pushData.setMaxLines(200);
				pushData.setVerticalScrollBarEnabled(true);
				pushData.setHorizontalScrollBarEnabled(true);
				WindowManager manager = (WindowManager)mActivity.getSystemService(Context.WINDOW_SERVICE);
				int height = manager.getDefaultDisplay().getHeight();
				Configuration config = mActivity.getResources().getConfiguration();

				if(config.orientation == Configuration.ORIENTATION_LANDSCAPE)
				{
					pushData.setMaxHeight(height / 4);
				}
				else
				{
					pushData.setMaxHeight(height / 2);

				}

				pushData.setMovementMethod(new ScrollingMovementMethod());

				Button btnOk = (Button) mLockView.findLockViewById(R.id.doOK);
				btnOk.setOnClickListener(mClickListener);

				//2019.03.20 popup.java 외에도 일반 팝업시에도 TTS 필요시 실행
				playSound(soundMode, parsedMsg, forcedTTSMsg);
			}
		}

	}

	/**
	 * Push 내용 분석
	 * @param notiMsg
	 */
	public void onSetPopUpData(String notiMsg)
	{
		parsedMsg = notiMsg;
	}

	private String getEventCode(String notiMsg)
	{
		int index = notiMsg.indexOf(';');
		return notiMsg.substring(0, index);
	}

	/**
	 * 서버로부터 받은 Push Message를 분석
	 *
	 * @param notiMsg 서버로부터 받은 내용
	 * @return 파싱된 메시지
	 */
	private String parsingMsg14(String notiMsg)
	{
		int i = 0;
		int tokenCnt = 0;
		String[] tokenMsg;
		StringTokenizer str = new StringTokenizer(notiMsg, ";");
		tokenCnt = str.countTokens();
		tokenMsg = new String[tokenCnt];

		while(str.hasMoreTokens()) {
			tokenMsg[i] = str.nextToken();
			i++;
		}

		String retVal = "";
		for (int j = 0; j < tokenCnt; j++) {
			retVal += (tokenMsg[j] + "\n");
		}

		return retVal;
	}

	/**
	 * 서버로부터 받은 배차 유형 변경 Push Message를 분석
	 *
	 * @param notiMsg 서버로부터 받은 내용
	 * @return 파싱된 메시지
	 */
	private String parsingMsg60(String notiMsg) {
		int i = 0;
		int tokenCnt = 0;
		String[] tokenMsg;
		StringTokenizer str = new StringTokenizer(notiMsg, ";");
		tokenCnt = str.countTokens();
		tokenMsg = new String[tokenCnt];

		while(str.hasMoreTokens()) {
			tokenMsg[i] = str.nextToken();
			i++;
		}

		return tokenMsg[0];
	}

	private String parsingMsg(String notiMsg)
	{
		int i = 0;
		int tokenCnt = 0;
		String[] tokenMsg;
		StringTokenizer str = new StringTokenizer(notiMsg, ";");
		tokenCnt = str.countTokens();
		tokenMsg = new String[tokenCnt];

		while(str.hasMoreTokens())
		{
			tokenMsg[i] = str.nextToken();
			i++;
		}

		String retVal = "";
		for (int j = 0; j < tokenCnt; j++)
		{
			retVal += (tokenMsg[j] + "\n");
		}

		return retVal;
	}



	private String onlyNum(String str) {

		String numeral = "", temp = "";
		if ( str == null )
		{
			numeral = "180";
		}
		else
		{
			for( int i = 0; i < str.length(); i++ )
			{
				temp = str.substring(i,i+1);
				if( Character.isDigit(str.charAt(i)) ) //isDigit를 이용
				{
					numeral += temp;
				}
			}
		}
		return numeral;
	}



	private String resendPushMessage(String notiMsg, String senderGb) {
		int index = notiMsg.indexOf(';');

		if(index != -1)
		{
			pushSeq = notiMsg.substring(0, index);

			//mMobJson.reqResendPushMessage(mContext, pushSeq, senderGb, mResendPushMessageListener);
			Log.d("tag", "************ Call Mobile 2, pushSeq: " + pushSeq + ", senderGb:" + senderGb);

			notiMsg = notiMsg.substring(index + 1, notiMsg.length());
		}

		return notiMsg;
	}


	private void playSound(int sound, String popupViewMsg, String forcedTTSMsg)
	{

		if(sound == 0) {
			//묵음 등
		} else {

			if(sound == 1 || sound == 2) {

				if (forcedTTSMsg == null) {
					tts = new TTS(mContext, Locale.KOREAN, popupViewMsg);//TTS실행;
				} else {
					tts = new TTS(mContext, Locale.KOREAN, forcedTTSMsg);//TTS실행;
				}

			} else {
				mSoundManager = new SoundManager();
				mSoundManager.initSounds(mContext);
				mSoundManager.addSound(1, sound);
				mSoundManager.playSound(1);
				mSoundManager.setOnSoundMangeListener(new SoundManager.onSoundMangerListener() {

					@Override
					public void onCompleted(SoundPool soundPool, AudioManager audioManager) {
						soundPool = null;
						audioManager = null;

						mSoundManager = null;
					}
				});
			}
		}

		// 통화중 상태 캐치
		mTelMan = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		PhoneStateListener mListener = new PhoneStateListener() {

			//이 리스너는 playSound와 연관되어 설명하면 playSound가 먼저 호출될지 여기가 먼저 호출될지는 케이스마다 다름.
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (mTelMan.getCallState()) {
					case TelephonyManager.CALL_STATE_IDLE:
						//callState = 0;

						//체크를 한번이라도 줄기기 위해 주석처리
						//if(Util.getSharedData(mContext, "Tts_Read", "").equals("Y")) {
						if(tts != null /*&& tts.getBeforeCalling()*/) {//전화관련 상태였고 그때 완료된게 아니라면

							tts.setOnCalling(false);
							if(!tts.getDoneYN())
								tts.speak(null);
						}
						//}
						break;

					case TelephonyManager.CALL_STATE_RINGING:
					case TelephonyManager.CALL_STATE_OFFHOOK:
						//callState = 1;

						//체크를 한번이라도 줄기기 위해 주석처리
						//if(Util.getSharedData(mContext, "Tts_Read", "").equals("Y")) {
						if(tts != null) {
							tts.setOnCalling(true);
							if (tts.isSpeaking()) {
								tts.stop();
							}
						}
						//}
						break;
				}


				Log.d("TTS", "CALL_STATE:" + mTelMan.getCallState());
			}
		};
		mTelMan.listen(mListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private String getTTSNormalMent(int sound) {

		String finalToReadMsg = "";

		finalToReadMsg = "이트럭뱅크 서비스 시작합니다.";
		return finalToReadMsg;

	}

	private void playVibrate()
	{
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mVibrator.vibrate(1000);
	}

	//현재 화면에 보이는 프로세스 패키지 이름 알아보기
	public ComponentName getCurrentActivity()
	{

		ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> taskInfo = am.getRunningTasks(1);

		ComponentName topActivity = taskInfo.get(0).topActivity;

		return topActivity ;

	}

	/**
	 * 리스트 항목 선택 시 동작해야하는 이벤트
	 * @uml.property  name="mHandler"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case HANDLER_LOGOUT:
					break;

				case HANDLER_LOGOUT_SUCCESS:
					break;

				case HANDLER_START_SUCCESS:

					break;

			}


		}
	};



	private boolean checkBluetooth() {


		if(mMinewBeaconManager == null) {
			mMinewBeaconManager = MinewBeaconManager.getInstance(this);
		}


		BluetoothState bluetoothState = mMinewBeaconManager.checkBluetoothState();



		switch (bluetoothState) {
			case BluetoothStateNotSupported:
				//Toast.makeText(this, "Not Support BLE", Toast.LENGTH_SHORT).show();
				break;
			case BluetoothStatePowerOff:
				showBLEDialog();
				break;

			case BluetoothStatePowerOn:
				break;
		}



		return true;

	}

	private void showBLEDialog() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.enable();
		Toast.makeText(mContext, "자동으로 블루투스 실행 하였습니다.", Toast.LENGTH_SHORT).show();


		//Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		//startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

	}
}