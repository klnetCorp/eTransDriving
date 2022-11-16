package kr.co.klnet.aos.etransdriving.trans.gps.push;

/**
 * Push 메시지 분석에 필요한 데이터
 */
public class UICommon
{
	public static final String ACTION_RESTART_PERIOD_REPORT = "com.klnet.trans.push.PRSRESTART";
	public static final String ACTION_RESTART_WORK_REPORT = "com.klnet.trans.push.WORKRESTART";
	
	public final static String INTENT_PUSH_POPUP = "intent.ssomon.action.PUSH_POPUP";
	public final static String INTENT_GCM_WAKE_UP = "intent.ssomon.action.GCM_WAKE_UP";

	public static final String EXTRA_NOTI = "com.ssomon.lib.services.push_noti";
	public static final String EXTRA_NOTI_CMD = "com.ssomon.lib.services.push_noti_cmd";
	public static final String EXTRA_NOTI_MSG = "com.ssomon.lib.services.push_noti_msg";
	public static final String EXTRA_NOTI_MSG1 = "com.ssomon.lib.services.push_noti_msg1";
	public static final String EXTRA_NOTI_MSG2 = "com.ssomon.lib.services.push_noti_msg2";

	// KLNET Push Command
	/** 지정배차 */
	public static final int CMD_FIXED_ASSIGN				= 10;
	/** 공유배차 의뢰 */
	public static final int CMD_REQ_SHARED_ASSIGN			= 11;
	/** 공유배차 수수료 결재 */
	public static final int CMD_REQ_PAY						= 12;
	/** 공유배차 확정 */
	public static final int CMD_CONFIRM_SHARED_ASSIGN		= 13;
	/** 배차 수정 */
	public static final int CMD_ASSIGN_UPDATE				= 14;
	/** 상차 업무보고 독려 */
	public static final int CMD_REQ_UPLOAD_REPORT			= 20;
	/** 작업 업무보고 독려 */
	public static final int CMD_REQ_WORK_REPORT				= 21;
	/** 하차 업무보고 독려 */
	public static final int CMD_REQ_DOWNLOAD_REPORT			= 22;
	/** 상차완료 변경 */
	public static final int CMD_CHANGE_UPLOAD_COMPLETE		= 23;
	/** 작업(적출)완료 변경 */
	public static final int CMD_CHANGE_WORK1_COMPLETE		= 24;
	/** 작업(적재)완료 변경 */
	public static final int CMD_CHANGE_WORK2_COMPLETE		= 25;
	/** 하차완료 변경 */
	public static final int CMD_CHANGE_DOWNLOAD_COMPLETE	= 26;
	/** 위치정보 수집주기 변경 */
	public static final int CMD_CHANGE_GET_PERIOD			= 30;
	/** 위치정보 보고주기 변경 */
	public static final int CMD_CHANGE_REPORT_PERIOD		= 31;
	/** 도착 예정시간 수집주기 변경 */
	public static final int CMD_CHANGE_GET_FINISH			= 32;
	/** 도착 예정시간 보고주기 변경 */
	public static final int CMD_CHANGE_REPORT_FINISH		= 33;	
	/** 위치정보 수집/보고 주기 변경 */
	public static final int CMD_CHANGE_GET_REPORT_FINISH	= 35;
	/** 관제시작 */
	public static final int CMD_CHANGE_LBS_TRACE_START	    = 36;
	/** 관제중지 */
	public static final int CMD_CHANGE_LBS_TRACE_STOP	    = 37;	
	/** 코피노 전송 알람 */
	public static final int CMD_REQ_COPINO_ALARM	   		= 38;
	/** 즉시 위치정보 보고 */
	public static final int CMD_FORCED_LOGOUT_EVENT			= 39;
	/** 즉시 위치정보 보고 */
	public static final int CMD_GET_GPS_POSITION			= 40;
	/** 메시지 전달 */
	public static final int CMD_PRIVATE_MESSAGE				= 50;
	/** 휘발성 메시지 */
	public static final int CMD_TEMPORARY_MESSAGE			= 51;
	/** 화물검색요청메세지 */
	public static final int CMD_DISPATCH_SEARCH_MESSAGE		= 52;
	/** 인수도증 확인 도착 메시지 */
	public static final int CMD_SLIP_ARRIVE_MESSAGE			= 53;
	/** 셔틀배차 */
	public static final int CMD_REQ_S_COPINO_ALARM			= 54;
	/** APK 강제 업데이트 */
	public static final int CMD_APK_UPDATE					= 55;
	/** 필수확인메세지 */
	public static final int CMD_CANNOT_CANCEL_MESSAGE		= 56;
	/** 리핸들링 알림 메시지 */
	public static final int CMD_REHANDLING_MESSAGE			= 57;
	/** 터미널 도착확인 알림 메시지 */
	public static final int CMD_TML_ARRIVAL_MESSAGE			= 58;
	/** 배차상태 변화. 관제서버에 변화된 값을 전달해야 함 */
	public static final int CMD_CHANGE_STATUS				= 60;
	/** 푸쉬 맟 전화 */
	public static final int CMD_MESSAGE_REQ_PHNONE			= 61;
	/** 블럭도착 팝업선택 */
	public static final int CMD_MESSAGE_BLOCK_REQ_ARRIVAL	= 62;
	/** 출발알림요청메세지 */
	public static final int CMD_TERMINAL_START_MESSAGE		= 63;
	/** 출발알림통보메세지  */
	public static final int CMD_TERMINAL_START_ALARM_MESSAGE= 64;


	/**beacon start  */
	public static final int CMD_TERMINAL_START_BEACON= 65;
	/**beacon stop  */
	public static final int CMD_TERMINAL_STOP_BEACON= 66;


}