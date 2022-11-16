package kr.co.klnet.aos.etransdriving.trans.gps.packet;

import android.content.Context;
import com.lbsok.framework.util.StrUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import kr.co.klnet.aos.etransdriving.trans.gps.common.AppCommon;


/**
 * 관제 서버로 보낼 보고 패킷 생성
 */
public class ReportPacket
{
	public final static char DEF_MOB_PACKET_START_OF_TEXT		= 0x01;
	public final static char DEF_MOB_PACKET_END_OF_TEXT			= 0x02;
	public final static String DEF_MOB_COMMAND_DEFAULT			= "RPT";

	public static final int DEF_MOB_HEAD_STX_LENGTH				= 1;
	public static final int DEF_MOB_HEAD_DATA_LENGTH_LENGTH		= 4;
	public static final int DEF_MOB_HEAD_DATA_COUNT_LENGTH		= 2;
	public static final int DEF_MOB_HEAD_COMMAND_LENGTH			= 3;
	public static final int DEF_MOB_HEAD_MDN_LENGTH				= 13;

	public static final int DEF_MOB_BODY_DISTANCE_LENGTH		= 10;

	public static final int DEF_MOB_BODY_CARRIER_ID_LENGTH		= 15;	// 운송사ID 길이
	public static final int DEF_MOB_BODY_CAR_CD_LENGTH			= 12;	// 차량ID 길이
	public static final int DEF_MOB_BODY_CHASSIS_NO_LENGTH		= 12;	// 샤시 길이
	public static final int DEF_MOB_BODY_CONTAINER_NO_LENGTH	= 12;	// 컨테이너번호 길이
	public static final int DEF_MOB_BODY_DISPATCH_NO_LENGTH		= 20;	// 배차번호 길이

	public static final int DEF_MOB_BODY_MAC_ADDRESS_LENGTH		= 12;	// 맥어드레스 길이

	public static final int DEF_MOB_BODY_ORDER_TYPE_LENGTH		= 3;	// ORDER TYPE 길이
	public static final int DEF_MOB_BODY_IMPORT_TYPE_LENGTH		= 1;	// IMPORT TYPE 길이
	public static final int DEF_MOB_BODY_SEND_TERM_LENGTH		= 5;	// SEND TERM 길이
	public static final int DEF_MOB_BODY_COLLECT_TERM_LENGTH	= 5;	// COLLECT TERM 길이
	public static final int DEF_MOB_BODY_REST_FLAG_LENGTH		= 1;	// REST FLAG 길이

	public static final int DEF_MOB_TAIL_CHECK_SUM_LENGTH		= 1;
	public static final int DEF_MOB_TAIL_ETX_LENGTH				= 1;

	public static int getBodySize()
	{
		return DEF_MOB_BODY_DISTANCE_LENGTH + DEF_MOB_BODY_CARRIER_ID_LENGTH + DEF_MOB_BODY_CAR_CD_LENGTH + DEF_MOB_BODY_CHASSIS_NO_LENGTH
				+ DEF_MOB_BODY_CONTAINER_NO_LENGTH + DEF_MOB_BODY_CONTAINER_NO_LENGTH + DEF_MOB_BODY_DISPATCH_NO_LENGTH
				+ DEF_MOB_BODY_MAC_ADDRESS_LENGTH + DEF_MOB_BODY_ORDER_TYPE_LENGTH + DEF_MOB_BODY_IMPORT_TYPE_LENGTH + DEF_MOB_BODY_SEND_TERM_LENGTH
				+ DEF_MOB_BODY_COLLECT_TERM_LENGTH + DEF_MOB_BODY_REST_FLAG_LENGTH;
	}

	/**
	 * 헤더의 크기를 구한다. 자바에는 sizeof()가 없으므로 length 또는 아래처럼 하드코딩되어져야 한다.
	 * 
	 * @return
	 */
	public static int getHeadSize()
	{
		return (DEF_MOB_HEAD_STX_LENGTH + DEF_MOB_HEAD_DATA_LENGTH_LENGTH + DEF_MOB_HEAD_DATA_COUNT_LENGTH +
				DEF_MOB_HEAD_COMMAND_LENGTH + DEF_MOB_HEAD_MDN_LENGTH);
	}

	public static int getTailSize()
	{
		return DEF_MOB_TAIL_CHECK_SUM_LENGTH + DEF_MOB_TAIL_ETX_LENGTH;
	}

	/**
	 * @uml.property  name="mStrEncoding"
	 */
	private String mStrEncoding = AppCommon.DEF_STRING_ENCODING_NAME;
	// ---------------------------------------------------
	/**
	 * @uml.property  name="mChrSTX"
	 */
	char			mChrSTX;			// Start
	/**
	 * @uml.property  name="mStrDataLength"
	 */
	String mStrDataLength;		// 총길이
	/**
	 * @uml.property  name="mStrDataCount"
	 */
	String mStrDataCount;		// Body1의 데이터 갯수
	/**
	 * @uml.property  name="mStrCommand"
	 */
	String mStrCommand;		// 전문종류

	/**
	 * @uml.property  name="mStrMDN"
	 */
	String mStrMDN;			// 단말기 MDN
	// ---------------------------------------------------
	/**
	 * @uml.property  name="mListGpsData"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="com.klnet.trans.gps.packet.GpsData"
	 */
	List<GpsData> mListGpsData;
	// ---------------------------------------------------
	/**
	 * @uml.property  name="mStrDistance"
	 */
	String mStrDistance;		// 거리
	/**
	 * @uml.property  name="mStrCarrierId"
	 */
	String mStrCarrierId;		// 운송사ID
	/**
	 * @uml.property  name="mStrCarCd"
	 */
	String mStrCarCd;			// 차량ID
	/**
	 * @uml.property  name="mStrChassisNo"
	 */
	String mStrChassisNo;		// 샤시번호

	/**
	 * @uml.property  name="mStrContainerNo1"
	 */
	String mStrContainerNo1;	// 컨테이너번호1
	/**
	 * @uml.property  name="mStrContainerNo2"
	 */
	String mStrContainerNo2;	// 컨테이너번호2
	/**
	 * @uml.property  name="mStrDispatchNo"
	 */
	String mStrDispatchNo;		// 배차번호

	/**
	 * @uml.property  name="mStrMacAddress"
	 */
	String mStrMacAddress;		// 맥어드레스
	/**
	 * @uml.property  name="mStrOrderType"
	 */
	String mStrOrderType;		// Order Type
	/**
	 * @uml.property  name="mStrImportType"
	 */
	String mStrImportType;		// Import Type

	/**
	 * @uml.property  name="mStrSendTerm"
	 */
	String mStrSendTerm;		// Send Term
	/**
	 * @uml.property  name="mStrCollectTerm"
	 */
	String mStrCollectTerm;	// Collect Term

	/**
	 * @uml.property  name="mStrRestFlag"
	 */
	String mStrRestFlag;		// Rest Flag

	/**
	 * @uml.property  name="mChrCheckSum"
	 */
	char			mChrCheckSum;		// Check Sum
	/**
	 * @uml.property  name="mChrETX"
	 */
	char			mChrETX;			// End
	
	/**
	 * @uml.property  name="mCtxservice"
	 */
	Context mCtxservice;

	/**
	 * 패킷 초기화
	 * @param strEncoding
	 */
	public ReportPacket(String strEncoding, Context ctxService)
	{
		// 한글 필드가 있을지 몰라 encoding을 받음.
		mStrEncoding		= strEncoding;
		mCtxservice 		= ctxService;
		
		mChrSTX				= 0x01;
		mStrDataLength		= null;
		mStrDataCount		= null;
		mStrCommand			= null;
		mStrMDN				= null;
		mListGpsData		= new ArrayList<GpsData>();
		mStrDistance		= null;

		mStrCarrierId		= null;
		mStrCarCd			= null;
		mStrChassisNo		= null;
		mStrContainerNo1	= null;
		mStrContainerNo2	= null;
		mStrDispatchNo		= null;
		mStrMacAddress		= null;

		mStrOrderType		= null;
		mStrImportType		= null;
		mStrSendTerm		= null;
		mStrCollectTerm		= null;
		mStrRestFlag		= null;

		mChrCheckSum		= 0x00;
		mChrETX				= 0x02;
	}

	/**
	 * 패킷 초기화
	 */
	public void clear()
	{
		clearGpsData();

		mChrSTX				= 0x01;
		mStrDataLength		= null;
		mStrDataCount		= null;
		mStrCommand			= null;
		mStrMDN				= null;

		mStrDistance		= null;
		mStrCarrierId		= null;
		mStrCarCd			= null;
		mStrChassisNo		= null;
		mStrContainerNo1	= null;
		mStrContainerNo2	= null;
		mStrDispatchNo		= null;

		mStrMacAddress		= null;

		mStrOrderType		= null;
		mStrImportType		= null;
		mStrSendTerm		= null;
		mStrCollectTerm		= null;
		mStrRestFlag		= null;

		mChrCheckSum		= 0x00;
		mChrETX				= 0x02;
	}

//	String CUR_DATE = "%s:%s:%s:%s:%s:%s";
//	private String getCurTime()
//	{
//		Calendar curDate = null;
//		curDate = Calendar.getInstance(Locale.KOREAN);
//
//		String year = String.valueOf(curDate.get(Calendar.YEAR));
//		String month = String.valueOf(curDate.get(Calendar.MONTH) + 1);
//		String date = String.valueOf(curDate.get(Calendar.DATE));
//		String hour = String.valueOf(curDate.get(Calendar.HOUR_OF_DAY));
//		String minute = String.valueOf(curDate.get(Calendar.MINUTE));
//		String second = String.valueOf(curDate.get(Calendar.SECOND));
//
//		String currDate = String.format(CUR_DATE, year, month, date, hour, minute, second);
//		return currDate;
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
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//	}

	/**
	 * GPS 데이터 추가
	 * @param clsBody
	 */
	public void addGpsData(GpsData clsBody)
	{
		if (mListGpsData != null)
		{
			if (mListGpsData.size() > 98)
			{
				mListGpsData.remove(0);
				//Log.d("KGW", "addGpsData Size : " + mListGpsData.size());
			}

			mListGpsData.add(clsBody);
			//TODO
//			saveToFile("addGpsData(Size : " + mListGpsData.size() + ")");
		}
	}

	public void clearGpsData()
	{
		if (mListGpsData != null)
		{
			for (int intCnt = 0; intCnt < mListGpsData.size(); intCnt++)
			{
				GpsData clsData = mListGpsData.get(intCnt);
				if (clsData != null)
				{
					clsData.clear();
					clsData = null;
				}
			}

			mListGpsData.clear();
		}
	}

	/**
	 * 패킷 내용 반환
	 * @return
	 */
	public byte[] getBody()
	{
		try
		{
			return getBodyText().getBytes(mStrEncoding);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 텍스트 패킷 내용 반환
	 * @return
	 */
	public String getBodyText()
	{
		return mStrDistance + mStrCarrierId + mStrCarCd + mStrChassisNo + mStrContainerNo1 + mStrContainerNo2 + mStrDispatchNo + mStrMacAddress
				+ mStrOrderType + mStrImportType + mStrSendTerm + mStrCollectTerm + mStrRestFlag;
	}

	/**
	 * GPS 데이터 반환
	 * @return
	 */
	public List<GpsData> getGpsData()
	{
		return mListGpsData;
	}

	/**
	 * GPS 데이터 크기
	 * @return
	 */
	public int getGpsDataSize()
	{
		return (mListGpsData != null) ? mListGpsData.size() : 0;
	}

	/**
	 * 패킷 헤더 반환
	 * @return
	 */
	public byte[] getHeader()
	{
		try
		{
			return (mChrSTX + mStrDataLength + mStrDataCount + mStrCommand + mStrMDN).getBytes(mStrEncoding);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 조합된 패킷 반환
	 * @return
	 */
	public byte[] getPacket()
	{
		int intCnt = 0;
		String strPacket = "";

		try
		{
			strPacket = mChrSTX + mStrDataLength + mStrDataCount + mStrCommand + mStrMDN;
			for (intCnt = 0; intCnt < mListGpsData.size(); intCnt++)
			{
				GpsData clsBody = mListGpsData.get(intCnt);
				strPacket += clsBody.getData();
			}

			strPacket += getBodyText() + mChrCheckSum + mChrETX;
			return strPacket.getBytes(mStrEncoding);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 패킷 Tail 반환
	 * @return
	 */
	public byte[] getTail()
	{
		try
		{
			return ("" + mChrCheckSum + mChrETX).getBytes(mStrEncoding);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 패킷 내용 설정
	 * @param lngDistance
	 * @param strCarrierId
	 * @param strCarCd
	 * @param strChassisNo
	 * @param strContainerNo1
	 * @param strContainerNo2
	 * @param strDispatchNo
	 * @param strMacAddress
	 * @param strOrderType
	 * @param strImportType
	 * @param strSendTerm
	 * @param strCollectTerm
	 * @param strRestType
	 */
	public void setBody(long lngDistance, String strCarrierId, String strCarCd, String strChassisNo,
                        String strContainerNo1, String strContainerNo2, String strDispatchNo, String strMacAddress,
                        String strOrderType, String strImportType, int strSendTerm, int strCollectTerm, String strRestType)
	{
		if (lngDistance >= 0 && lngDistance <= 9999999999L)
		{
			mStrDistance = String.format("%010d", lngDistance);
		}
		else
		{
			mStrDistance = String.format("%010d", 0);
		}

		// 빈자리는 스페이스로 채운다.
		if (strCarrierId.length() == DEF_MOB_BODY_CARRIER_ID_LENGTH)
		{
			mStrCarrierId = strCarrierId;
		}
		else
		{
			mStrCarrierId = StrUtil.padRightChar(AppCommon.DEF_STRING_ENCODING_NAME, strCarrierId, ' ', DEF_MOB_BODY_CARRIER_ID_LENGTH);
		}

		mStrCarCd = StrUtil.padRightChar(AppCommon.DEF_STRING_ENCODING_NAME, strCarCd, ' ', DEF_MOB_BODY_CAR_CD_LENGTH);
		if (mStrCarCd == null)
		{
			mStrCarCd = strCarCd;
		}

		mStrChassisNo = StrUtil.padRightChar(AppCommon.DEF_STRING_ENCODING_NAME, strChassisNo, ' ', DEF_MOB_BODY_CHASSIS_NO_LENGTH);
		if (mStrChassisNo == null)
		{
			mStrChassisNo = strChassisNo;
		}

		mStrContainerNo1 = StrUtil.padRightChar(AppCommon.DEF_STRING_ENCODING_NAME, strContainerNo1, ' ', DEF_MOB_BODY_CONTAINER_NO_LENGTH);
		if (mStrContainerNo1 == null)
		{
			mStrContainerNo1 = strContainerNo1;
		}

		mStrContainerNo2 = StrUtil.padRightChar(AppCommon.DEF_STRING_ENCODING_NAME, strContainerNo2, ' ', DEF_MOB_BODY_CONTAINER_NO_LENGTH);
		if (mStrContainerNo2 == null)
		{
			mStrContainerNo2 = strContainerNo2;
		}

		if (strDispatchNo.length() == DEF_MOB_BODY_DISPATCH_NO_LENGTH)
		{
			mStrDispatchNo = strDispatchNo;
		}
		else
		{
			mStrDispatchNo = StrUtil.padRightChar(AppCommon.DEF_STRING_ENCODING_NAME, strDispatchNo, ' ', DEF_MOB_BODY_DISPATCH_NO_LENGTH);
		}

		if (strMacAddress.length() == DEF_MOB_BODY_MAC_ADDRESS_LENGTH)
		{
			mStrMacAddress = strMacAddress;
		}
		else
		{
			mStrMacAddress = StrUtil.padRightChar(AppCommon.DEF_STRING_ENCODING_NAME, strMacAddress, ' ', DEF_MOB_BODY_MAC_ADDRESS_LENGTH);
		}

		if (strOrderType.length() == DEF_MOB_BODY_ORDER_TYPE_LENGTH)
		{
			mStrOrderType = strOrderType;
		}
		else
		{
			mStrOrderType = StrUtil.padRightChar(AppCommon.DEF_STRING_ENCODING_NAME, strOrderType, ' ', DEF_MOB_BODY_ORDER_TYPE_LENGTH);
		}

		if (strImportType.length() == DEF_MOB_BODY_IMPORT_TYPE_LENGTH)
		{
			mStrImportType = strImportType;
		}
		else
		{
			mStrImportType = StrUtil.padRightChar(AppCommon.DEF_STRING_ENCODING_NAME, strImportType, ' ', DEF_MOB_BODY_IMPORT_TYPE_LENGTH);
		}

		if (strSendTerm >= 0 && strSendTerm <= 9999999999L)
		{
			mStrSendTerm = String.format("%05d", strSendTerm);
		}
		else
		{
			mStrSendTerm = String.format("%05d", 0);
		}

		if (strCollectTerm >= 0 && strCollectTerm <= 9999999999L)
		{
			mStrCollectTerm = String.format("%05d", strCollectTerm);
		}
		else
		{
			mStrCollectTerm = String.format("%05d", 0);
		}

		if (strRestType != null && strRestType.length() != 0)
		{
			mStrRestFlag = strRestType;
		}
		else
		{
			mStrRestFlag = "N";
		}
		//TODO
//		saveToFile("setBody(mStrCarrierId : " + mStrCarrierId + ", mStrCarCd : " + mStrCarCd + ")");
	}

	/**
	 * 패킷 Header 설정
	 * @param chrSTX
	 * @param strCommand
	 * @param strMDN
	 * @param intDataLegth
	 * @param intDataCount
	 */
	public void setHeader(char chrSTX, String strCommand, String strMDN, int intDataLegth, int intDataCount)
	{
		mChrSTX			= chrSTX;
		mStrDataLength	= String.format("%04d", intDataLegth);
		mStrDataCount	= String.format("%02d", intDataCount);
		mStrCommand		= strCommand;
		mStrMDN			= StrUtil.padLeftChar(strMDN, '@', DEF_MOB_HEAD_MDN_LENGTH);
	}

	/**
	 * 패킷 Tail 설정
	 * @param chrCheckSum
	 * @param chrETX
	 */
	public void setTail(char chrCheckSum, char chrETX)
	{
		mChrCheckSum = chrCheckSum;
		mChrETX = chrETX;
	}
}