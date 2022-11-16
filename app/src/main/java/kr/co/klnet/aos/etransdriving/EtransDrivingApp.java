package kr.co.klnet.aos.etransdriving;
import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import kr.co.klnet.aos.etransdriving.json.JsonAsync;
import kr.co.klnet.aos.etransdriving.util.DataSet;
import okhttp3.Cookie;
import pl.brightinventions.slf4android.FileLogHandlerConfiguration;
import pl.brightinventions.slf4android.LogRecord;
import pl.brightinventions.slf4android.LoggerConfiguration;
import pl.brightinventions.slf4android.MessageValueSupplier;

import static android.os.Build.VERSION_CODES.M;


public class EtransDrivingApp extends Application {
    private final static String TAG = "EtransDrivingApp";
    private static final Logger LOG = LoggerFactory.getLogger(TAG);

    private static EtransDrivingApp _instance;
    static public List<Cookie> cookies; //main webview anddo
    static public String _userCookie; //user cookie
    public final String basicPrefName = "basicInfo";
    public final String prefPushRegsterID = "fcm_registered_id";
    SharedPreferences _basicPref = null;

    private String fcUserId = "010-0000-0000"; //id for firebase crashytics
    private static List<String[]> BeaconSelect = new ArrayList<String[]>();
    private static JsonAsync mBasicInfoJsonAsync;
    private static int mSelectType;

    public static float DEFAULT_FONT_SIZE			= 16.5f;

    /** 시/도 */
    public final static int SELECT_CITY = 0;
    /** 구/군 */
    public final static int SELECT_DISTRICT = 1;
    /** 화물종류 */
    public final static int SELECT_LUGGAGE_TYPE = 4;
    /** 차종 */
    public final static int CARTYPE = 2;
    /** 톤급 */
    public final static int WEIGHTTYPE = 3;
    /** 차량등록증 첨부하기 */
    public final static int UPLOAD_CAR_REGISTRATION = 4;
    /** 배너 **/
    public final static int AD_BANNER	=	4;
    /** 비콘 **/
    public final static int BEACON_LIST	=	55;

    /** 웹뷰 URL 주소 **/
    public static String webViewURL = "";

    /** 웹뷰 다운로드 파일 경로 **/
    public static String webViewFilePath = "";

    /** 웹뷰 다운로드 파일 명 **/
    public static String webViewFileNm = "";

    interface FcmRequestCallback{
        public void onFail(String msg);
        public void onSuccess(String token);
    }

    public EtransDrivingApp() {

    }

    public static EtransDrivingApp getInstance() {
        if(_instance==null) {
            _instance = new EtransDrivingApp();
        }

        return _instance;
    }

    public static Context context()
    {
        return _instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        _basicPref = getSharedPreferences(basicPrefName, Activity.MODE_PRIVATE);

        //set logging to file.  default: 512KB, 9개 로그 파일 순환
        FileLogHandlerConfiguration fileHandler = LoggerConfiguration.fileLogHandler(this);
        fileHandler.setLogFileSizeLimitInBytes(10*1024*1024);
        fileHandler.setRotateFilesCountLimit(64);

//        fileHandler.setFullFilePathPattern("/sdcard/etdriving/log.%g.%u.log");
        LoggerConfiguration.configuration().addHandlerToRootLogger(fileHandler);

    }
    public void requestFcmToken(Context context, FcmRequestCallback callback) {
        final Context ctx = context;
        final FcmRequestCallback cb = callback;
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG,  "getInstanceId falied, what=" + task.getException());
                            String msg = getString(R.string.alert_msg_request_token) + ":" + task.getException();
                            cb.onFail(msg);
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        cb.onSuccess(token);
                    }
                });

    }

    public void hideKeyboard(Context context, View v) {
        InputMethodManager imm;
        imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        // 검색 동작
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

    }

    public static String getDeviceID(Context context)
    {
        String serial = "";
        String androidId = "";
        try {
            serial = (String) Build.class.getField("SERIAL").get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), serial.hashCode());
        return deviceUuid.toString();
    }


    public String getSdkName() {
//        return "ICE_CREAM_SANDWICH_MR1";
        return Build.VERSION.CODENAME;
    }

    public String getPhoneModel() {
        return Build.MODEL;
    }

    public String getPhoneAppVer() {
        String deviceVersion;
        try {
            deviceVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            deviceVersion = "0.0.0";
        }
        return deviceVersion;
    }

    public String getMacAddress() {
//      return "F8D0BDF96CF6";
        String mac = getMACAddress("wlan0");
        return mac.replace(":", "");
    }

    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac==null) return "";
                StringBuilder buf = new StringBuilder();
                for (int idx=0; idx<mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
                return buf.toString();
            }
        } catch (Exception ex) { } // for now eat exceptions

        return "ABCDEFGHIJKL";
    }

    @RequiresApi(api = M)
    public String getPhoneMsp() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int permissionResult = getApplicationContext().checkSelfPermission(android.Manifest.permission.READ_PHONE_NUMBERS);

            if (permissionResult == PackageManager.PERMISSION_DENIED) {
                return "";
            }
        } else {
            int permissionResult = getApplicationContext().checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE);

            if (permissionResult == PackageManager.PERMISSION_DENIED) {
                return "";
            }
        }

        TelephonyManager tm =(TelephonyManager) getSystemService(Activity.TELEPHONY_SERVICE);
        String operatorName = tm.getNetworkOperatorName();
        return operatorName;
    }

    public String getOsVer() {
//        return "4.0.4";
        return Build.VERSION.RELEASE;
    }

    public String getDownGB() {
        return "market";
    }

    public String getIsAgreeType() {
        String agreeType = readPref("agreeType", "agreeType");

        if(agreeType==null || "".equals(agreeType)) agreeType = "N";

        return agreeType;
    }

    public String getGpsAgreementYn() {
        String gpsAgreeYn = readPref("gpsAgreeYn", "gpsAgreeYn");

        if(gpsAgreeYn==null || "".equals(gpsAgreeYn)) gpsAgreeYn = "N";

        return gpsAgreeYn;
    }

    public void setGpsAgreementYn(String gpsAgreeYn) {
        writePref("gpsAgreeYn", "gpsAgreeYn", gpsAgreeYn);
    }


    /* 1: 사용가능, 0:꺼져있음, -1:권한없음 */
    public int getGpsStatus() {
        if (android.os.Build.VERSION.SDK_INT >= M) {
            int permissionResult = getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

            if (permissionResult == PackageManager.PERMISSION_DENIED) {
                return -1;
            }
        }

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            return 0;
        }
        return 1;
    }
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public String getSystemMobileNo() {
        return getMobileNo();
    }

    public void setUserCookie(String userCookie) {
        _userCookie = userCookie;
    }

    public String getUserCookie() {
        return _userCookie;
    }

    public String getAuthKey() {
        String authkey = readPref("AutoLogin", "vAuthKey");
        return authkey;
    }

    public void setAuthKey(String authkey) {
        writePref("AutoLogin", "vAuthKey", authkey);
    }

    public String getAutoLogin() {
        String autoLogin = readPref("AutoLogin", "isAutoLogin");

        if(autoLogin==null || autoLogin.equals("")) autoLogin = "N";

        return autoLogin;
    }

    public void setAutoLogin(String autoLogin) {
        Log.i(TAG, "autoLogin=" + autoLogin);
        writePref("AutoLogin", "isAutoLogin", autoLogin);
    }

    public void setLoggedIn(String loggedIn) {
        Log.i(TAG, "loggedIn=" + loggedIn);
        writePref("AutoLogin", "loggedIn", loggedIn);
    }

    public String getLoggedIn() {
        String loggedIn = readPref("AutoLogin", "loggedIn");

        if(loggedIn==null || loggedIn.equals("")) loggedIn = "N";

        return loggedIn;
    }

    public String getFstLogin() {
        String fstLogin = readPref("isFirst", "isFirst");

        if(fstLogin==null || fstLogin.equals("")) fstLogin = "N";

        return fstLogin;
    }

    public void setFstLogin(String fstLogin) {
        writePref("isFirst", "isFirst", fstLogin);
    }

    public String readPref(String prefName, String key) {
        SharedPreferences prefs = getSharedPreferences(prefName, Activity.MODE_PRIVATE);
        String value = prefs.getString(key, "");
        return value;
    }

    public void writePref(String prefName, String key, String value) {
        SharedPreferences pref = getSharedPreferences(prefName, Activity.MODE_PRIVATE);
        String strValue = pref.getString(key, "");

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }


    public void setFcUserId(String id) {
        fcUserId = id;
        DataSet.getInstance().userid = id;
    }

    public String getFcUserId() {
        return fcUserId;
    }


    public String getAuthMobileNo() {
        String phoneNo = _basicPref.getString("authPhoneNo", null);
        return phoneNo;
    }

    public void setAuthMobileNo(String phoneNo) {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("authPhoneNo", phoneNo);
        editor.commit();
    }

    @RequiresApi(api = M)
    public String getMobileNo() {
        String mac = getMacAddress();

        String testNo = "01023427113";//lim:01023427113";  //an:01045186291

        if("ACEE9EAEA373".equalsIgnoreCase(mac)) {

            setFcUserId(testNo);
            return testNo;
        }
        //g4
        if("C49A028A820C".equalsIgnoreCase(mac)) {
            setFcUserId(testNo);
            return testNo;
        }

//        //A90 - juis
        if("4A2E0662951A".equalsIgnoreCase(mac)
        || "3A567CC7FD79".equalsIgnoreCase(mac)
        || "4A2E0662951A".equalsIgnoreCase(mac)
        || "ECAA257C1F7D".equalsIgnoreCase(mac)
        || "76131B7419F6".equalsIgnoreCase(mac)
        || "080027394360".equalsIgnoreCase(mac) //nox app player
        ) {
            setFcUserId(testNo);
            return testNo;
        }

        //emulator
        if(isEmulator()) {
            setFcUserId(testNo);
            return testNo;
        }


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int permissionResult = getApplicationContext().checkSelfPermission(android.Manifest.permission.READ_PHONE_NUMBERS);

            if (permissionResult == PackageManager.PERMISSION_DENIED) {
                return "";
            }
        } else {
            int permissionResult = getApplicationContext().checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE);

            if (permissionResult == PackageManager.PERMISSION_DENIED) {
                return "";
            }
        }

        TelephonyManager tMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNo = tMgr.getLine1Number();

        if(phoneNo!=null && !"".equals(phoneNo)) {
            String newPhoneNo = phoneNo.replace("+82", "0");
            setFcUserId(newPhoneNo);
            return newPhoneNo;
        }

//        return "";
        return "01000000000";
    }


    public String getEmail() {
        AccountManager mgr = AccountManager.get(this);
        Account[] accts = mgr.getAccounts();
        final int count = accts.length;

        String email = "";

        for(int i=0;i<count;i++) {
            Account acct  = accts[i];
            Log.d("ACCT", "account - name="+acct.name+", type="+acct.type);
            String item = acct.name;
            if(accts.length>0) {
                if("com.google".equalsIgnoreCase(acct.type)) {
                    email = acct.name;
                    break;
                }
//                else if("com.google".equalsIgnoreCase(acct.type)) {
//
//                }
            }
        }
        return email;
    }

    public void savePushToken(String token) {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString(prefPushRegsterID, token);
        editor.commit();
    }

    public String loadPushToken() {
        String token = _basicPref.getString(prefPushRegsterID, null);
        return token;
    }

    public void removePushToken() {
        Log.d("CHECK", "removePushToken.");

        SharedPreferences.Editor editor = _basicPref.edit();
        editor.remove(prefPushRegsterID);
        editor.commit();
    }

    /**
     * 관제시작여부
     *
     */

    public void setIsLbsStartYn(String strIsLbsStartYn)
    {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("isLbsStartYn", strIsLbsStartYn);
        editor.commit();
    }

    /**
     * 관제시작여부
     *
     */
    public String getIsLbsStartYn() {
        String isLbsStartYn = _basicPref.getString("isLbsStartYn", "");
        return isLbsStartYn;
    }

    public void setVehicleId(String vehicleId) {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("Vehicle_Id", vehicleId);
        editor.commit();
    }

    public String getVehicleId() {
        String vehicleId = _basicPref.getString("Vehicle_Id", "");
        return vehicleId;
    }

    /**
     * 차량ID(번호) 설정
     *
     * @param strVehId
     */
    public void setCarCd(String strVehId)
    {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("carCd", strVehId);
        editor.commit();
    }

    public String getCarCd()
    {
        String carCd = _basicPref.getString("carCd", "");
        return carCd;
    }

    /**
     *  벌크(1)/컨테이너(2) 구분
     *
     * @param carGb
     */
    public void setCarGb(String carGb)
    {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("carGb", carGb);
        editor.commit();
    }

    public String getCarGb()
    {
        String carGb = _basicPref.getString("carGb", "");
        return carGb;
    }

    /**
     * 생성(수집)주기
     *
     * @param strCreationPeroid
     */
    public void setCreationPeroid(String strCreationPeroid)
    {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("creationPeroid", strCreationPeroid);
        editor.commit();
    }

    public String getCreationPeroid() {

        String creationPeroid = _basicPref.getString("creationPeroid", "10");
        if(creationPeroid==null || creationPeroid.length()<=0) creationPeroid = "180";
        return creationPeroid;
//        return "5";
    }

    public int getCreationPeroidInt() {
        int creationPeroid = Integer.parseInt(getCreationPeroid());
        if(creationPeroid<5) creationPeroid = 5;
        return creationPeroid;
//        return 5;
    }



    /**
     * 보고주기
     *
     * @param strReportPeroid
     */
    public void setReportPeroid(String strReportPeroid)
    {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("reportPeroid", strReportPeroid);
        editor.commit();
    }

    public String getReportPeroid() {

        String reportPeroid = _basicPref.getString("reportPeroid", "180");
        if(reportPeroid==null || reportPeroid.length()<=0) reportPeroid = "180";
        return reportPeroid;
//        return "6";
    }

    public int getReportPeroidInt() {
        int reportPeroid = Integer.parseInt(getReportPeroid());
        if(reportPeroid<5) reportPeroid = 5;
        return reportPeroid;
//        return 6;
    }

    /**
     * 도착 예정 주기 설정
     *
     * @param strArrPlanDtmPeroid
     */
    public void setArrPlanDtmPeroid(String strArrPlanDtmPeroid)
    {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("arrPlanDtmPeroid", strArrPlanDtmPeroid);
        editor.commit();
    }

    public String getArrPlanDtmPeroid() {
        String arrPlanDtmPeroid = _basicPref.getString("arrPlanDtmPeroid", "10");
        return arrPlanDtmPeroid;
    }

    /**
     * 운송사ID
     *
     * @param strCarrierId
     */
    public void setCarrierId(String strCarrierId)
    {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("carrierId", strCarrierId);
        editor.commit();
    }

    public String getCarrierId() {
        String carrierId = _basicPref.getString("carrierId", "");
        if("null".equalsIgnoreCase(carrierId)) carrierId = "";
        return carrierId;
    }

    /**
     * 이벤트 코드
     *
     * @param strEventCode
     */
    public void setEventCode(String strEventCode) {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("eventCode", strEventCode);
        editor.commit();
    }

    public String getEventCode() {
        String eventCode = _basicPref.getString("eventCode", "");
        return eventCode;
    }

    /**
     * 마지막 저장된 GPS
     *
     */
    public void setLastGps(String lastGps) {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("LAST_GPS", lastGps);
        editor.commit();
    }

    public String getLastGps() {
        String lastGps = _basicPref.getString("LAST_GPS", "");
        return lastGps;
    }

    /**
     * 샤시번호
     *
     */
    public void setChassisNo(String chassisNo) {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("chassisNo", chassisNo);
        editor.commit();
    }

    public String getChassisNo() {
        String chassisNo = _basicPref.getString("chassisNo", "");
        return chassisNo;
    }

    /**
     * 컨테이너번호1
     *
     */
    public void setContainerNo1(String containerNo1) {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("containerNo1", containerNo1);
        editor.commit();
    }

    public String getContainerNo1() {
        String containerNo1 = _basicPref.getString("containerNo1", "");
        return containerNo1;
    }

    /**
     * 컨테이너번호2
     *
     */
    public void setContainerNo2(String containerNo2) {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("containerNo1", containerNo2);
        editor.commit();
    }

    public String getContainerNo2() {
        String containerNo2 = _basicPref.getString("containerNo2", "");
        return containerNo2;
    }

    /**
     * 배차번호
     *
     */
    public void setDispatchNo(String dispatchNo) {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("dispatchNo", dispatchNo);
        editor.commit();
    }

    public String getDispatchNo() {
        String dispatchNo = _basicPref.getString("dispatchNo", "");
        return dispatchNo;
    }

    /**
     * 오더유형
     *
     */
    public void setOrderType(String orderType) {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("orderType", orderType);
        editor.commit();
    }

    public String getOrderType() {
        String orderType = _basicPref.getString("orderType", "");
        return orderType;
    }

    /**
     * 수출입구분
     *
     */
    public void setImportType(String importType) {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("importType", importType);
        editor.commit();
    }

    public String getImportType() {
        String importType = _basicPref.getString("importType", "");
        return importType;
    }

    /**
     * 수출입구분
     *
     */
    public void setRestFlag(String restFlag) {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("restFlag", restFlag);
        editor.commit();
    }

    public String getRestFlag() {
        String restFlag = _basicPref.getString("restFlag", "N");
        return restFlag;
    }

    /**
     * 설정>네비게이션 종류 지정
     *
     */
    public void setNavigationType(String navigationType) {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("navigationType", navigationType);
        editor.commit();
    }

    public String getNavigationType() {
        String navigationType = _basicPref.getString("navigationType", "01");
        return navigationType;
    }


    /**
     * 비콘실행여부
     *
     * @param beaconStartYN
     */
    public void setBeaconStartYN(String beaconStartYN) {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("beaconStartYN", beaconStartYN);
        editor.commit();
    }

    /**
     * 비콘실행여부
     *
     * @return
     */
    public String getBeaconStartYN() {
        String beaconStartYN = _basicPref.getString("beaconStartYN", "01");
        return beaconStartYN;
    }

    static JsonAsync.JsonAsyncListener basicInfoAsyncListener = new JsonAsync.JsonAsyncListener() {
        @Override
        public void OnResponse(int respCode) {
            if (respCode == JsonAsync.JSONASYNC_200OK) {
                if (mBasicInfoJsonAsync.size() > 0) {
                    for (int position = 0; position < mBasicInfoJsonAsync.size(); position++) {
                        if (mBasicInfoJsonAsync.get("CodeList" + "_" + position + "_" + "codeCd") != null && mBasicInfoJsonAsync.get("CodeList" + "_" + position + "_" + "codeCd") != "null") {
                            EtransDrivingApp.getInstance().setData(mSelectType, mBasicInfoJsonAsync.get("CodeList" + "_" + position + "_" + "codeCd"), mBasicInfoJsonAsync.get("CodeList" + "_" + position + "_" + "codeNm"));
                        }
                    }
                }
            } else if (respCode == JsonAsync.JSONASYNC_NETWORK_ERROR_CONNECT) {
            } else if (respCode == JsonAsync.JSONASYNC_NETWORK_ERROR_WRITE) {
            } else if (respCode == JsonAsync.JSONASYNC_NETWORK_ERROR_READ) {
            } else if (respCode == JsonAsync.JSONASYNC_NETWORK_ERROR_IPADDR) {
            }
        }
    };

    /**
     * 기준 데이터 설정
     * @param activity
     * @param SelectType
     */
    public static void GetBasicData(Activity activity, final int SelectType) {
        mSelectType = SelectType;
        mBasicInfoJsonAsync = new JsonAsync(activity);

        switch (SelectType) {
            case BEACON_LIST:
                mBasicInfoJsonAsync.addParam("codeType", "BAECON");
                mBasicInfoJsonAsync.addParam("carTransType", EtransDrivingApp.getInstance().getCarGb() );
                break;
        }

        mBasicInfoJsonAsync.request(activity.getString(R.string.URL_BASIC_DATA), basicInfoAsyncListener);
    }


    public static void clearData() {
        BeaconSelect.clear();
    }
    /**
     * 서버로부터 받은 기준데이터를 각 데이터 리스트에 추가
     * @param SelectType
     * @param CdValue
     * @param NmValue
     */
    public static void setData(int SelectType, String CdValue, String NmValue) {
        String[] dataValue = { CdValue, NmValue };
        switch (SelectType) {
            case BEACON_LIST:
                BeaconSelect.add(dataValue);
                break;
        }
    }

    /**
     * 기준 데이터 반환
     * @param activity
     * @param SelectType
     * @return
     */
    public static List<String[]> getData(Activity activity, int SelectType) {
        switch (SelectType) {
            case BEACON_LIST:
                if (BeaconSelect.size() == 0) {
                    GetBasicData(activity, BEACON_LIST);
                }
                return BeaconSelect;
        }

        return new ArrayList<String[]>();
    }

    private boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");
    }


    public String getHashKey(){
        PackageInfo packageInfo = null;
        String hashKey = "unknown";
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                hashKey = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d("KeyHash", "hash Key=" + hashKey);
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
        return hashKey;
    }

    public void debugMessage(String debugMsg) {
        Log.i(TAG, "debugMessage, msg=" + debugMsg);

        Intent debugPrintIntent = new Intent(MainActivity.ACTION_DEBUG_MESSAGE);
        debugPrintIntent.putExtra("debugMsg", debugMsg);
        sendBroadcast(debugPrintIntent);
    }

    public void procChangeCollectTerm() {
        String push_param       = DataSet.getInstance().push_param;

        //관제주기 변경알림
        String collectTerm = MainActivity.getQueryString("\\|", push_param, "collectTerm");
        String sendTerm = MainActivity.getQueryString("\\|", push_param, "sendTerm");

        if (collectTerm != null && collectTerm.length() > 0) {
            EtransDrivingApp.getInstance().setCreationPeroid(collectTerm); //수집주기
        }
        if (sendTerm != null && sendTerm.length() > 0) {
            EtransDrivingApp.getInstance().setReportPeroid(sendTerm); //보고주기
        }
    }

    public void setBadgeNumber(int count) {
        SharedPreferences.Editor editor = _basicPref.edit();
        editor.putString("badgeCount", Integer.toString(count));
        editor.commit();
    }

    public int getBadgeNumber() {
        String value = _basicPref.getString("badgeCount", "0");
        int count = Integer.parseInt(value);
        return count;
    }

}
