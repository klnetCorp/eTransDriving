package kr.co.klnet.aos.etransdriving.json;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class JsonConnection
{
	/**
	 * @uml.property  name="mLockObj"
	 */
	private Object mLockObj;
	/**
	 * @uml.property  name="mPostParam"
	 */
	private String mPostParam;
	/**
	 * @uml.property  name="mRespCode"
	 */
	protected int					mRespCode		= 0;
	/**
	 * @uml.property  name="mRespJson"
	 */
	protected String mRespJson		= null;	// Response JSON Message

	/**
	 * @uml.property  name="mConnHttp"
	 */
	protected HttpsURLConnection mConnHttp		= null;
	/**
	 * @uml.property  name="mOutputStream"
	 */
	protected OutputStream mOutputStream;
	/**
	 * @uml.property  name="mStreamWriter"
	 */
	protected OutputStreamWriter mStreamWriter;
	/**
	 * @uml.property  name="mBufferedReader"
	 */
	protected BufferedReader mBufferedReader;

	/**
	 * 소켓 타임 아웃
	 * @uml.property  name="hTTP_TIMEOUT"
	 */
	protected final int				HTTP_TIMEOUT	= 30000;
	// 소켓 처리 상태
	public final static int			STATUS_NONE		= 0;
	public final static int			STATUS_CREATED	= 1;
	public final static int			STATUS_WRITING	= 2;
	public final static int			STATUS_READING	= 3;
	/**
	 * @uml.property  name="mSockStatus"
	 */
	private int						mSockStatus		= STATUS_NONE;

	private Context mContext;

	/**
	 * @uml.property  name="mJsonConnListener"
	 * @uml.associationEnd  
	 */
	private JsonConnListener		mJsonConnListener;

	public JsonConnection()
	{
		mPostParam = "";
		mLockObj = new Object();
	}

	public JsonConnection(Context _context)
	{
		mContext = _context;
		mPostParam = "";
		mLockObj = new Object();
	}

	public interface JsonConnListener
	{
		/**
		 * 응답 코드를 알리는 콜백 메소드
		 * @param *respCode 응답 코드
		 */
		void OnResponse();
	}

	/**
	 * 소켓 상태를 반환한다.
	 * @return int
	 */
	public int getStatus()
	{
		return mSockStatus;
	}

	/**
	 * 서버와의 통신을 위해 필요한 파라메터들을 설정
	 * @param _key
	 * @param _value
	 */
	public void addParam(String _key, String _value)
	{
		try
		{
			mPostParam += URLEncoder.encode(_key, "euc-kr") + "=" + URLEncoder.encode(_value, "euc-kr") + "&";
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public int getRespCode() throws IOException
	{
		return mRespCode;
	}

	public String getRespJson() throws IOException
	{
		return mRespJson;
	}

	public void jsonConnection(final String addr) throws MalformedURLException, IOException, IllegalStateException, IllegalAccessError, IllegalArgumentException, Exception
	{
		URL connUrl = new URL(addr);
		mConnHttp = (HttpsURLConnection) connUrl.openConnection();
		mConnHttp.setUseCaches(false);		
 		mConnHttp.setDoInput(true);					// 서버로부터 메세지를 받을 수 있도록 한다. 기본값은 true이다.
 		mConnHttp.setDoOutput(true);				// 서버로 데이터를 전송할 수 있도록 한다. GET방식이면 사용될 일이 없으나, true로 설정하면 자동으로 POST로 설정된다. 기본값은 false이다.
 		mConnHttp.setConnectTimeout(HTTP_TIMEOUT);	// 서버 연결 시간 설정: 30초
 		mConnHttp.setReadTimeout(HTTP_TIMEOUT);
 		mConnHttp.setRequestMethod("POST");			// 전달 방식을 설정한다. POST or GET, 기본값은 GET 이다.
	}

	/**
	 * 연결된 HTTP 연결을 끊는다.
	 */
	public void jsonCloseConnection()
	{
		synchronized (mLockObj)
		{
			if (mConnHttp != null)
			{
				mConnHttp.disconnect();
				mConnHttp = null;
			}	
		}	
	}

	/**
	 * 서버로부터 JSON 형식의 데이터를 수신한다
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws UnsupportedEncodingException
	 */
	public void jsonIn(JsonConnListener _listener) throws IOException, NullPointerException, UnsupportedEncodingException
	{
		mJsonConnListener = _listener;
		mSockStatus = STATUS_READING;

		StringBuilder html = new StringBuilder();
		String charsetName = "EUC-KR";
		mBufferedReader = new BufferedReader(new InputStreamReader(mConnHttp.getInputStream(), charsetName));
		for ( ; ; )
		{
			String line = mBufferedReader.readLine();
			if (line == null)
			{
				break;
			}

			html.append(line + '\n');
		}

		mBufferedReader.close();
		mConnHttp.disconnect();

		mRespJson = html.toString().trim();
		mSockStatus = STATUS_CREATED;
		mJsonConnListener.OnResponse();
	}

	/**
	 * 서버로 JSON 형식의 데이터를 전달한다.
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws UnsupportedEncodingException
	 */
	public void jsonOut() throws IOException, NullPointerException, UnsupportedEncodingException
	{
		if (mConnHttp != null)
		{
			mSockStatus = STATUS_WRITING;

			mStreamWriter = new OutputStreamWriter(mConnHttp.getOutputStream(), "euc-kr");
			if (mStreamWriter != null)
			{
				//Log.d("<<KGW>>", "JsonConnection::jsonOut(mPostParam : " + mPostParam + ")");
				mStreamWriter.write(mPostParam);
				mStreamWriter.flush();
				mStreamWriter.close();
			}

			mSockStatus = STATUS_CREATED;

			synchronized (mLockObj)
			{
				if (mConnHttp != null)
				{
					mRespCode = mConnHttp.getResponseCode();
				}
			}
		}
	}

	/**
	 * StreamWriter를 닫는다.
	 * @throws IOException
	 */
	public void jsonWriteClose() throws IOException
	{
		if (mStreamWriter != null)
		{
			mStreamWriter.close();
			mStreamWriter = null;
		}
	}

	/**
	 * BufferedReader를 닫는다.
	 * @throws IOException
	 */
	public void jsonReadClose() throws IOException
	{
		if (mBufferedReader != null)
		{
			mBufferedReader.close();
			mBufferedReader = null;
		}
	}

	/**
	 * 통신 중인 소켓을 닫는다.
	 * @throws IOException
	 */
	public void jsonCancel() throws IOException
	{
		if (mStreamWriter != null)
		{
			mStreamWriter.close();
			mStreamWriter = null;
		}

		if (mBufferedReader != null)
		{
			mBufferedReader.close();
			mBufferedReader = null;
		}

		if (mConnHttp != null)
		{
			new Thread(){
				@Override
				public void run() {
					mConnHttp.disconnect();
					mConnHttp = null;
				}
			}.start();
		}
	}
}