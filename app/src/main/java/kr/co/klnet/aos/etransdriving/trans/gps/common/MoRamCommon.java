package kr.co.klnet.aos.etransdriving.trans.gps.common;

public class MoRamCommon
{
	/** 서버로 전송시 한글필드의 경우 2바이트로 인식할수 있게 인코딩 */
	public static final String ENCODING_NAME			= "euc-kr";

	// 이벤트 코드
	/** 주기보고 */
	public static final String EVENT_CODE_PERIOD_REPORT	= "01";
	/** 즉시보고 */
	public static final String EVENT_CODE_DIRECT_REPORT	= "02";

	public static final char REP_PKT_STX				= 0x01;
	public final static char REP_PKT_ETX				= 0x02;
	public final static String REP_PKT_COMMAND			= "RPT";

	// Response Error Code
	/** 즉시보고 */
	public static final byte RESP_ERR_SUCCESS			= 0x30;
	/** 인증오류 */
	public static final byte RESP_ERR_AUTHENTICATION	= 0x31;
	/** Check Sum 오류 */
	public static final byte RESP_ERR_CHECKSUM			= 0x32;
	/** Packet 길이 오류 */
	public static final byte RESP_ERR_LENGTH			= 0x33;

	// Report Packet Field Length
	// Header
	/** Packet의 시작 Length */
	public static final int REP_PKT_LENGTH_STX			= 1;
	/** Data 총 길이 Length */
	public static final int REP_PKT_LENGTH_DATA_LENGTH	= 4;
	/** GPS Data 부분의 개수 Length */
	public static final int REP_PKT_LENGTH_DATA_COUNT	= 2;
	/** 명령구분 Length */
	public static final int REP_PKT_LENGTH_COMMAND		= 3;
	/** 단말기 MDN Length */
	public static final int REP_PKT_LENGTH_UNIT_ID		= 13;

	// BODY
	// GPS
	/** GPS Data 생성 일자 Length */
	public static final int REP_PKT_LENGTH_DATE			= 6;
	/** GPS Data 생성 시각 Length */
	public static final int REP_PKT_LENGTH_TIME			= 6;
	/** GPS 상태 Length */
	public static final int REP_PKT_LENGTH_GPS_STATUS	= 1;
	/** 위도 Length */
	public static final int REP_PKT_LENGTH_LATITUDE		= 9;
	/** 경도 Length */
	public static final int REP_PKT_LENGTH_LONGITUDE	= 9;
	/** 속도 Length */
	public static final int REP_PKT_LENGTH_SPEED		= 3;
	/** 방향 Length */
	public static final int REP_PKT_LENGTH_DIRECTION	= 3;
	/** Event Code Length */
	public static final int REP_PKT_LENGTH_EVENT_CODE	= 2;
	// 기본정보
	/** 이전좌표와 현재 좌표와의 거리 Length */
	public static final int REP_PKT_LENGTH_DISTANCE		= 10;
	/** 운송사 코드 Length */
	public static final int REP_PKT_LENGTH_CARRIER_CODE	= 15;
	/** 차량번호 Length */
	public static final int REP_PKT_LENGTH_CAR_CODE		= 12;
	/** 샷시번호 Length */
	public static final int REP_PKT_LENGTH_CHASSIS_NO	= 12;
	/** 컨테이너 번호 Length */
	public static final int REP_PKT_LENGTH_CONTAINER_NO	= 12;
	/** 배차번호 Length */
	public static final int REP_PKT_LENGTH_DISPATCH_NO	= 20;
	/** 단말 MAC Address Length */
	public static final int REP_PKT_LENGTH_MAC_ADDRESS	= 12;
	/** 업무유형 Length */
	public static final int REP_PKT_LENGTH_ORDER_TYPE	= 3;
	/** 수출입 유형 Length */
	public static final int REP_PKT_LENGTH_IETYPE		= 1;
	/** GPS Data 전송 주기 Length */
	public static final int REP_PKT_LENGTH_SEND_TERM	= 5;
	/** GPS Data 수집 주기 Length */
	public static final int REP_PKT_LENGTH_COLLECT_TERM	= 5;
	/** 휴식 설정 유무 Length */
	public static final int REP_PKT_LENGTH_REST_FLAG	= 1;

	// TAIL
	/** Check Sum Length */
	public static final int REP_PKT_LENGTH_CHECK_SUM	= 1;
	/** Packet의 끝 Length */
	public static final int REP_PKT_LENGTH_ETX			= 1;
}