package kr.co.klnet.aos.etransdriving.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.TextTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.helper.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import kr.co.klnet.aos.etransdriving.EtransDrivingApp;

/**
 * Copyright 2011 Kakao Corp. All rights reserved.
 * 
 * @author kakaolink@kakao.com
 * @version 1.0
 * 
 */
public class KakaoLink {

	private static KakaoLink kakaoLink = null;

	private static String KakaoLinkApiVersion = "2.0";
	private static String KakaoLinkURLBaseString = "kakaolink://sendurl";

	private static Charset KakaoLinkCharset = Charset.forName("UTF-8");
	private static String KakaoLinkEncoding = KakaoLinkCharset.name();

	private Context context;
	private String params;

	private KakaoLink(Context context) {
		super();
		this.context = context;
		this.params = getBaseKakaoLinkUrl();
	}

	/**
	 * Return the default singleton instance
	 * 
	 * @param context
	 * @return KakaoLink instance.
	 */
	public static KakaoLink getLink(Context context) {
		if (kakaoLink != null)
			return kakaoLink;

		return new KakaoLink(context);
	}

	/**
	 * Opens kakaoLink for parameter.
	 * 
	 * @param activity
	 * @param url
	 */
	private void openKakaoLink(Activity activity, String url, String msg) {
		//Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse(params));
		//activity.startActivity(intent);

		Log.v("qwer", "qwer URL :: " + url);
		LinkObject link = LinkObject.newBuilder()
				.setWebUrl(url)
				.setMobileWebUrl(url)
				.build();

		TextTemplate params = TextTemplate.newBuilder(msg, link)
				.setButtonTitle("설치하기").build()
				;

		KakaoLinkService.getInstance()
				.sendDefault(activity, params, new ResponseCallback<KakaoLinkResponse>() {
					@Override
					public void onFailure(ErrorResult errorResult) {
						Log.e("KAKAO_API", "카카오링크 공유 실패: " + errorResult);
						EtransDrivingApp.getInstance().showToast("카카오링크 공유 실패: " + errorResult);
					}
					@Override
					public void onSuccess(KakaoLinkResponse result) {
						Log.i("KAKAO_API", "카카오링크 공유 성공");
						// 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
						Log.i("KAKAO_API", "warning messages: " + result.getWarningMsg());
						Log.i("KAKAO_API", "argument messages: " + result.getArgumentMsg());
					}
				});
		/*
		TextTemplate params = TextTemplate.newBuilder(msg,
				LinkObject.newBuilder().setWebUrl(url)
						.setMobileWebUrl(url).build())
				.setButtonTitle("설치하기").build()
				;
//			com.kakao.KakaoLink kakaoLinkApi = com.kakao.KakaoLink.getKakaoLink(context);
//			KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder = kakaoLinkApi.createKakaoTalkLinkMessageBuilder();
//			kakaoTalkLinkMessageBuilder.addText(msg);
//			kakaoTalkLinkMessageBuilder.addWebButton("이트럭뱅크", url);
//			kakaoLinkApi.sendMessage(kakaoTalkLinkMessageBuilder.build(), context);


		KakaoLinkService.getInstance().sendDefault(activity, params, new ResponseCallback<KakaoLinkResponse>() {
			@Override
			public void onFailure(ErrorResult errorResult) {
				Logger.e(errorResult.toString());
			}

			@Override
			public void onSuccess(KakaoLinkResponse result) {
				Logger.i(result.toString());

			}
		});
		*/
	}

	/**
	 * Opens kakaoLink URL for parameters.
	 * 
	 * @param activity
	 * @param url
	 * @param message
	 * @param appId
	 *            your application ID
	 * @param appVer
	 *            your application version
	 * @param appName
	 *            your application name
	 * @param encoding
	 *            recommend UTF-8
	 */


	public void openKakaoLink(Activity activity, String url, String message, String appId, String appVer, String appName, String encoding) {

		if (isEmptyString(url) || isEmptyString(message) || isEmptyString(appId) || isEmptyString(appVer) || isEmptyString(appName) || isEmptyString(encoding))
			throw new IllegalArgumentException();

		try {
			if (KakaoLinkCharset.equals(Charset.forName(encoding)))
				message = new String(message.getBytes(encoding), KakaoLinkEncoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		this.params = getBaseKakaoLinkUrl();

		appendParam("url", url);
		appendParam("msg", message);
		appendParam("apiver", KakaoLinkApiVersion);
		appendParam("appid", appId);
		appendParam("appver", appVer);
		appendParam("appname", appName);
		appendParam("type", "link");

		openKakaoLink(activity, url, message);
	}


	/**
	 * Opens kakaoAppLink with parameters.
	 * 
	 * @param activity
	 * @param url
	 * @param message
	 * @param appId
	 *            your application ID
	 * @param appVer
	 *            your application version
	 * @param appName
	 *            your application name
	 * @param encoding
	 *            recommend UTF-8
	 * @param encoding
	 */
	public void openKakaoAppLink(Activity activity, String url, String message, String appId, String appVer, String appName, String encoding) {

		if (isEmptyString(url) || isEmptyString(message) || isEmptyString(appId) || isEmptyString(appVer) || isEmptyString(appName) || isEmptyString(encoding)) {
			throw new IllegalArgumentException();
		}

		try {
			if (KakaoLinkCharset.equals(Charset.forName(encoding)))
				message = new String(message.getBytes(encoding), KakaoLinkEncoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		this.params = getBaseKakaoLinkUrl();
		
		ArrayList<Map<String, String>> metaInfoArray = new ArrayList<Map<String, String>>();
		// If application is support Android platform.		
		Map<String, String> metaInfoAndroid = new Hashtable<String, String>(1);
		metaInfoAndroid.put("os", "android");		
		metaInfoAndroid.put("devicetype", "phone");		
		metaInfoAndroid.put("installurl", "market://details?id=kr.co.klnet.aos.etransdriving");
//		metaInfoAndroid.put("installurl", "market://details?id=com.klnet.mob");
		metaInfoAndroid.put("executeurl", "etruckbank://starActivity");
		
		appendParam("url", url);
		appendParam("msg", message);
		appendParam("apiver", KakaoLinkApiVersion);
		appendParam("appid", appId);
		appendParam("appver", appVer);
		appendParam("appname", appName);
		appendParam("type", "app");
		appendMetaInfo(metaInfoArray);

		openKakaoLink(activity, url, message);
	}

	/**
	 * @return Whether the application can open kakaolink URLs.
	 */
	public boolean isAvailableIntent() {
		Uri kakaoLinkTestUri = Uri.parse(KakaoLinkURLBaseString);
		Intent intent = new Intent(Intent.ACTION_SEND, kakaoLinkTestUri);
		List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if (list == null)
			return false;
		return list.size() > 0;
	}

	private boolean isEmptyString(String str) {
		return (str == null || str.trim().length() == 0);
	}

	private void appendParam(final String name, final String value) {
		try {
			String encodedValue = URLEncoder.encode(value, KakaoLinkEncoding);
			params = params + name + "=" + encodedValue + "&";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private void appendMetaInfo(ArrayList<Map<String, String>> metaInfoArray) {
		params += "metainfo=";

		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();

		try {
			for (Map<String, String> metaInfo : metaInfoArray) {
				JSONObject metaObj = new JSONObject();
				for (String key : metaInfo.keySet()) {
					metaObj.put(key, metaInfo.get(key));
				}
				arr.put(metaObj);
			}
			obj.put("metainfo", arr);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			String encodedValue = URLEncoder.encode(obj.toString(), KakaoLinkEncoding);
			params += encodedValue;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private String getBaseKakaoLinkUrl() {
		return KakaoLinkURLBaseString + "?";
	}
}
