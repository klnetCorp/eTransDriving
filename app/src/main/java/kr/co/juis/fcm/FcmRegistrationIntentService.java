package kr.co.juis.fcm;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import kr.co.klnet.aos.etransdriving.R;

/**
 * Created by JUIS on 2019-09-05.
 */
public class FcmRegistrationIntentService extends IntentService {

    private static final String TAG = "FcmRegistrationIntentService";

    public FcmRegistrationIntentService() {
        super(TAG);
    }

    /**
     * FCM을 위한 Instance ID의 토큰을 생성하여 가져온다.
     * @param intent
     */
    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(Intent intent) {

        // FCM Instance ID의 토큰을 가져오는 작업이 시작되면 LocalBoardcast로 GENERATING 액션을 알려 ProgressBar가 동작하도록 한다.
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(QuickstartPreferences.REGISTRATION_GENERATING));

        synchronized (TAG) {
            // Get token
            // [START retrieve_current_token]
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {

                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult().getToken();

                    // Log and toast
                    String msg = getString(R.string.msg_token_fmt, token);
                    Log.d(TAG, msg);


                    // FCM Instance ID에 해당하는 토큰을 획득하면 LocalBoardcast에 COMPLETE 액션을 알린다.
                    // 이때 토큰을 함께 넘겨주어서 UI에 토큰 정보를 활용할 수 있도록 한다.
                    Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
                    registrationComplete.putExtra("token", token);
                    LocalBroadcastManager.getInstance(FcmRegistrationIntentService.this).sendBroadcast(registrationComplete);
                }
            });
            // [END retrieve_current_token]
        }

        // FCM Instance ID에 해당하는 토큰을 획득하면 LocalBoardcast에 COMPLETE 액션을 알린다.
        // 이때 토큰을 함께 넘겨주어서 UI에 토큰 정보를 활용할 수 있도록 한다.
//        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
////        registrationComplete.putExtra("token", token);
//        LocalBroadcastManager.getInstance(RegistrationIntentService.this).sendBroadcast(registrationComplete);
    }
}
