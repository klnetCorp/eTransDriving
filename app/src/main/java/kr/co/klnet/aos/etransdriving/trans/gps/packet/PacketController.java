package kr.co.klnet.aos.etransdriving.trans.gps.packet;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.lbsok.framework.gis.Distance;
//import com.lbsok.framework.gps.GpsInfo;
import com.lbsok.framework.gps.LocationMgr;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.klnet.aos.etransdriving.BuildConfig;
import kr.co.klnet.aos.etransdriving.EtransDrivingApp;
import kr.co.klnet.aos.etransdriving.trans.gps.common.AppCommon;
import kr.co.klnet.aos.etransdriving.trans.gps.push.JLocationManager;
import kr.co.klnet.aos.etransdriving.util.CommonUtil;

/**
 * 관제 서버로 보낼 패킷 설정
 */
public class PacketController
{
	private final static String TAG = "PacketController";
	/**
	 * @uml.property  name="mClsReportPacket"
	 * @uml.associationEnd  
	 */
	private ReportPacket	mClsReportPacket;
	/**
	 * @uml.property  name="mClsLocationMgr"
	 * @uml.associationEnd  
	 */
	private JLocationManager mClsLocationMgr;

	private static double	mLongitude;
	private static double	mLatitude;
	private static long		mLngDistance;

	/**
	 * @uml.property  name="mCtxService"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private Context mCtxService;

	/**
	 * 생성자
	 * @param ctxService
	 * @throws Exception
	 */
	public PacketController(Context ctxService) throws Exception {
		mCtxService		= ctxService;
		mLongitude		= 0;
		mLatitude		= 0;
		mLngDistance	= 0;

		mClsReportPacket = new ReportPacket(AppCommon.DEF_STRING_ENCODING_NAME, ctxService);
	}

	public void startLocationService() {

		mClsLocationMgr = new JLocationManager(mCtxService);
		mClsLocationMgr.setName("REPORT");
		mClsLocationMgr.execute();
	}

	public void requestSingleUpdate(String provider) {
		mClsLocationMgr.requestSingleUpdate(provider);
	}

	/**
	 * 패킷 내용 초기화
	 */
	public void clearPacket()
	{
		mClsReportPacket.clear();
	}
	
	public int getGpsDataSize()
	{
		return mClsReportPacket.getGpsDataSize();
	}

	/**
	 * 패킷에 데이터 설정 후 반환
	 * @return
	 */
	public ByteBuffer makePacket()
	{
		int			intCnt				= 0;
		int			intDataCount		= 0;
		int			intDataLength		= 0;
		byte		bytCheckSum			= 0x00;
		ByteBuffer bbSendBuffer		= null;

		String strCarrierId		= "";
		String strVehId			= "";
		String strChassisNo		= "";
		String strContainerNo1	= "";
		String strContainerNo2	= "";
		String strDispatchNo	= "";
		String strMacAddress	= "";

		String strOrderType		= "100";
		String strImportType	= "O";
		int	strSendTerm			= 0;
		int	strCollectTerm		= 0;
		String strRestFlag		= "N";

		try
		{
			// -------------------------------------------------------------
			// 헤더
			// -------------------------------------------------------------
			intDataCount = mClsReportPacket.getGpsDataSize(); // 생성된 GPS Data의 갯수

			intDataLength = ReportPacket.getHeadSize() + (GpsData.getGpsDataSize() * intDataCount) + // intDataCount만큼 반복
					ReportPacket.getBodySize() + ReportPacket.getTailSize();

			mClsReportPacket.setHeader(ReportPacket.DEF_MOB_PACKET_START_OF_TEXT, ReportPacket.DEF_MOB_COMMAND_DEFAULT,
					EtransDrivingApp.getInstance().getMobileNo(), intDataLength, intDataCount);
			// -------------------------------------------------------------

			// -------------------------------------------------------------
			// Body
			// -------------------------------------------------------------
			strCarrierId		= EtransDrivingApp.getInstance().getCarrierId();
			if (strCarrierId.equals("") || strCarrierId.length() == 0 || strCarrierId == null)
			{
				strCarrierId = "0"; // 0번
				EtransDrivingApp.getInstance().setCarrierId(strCarrierId);
			}

			String carCd = EtransDrivingApp.getInstance().getCarCd();
			strVehId = CommonUtil.encodeUtf8ToEuckr(carCd);

			strMacAddress		= EtransDrivingApp.getInstance().getMacAddress();
			strChassisNo		= EtransDrivingApp.getInstance().getChassisNo();
			if (strChassisNo.equals("샷시없음"))
			{
				strChassisNo = " ";
				EtransDrivingApp.getInstance().setChassisNo(strChassisNo);
			}
			strContainerNo1		= EtransDrivingApp.getInstance().getContainerNo1();
			strContainerNo2		= EtransDrivingApp.getInstance().getContainerNo2();
			strDispatchNo		= EtransDrivingApp.getInstance().getDispatchNo();
			if (strDispatchNo.equals("null") || strDispatchNo.equals("NULL"))
			{
				strDispatchNo = " ";
				EtransDrivingApp.getInstance().setDispatchNo(strDispatchNo);
			}
			strOrderType		= EtransDrivingApp.getInstance().getOrderType();
			strImportType		= EtransDrivingApp.getInstance().getImportType();
			strRestFlag			= EtransDrivingApp.getInstance().getRestFlag();
			strSendTerm			= Integer.parseInt(EtransDrivingApp.getInstance().getReportPeroid());
			strCollectTerm		= Integer.parseInt(EtransDrivingApp.getInstance().getCreationPeroid());

			mClsReportPacket.setBody(
					mLngDistance * 100, // m에서 Cm로 환산
					strCarrierId, strVehId, strChassisNo, strContainerNo1, strContainerNo2, strDispatchNo, strMacAddress, strOrderType,
					strImportType, strSendTerm, strCollectTerm, strRestFlag);

			mLngDistance = 0; // 누적거리 계산은 0으로 초기화
			// -------------------------------------------------------------

			// -------------------------------------------------------------
			// 테일
			// -------------------------------------------------------------
			// CheckSum
			for (intCnt = 0; intCnt < intDataCount; intCnt++)
			{
				GpsData clsGpsData = mClsReportPacket.getGpsData().get(intCnt);
				byte[] arbGpsData = clsGpsData.getData().getBytes();

				for (int intLoopCnt = 0; intLoopCnt < arbGpsData.length; intLoopCnt++)
				{
					bytCheckSum = (byte) (bytCheckSum ^ arbGpsData[intLoopCnt]);
				}
			}

			byte[] arbBody = mClsReportPacket.getBody();
			for (intCnt = 0; intCnt < arbBody.length; intCnt++)
			{
				bytCheckSum = (byte) (bytCheckSum ^ arbBody[intCnt]);
			}

			mClsReportPacket.setTail((char) bytCheckSum, ReportPacket.DEF_MOB_PACKET_END_OF_TEXT);
			// -------------------------------------------------------------
			bbSendBuffer = ByteBuffer.wrap(mClsReportPacket.getPacket());
		}
		catch (Exception ex)
		{
			//ex.printStackTrace();
		}
		finally
		{

		}

		return bbSendBuffer;
	}

	public void collectNetworkLocationData() {
		mClsLocationMgr.startNetworkProvider();
	}

	public Location getLatestLocation(String provider) {
		Location loc = null;
		if(mClsLocationMgr!=null)
			loc = mClsLocationMgr.getLatestLocation(provider);
		return loc;
	}
	//TODO
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
//		catch(IOException e)
//		{
//			e.printStackTrace();
//		}
//	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}


	public static String byteArrayToHexString(byte[] bytes){

		StringBuilder sb = new StringBuilder();

		for(byte b : bytes){
			sb.append(String.format("%02X ", b&0xff));
		}

		return sb.toString();
	}


	public static String byteArrayToString(byte[] bytes){

//		StringBuilder buffer = new StringBuilder();
//
//		for(byte b : bytes){
//			buffer.append(String.format("%c ", b));
//		}
		//		return sb.toString();

		String buffer = new String(bytes);
		return buffer;
	}

	public void dumpPacket(ByteBuffer b)
	{
		try {
			Log.d("Report Packet", "dumpPacket, data=[" + byteArrayToString(b.array()) + "]");
			Log.d("Report Packet", "dumpPacket, hex=[" + byteArrayToHexString(b.array()) + "]");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * GPS Data를 생성하여 PeriodReportPacket 클래스에 계속 추가한다.
	 * 
	 * @param strEventCode
	 * @return 0: succcess, -1:전송할GPS데이터가 없음, -2:저장된GPS데이터가 비정상
	 */
	public int makePeriodReportGpsData(String strEventCode)
	{
		double	longitude		= 0;
		double	latitude		= 0;
		float	speed			= 0;
		float	bearing			= 0;
		short	shrSpeed		= 0;
		short	shtDirection	= 0;
		String provider		= "";
		char	chrGpsStatus	= 'G'; // ‘G’ : GPS Active / 'N' : Network Active / ‘I’ : Provider Invalid
		GpsData	clsGpsData		= null;
		Location location		= null;

		try
		{
			JGpsInfo gpsInfo	= JGpsInfo.getInst();
			int size		= gpsInfo.getArrayListSize();
			if (size <= 0)
			{

				if (mClsLocationMgr != null)
				{
					mClsLocationMgr.resetCurrentBestLocation();
					mClsLocationMgr.startNetworkProvider();
					Log.d(TAG, "restart network provider");
					return -1;
				}
			}

			clsGpsData = new GpsData();
			SimpleDateFormat sdfDateTimeFormat = new SimpleDateFormat("yyMMddHHmmss");
			String strCurDateTime = sdfDateTimeFormat.format(new Date());

//			longitude		= gpsInfo.getLongitude();
//			latitude		= gpsInfo.getLatitude();
//			speed			= gpsInfo.getSpeed();
//			bearing			= gpsInfo.getBearing();
//			provider		= gpsInfo.getProvider();
//			gpsInfo.removeLocation();
			location	= gpsInfo.getLocation();
			if (location == null)
			{
				Log.d(TAG, "invalid gps packet");
				longitude	= 0;
				latitude	= 0;
				speed		= 0;
				bearing		= 0;
				provider	= "I";
				return -2;
			}
			else
			{
				longitude	= location.getLongitude();
				latitude	= location.getLatitude();
				speed		= location.getSpeed();
				bearing		= location.getBearing();
				provider	= location.getProvider();
			}

//			saveToFile("makePeriodReportGpsData(strCurDateTime : " + strCurDateTime + ", longitude : " + longitude + ", latitude : " + latitude + ")");
            LocationManager lmMgr = (LocationManager) mCtxService.getSystemService(Context.LOCATION_SERVICE);

			if (provider.equals(JLocationManager.FUSED_PROVIDER)) {
				chrGpsStatus = 'F';
			}
			else if (provider.equals(LocationManager.GPS_PROVIDER))
			{
				chrGpsStatus = 'G';
			}
			else if (lmMgr.isProviderEnabled(LocationManager.GPS_PROVIDER) == true && provider.equals(LocationManager.NETWORK_PROVIDER))
			{
				chrGpsStatus = 'N';
			}
			else if (lmMgr.isProviderEnabled(LocationManager.GPS_PROVIDER) == false && provider.equals(LocationManager.NETWORK_PROVIDER))
			{
				chrGpsStatus = 'C';
			}
			else
			{
				chrGpsStatus = 'I';
			}

			
			//Log.d("KLNET_LBS :::: ", "값 " + chrGpsStatus + " " + longitude + " " + latitude );
			
			// 거리 누적 : 이전좌표와 현재좌표를 이용하여 거리 계산하여 누적 한 다음 패킷 전송후 초기화 한다.
			if (longitude > 0 && latitude > 0)
			{
				if (mLongitude > 0 && mLatitude > 0)
				{
					mLngDistance = +(long) Distance.calcDistance(mLongitude, mLatitude, longitude, latitude);
				}
				else
				{
					mLngDistance = 0;
				}

				// 이전거리와 현재거리를 비교하기 위해, 현재 위치를 백업한다.
				mLongitude = longitude;
				mLatitude = latitude;

				shrSpeed = (short) (speed * 3.6);
				shtDirection = (short) bearing;
			}

			if (longitude <= 0 || latitude <= 0)
			{
				longitude = 000.00000; // Default
				latitude = 000.00000; // Default
				chrGpsStatus = 'I'; // GPS 수신 못함
			}

			// 이벤트 코드가 없다면 Preference 변수에서 찾는다.
			if (strEventCode.equals(""))
			{
				strEventCode = EtransDrivingApp.getInstance().getEventCode();
			}

			//마지막 GPS 정보 저장하기 13.10.25 황용민
			EtransDrivingApp.getInstance().setLastGps(strCurDateTime + chrGpsStatus + latitude + longitude + shrSpeed + shtDirection);
			clsGpsData.setData(strCurDateTime, chrGpsStatus, latitude, longitude, shrSpeed, shtDirection, strEventCode);

			String msg = "GPS provider=" + provider + ", Gps Type="  + chrGpsStatus + ", lat=" + latitude
					+", lon=" +longitude + ", dir=" + shtDirection + ", speed=" + shrSpeed
					+ ", distance=" + mLngDistance + ", evt=" +strEventCode;

			EtransDrivingApp.getInstance().debugMessage(msg);

//			if (BuildConfig.DEBUG) {
//				EtransDrivingApp.getInstance().showToast(msg);
//			}

			Log.i(TAG, "::::: Add Packet :::::, " + msg);

			// -----------------------------------------------------
			// 생성주기시 만들어진 패킷의 GPSData를 보고주기관련 클래스에 추가한다.
			// -----------------------------------------------------
			mClsReportPacket.addGpsData(clsGpsData);
			// -----------------------------------------------------
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return 0;
	}

	public void onStart()
	{
		Log.d(TAG, "onStart, clear all GPS pakcets");
		mLngDistance = 0;
	}

	public void onStop()
	{
		Log.d(TAG, "onStop, clear all GPS pakcets");

		if (mClsLocationMgr != null)
		{
			mClsLocationMgr.removeUpdates();
			mClsLocationMgr = null;
		}

		if (mClsReportPacket != null)
		{
			mClsReportPacket.clear();
			mClsReportPacket = null;
		}
	}
}