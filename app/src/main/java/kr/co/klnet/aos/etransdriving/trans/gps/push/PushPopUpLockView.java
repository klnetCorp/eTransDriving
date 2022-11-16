package kr.co.klnet.aos.etransdriving.trans.gps.push;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * Push 메시지 팝업 Display 시 하드웨어 컨트롤
 */
public class PushPopUpLockView {
	/**
	 * @uml.property  name="mContext"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private Context mContext;

	private Activity mActivity;

	/**
	 * 잠금 화면
	 * @uml.property  name="mLockLayout"
	 * @uml.associationEnd  
	 */
	private LinearLayout mLockLayout = null;

	/**
	 * 잠금 화면을 컨트롤 하기 위한 윈도우 매니저.
	 * @uml.property  name="mWindowMgr"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private WindowManager mWindowMgr = null;

	/**
	 * 잠금 화면 상태에서 key 입력을 막기 위해 사용한다.
	 * @uml.property  name="mKeyLock"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private KeyguardManager.KeyguardLock mKeyLock = null;

	/**
	 * 하드웨어 Key Guard 설정 및 WindowManager 설정
	 * @param context
	 */
	public PushPopUpLockView(Context context, Activity activity) {
		mContext = context;
		mActivity = activity;
		// Key Guard
		KeyguardManager keyManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
		mKeyLock = keyManager.newKeyguardLock(mContext.getPackageName() + ".KEYLOCK");

		mWindowMgr = activity.getWindowManager();
		
//		14.05.14 KitKat에서 컴포넌트 명 오류로 주석처리 [황]
//		@SuppressWarnings("rawtypes")
//		Class clsWindow;
//
//		try {
//			clsWindow = Class.forName("android.view.WindowManagerImpl");
//			Method m = clsWindow.getMethod("getDefault", (Class[]) null);
//			mWindowMgr = (WindowManager) m.invoke(null, (Object[]) null);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * 팝업 고정에 필요한 데이터 로드
	 * @param resource
	 * @return
	 */
	public View findLockViewById(int resource) {
		return mLockLayout.findViewById(resource);
	}

	/**
	 * XML 로드 및 레이아웃 인자 설정
	 * @param resource
	 */
	public void lockView(int resource) {
		mKeyLock.disableKeyguard();
		if (mLockLayout != null) {
			mWindowMgr.removeView(mLockLayout);
		}

		mLockLayout = (LinearLayout) View.inflate(mContext, resource, null);
		final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		params.height = LayoutParams.FILL_PARENT;
		params.width = LayoutParams.FILL_PARENT;

		params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;

		//params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		params.format = PixelFormat.TRANSLUCENT;

		//권한이 없다면 에러가 발생하므로 실행하지 않음 18.04.05 황
		//2018.12.03 park 오레오 추가
		//Android O 다른 앱 및 시스템 창 위에 모달창 표시 제한

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
			params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(mContext)) {
			params.type = WindowManager.LayoutParams.TYPE_TOAST;
		} else {
			params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		}


		//params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED;

		mWindowMgr.addView(mLockLayout, params);
	}

	/**
	 * 팝업 고정 해제
	 */
	public void unLockView() {
		mKeyLock.reenableKeyguard();
		if (mLockLayout != null) {
			mWindowMgr.removeView(mLockLayout);
			mLockLayout = null;
		}
	}
}