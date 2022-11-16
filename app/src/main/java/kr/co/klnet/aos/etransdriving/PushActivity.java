package kr.co.klnet.aos.etransdriving;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.klnet.aos.etransdriving.util.DataSet;


public class PushActivity extends Activity {

    String push_seq = null;
    String push_msg = null;
    String push_obj_id = null;
    String push_sub_obj_id = null;
    String push_recv_id = null;
    String push_type = null;
    String push_isbackground = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataSet.getInstance().isrunapppush = true;

        setContentView(R.layout.activity_push);

        if (getIntent().getExtras() != null) {

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    Object value = bundle.get(key);
                    Log.d("CHECK", String.format("%s %s (%s)", key,
                            value.toString(), value.getClass().getName()));
                }
            }

            Log.d("CHECK", "----- userid : " + getIntent().getExtras().getString("userid"));

            String msg = getIntent().getExtras().getString("msg");
            String body = getIntent().getExtras().getString("body");;
            String alert = getIntent().getExtras().getString("alert");;

            Log.w("CHECK", "::::::::::::::::::::::::::::::::::::");
            Log.w("CHECK", "push msg : " + msg);
            Log.w("CHECK", "::::::::::::::::::::::::::::::::::::");
            JSONObject data = null;
            try {
                data = new JSONObject(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (data != null) {
                String push_seq = "";
                String push_type = "";
                String push_doc_gubun = "";
                String push_param = "";

                String push_title = (alert!=null)?alert:"";
                String push_body = (body!=null)?body:"";

                try { push_seq = data.getString("seq"); }  catch (JSONException e) {};
                try { push_type = data.getString("type"); }  catch (JSONException e) {};
                try { push_doc_gubun = data.getString("doc_gubun"); }  catch (JSONException e) {};
                try { push_param = data.getString("param"); }  catch (JSONException e) {};

                DataSet.getInstance().setPushInfo(push_seq, push_type, push_doc_gubun
                        , push_title, push_body, push_param);

                if ("99".equalsIgnoreCase(push_doc_gubun)) {
                    //주기변경은 알림없이 처리
                    EtransDrivingApp.getInstance().procChangeCollectTerm();
                    return;
                }

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("seq", push_seq);
                intent.putExtra("msg", push_type);
                intent.putExtra("doc_gubun", push_doc_gubun);
                intent.putExtra("param", push_param);
                intent.putExtra("title", push_title);
                intent.putExtra("body", push_body);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                DataSet.getInstance().isbackground = "true";

                this.startActivity(intent);
            }
        }

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i("CHECK", "onResume");
    }
}
