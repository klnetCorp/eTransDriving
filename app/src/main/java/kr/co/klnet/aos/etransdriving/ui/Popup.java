package kr.co.klnet.aos.etransdriving.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

//import com.klnet.mob.AppData;
import kr.co.klnet.aos.etransdriving.EtransDrivingApp;
import kr.co.klnet.aos.etransdriving.R;
import kr.co.klnet.aos.etransdriving.trans.gps.push.SoundManager;
import kr.co.klnet.aos.etransdriving.trans.gps.push.SoundManager.onSoundMangerListener;
import kr.co.klnet.aos.etransdriving.util.TTS;
//import com.klnet.util.Util;

import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * 확인 알림창
 */
public class Popup extends AlertDialog
{
	/**
	 * @uml.property  name="imageView"
	 * @uml.associationEnd  
	 */
	ImageView imageView;
	/**
	 * @uml.property  name="text"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	TextView[]			text	= new TextView[2];
	static Button[]		button	= new Button[5];
	
	CheckBox mChkBox;

	/**
	 * @uml.property  name="title"
	 */
	String title	= "";
	/**
	 * @uml.property  name="message"
	 */
	String message	= "";

	/**
	 * @uml.property  name="scrollView"
	 * @uml.associationEnd  
	 */
	ScrollView scrollView;
	/**
	 * @uml.property  name="listview"
	 * @uml.associationEnd  
	 */
	ListView listview;
	/**
	 * @uml.property  name="listData"
	 */
	ArrayList<String> listData;
	
	boolean isBackAble = true;
	
	boolean isPushPop = false;

	static RelativeLayout relativeLayout;
	static LinearLayout linearLayout;
	static LinearLayout linearLayout2;
	
	Activity mActivity;

	int playSoundMode = -1; //-1 : 초기화안됨, 0:무음, 1:TTS재생, R.raw.meassage_arrive 등등등 : 음성 성우
	String forcedTTSMsg = null;
	TTS tts;

	SoundManager  mSoundManager;
	private TelephonyManager mTelMan = null;

	public Popup(Activity _activity, String _title, ArrayList<String> _listData)
	{
		super(_activity);
		mActivity = _activity;
		title		= _title;
		listData	= _listData;

		show();
	}

	/**
	 * 생성자
	 */
	public Popup(Activity _activity, String _title, String _message)
	{
		super(_activity);
		mActivity = _activity;
		title	= _title;
		message	= _message;

		show();
	}

	/**
	 * 생성자
	 */
	public Popup(Activity _activity, String _title, String _message, int _playSoundMode, String _forcedTTSMsg)
	{
		super(_activity);
		mActivity = _activity;
		title	= _title;
		message	= _message;
		playSoundMode = _playSoundMode;
		forcedTTSMsg = _forcedTTSMsg;

		show();
	}

	@Override
	public void show() {
		if(!mActivity.isFinishing()){
			super.show();
		}
	}

	public TextView getTextView(int _index)
	{
		return text[_index];
	}

	@Override
	public void onBackPressed()
	{		
		//배차 확정 시 통화 확인결과 이거나 앱업데이트 경우 2.2.7버전부터
		if(isBackAble)
		{
			if(!title.equals("통화 확인 결과"))
			{			
				if (!title.equals("업데이트 알림")){
					super.onBackPressed();
					cancel();	
				}
			}

			if(mActivity.getClass().getName().indexOf("LibPopUpActivity") >= 0)
			{
				mActivity.finish();
			}
		}
		
		if(isPushPop)
		{
			super.onBackPressed();
			cancel();
			mActivity.finish();
		}
	}

	/**
	 * 화면 생성
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.popup);

		listview	= (ListView) findViewById(R.id.listView1);
//		scrollView	= (ScrollView) findViewById(R.id.scrollView1);
		imageView	= (ImageView) findViewById(R.id.imageView1);
		RelativeLayout relativeLayoutTitleBar = (RelativeLayout) findViewById(R.id.relativeLayout3);

//		int size = Util.getSharedData(mActivity, "FontSize", 2);
		int size = 2;

		text[0] = (TextView) findViewById(R.id.textView1);
		if (title.equals(""))
		{
			imageView.setVisibility(View.VISIBLE);
			relativeLayoutTitleBar.setVisibility(View.GONE);
		}
		else
		{
			relativeLayoutTitleBar.setVisibility(View.VISIBLE);
			text[0].setText(title);
		}

		text[1] = (TextView) findViewById(R.id.textView2);
		text[1].setTextSize(EtransDrivingApp.DEFAULT_FONT_SIZE + size * (float)1.5);
		text[1].setText(message);
		if (message.equals(""))
		{
			text[1].setVisibility(View.GONE);
			listview.setAdapter(new Popup_BaseAdapter(getContext(), listData));
		}
		else
		{
			listview.setVisibility(View.GONE);
			
			text[1].setVisibility(View.VISIBLE);
			text[1].setMaxLines(200); 
			text[1].setVerticalScrollBarEnabled(true); 
			text[1].setHorizontalScrollBarEnabled(true); 
			text[1].setMovementMethod(new ScrollingMovementMethod());
			
			
			WindowManager manager = (WindowManager)mActivity.getSystemService(Context.WINDOW_SERVICE);
			int height = manager.getDefaultDisplay().getHeight();
			
			text[1].setMaxHeight(height / 2);
			text[1].setText(message);
		}
		Log.v("qwer", "qwer popupInit");
		relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout2);
		relativeLayout.setBackgroundResource(R.mipmap.pop_bg_05);
		linearLayout = (LinearLayout) findViewById(R.id.linearLayout2);
		linearLayout.setVisibility(View.GONE);
		
		linearLayout2 = (LinearLayout)findViewById(R.id.linearLayout3);
		linearLayout2.setVisibility(View.GONE);

		button[0] = (Button) findViewById(R.id.button1);
		button[1] = (Button) findViewById(R.id.button2);
		button[2] = (Button) findViewById(R.id.button3);
		button[3] = (Button) findViewById(R.id.button4);
		button[4] = (Button) findViewById(R.id.button5);

		mChkBox = (CheckBox)findViewById(R.id.chkBox);
		
		button[0].setTag(this);
		button[1].setTag(this);
		button[2].setTag(this);
		button[3].setTag(this);
		button[4].setTag(this);

		if(playSoundMode > -1) //적어도 초기화는 되었다면
			playSound(playSoundMode, message, forcedTTSMsg);
	}

	@Override
	public void dismiss() {

		if(tts != null){
			tts.stop();
			tts.shutdown();
		}

		super.dismiss();
	}

	@Override
	public void cancel() {

		if(tts != null){
			tts.stop();
			tts.shutdown();
		}

		super.cancel();
	}

	/**
	 * 팝업 창에 버튼을 생성
	 * @param _buttonName
	 * @param _onClickListener
	 */
	public void setButton1(String _buttonName, View.OnClickListener _onClickListener)
	{
		if(relativeLayout==null) return;
		relativeLayout.setBackgroundResource(R.mipmap.pop_bg_04);
		linearLayout.setVisibility(View.VISIBLE);
		button[0].setVisibility(View.VISIBLE);
		button[0].setText(_buttonName);
		button[0].setOnClickListener(_onClickListener);
	}

	/**
	 * 팝업 창에 버튼을 생성
	 * @param _buttonName
	 * @param _onClickListener
	 */
	public void setButton2(String _buttonName, View.OnClickListener _onClickListener)
	{
		relativeLayout.setBackgroundResource(R.mipmap.pop_bg_04);
		linearLayout.setVisibility(View.VISIBLE);
		button[1].setVisibility(View.VISIBLE);
		button[1].setText(_buttonName);
		button[1].setOnClickListener(_onClickListener);
	}
	/**
	 * 팝업 창에 버튼을 생성
	 * @param _buttonName
	 * @param _onClickListener
	 */
	public void setButton3(String _buttonName, View.OnClickListener _onClickListener)
	{
		relativeLayout.setBackgroundResource(R.mipmap.pop_bg_04);
		linearLayout2.setVisibility(View.VISIBLE);
		button[2].setVisibility(View.VISIBLE);
		button[2].setText(_buttonName);
		button[2].setOnClickListener(_onClickListener);
	}
	/**
	 * 팝업 창에 버튼을 생성
	 * @param _buttonName
	 * @param _onClickListener
	 */
	public void setButton4(String _buttonName, View.OnClickListener _onClickListener)
	{
		relativeLayout.setBackgroundResource(R.mipmap.pop_bg_04);
		linearLayout2.setVisibility(View.VISIBLE);
		button[3].setVisibility(View.VISIBLE);
		button[3].setText(_buttonName);
		button[3].setOnClickListener(_onClickListener);
	}
	/**
	 * 팝업 창에 버튼을 생성
	 * @param _buttonName
	 * @param _onClickListener
	 */
	public void setButton5(String _buttonName, View.OnClickListener _onClickListener)
	{
		relativeLayout.setBackgroundResource(R.mipmap.pop_bg_04);
		linearLayout2.setVisibility(View.VISIBLE);
		button[4].setVisibility(View.VISIBLE);
		button[4].setText(_buttonName);
		button[4].setOnClickListener(_onClickListener);
	}

	/**
	 * 팝업 창에 체크박스를 설정
	 * @param _buttonName
	 */
	public void setCheckBox(String _buttonName)
	{
		findViewById(R.id.linearLayout4).setVisibility(View.VISIBLE);
		mChkBox.setText(_buttonName);
	}
	
	public boolean isChecked()
	{
		return mChkBox.isChecked();
	}
	
	/**
	 * 팝업 내용 설정
	 * @param _message
	 */
	public void setMassage(String _message)
	{
		message = _message;
		text[1].setText(message);
	}

	/**
	 * 팝업 크기 설정
	 * @param _w
	 * @param _h
	 */
	public void setScrollView(int _w, int _h)
	{
		scrollView.setLayoutParams(new LinearLayout.LayoutParams(_w, _h));
	}

	/**
	 * 팝업 내용 좌측 정렬
	 */
	public void setTextLeftAlign()
	{
		text[1].setGravity(Gravity.LEFT);
	}
	
	/**
	 * 푸시 메시지에 따른 팝업창 제어
	 */
	public void setBackKeyAble(boolean _able)
	{
		isBackAble = _able;
		setCancelable(_able);
		setCanceledOnTouchOutside(_able);
	}
	
	public void setPushPopMode(boolean _isPushPop) 
	{
		isPushPop = _isPushPop;
	}

	private void playSound(int sound, String popupViewMsg, String forcedTTSMsg)
	{

		Context mContext = getContext();

		if(sound == 0) {
			//묵음 등
		} else {

			if(sound == 1 || sound == 2) {

				if (forcedTTSMsg == null) {
					tts = new TTS(mContext, Locale.KOREAN, popupViewMsg);//TTS실행;
				} else {
					tts = new TTS(mContext, Locale.KOREAN, forcedTTSMsg);//TTS실행;
				}

			} else {
				mSoundManager = new SoundManager();
				mSoundManager.initSounds(mContext);
				mSoundManager.addSound(1, sound);
				mSoundManager.playSound(1);
				mSoundManager.setOnSoundMangeListener(new onSoundMangerListener() {

					@Override
					public void onCompleted(SoundPool soundPool, AudioManager audioManager) {
						soundPool = null;
						audioManager = null;

						mSoundManager = null;
					}
				});
			}
		}

		// 통화중 상태 캐치
		mTelMan = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		PhoneStateListener mListener = new PhoneStateListener() {

			//이 리스너는 playSound와 연관되어 설명하면 playSound가 먼저 호출될지 여기가 먼저 호출될지는 케이스마다 다름.
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (mTelMan.getCallState()) {
					case TelephonyManager.CALL_STATE_IDLE:
						//callState = 0;

						//체크를 한번이라도 줄기기 위해 주석처리
						//if(Util.getSharedData(mContext, "Tts_Read", "").equals("Y")) {
						if(tts != null /*&& tts.getBeforeCalling()*/) {//전화관련 상태였고 그때 완료된게 아니라면

							tts.setOnCalling(false);
							if(!tts.getDoneYN())
								tts.speak(null);
						}
						//}
						break;

					case TelephonyManager.CALL_STATE_RINGING:
					case TelephonyManager.CALL_STATE_OFFHOOK:
						//callState = 1;

						//체크를 한번이라도 줄기기 위해 주석처리
						//if(Util.getSharedData(mContext, "Tts_Read", "").equals("Y")) {
						if(tts != null) {
							tts.setOnCalling(true);
							if (tts.isSpeaking()) {
								tts.stop();
							}
						}
						//}
						break;
				}


				Log.d("TTS", "CALL_STATE:" + mTelMan.getCallState());
			}
		};
		mTelMan.listen(mListener, PhoneStateListener.LISTEN_CALL_STATE);
	}
}

/**
 * 팝업 내용 스크롤을 위한 BaseAdapter
 */
class Popup_BaseAdapter extends BaseAdapter
{
	class ViewHolder
	{
		TextView textView1;
		TextView textView2;
	}

	/**
	 * @uml.property  name="context"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	Context context;
	/**
	 * @uml.property  name="listData"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
	ArrayList<String> listData;

	public Popup_BaseAdapter(Context _context, ArrayList<String> _listData)
	{
		context = _context;
		listData = _listData;
	}

	@Override
	public int getCount()
	{
		return listData.size();
	}

	@Override
	public Object getItem(int position)
	{
		return position;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = new ViewHolder();

		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.popup_listview_item01, null);

			holder.textView1 = (TextView) convertView.findViewById(R.id.textView1);
			holder.textView2 = (TextView) convertView.findViewById(R.id.textView2);

			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		StringTokenizer stringTokenizer = new StringTokenizer(listData.get(position), "|");
		holder.textView1.setText(stringTokenizer.nextToken());
		holder.textView2.setText(stringTokenizer.nextToken());
		return convertView;
	}
}