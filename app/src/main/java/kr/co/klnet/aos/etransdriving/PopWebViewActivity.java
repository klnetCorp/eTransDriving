package kr.co.klnet.aos.etransdriving;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.URLDecoder;
import java.util.HashMap;

import kr.co.klnet.aos.etransdriving.util.DataSet;

public class PopWebViewActivity extends AppCompatActivity {

    private WebView wv_main;
    static Context ctxFromThis;
    private String loadUrl;
    private String loadTitle;
    private ImageButton btn_close;
    private TextView tv_title;
    CookieManager cookieManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop);
        ctxFromThis = PopWebViewActivity.this;
        wv_main = (WebView) findViewById(R.id.wv_main);
        btn_close = (ImageButton)findViewById(R.id.btn_close);
        tv_title = (TextView)findViewById(R.id.tv_title);
        loadTitle = getIntent().getStringExtra("loadTitle");
        tv_title.setText(loadTitle);
        loadUrl = getIntent().getStringExtra("loadUrl");
        makeWebView();

        btn_close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });
    }


    public void makeWebView() {
        // 웹뷰에서 플러그인 기능을 사용할 수 있도록 세팅
//        wv_main.getSettings().setPluginState(WebSettings.PluginState.ON);

        wv_main.clearCache(true);
        String agent = wv_main.getSettings().getUserAgentString();

        wv_main.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        wv_main.getSettings().setDomStorageEnabled(true);
        wv_main.getSettings().setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            wv_main.getSettings().setDatabasePath(
                    "/data/data/" + wv_main.getContext().getPackageName()
                            + "/databases/");
        }
        wv_main.getSettings().setJavaScriptEnabled(true);
        wv_main.getSettings().setAppCachePath(
                getApplicationContext().getCacheDir().getAbsolutePath());
        wv_main.getSettings().setAllowFileAccess(true);
        wv_main.getSettings().setAppCacheEnabled(false);
        wv_main.getSettings().setSupportZoom(true);
        wv_main.getSettings().setBuiltInZoomControls(true);
        wv_main.getSettings().setDisplayZoomControls(false);
        wv_main.getSettings().setUseWideViewPort(true);

        wv_main.setWebViewClient(new webViewClient());


//        setCookieAllow(cookieManager, wv_main);


        wv_main.loadUrl(loadUrl);

    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i("CHECK", "=== onConfigurationChanged is called !!! ===");

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) { // 세로 전환시 발생
            Log.i("CHECK", "=== Configuration.ORIENTATION_PORTRAIT !!! ===");
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) { // 가로 전환시 발생
            Log.i("CHECK", "=== Configuration.ORIENTATION_LANDSCAPE !!! ===");
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }

    public class webViewClient extends WebViewClient {

        /**
         * @param view WebView
         * @param url  로딩하려는 URL
         * @return 계속 진행하려면 false, 멈추려면 true 리턴
         */
        public boolean preShouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        /**
         * @param view   WebView
         * @param url    로딩하려는 URL
         * @param name   스키마 명령어 이름
         * @param values 커스텀 스키마로 넘어온 값 HashMap
         */
        public void runForCustomScheme(WebView view, String url, String name, HashMap<String, String> values) {

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (preShouldOverrideUrlLoading(view, url)) {
                return true;
            }

            if (url.startsWith("tel:")) {
                Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(tel);
                return true;
            }

            return false;
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            Log.e("CHECK", "onPageFinished:" + url);
            super.onPageFinished(view, url);
            Log.e("CHECK", "onPageFinished:");
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            // TODO Auto-generated method stub
            Log.i("CHECK", "onReceivedError: " + errorCode);
            Log.i("CHECK", "onReceivedError: " + failingUrl);
            Log.i("CHECK", "onReceivedError: " + description);
            super.onReceivedError(view, errorCode, description, failingUrl);

        }

        @Override
        public void onReceivedSslError(WebView view,
                                       SslErrorHandler handler, SslError error) {
            // TODO Auto-generated method stub
            super.onReceivedSslError(view, handler, error);
            Log.i("CHECK", "onReceivedSslError");

            final SslErrorHandler fHandler = handler;
            final AlertDialog.Builder builder = new AlertDialog.Builder(ctxFromThis);
            builder.setMessage("유효하지 않은 인증서를 사용하는 페이지 입니다. 계속 사용하시겠습니까?");
            builder.setCancelable(false);
            builder.setPositiveButton("예",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub

                            fHandler.proceed();
                        }
                    });
            builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    fHandler.cancel();
                }
            });
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            // TODO Auto-generated method stub

            super.onLoadResource(view, url);
            Log.i("CHECK", "onLoadResource: " + url);
        }
    }

//    private void setCookieAllow(CookieManager cookieManager, WebView webView) {
//        try {
//            DataSet.cookManager = CookieManager.getInstance();
//            DataSet.cookManager.setAcceptCookie(true);
//            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//                DataSet.cookManager.setAcceptThirdPartyCookies(webView, true);
//            }
//        } catch (Exception e) {
//
//        }
//    }
}


