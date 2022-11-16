package kr.co.klnet.aos.etransdriving.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.util.UUID;

public class DataSet {
	public String isrunning = "false";
	public String islogin = "false";
	public String userid = "";
	public String isbackground = "false";
	public String istext = "false";
	public boolean isrunapppush = false;

	public String recv_id = "";

	public String push_seq;	// 푸시ID
	public String push_title;    		// 알림 메세지
	public String push_body;    		// 알림 메세지
	public String push_type;		// 메세지 종류
	public String push_doc_gubun;		// 메세지 종류("01", "02", "03", "99"
	public String push_param;		// 메세지 파라미터

	public String obj_id;		// 푸시 연관 계시물 ID

	public String currentPhotoPath_ = "";

	public static String push_url = "https://testpush.plism.com";  //개발계 push
//	public static String push_url = "https://push.plism.com"; //운영계 push

	//eTransDriving service
//	public static String connect_url = "https://www.etruckbank.co.kr:8443"; //운영
	public static String connect_url = "https://smartest.klnet.co.kr:8443"; //개발계
	public static String login_path = "/etdriving/login.jsp";
	public static String logout_path = "/etdriving/loginOut.jsp";
	public static String loginOutDo_path = "/loginOut.do";
	public static String main_path = "/main.do";
	public static String push_redirect_url = "/etdriving/pushList.jsp";  //push 수신시 이동하는 url, javascript:pushLink(seq, doc_gubun, type, call_text, call_text_sub, call_param)

	private static DataSet _instance;

//	public static CookieSyncManager syncManager;
//	public static CookieManager cookManager;

	static {
		_instance = new DataSet();
	}

	private DataSet() {
		
	}

	public static DataSet getInstance() {
		return _instance;
	}

	public void setPushInfo(String seq, String type, String doc_gubun
			, String title, String body, String param)  {
		push_seq = seq;
		push_type = type;
		push_doc_gubun = doc_gubun;
		push_title = title;
		push_body = body;
		push_param = param;
	}

	public void clearPushInfo()  {
		push_seq = "";
		push_type = "";
		push_doc_gubun = "";
		push_title = "";
		push_body = "";
		push_param = "";
	}
}
