package kr.co.klnet.aos.etransdriving.json;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import kr.co.klnet.aos.etransdriving.EtransDrivingApp;
import kr.co.klnet.aos.etransdriving.R;

public class MOB_Json
{
	private static MOB_Json mObject;
	private MOB_JsonListener mListener;
	private MOB_JsonListener mApkListener;
	private JsonAsync mCurPlayJsonAsync;

	private JsonAsync mBeaconTerminalAlarmJsonAsync;
	//비콘목록 조회
	private HashMap<String, String> mBeaconTerminalAlarmHashMap;

	/**
	 * MOB_Json 객체 반환
	 * @return
	 */
	public static MOB_Json getInst()
	{
		if (mObject == null)
		{
			mObject = new MOB_Json();
		}

		return mObject;
	}

	public interface MOB_JsonListener
	{
		/**
		 * 응답 코드를 알리는 콜백 메소드
		 * @param respCode 응답 코드
		 */
		void OnResponse(int respCode);
	}

	public void disconnect()
	{
		mCurPlayJsonAsync.cancel();
	}

	/**
	 * 이트럭뱅크 비콘 결과 수신 Listener
	 * @uml.property  name="mBeaconTerminalAlarm_AsyncListener"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JsonAsync.JsonAsyncListener mBeaconTerminalAlarm_AsyncListener = new JsonAsync.JsonAsyncListener()
	{
		@Override
		public void OnResponse(int respCode)
		{
			if (respCode == JsonAsync.JSONASYNC_200OK)
			{
				mBeaconTerminalAlarmHashMap = mBeaconTerminalAlarmJsonAsync;
//				mLogOutJsonAsync = null;
			}

			mListener.OnResponse(respCode);
		}
	};

	/**
	 * 비콘전송
	 * @param _context
	 * @param
	 * @param _listener
	 */

	public void reqBeaconAlarmJson(Context _context, String beaconNm, String beaconMac, String beaconUUID, String beaconMajor, String beaconMinor, String beaconBattery, MOB_JsonListener _listener)
	{

		mListener = _listener;

		mBeaconTerminalAlarmJsonAsync = new JsonAsync(_context);
		mBeaconTerminalAlarmJsonAsync.addParam("PhoneNumber",	EtransDrivingApp.getInstance().getMobileNo());
		mBeaconTerminalAlarmJsonAsync.addParam("Vehicle_Id",	EtransDrivingApp.getInstance().getVehicleId());
		mBeaconTerminalAlarmJsonAsync.addParam("Vehicle_ID",	EtransDrivingApp.getInstance().getVehicleId());
		mBeaconTerminalAlarmJsonAsync.addParam("Mac_Address",	EtransDrivingApp.getInstance().getMacAddress());
		mBeaconTerminalAlarmJsonAsync.addParam("BEACON_NAME",	beaconNm);
		mBeaconTerminalAlarmJsonAsync.addParam("BEACON_MAC",	beaconMac);
		mBeaconTerminalAlarmJsonAsync.addParam("BEACON_UUID",	beaconUUID);
		mBeaconTerminalAlarmJsonAsync.addParam("BEACON_MAJOR",	beaconMajor);
		mBeaconTerminalAlarmJsonAsync.addParam("BEACON_BATTERY",	beaconBattery);
		mBeaconTerminalAlarmJsonAsync.addParam("BEACON_MINOR",	beaconMinor);


		mBeaconTerminalAlarmJsonAsync.request(_context.getString(R.string.URL_TERMINAL_BEACON_ALARM_INFO), mBeaconTerminalAlarm_AsyncListener);

		mCurPlayJsonAsync = mBeaconTerminalAlarmJsonAsync;
	}

	/**
	 * 기호 변환
	 * @return
	 */
	public static String replaceChar(String str)
	{
		str = str.replace("!SB_L!", "[");
		str = str.replace("!SB_R!", "]");
		str = str.replace("!DQ!", "\"");
		
		return str;
	}
	
	/**
	 * 기호 역변환
	 * @return
	 */
	public static String replaceRevChar(String str)
	{
		str = str.replace("[","!SB_L!");
		str = str.replace("]","!SB_R!");
		str = str.replace("\"","!DQ!");
		
		return str;
	}

	/**
	 * 비콘목록조회 결과 HashMap 반환
	 * @return
	 */
	public HashMap<String, String> getBeaconTerminalAlarmHashMap()
	{
		return mBeaconTerminalAlarmHashMap;
	}
}