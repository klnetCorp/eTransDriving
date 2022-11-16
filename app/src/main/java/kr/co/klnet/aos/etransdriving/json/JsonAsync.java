package kr.co.klnet.aos.etransdriving.json;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

public class JsonAsync extends Json implements Callback
{
	private static final long	serialVersionUID				= 1L;

	/**
	 * @uml.property  name="mContext"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private Context mContext;

	/**
	 * @uml.property  name="mListener"
	 * @uml.associationEnd  
	 */
	private JsonAsyncListener	mListener;
	/**
	 * @uml.property  name="mJsonConnection"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JsonConnection		mJsonConnection;
	/**
	 * @uml.property  name="jsonObject"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JSONObject jsonObject						= new JSONObject();

	/**
	 * @uml.property  name="mSocketLock"
	 */
	private Object mSocketLock						= new Object();
	/**
	 * @uml.property  name="mSockThread"
	 * @uml.associationEnd  inverse="this$0:com.klnet.json.JsonAsync$SockThread"
	 */
	private SockThread			mSockThread;
	/**
	 * @uml.property  name="mHandler"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private Handler mHandler						= new Handler(this);

	/**
	 * @uml.property  name="mAddr"
	 */
	private String mAddr;
	/**
	 * @uml.property  name="mResponse"
	 */
	private String mResponse = "";
	/**
	 * @uml.property  name="mRespCode"
	 */
	private int					mRespCode						= 0;	// HttpURLConnection.HTTP_OK

	public static final int		JSONASYNC_200OK					= 200;
	public static final int		JSONASYNC_NETWORK_ERROR_CONNECT	= -1;
	public static final int		JSONASYNC_NETWORK_ERROR_WRITE	= -2;
	public static final int		JSONASYNC_NETWORK_ERROR_READ	= -3;
	public static final int		JSONASYNC_NETWORK_ERROR_ETC		= -4;
	public static final int		JSONASYNC_NETWORK_ERROR_IPADDR	= -1;

	/**
	 * 생성자
	 */
	public JsonAsync(Context _context)
	{
		mContext		= _context;
		mJsonConnection	= new JsonConnection(_context);
	}

	/**
	 * 서버에게 데이터 보낸 후 수신된 응답 코드 반환
	 * @return
	 */
	public int getRespCodes()
	{
		return mRespCode;
	}

	/**
	 * JSON 형식의 데이터를 서버로부터 수신 후<br>
	 * 파싱하기전의 데이터를 로그로 남김
	 */
	//public void Logs()
	//{
		//Log.d("JSON", mResponse);
	//}

	/**
	 * JSon Object에 key, value 값 추가
	 * @param _key
	 * @param _value
	 */
	public void addParam(String _key, String _value)
	{
		try
		{
			jsonObject.put(_key, _value);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * UI에 결과 값을 보낼 핸들 메시지
	 */
	@Override
	public boolean handleMessage(Message msg)
	{
		if (mListener != null)
		{
			mListener.OnResponse(msg.what);
			mListener = null;
			return true;
		}

		return false;
	}

	/**
	 * 서버에 접속
	 * @param _addr
	 * @param _listener
	 */
	public void request(final String _addr, JsonAsyncListener _listener)
	{
		Log.i("REQ", ":::::Req::::: url=" + _addr + ", ");
		mAddr = _addr;
		mListener = _listener;
		if (JsonNetInfo.getNetworkInfo(mContext).status != JsonNetInfo.JSON_NET.CONNECTED)
		{
			mListener.OnResponse(JSONASYNC_NETWORK_ERROR_IPADDR);
			return ;
		}

		mSockThread = new SockThread(mHandler);
		mSockThread.sendMessage();
	}

	/**
	 * 데이터 통신을 강제로 종료
	 */
	public void cancel()
	{
		if (mSockThread != null)
		{
			mSockThread.quit();
			mSockThread = null;
		}
	}

	/**
	 * 데이터 통신 결과를 알려주는 리스너
	 */
	public interface JsonAsyncListener
	{
		/**
		 * 응답 코드를 알리는 콜백 메소드
		 * @param respCode 응답 코드
		 */
		void OnResponse(int respCode);
	}

	/**
	 * 서버와 통신을 위해 핸들러 스레드를 상속받은 소켓 스레드
	 */
	private class SockThread extends HandlerThread
	{
		private volatile Looper mLooper;
		/**
		 * @uml.property  name="mThreadHandler"
		 * @uml.associationEnd  
		 */
		private volatile ThreadHandler	mThreadHandler;
		private Handler mMainHandler;
		private boolean					mQuits = false;

		/**
		 * 소켓 스레드 생성자
		 * @param handler
		 */
		public SockThread(Handler handler)
		{
			super("JsonAsync Socket Thread");
			mMainHandler = handler;

			this.start();
			mLooper = this.getLooper();
			mThreadHandler = new ThreadHandler(mLooper);
		}

		/**
		 * Thread 종료
		 */
		@Override
		public boolean quit()
		{
			mQuits = true;

			mThreadHandler.removeMessages(0);
			stopLoop();

			synchronized (mSocketLock)
			{
				if (mJsonConnection != null)
				{
					try
					{
						mJsonConnection.jsonCancel();
						mJsonConnection = null;
					}
					catch (IOException exceptions)
					{
						exceptions.printStackTrace();
					}
				}
			}

			return true;
		}

		/**
		 * Looper 종료
		 */
		private void stopLoop()
		{
			if (mLooper != null)
			{
				mLooper.quit();
				mLooper = null;
			}
		}

		public void sendMessage()
		{
			mThreadHandler.sendEmptyMessage(0);
		}

		private class ThreadHandler extends Handler
		{
			/**
			 * Constructor
			 * @param looper
			 */
			public ThreadHandler(Looper looper)
			{
				super(looper);
			}

			@Override
			public void handleMessage(Message msg)
			{
				// Network Connect
				try
				{
					// Parameter add
					mJsonConnection.addParam("paramStr", jsonObject.toString());
					// Network Connect
					mJsonConnection.jsonConnection(mAddr);
				}
				catch (Exception exceptions)
				{
					exceptions.printStackTrace();
					onError(JsonAsync.JSONASYNC_NETWORK_ERROR_CONNECT);
					return ;
				}

				// Network Write
				try
				{
					mJsonConnection.jsonOut();
				}
				catch (Exception exceptions)
				{
					exceptions.printStackTrace();
					onError(JsonAsync.JSONASYNC_NETWORK_ERROR_WRITE);
					return ;
				}

				// Network Read
				try
				{
					mRespCode = mJsonConnection.getRespCode();
					if (mRespCode == HttpURLConnection.HTTP_OK)
					{
						// Network Read
						mJsonConnection.jsonIn(mJsonConnListener);
					}
					else
					{
						// Network Close
						mJsonConnection.jsonCloseConnection();
						// Call CallBack Method
						mMainHandler.sendEmptyMessage(mRespCode);
					}
				}
				catch (Exception exceptions)
				{
					exceptions.printStackTrace();
					onError(JsonAsync.JSONASYNC_NETWORK_ERROR_READ);
					return ;
				}
			}
		}

		/**
		 * @uml.property  name="mJsonConnListener"
		 * @uml.associationEnd  
		 */
		private JsonConnection.JsonConnListener mJsonConnListener = new JsonConnection.JsonConnListener()
		{
			@Override
			public void OnResponse()
			{
				try
				{
					mResponse = mJsonConnection.getRespJson();
					// Call Json parser
					paser(mResponse);

					// Network Close
					mJsonConnection.jsonCloseConnection();
					// Call CallBack Method
					mMainHandler.sendEmptyMessage(mRespCode);
				}
				catch(Exception exceptions)
				{
					exceptions.printStackTrace();
					onError(JsonAsync.JSONASYNC_NETWORK_ERROR_READ);
					return ;
				}
			}
		};

		/**
		 * 예외 발생 시 소켓 자원 정리
		 */
		private void onError(int errorCode)
		{
			if (mJsonConnection != null)
			{
				try
				{
					mJsonConnection.jsonCancel();
				}
				catch (IOException exceptions)
				{
					exceptions.printStackTrace();
				}

				synchronized (mSocketLock)
				{
					mJsonConnection = null;
				}
			}

			// 에러를 전달한다.
			if (mQuits == false)
			{
				mMainHandler.sendEmptyMessage(errorCode);
			}
		}
	}
}