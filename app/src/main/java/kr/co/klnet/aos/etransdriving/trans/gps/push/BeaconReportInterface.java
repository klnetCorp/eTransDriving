package kr.co.klnet.aos.etransdriving.trans.gps.push;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.minew.beacon.BeaconValueIndex;
import com.minew.beacon.BluetoothState;
import com.minew.beacon.MinewBeacon;
import com.minew.beacon.MinewBeaconManager;
import com.minew.beacon.MinewBeaconManagerListener;

import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.klnet.aos.etransdriving.BuildConfig;
import kr.co.klnet.aos.etransdriving.EtransDrivingApp;
import kr.co.klnet.aos.etransdriving.json.MOB_Json;
import kr.co.klnet.aos.etransdriving.util.CommonUtil;
import kr.co.klnet.aos.etransdriving.util.UserRssi;

public class BeaconReportInterface extends Activity
{
	private final static String TAG = "BeaconReport";
	private static BeaconReportInterface mObject = null;


	private MinewBeaconManager mMinewBeaconManager;
	private boolean isScanning;
	private int		stateBeacon;
	UserRssi comp = new UserRssi();

	private Activity mActivity;
	private Context mContext;

	private int					mBeaconMacCountCnt = 0;
	private int					mBeaconMacErrCountCnt = 0;
	private String mBeaconMacInrage;
	private String mBeaconName;
	private String mBeaconMac;
	private String mBeaconOldMac;
	private String mBeaconUUID;
	private String mBeaconBattery;
	private String mBeaconMajor;
	private String mBeaconMinor;

	private static final int REQUEST_ENABLE_BT = 2;

	//비콘리스트..임시
	List<HashMap<String, String>> beaconTgtlist = new ArrayList<HashMap<String, String>>();

	/**
	 * ReportInterface 인스턴스 반환
	 * @return
	 */
	public static BeaconReportInterface inst() {
		if (mObject == null) {
			mObject = new BeaconReportInterface();
		}
		return mObject;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(mMinewBeaconManager != null) {
			try {
				mMinewBeaconManager.stopScan();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQUEST_ENABLE_BT:
				break;
		}
	}

	/**
	 *  초기화
	 * @param context
	 */
	public void init(Context context) {

	}

	/**
	 * 서비스를 시작/중지한다.
	 *
	 * @param context
	 *            Context
	 * @param bStart
	 *            서비스 시작/중지 여부
	 */
	public void doBeaconService(Context context, boolean bStart) {
		mContext = context;

		if(mMinewBeaconManager == null) {
			mMinewBeaconManager = MinewBeaconManager.getInstance(mContext);
		}

        //비콘리스트..2018.11.06 12대
		String[] strBeaconList = {"C2:00:73:00:00:91","C2:00:73:00:00:92","C2:00:73:00:00:76"
				                 ,"C2:00:73:00:01:07","C2:00:73:00:00:74","C2:00:73:00:00:73"
				                 ,"C2:00:73:00:00:96","C2:00:73:00:01:05","C2:00:73:00:01:08"
				                 ,"C2:00:73:00:00:95","C2:00:73:00:01:04","C2:00:73:00:01:06"
							 	 ,"C2:00:73:00:00:9B","C2:00:73:00:01:FF","C2:00:73:00:01:00"
								 ,"C2:00:73:00:00:97","C2:00:73:00:01:9C","C2:00:73:00:01:98"};

		try {

			//기초마스터 조회 2018.11.13
			//무조건하나 있어야함..
			List<String[]> appTempBeaConList = EtransDrivingApp.getInstance().getData(mActivity, EtransDrivingApp.BEACON_LIST);
			List<String> appBeaConList = new ArrayList<String>();
			for(int i = 0; i < appTempBeaConList.size(); i++) {
				appBeaConList.add(appTempBeaConList.get(i)[0]);
			}

			if(appBeaConList.size() > 0) {
				strBeaconList = appBeaConList.toArray(new String[appBeaConList.size()]);
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}

		beaconTgtlist.clear();
		for (int i=0;i < strBeaconList.length; i++) {
			HashMap<String, String> beaconTgtMap = new HashMap<String, String>();
			beaconTgtMap.put("TGT_MAC", strBeaconList[i]);
			beaconTgtlist.add(beaconTgtMap);
		}


		/*
		// test beacon, klnet_16646,
		// MiniBeacon_16646, Major=30001, Minor=16646
		// mac=C2:00:73:00:00:92, name=klnet16646
		{
			HashMap<String, String> testBeacon = new HashMap<String, String>();
			testBeacon.put("TGT_MAC", "C2:00:73:00:00:92");
			beaconTgtlist.add(testBeacon);
		}
		 */
		if (bStart == true /* && EtransDrivingApp.getInstance().getBeaconStartYN().equals("Y") */) {
			if(mMinewBeaconManager == null) {
				mMinewBeaconManager = MinewBeaconManager.getInstance(mContext);
			}

			try {
				if (mMinewBeaconManager != null) {
					BluetoothState bluetoothState = mMinewBeaconManager.checkBluetoothState();

					switch (bluetoothState) {
						case BluetoothStateNotSupported:
							break;
						case BluetoothStatePowerOff:
							break;

						case BluetoothStatePowerOn:
							mMinewBeaconManager.startScan();
							break;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			mMinewBeaconManager.setDeviceManagerDelegateListener(new MinewBeaconManagerListener() {

				@Override
				public void onAppearBeacons(List<MinewBeacon> minewBeacons) {
					Log.d(TAG, "========onAppearBeacons size())======1=====: " + minewBeacons.size());
				}

				/**
				 *  if a beacon didn't update data in 10 seconds, we think this beacon is out of rang, the manager will call back this method.
				 *
				 *  @param minewBeacons beacons out of range
				 */
				@Override
				public void onDisappearBeacons(List<MinewBeacon> minewBeacons) {
					Log.d(TAG, "========onDisappearBeacons size())=====22======: " + minewBeacons.size());
				}

				/**
				 *  the manager calls back this method every 1 seconds, you can get all scanned beacons.
				 *
				 *  @param minewBeacons all scanned beacons
				 */
				@Override
				public void onRangeBeacons(final List<MinewBeacon> minewBeacons) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//정렬
							Collections.sort(minewBeacons, comp);
							mBeaconMacInrage = "N";
							mBeaconMac = "";
							if(minewBeacons.size() == 0) {
								mBeaconMacCountCnt = 0;
								mBeaconOldMac = "ERR";
							}
							String deviceMac = "";
							String deviceName = "";
							String deviceUUID = "";
							String deviceBattery = "";
							String deviceMajor = "";
							String deviceMinor = "";
							for (int z = 0 ; z < beaconTgtlist.size(); z++) {
								Map beaConParamMap = new HashMap();
								beaConParamMap.putAll((Map)beaconTgtlist.get(z));
//								Log.w(TAG, "========minewBeacons.size()======1=====: " + minewBeacons.size());
//								Log.w(TAG, "========onAppearBeacons======1=====: " + beaConParamMap.get("TGT_MAC"));
								for (MinewBeacon minewBeacon : minewBeacons) {

									 deviceMac = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_MAC).getStringValue();
									 deviceName = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue();
									 //deviceUUID = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_UUID).getStringValue();

									 //deviceBattery = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_BatteryLevel).getStringValue();
									 //int batt = Integer.parseInt(deviceBattery);

									  //deviceMajor = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Major).getStringValue();
									 // deviceMinor = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Minor).getStringValue();
									 //String deviceRssi = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).getStringValue();
									 //boolean deviceInrage = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_InRage).isBool();

//									Log.d(TAG, "========onAppearBeacons======, mac=" + deviceMac + ", name=" + deviceName);

									if(beaConParamMap.get("TGT_MAC").equals(deviceMac) ) {
										//존재여부
										mBeaconMacInrage = "Y";
										//값
										mBeaconName = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue();
										mBeaconMac = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_MAC).getStringValue();
										mBeaconUUID = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_UUID).getStringValue();
										mBeaconBattery = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_BatteryLevel).getStringValue();
										mBeaconMajor = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Major).getStringValue();
										mBeaconMinor = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Minor).getStringValue();

										Log.d(TAG, "Beacon Found, mac=" + mBeaconMac + ", name=" + mBeaconName + ", major=" + mBeaconMajor + ", minor=" + mBeaconMinor + ", battery=" + mBeaconBattery);
										if(mBeaconMacCountCnt % 30 == 0 || mBeaconMacCountCnt < 5) {
											//Toast.makeText(mContext, mBeaconName + " " + mBeaconMinor + " Cnt: " + mBeaconMacCountCnt, Toast.LENGTH_SHORT).show();
											if(BuildConfig.DEBUG) {
												EtransDrivingApp.getInstance().showToast( " 터미널 비콘 발견 [" + mBeaconName + ", major=" + mBeaconMajor + ", " + mBeaconMinor + "]");
											}
										}
										mBeaconMacErrCountCnt = 0;
										break;
									}
								}
								//반경에 값이 있으면..
								if(mBeaconMacInrage.equals("Y")) {
									break;
								}
							}

							if(mBeaconMacInrage.equals("N")) {
								mBeaconMacErrCountCnt = mBeaconMacErrCountCnt+1;
								//if(mBeaconMacErrCountCnt % 30 == 0 || mBeaconMacErrCountCnt == 1 || mBeaconMacErrCountCnt == 3) {
//								if(mBeaconMacErrCountCnt < 5) {
//									if(BuildConfig.DEBUG) {
//										EtransDrivingApp.getInstance().showToast("out of range beacon, error count= " + mBeaconMacErrCountCnt);
//									}
//								}
							}

							//반경에 있으면..
							if(mBeaconMacInrage.equals("Y")) {
								mBeaconMacCountCnt = mBeaconMacCountCnt + 1;
							}

							//이전값 저장..
							if(mBeaconMac.length() > 0) {
								mBeaconOldMac = mBeaconMac;
							}

							if(mBeaconMac.length() > 0 && !mBeaconOldMac.equals(mBeaconMac)) {
								//이전값과 비교시....
								mBeaconMacCountCnt = 0;
							}
							//서버전송
							if(mBeaconMacCountCnt == 1 && mBeaconMacInrage.equals("Y")) {
								MOB_Json.getInst().reqBeaconAlarmJson(mContext,mBeaconName, mBeaconMac, mBeaconUUID, mBeaconMajor, mBeaconMinor, mBeaconBattery, mBeaconTerminalAlarmListener);
								if(BuildConfig.DEBUG) {
									EtransDrivingApp.getInstance().showToast(mBeaconName +"[Success] 비콘정보 전송완료: " + mBeaconMacCountCnt);
								}
								//Toast.makeText(mContext, mBeaconName +" beacon sever send Ok: " + mBeaconMacCountCnt, Toast.LENGTH_SHORT).show();

							}
							if (minewBeacons != null) {
								minewBeacons.clear();
							}
						}
					});
				}

				/**
				 *  the manager calls back this method when BluetoothStateChanged.
				 *
				 *  @param state BluetoothState
				 */
				@Override
				public void onUpdateState(BluetoothState state) {
					switch (state) {
						case BluetoothStatePowerOn:
							//Toast.makeText(getApplicationContext(), "BluetoothStatePowerOn", Toast.LENGTH_SHORT).show();
							break;
						case BluetoothStatePowerOff:
							//Toast.makeText(getApplicationContext(), "BluetoothStatePowerOff", Toast.LENGTH_SHORT).show();
							break;
					}
				}
			});
		} else {
			if(mMinewBeaconManager == null) {
				mMinewBeaconManager = MinewBeaconManager.getInstance(context);
			}

			if(mMinewBeaconManager != null) {
				try {
					mMinewBeaconManager.stopScan();
				} catch (Exception e) {
					//전송카운터 초기화
					mBeaconMacCountCnt = 0;
					e.printStackTrace();
				}
			}

			//전송카운터 초기화
			mBeaconMacCountCnt = 0;
		}
	}

	/**
	 * @uml.property  name="mBeaconTerminalAlarmListener"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private MOB_Json.MOB_JsonListener mBeaconTerminalAlarmListener = new MOB_Json.MOB_JsonListener()
	{
		@Override
		public void OnResponse(int respCode)
		{
		}
	};

	private boolean checkBluetoothNot() {
		try {
			if(mMinewBeaconManager == null) {
				mMinewBeaconManager = MinewBeaconManager.getInstance(this);
			}

			BluetoothState bluetoothState = mMinewBeaconManager.checkBluetoothState();

			switch (bluetoothState) {
				case BluetoothStateNotSupported:
					return false;
				case BluetoothStatePowerOff:
					break;
				case BluetoothStatePowerOn:
					break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
}