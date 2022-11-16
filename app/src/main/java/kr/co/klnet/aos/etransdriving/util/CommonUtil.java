package kr.co.klnet.aos.etransdriving.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.api.client.util.Base64;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import com.skt.Tmap.TMapTapi;
import com.kakao.kakaonavi.KakaoNaviParams;
import com.kakao.kakaonavi.KakaoNaviService;
import com.kakao.kakaonavi.Location;
import com.kakao.kakaonavi.NaviOptions;
import com.kakao.kakaonavi.options.CoordType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import kr.co.klnet.aos.etransdriving.EtransDrivingApp;
import kr.co.klnet.aos.etransdriving.MainActivity;
import kr.co.klnet.aos.etransdriving.R;
import kr.co.klnet.aos.etransdriving.WebViewContainer;

public class CommonUtil {
    public static final String[] NAVI_LIST = new String[]{"01", "02"}; //01:TMap, 02:Kakao

    private static boolean tmapAuthCompleted = false;
    private static boolean tmapInstalled = true;

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static void moveTMap(final Activity _activity, final String goalNm, final float fX, final float fY, String apiKey){
        final TMapTapi tMapTapi = new TMapTapi(_activity);

        //checkApiKey(apiKey);

         if(tmapAuthCompleted) {
             boolean tmapInstalled = tMapTapi.isTmapApplicationInstalled();
             if (tmapInstalled == false) {
                 Log.i("TMAP", "TMAP is need to installation");
                 ArrayList<String> _ar = tMapTapi.getTMapDownUrl();
                 Log.d("test", "" + _ar);
                 if (_ar != null && _ar.size() > 0) {
                     Log.d("_ar.size() : ", "" + _ar.size());
                     Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(_ar.get(0)));
                     _activity.startActivity(intent);
                 }
             } else {
                 Log.i("TMAP", "TMAP is already installed");
                 boolean result = tMapTapi.invokeRoute(goalNm, fX, fY);
                 Log.i("TMAP", "invoke route, result=" + (result ? "success" : "failed"));
             }

         } else {
             tMapTapi.setOnAuthenticationListener(new TMapTapi.OnAuthenticationListenerCallback() {
                 @Override
                 public void SKTMapApikeySucceed() {
                     tmapAuthCompleted = true;

                     boolean tmapInstalled = tMapTapi.isTmapApplicationInstalled();
                     if (tmapInstalled == false) {
                         Log.i("TMAP", "TMAP is need to installation");
                         ArrayList<String> _ar = tMapTapi.getTMapDownUrl();
                         Log.d("test", "" + _ar);
                         if (_ar != null && _ar.size() > 0) {
                             Log.d("_ar.size() : ", "" + _ar.size());
                             Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(_ar.get(0)));
                             _activity.startActivity(intent);
                         }
                     } else {
                         Log.i("TMAP", "TMAP is already installed");
                         boolean result = tMapTapi.invokeRoute(goalNm, fX, fY);
                         Log.i("TMAP", "invoke route, result=" + (result ? "success" : "failed"));
                     }
                 }

                 @Override
                 public void SKTMapApikeyFailed(String errorMsg) {
                     tmapAuthCompleted = false;
                     Log.e("sl", errorMsg);
                 }
             });
             tMapTapi.setSKTMapAuthentication(apiKey);
         }

    }

    public static void moveKaKaoNavi(Activity _activity, String goalNm, float fX, float fY){
        CoordType coordType = CoordType.WGS84; //CoordType.KATEC;

        Location destination = Location.newBuilder(goalNm, fX, fY).build();
        KakaoNaviParams.Builder builder = KakaoNaviParams.newBuilder(destination)
                .setNaviOptions(NaviOptions.newBuilder().setCoordType(coordType).build());

        KakaoNaviService.shareDestination(_activity, builder.build());
    }

    public static Notification createNotification(Context context, PendingIntent pendingIntent, String title, String text, int iconId, String ticker) {
        Notification notification;
        if (isNotificationBuilderSupported()) {
            notification = buildNotificationWithBuilder(context, pendingIntent, title, text, iconId, ticker);
        } else {
            notification = buildNotificationPreHoneycomb(context, pendingIntent, title, text, iconId, ticker);
        }
        return notification;
    }


    public static boolean isNotificationBuilderSupported() {
        try {
            return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) && Class.forName("android.app.Notification.Builder") != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    @SuppressWarnings("deprecation")
    private static Notification buildNotificationPreHoneycomb(Context context, PendingIntent pendingIntent, String title, String text, int iconId, String ticker) {
        Notification notification = new Notification(iconId, ticker, System.currentTimeMillis());
        try {
            // try to call "setLatestEventInfo" if available
            Method m = notification.getClass().getMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
            m.invoke(notification, context, title, text, pendingIntent);
        } catch (Exception e) {
            // do nothing
        }
        return notification;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressWarnings("deprecation")
    private static Notification buildNotificationWithBuilder(Context context, PendingIntent pendingIntent, String title, String text, int iconId, String ticker) {

        android.app.Notification.Builder builder = new android.app.Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setTicker(ticker)
                .setSmallIcon(iconId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return builder.build();
        } else {
            return builder.getNotification();
        }
    }

    public static void showAlertDialogYes(Context context, String title, String msg, AlertDialog.OnClickListener clickListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("확인", clickListener)
                .create()
                .show();
    }

    /**
     * 미디어 스캐닝
     * @param activity
     *  @param path
     * @return
     */
    public static void mediaScanning(Activity activity, String path)
    {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        activity.sendBroadcast(mediaScanIntent);
    }

    public static File writeBitmapFileOnLocal(Activity _activity, Bitmap _bitmap, String fileNm)
    {
        FileOutputStream out = null;
        File f = null;
        try {
            File path = new File(Environment.getExternalStorageDirectory().toString()+"/capture/");
            if (!path.isDirectory()) {
                path.mkdirs();
            }
            //out = new FileOutputStream("/sdcard/capture/" + _filename);
            //서명 저장 및 서버로의 전송 오류 개선 (황용민) 12.10.19
            f = new File(path +"/"+ fileNm + ".png");
            if(f.exists()) {
                f.delete();
            }

            out = new FileOutputStream(f);

            _bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return f;
        } catch (FileNotFoundException e) {
//			Log.d("FileNotFoundException:", e.getMessage());
            return f;
        } finally {
            try {
                out.close();
                mediaScanning(_activity, "file://"+ Environment.getExternalStorageDirectory());
            } catch(Exception e) {

            }
        }
    }

    public static File reportFileWriteOnLocal(Activity _activity, String _url, String fileNm, String filePath)
    {
        FileOutputStream out = null;
        File f = null;
        try {
            //File path = new File(Environment.getExternalStorageDirectory().toString()+"/"+filePath);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS+"/"+filePath);
            //File path = _activity.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS+"/"+filePath);

            if (!path.isDirectory()) {
                path.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

            //out = new FileOutputStream("/sdcard/capture/" + _filename);
            //서명 저장 및 서버로의 전송 오류 개선 (황용민) 12.10.19
            fileNm = URLDecoder.decode(fileNm, "utf-8");

            f = new File(path +"/"+ fileNm + "_"+timeStamp+".pdf");
            if(f.exists()) {
                f.delete();
            }
/*
			byte[] decodedString = Base64.decodeBase64(_url.getBytes());

			out = new FileOutputStream(f);

			out.write(decodedString);
			out.flush();
			out.close();


			Document document = new Document();
			*/
            //

            String output = path +"/"+ fileNm +"_"+timeStamp + ".pdf";
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(output));
            document.open();
            byte[] decoded = Base64.decodeBase64(_url.getBytes());

            PdfContentByte cb = writer.getDirectContent();

            // Load existing PDF
            PdfReader reader = new PdfReader (decoded);

            for(int i = 0; i < reader.getNumberOfPages(); i++){
                PdfImportedPage page = writer.getImportedPage(reader, i+1);

                // Copy first page of existing PDF into output PDF
                document.newPage();
                cb.addTemplate(page, 0, 0);
            }

            // Add your new data / text here
            // for example...
            //document.add(new Paragraph("my timestamp"));
            document.close();
            return f;
        }
        catch (Exception e) {
            Log.d("FileNotFoundException:", e.getMessage());
            return f;
        }
        finally
        {
            try
            {
                out.close();
                mediaScanning(_activity, "file://"+ Environment.getExternalStorageDirectory());
            } catch(Exception e){

            }

        }
    }

    /**
     * 인텐트 파라미터 존재유무 확인
     * @param intent
     * @param key
     * @return
     */
    public static boolean hasUsableIntentData(Intent intent ,String key)
    {
        try
        {
            if(intent == null)
            {
                return false;
            }
            else
            {
                if(intent.getExtras() == null)
                {
                    return false;
                }
                else
                {
                    int chkCnt = 0;
                    Iterator<String> iterator = intent.getExtras().keySet().iterator();
                    while (iterator.hasNext())
                    {
                        String hasKey = iterator.next();

                        if(hasKey.equals(key))
                        {
                            ++chkCnt;
                        }
                    }

                    if(chkCnt > 0) return true;
                    else return false;
                }
            }
        }
        catch(Exception e)
        {
            return false;
        }
    }

    public static String encodeUtf8ToEuckr(String str) {
        String encode = "";
        try {
            byte[] bytes = str.getBytes(Charset.forName("euc-kr"));
            encode = new String(bytes, "euc-kr");
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();;
        }
        return encode;
    }

    public static String makeReportUrl(final HashMap<String, String> _hashMap)
    {
        String url = _hashMap.get("url") ;
        url += _hashMap.get("reportNm") + ".jsp?";
        _hashMap.remove("url");

        Iterator<String> iterator = _hashMap.keySet().iterator();
        while (iterator.hasNext())
        {
            String key = (String) iterator.next();
            url += "&" + key + "=" + _hashMap.get(key);
        }

        url = url.replace("?&", "?");
        return url;
    }

    public static void setOnWisutakView(Context context, String viewUrl, String fileNm)
    {
        EtransDrivingApp.getInstance().webViewURL = "http://" + context.getString(R.string.URL_CONN_SVR)
                + viewUrl;
        EtransDrivingApp.getInstance().webViewFileNm = fileNm;
        context.startActivity(new Intent(context, WebViewContainer.class));
    }

    public static void setOnWisutakView(Context context, final HashMap<String, String> _hashMap) {

    }
}
