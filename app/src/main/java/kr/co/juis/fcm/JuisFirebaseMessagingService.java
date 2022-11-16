package kr.co.juis.fcm;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import kr.co.klnet.aos.etransdriving.EtransDrivingApp;
import kr.co.klnet.aos.etransdriving.util.DataSet;
import kr.co.klnet.aos.etransdriving.MainActivity;
import kr.co.klnet.aos.etransdriving.R;

public class JuisFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "CHECK";
    private static final Logger LOG = LoggerFactory.getLogger(TAG);

    public static final int REQUEST_PUSH_ARRIVED = 2000;
    public JuisFirebaseMessagingService() {
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "::::::::::::: FCM Message [Received] :::::::::::::");
        Log.d(TAG, ":    From=" + remoteMessage.getFrom());

        String title = "";
        String message = "";
        if( remoteMessage.getNotification() == null   ){
            title = remoteMessage.getData().get("title");
            message = remoteMessage.getData().get("body");
        }
        else{
            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();
        }

        Log.d(TAG, "::::::::::::::::::::::::::::::::::::::::::::::::::");
        //msg {\"param\":\"20200910180157146001\",\"seq\":\"20200910180259280001\",\"type\":\"1\",\"doc_gubun\":\"01\"}
        String msg = remoteMessage.getData().get("msg");
        if(msg!=null) {
            //msg={"param":"data","seq":"2020062917460784001","type":"1","doc_gubun":"01"}
            JSONObject data = null;
            try {
                data = new JSONObject(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String add = remoteMessage.getData().get("add");
            String alert = remoteMessage.getData().get("alert");
            DataSet.getInstance().recv_id = remoteMessage.getData().get("userid");

            Log.d("CHECK", "recv_id : " + DataSet.getInstance().recv_id);

            //앱 실행 아이콘 개수 조절
            setBadge(1);
            sendNotification(title, message, data, add, alert);
        } else {
            Log.e(TAG, "FCM message has no body for EtransDriving");
            JSONObject data = null;
            try {
                data = new JSONObject();

            } catch(Exception e) {

            }

            sendNotification("알림", "이트랜스드라이빙", data, "add", "alert");
        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "::::::::::::: Refreshed FCM token :::::::::::::");
        Log.d(TAG, ":                                                :");
        Log.d(TAG, ": token=" + token);
        Log.d(TAG, ":                                                :");
        Log.d(TAG, "::::::::::::::::::::::::::::::::::::::::::::::::::");
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server siFileOutputStreamde, send the
        // Instance ID token to your app server.
        EtransDrivingApp.getInstance().savePushToken(token);
        sendRegistrationToServer(token);
    }


    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     *
     */
    private void sendNotification(String title, String body, JSONObject data, String add, String alert) {
        String seq = null;
        String type = null;
        String doc_gubun = null;
        String param = null;
        if (data != null) {
            try {
                seq = data.getString("seq");
                type = data.getString("type");
                doc_gubun = data.getString("doc_gubun");
                param = data.getString("param");
            } catch (JSONException e) {
                e.printStackTrace();
                seq = "seq";
                type = "type";
                doc_gubun = "doc_gubun";
                param = "param";
            }
        }
        Log.d("CHECK", "title:" + title);
        Log.d("CHECK", "body:" + body);
        Log.d("CHECK", "seq:" + seq);
        Log.d("CHECK", "type:" + type);
        Log.d("CHECK", "doc_gubun:" + doc_gubun);
        Log.d("CHECK", "param:" + param);


        DataSet.getInstance().setPushInfo(seq, type, doc_gubun
                , title, body, param);

        String push_doc_gubun   = DataSet.getInstance().push_doc_gubun;
        if ("99".equalsIgnoreCase(push_doc_gubun)) {
            //주기변경은 알림없이 처리
            EtransDrivingApp.getInstance().procChangeCollectTerm();
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("seq", seq);
        intent.putExtra("type", type);
        intent.putExtra("title", title);
        intent.putExtra("body", body);
        intent.putExtra("doc_gubun", doc_gubun);
        intent.putExtra("param", param);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), REQUEST_PUSH_ARRIVED /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = null;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.

            String channelName = getString(R.string.default_notification_channel_name);
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder = new NotificationCompat.Builder(this, channel.getId());

        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }

        notificationBuilder.setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setNumber(1)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        notificationManager.notify(REQUEST_PUSH_ARRIVED /* ID of notification */, notificationBuilder.build());

        //DataSet.getInstance().isrunapppush = true;

        Intent intent3 = new Intent(getApplicationContext(), MainActivity.class);
        intent3.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent3.putExtra("seq", seq);
        intent3.putExtra("type", type);
        intent3.putExtra("title", title);
        intent3.putExtra("body", body);
        intent3.putExtra("doc_gubun", doc_gubun);
        intent3.putExtra("param", param);

        intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent3);
    }

    private boolean isApplicationBroughtToBackground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }

        return false;
    }

    private String getLauncherClassName() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        final PackageManager pm = getApplicationContext().getPackageManager();
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo resolveInfo : resolveInfos) {
            final String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }

    public void setBadge(int count) {
        String launcherClassName = getLauncherClassName();
        if (launcherClassName == null) {
            return;
        }

        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);

        sendBroadcast(intent);
    }

}
