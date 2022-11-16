package kr.co.klnet.aos.etransdriving.trans.gps.common;

/**
 * 관제 공통 코드 정리
 */
public class AppCommon
{
	public static final int DEF_LOGOUT_TYPE_NONE = 0;
	public static final int DEF_LOGOUT_TYPE_AFTER_EXIT = 1; // 로그아웃시 종료
	public static final int DEF_LOGOUT_TYPE_AFTER_ANOTHER_USER_LOGIN = 2; // 다른사용자로 로그인

	/** 플래인 채널 */
	public static final int SOCKET_CHANNEL_TYPE_PLAIN = 0;
	/** SSL 채널 */
	public static final int SOCKET_CHANNEL_TYPE_SSL = 1;

	/** 서버로 전송시 한글필드의 경우 2바이트로 인식할수 있게 인코딩 */
	public static final String DEF_STRING_ENCODING_NAME = "euc-kr";

	// 이벤트 코드
	/** 주기보고 */
	public static final String DEF_EVENT_CODE_PERIOD_REPORT = "01";
	/** 즉시보고 */
	public static final String DEF_EVENT_CODE_DIRECT_REPORT = "02";

	// 코드 결정나면 재코딩
	public static final String DEF_EVENT_CODE_F1 = "30"; // 펑션키1
	public static final String DEF_EVENT_CODE_F2 = "35"; // 펑션키2
	public static final String DEF_EVENT_CODE_F3 = "40"; // 펑션키3
	public static final String DEF_EVENT_CODE_F4 = "45"; // 펑션키4
	public static final String DEF_EVENT_CODE_F5 = "50"; // 펑션키5
	public static final String DEF_EVENT_CODE_F6 = "60"; // 펑션키6
}