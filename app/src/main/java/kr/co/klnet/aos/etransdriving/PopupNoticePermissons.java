package kr.co.klnet.aos.etransdriving;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * 확인 알림창
 */
public class PopupNoticePermissons extends AlertDialog
{
	private Button btn1;

	/**
	 * 생성자
	 */
	public PopupNoticePermissons(Activity _activity)
	{
		super(_activity);

		show();
	}

//	@Override
//	public void onBackPressed(){}

	/**
	 * 화면 생성
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notice_permissions_popup);

		btn1 = (Button) findViewById(R.id.mob_btn01);
	}

	public void setButton1(CharSequence text, View.OnClickListener _listener) {
		btn1.setText(text);
		btn1.setOnClickListener(_listener);
	}
}
