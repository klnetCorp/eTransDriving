package kr.co.klnet.aos.etransdriving.trans.gps.packet;

/**
 * GPS 데이터를 추출
 */
public class GpsData
{
	public static final int DEF_MOB_DATE_LENGTH			= 6;
	public static final int DEF_MOB_TIME_LENGTH			= 6;
	public static final int DEF_MOB_GPS_STATUS_LENGTH	= 1;
	public static final int DEF_MOB_LATITUDE_LENGTH		= 9;
	public static final int DEF_MOB_LONGITUDE_LENGTH	= 9;
	public static final int DEF_MOB_SPEED_LENGTH		= 3;
	public static final int DEF_MOB_DIRECTION_LENGTH	= 3;
	public static final int DEF_MOB_EVENT_CODE_LENGTH	= 2;

	/**
	 * GPS 데이터 크기를 반환
	 * @return
	 */
	public static int getGpsDataSize()
	{
		return (DEF_MOB_DATE_LENGTH + DEF_MOB_TIME_LENGTH + DEF_MOB_GPS_STATUS_LENGTH + DEF_MOB_LATITUDE_LENGTH +
				DEF_MOB_LONGITUDE_LENGTH + DEF_MOB_SPEED_LENGTH + DEF_MOB_DIRECTION_LENGTH + DEF_MOB_EVENT_CODE_LENGTH);
	}

	/**
	 * @uml.property  name="mChrGPSStatus"
	 */
	char mChrGPSStatus;		// GPS상태 ('A':GPS Active / 'V':GPS Invalid(마지막 GPS Active 값을전송)
	/**
	 * @uml.property  name="mStrDateTime"
	 */
	String mStrDateTime;	// 날짜시간 (YYMMDDHHMMSS)
	/**
	 * @uml.property  name="mStrLatitude"
	 */
	String mStrLatitude;	// 위도
	/**
	 * @uml.property  name="mStrLongitude"
	 */
	String mStrLongitude;	// 경도
	/**
	 * @uml.property  name="mStrSpeed"
	 */
	String mStrSpeed;		// 속도
	/**
	 * @uml.property  name="mStrDirection"
	 */
	String mStrDirection;	// 방향
	/**
	 * @uml.property  name="mStrEventCode"
	 */
	String mStrEventCode;	// 이벤트코드

	/**
	 * 생성자
	 */
	public GpsData()
	{
		clear();
	}

	/**
	 * 주기보고 변수 초기화
	 */
	public void clear()
	{
		mStrDateTime	= null;
		mChrGPSStatus	= 0x00;
		mStrLatitude	= null;
		mStrLongitude	= null;
		mStrSpeed		= null;
		mStrDirection	= null;
		mStrEventCode	= null;
	}

	/**
	 * 주기보고 데이터 반환
	 * @return
	 */
	public String getData()
	{
		return mStrDateTime + mChrGPSStatus + mStrLatitude + mStrLongitude + mStrSpeed + mStrDirection + mStrEventCode;
	}

	/**
	 * 주기 보고 변수에 데이터 설정
	 * @param strDateTime
	 * @param chrGpsStatus
	 * @param dblLat
	 * @param dblLon
	 * @param shtSpeed
	 * @param shtDir
	 * @param strEventCode
	 */
	public void setData(String strDateTime, char chrGpsStatus, double dblLat, double dblLon,
                        short shtSpeed, short shtDir, String strEventCode)
	{
		mStrDateTime	= strDateTime;
		mChrGPSStatus	= chrGpsStatus;
		mStrLatitude	= String.format("%09.5f", dblLat);
		mStrLongitude	= String.format("%09.5f", dblLon);
		mStrSpeed		= String.format("%03d", shtSpeed);
		mStrDirection	= String.format("%03d", shtDir);	// 0~359

		// 00: NONE
		if (strEventCode.equals(""))
		{
			strEventCode = "00";
		}

		mStrEventCode = strEventCode;
	}
}