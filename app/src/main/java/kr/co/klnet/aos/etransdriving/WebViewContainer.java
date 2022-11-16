package kr.co.klnet.aos.etransdriving;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Browser;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ZoomButtonsController;

import androidx.core.content.FileProvider;

import org.apache.http.util.EncodingUtils;

import java.io.File;
import java.lang.reflect.Field;

import kr.co.klnet.aos.etransdriving.util.CommonUtil;
import kr.co.klnet.aos.etransdriving.util.StringUtil;

public class WebViewContainer extends Activity {
	private WebView mWebView;
	private Context mContext;
	private Activity mActivity;
	//private LoadingDialogs mLoadingDlg;
	private String mLoadingMsg = "페이지를\n불러오는 중입니다.";
	private final Handler handler = new Handler();
	private boolean forKakaoShare_ = false;

	@SuppressLint("JavascriptInterface")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mob_web_view_container);
		mContext = this;
		mActivity = this;

		mWebView = (WebView) findViewById(R.id.mob_webView01);


		mWebView.setWebViewClient(webViewClient);
		mWebView.setWebChromeClient(new WebChromeClient());

		mWebView.addJavascriptInterface(new AndroidBridge(), "EtruckApp");
		mWebView.setVerticalScrollbarOverlay(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		mWebView.getSettings().setSupportMultipleWindows(true);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setAllowFileAccess(true);
		mWebView.getSettings().setDomStorageEnabled(true);

		//mWebView.getSettings().setUserAgent(0);
		mWebView.setInitialScale(1);
		//mWebView.getSettings().setPluginsEnabled(true);

		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setSupportZoom(true);
		setZoomControlGone(mWebView);
		//mWebView.getSettings().setTextZoom(100);
		mWebView.clearCache(true); // 캐시 지우기

//		mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR); // 페이지 크기 자동 조절?
		if (StringUtil.hasValue(EtransDrivingApp.getInstance().webViewFileNm) && EtransDrivingApp.getInstance().webViewURL.indexOf("WiSutak") >= 0) {
			setOnSaveButton();

		}else if(StringUtil.hasValue(EtransDrivingApp.getInstance().webViewFileNm) && EtransDrivingApp.getInstance().webViewURL.indexOf("reportMobileTs") >= 0){
			setOnReportTsButton();

			if(EtransDrivingApp.getInstance().webViewURL.indexOf("reportMobileTsPrint") >= 0){
				findViewById(R.id.mob_webView01_pageLayout).setVisibility(View.VISIBLE);
			}

		}else if(StringUtil.hasValue(EtransDrivingApp.getInstance().webViewFileNm) && EtransDrivingApp.getInstance().webViewURL.indexOf("reportMobilePay") >= 0){
			setOnReportButton();

			if(EtransDrivingApp.getInstance().webViewURL.indexOf("reportMobilePayPrint") >= 0){
				findViewById(R.id.mob_webView01_pageLayout).setVisibility(View.VISIBLE);
			}
		}

		if(getIntent()!=null){
			Uri uri = getIntent().getData();
			if(uri != null)
			{
				AndroidBridge androidBridge = new AndroidBridge();
				androidBridge.startSettingViewAndFinish(uri.getQueryParameter("action"));
			}
		}


		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			CookieSyncManager.createInstance(this);
		}

		setCookieAllow(mWebView);

		if (CommonUtil.hasUsableIntentData(getIntent(), "postParam")) {
			String postData = getIntent().getExtras().getString("postParam"); //POST로 넘길 값들..

			//POST 방식 호출
			mWebView.postUrl(EtransDrivingApp.getInstance().webViewURL, EncodingUtils.getBytes(postData, "BASE64"));
		} else {
			mWebView.loadUrl(EtransDrivingApp.getInstance().webViewURL);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			CookieSyncManager.getInstance().stopSync();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			CookieSyncManager.getInstance().startSync();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWebView.destroy();
		EtransDrivingApp.getInstance().webViewFileNm = "";
	}

	private void setCookieAllow(WebView webView) {

		try {

			String url = "http://" + getString(R.string.URL_CONN_SVR);// + "mi330U/etruckbank/jsp/";
			String cookie = CookieManager.getInstance().getCookie(url);

			CookieManager.getInstance().setAcceptCookie(true);
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
				CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
			}
		} catch (Exception e) {

		}

	}


	public void setOnSaveButton() {
		findViewById(R.id.mob_webView_textView_01).setVisibility(View.VISIBLE);
		Button saveBtn = (Button) findViewById(R.id.mob_webView_btn_01);
		saveBtn.setText("저장하기");
		saveBtn.setVisibility(View.VISIBLE);
		saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mWebView.loadUrl("javascript:fileSave();");
			}
		});

		mWebView.setDownloadListener(new DownloadListener() {

			@Override
			public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype, long contentLength) {
				Bitmap bitmap = decodeToImage(url.substring(url.indexOf(",") + 1));
				File imgFile = CommonUtil.writeBitmapFileOnLocal(mActivity, bitmap, EtransDrivingApp.getInstance().webViewFileNm);

				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setDataAndType(Uri.parse("file://" + imgFile.getAbsolutePath()), "image/*");
				startActivity(i);
//				finish();
			}
		});
	}

	public void setOnReportButton()
	{
		findViewById(R.id.mob_webView_textView_01).setVisibility(View.VISIBLE);
		Button btn1 = (Button) findViewById(R.id.mob_webView_btn_01);
		btn1.setText("세금계산서");
		btn1.setVisibility(View.VISIBLE);
		btn1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Button btn = (Button) v;
				if(btn.getText().equals("세금계산서"))
				{
					String url = EtransDrivingApp.getInstance().webViewURL;
					url = url.replace("reportMobilePayPrint", "reportMobileSupplierBillPrint");
					mWebView.loadUrl(url);
					btn.setText("거래명세서");
					findViewById(R.id.mob_webView01_pageLayout).setVisibility(View.GONE);
				}
				else if(btn.getText().equals("거래명세서"))
				{
					mWebView.loadUrl(EtransDrivingApp.getInstance().webViewURL);
					findViewById(R.id.mob_webView01_pageLayout).setVisibility(View.VISIBLE);
					btn.setText("세금계산서");
				}
			}
		});

		Button btn2 = (Button) findViewById(R.id.mob_webView_btn_02);
		btn2.setText("저장하기");
		btn2.setVisibility(View.VISIBLE);
		btn2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				forKakaoShare_ = false;
				mWebView.loadUrl("javascript:pdfSave();");
			}
		});

		Button btn3 = (Button) findViewById(R.id.mob_webView_btn_03);
		btn3.setText("카카오톡\n공유하기");
		btn3.setVisibility(View.VISIBLE);
		btn3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//mWebView.loadUrl("javascript:imgSave();");
				forKakaoShare_ = true;
				mWebView.loadUrl("javascript:pdfSave();");
			}
		});

		mWebView.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype, long contentLength) {
				if(url.indexOf("data:application/pdf") >= 0)
				{
					Log.i("tag", "url="+url);
					Log.i("tag", "userAgent="+userAgent);
					Log.i("tag", "contentDisposition="+contentDisposition);
					Log.i("tag", "mimetype="+mimetype);
					Log.i("tag", "contentLength="+contentLength);
//					Uri uri = Uri.parse(url);
//					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//					startActivity(intent);

					if(mWebView.getUrl().indexOf("reportMobilePayPrint") >= 0)
					{
						EtransDrivingApp.getInstance().webViewFilePath = "eTransDriving_Spec";
					}
					else if (mWebView.getUrl().indexOf("reportMobileSupplierBillPrint") >= 0)
					{
						EtransDrivingApp.getInstance().webViewFilePath = "eTransDriving_Tax";
					}
					File file = CommonUtil.reportFileWriteOnLocal(mActivity, url.substring(url.indexOf(",") + 1), EtransDrivingApp.getInstance().webViewFileNm, EtransDrivingApp.getInstance().webViewFilePath);
					Toast.makeText(mContext, "저장되었습니다.", Toast.LENGTH_LONG).show();

					if(forKakaoShare_) {
						Uri providerURI;
						if(Build.VERSION.SDK_INT < 24) {
							providerURI = Uri.fromFile(file);
						}
						else {
							providerURI = FileProvider.getUriForFile(getBaseContext(), getPackageName() + ".provider", file);
						}

						Intent shareIntent = new Intent();
						shareIntent.setAction(Intent.ACTION_SEND);
						shareIntent.putExtra("EXTRA_SUBJECT", "명세서");
						shareIntent.putExtra(Intent.EXTRA_STREAM, providerURI);
						shareIntent.setType("application/*");
						shareIntent.setPackage("com.kakao.talk");

						startActivity(Intent.createChooser(shareIntent, "공유"));
					}
				}
				else if(url.indexOf("data:image/jpeg") >= 0 || url.indexOf("data:image/png") >= 0)
				{
					Bitmap bitmap = decodeToImage(url.substring(url.indexOf(",") + 1));
					File imgFile = CommonUtil.writeBitmapFileOnLocal(mActivity, bitmap, EtransDrivingApp.getInstance().webViewFileNm);

					Uri providerURI;
					if(Build.VERSION.SDK_INT < 24) {
						providerURI = Uri.fromFile(imgFile);
					}
					else {
						providerURI = FileProvider.getUriForFile(getBaseContext(), getPackageName() + ".provider", imgFile);
					}

					Intent shareIntent = new Intent();
					shareIntent.setAction(Intent.ACTION_SEND);
					shareIntent.putExtra("EXTRA_SUBJECT", "명세서");
					shareIntent.putExtra(Intent.EXTRA_STREAM, providerURI);
					shareIntent.setType("image/*");
					shareIntent.setPackage("com.kakao.talk");

					startActivity(Intent.createChooser(shareIntent, "공유"));
				}
				mWebView.reload();
				showLoadingDlgs(mLoadingMsg, false, false);
			}
		});
	}

	public void setOnReportTsButton()
	{
		findViewById(R.id.mob_webView_textView_01).setVisibility(View.VISIBLE);


		Button btn2 = (Button) findViewById(R.id.mob_webView_btn_02);
		btn2.setText("저장하기");
		btn2.setVisibility(View.VISIBLE);


		btn2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mWebView.loadUrl("javascript:pdfSave();");
			}
		});

		mWebView.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype, long contentLength) {

				EtransDrivingApp.getInstance().webViewFilePath = "eTruckBank_Ts";
				CommonUtil.reportFileWriteOnLocal(mActivity, url.substring(url.indexOf(",") + 1)
						, EtransDrivingApp.getInstance().webViewFileNm, EtransDrivingApp.getInstance().webViewFilePath);
				Toast.makeText(mContext, "저장되었습니다.", Toast.LENGTH_LONG).show();

				mWebView.reload();
				showLoadingDlgs(mLoadingMsg, false, false);
			}
		});



	}

	public void setDownloadListener(DownloadListener _listener)
	{
		mWebView.setDownloadListener(_listener);
	}

	private void setZoomControlGone(View view) {
		Class<?> classType;
		Field field;
		try {
			classType = WebView.class;
			field = classType.getDeclaredField("mZoomButtonsController");
			field.setAccessible(true);
			ZoomButtonsController mZoomButtonsController = new ZoomButtonsController(view);
			mZoomButtonsController.getZoomControls().setVisibility(View.GONE);
			try {
				field.set(view, mZoomButtonsController);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 서버와 통신 중임을 사용자에게 알림
	 *
	 * @param msg
	 * @param status
	 * @param cancelable
	 */
	private void showLoadingDlgs(String msg, boolean status, boolean cancelable) {
		/*
		if (status) {
			mLoadingDlg = new LoadingDialogs(mContext, cancelable);
			mLoadingDlg.setMessage(msg);
			mLoadingDlg.show();
		} else {
			if (mLoadingDlg != null) {
				mLoadingDlg.dismiss();
				mLoadingDlg = null;
			}
		}
		*/
	}

	private WebViewClient webViewClient = new WebViewClient() {

		@Override
		public void onLoadResource(WebView view, String url) {

		}

		@Override
		public void onPageFinished(WebView view, String url) {
			showLoadingDlgs(mLoadingMsg, false, false);

			if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				CookieSyncManager.getInstance().sync();
			} else {
				CookieManager.getInstance().flush();
			}

		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
			showLoadingDlgs(mLoadingMsg, false, false);
			Toast.makeText(mContext, "페이지를 불러오지 못헀습니다. 잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			showLoadingDlgs(mLoadingMsg, false, false);
			// Starts With String
			if (url.startsWith("sms:")) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
				Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
				startActivity(i);
				return true;
			} else if (url.startsWith("tel:")) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());

				Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
				startActivity(i);
				return true;
			} else if (url.startsWith("mailto:")) {
				Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
				startActivity(i);
				return true;
			} else if (url.startsWith("intent:")) {
				try {
					Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
					Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
					if (existPackage != null) {
						startActivity(intent);
					} else {
						Intent marketIntent = new Intent(Intent.ACTION_VIEW);
						marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
						startActivity(marketIntent);
					}
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// ends With String
			if (url.endsWith(".mp3")) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse(url), "audio/*"); // Audio
				view.getContext().startActivity(intent);
				return true;
			} else if (url.endsWith(".mp4") || url.endsWith(".3gp")) { // Movie
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse(url), "video/*");
				view.getContext().startActivity(intent);
				return true;
			}

			return super.shouldOverrideUrlLoading(view, url);
		}

	};

	public static Bitmap decodeToImage(String imageString) {

		byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
		Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
		return decodedByte;
	}

	private class AndroidBridge {
		public void closeWebView() {
			handler.post(new Runnable() {
				public void run() {
					mActivity.finish();
				}
			});
		}

		public void startSettingView(String _action) {
			final String action = _action;
			handler.post(new Runnable() {
				public void run() {
					if(Build.VERSION.SDK_INT >= 23)
					{
						Intent intent = new Intent();
						//intent.setAction("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS");
						intent.setAction(action);
						mContext.startActivity(intent);
					}
					else
					{
						Toast.makeText(mContext, "해당 기능을 지원하지 않는 핸드폰 버전 입니다.", Toast.LENGTH_LONG).show();
					}
				}
			});
		}

		public void startSettingViewAndFinish(String _action) {
			final String action = _action;
			handler.post(new Runnable() {
				public void run() {
					if(Build.VERSION.SDK_INT >= 23)
					{
						Intent intent = new Intent();
						//intent.setAction("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS");
						intent.setAction(action);
						mContext.startActivity(intent);
					}
					else
					{
						Toast.makeText(mContext, "해당 기능을 지원하지 않는 핸드폰 버전 입니다.", Toast.LENGTH_LONG).show();
					}
					mActivity.finish();
				}
			});
		}

	}

	public void onClickLayoutToPrev(View _v){
		findViewById(R.id.mob_webView_prvBtn).performClick();
	}

	public void onClickLayoutToNext(View _v){
		findViewById(R.id.mob_webView_nxtBtn).performClick();
	}

	public void onClickToPrev(View _v){
		mWebView.loadUrl("javascript:pagingPrv();");
	}

	public void onClickToNext(View _v){
		mWebView.loadUrl("javascript:pagingNxt();");
	}

}
