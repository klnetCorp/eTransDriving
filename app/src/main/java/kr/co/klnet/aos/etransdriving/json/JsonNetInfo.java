package kr.co.klnet.aos.etransdriving.json;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class JsonNetInfo
{
	/**
	 * 네트워크 상태
	 */
	public enum JSON_NET
	{
		/**
		 * @uml.property  name="dISABLE"
		 * @uml.associationEnd  
		 */
		DISABLE,		// 활성화되어 있는 네트워크가 없음.
		/**
		 * @uml.property  name="dISCONNECTED"
		 * @uml.associationEnd  
		 */
		DISCONNECTED,	// 연결되어 있지 않음.
		/**
		 * @uml.property  name="cONNECTED"
		 * @uml.associationEnd  
		 */
		CONNECTED,		// 연결됨.
	}

	/**
	 * 네트워크 정보
	 */
	public static class JSON_NET_INFO
	{
		/**
		 * @uml.property  name="status"
		 * @uml.associationEnd  
		 */
		public JSON_NET	status;		// DISABLE, DISCONNECTED, CONNECTED
		public int		netType;	// MOBILE, WIFI, ..., WIMAX(진저브레드 이상)
		public String extraInfo;	// Extra Info
	}

	/**
	 * 네트워크 상황을 가져온다.
	 * @param context
	 * @return NET_INFO
	 */
	public static JSON_NET_INFO getNetworkInfo(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();

		JSON_NET_INFO netInfo = new JSON_NET_INFO();

		if (info == null)
		{
			netInfo.status	= JSON_NET.DISABLE;
			netInfo.netType	= -1;
		}
		else
		{
			String extra = info.getExtraInfo();

			// 네트워크가 연결되어 있는가?
			netInfo.status = (info.isConnected()) ? JSON_NET.CONNECTED : JSON_NET.DISCONNECTED;
			// 네트워크 타입
			netInfo.netType = info.getType();

			// APN이 변경되었는가?
			netInfo.extraInfo = (extra != null) ? extra : "";
		}

		return netInfo;
	}
    
	/**
	 * 현재 연결된 모바일 네트워크의 IP를 반환한다.
	 * @note 모바일 IP인지를 정확하게 판단할 수 없다.<br>
	 * 리눅스 Property 파일을 분석해 보면 알 수 있으나 적용하지 않는다.
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getMobileIP()
	{
		NetworkInterface iface = null;
		String ip = "";

		try
		{
			for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); )
			{
				iface = (NetworkInterface)ifaces.nextElement();
				InetAddress ia = null;
				boolean isExit = false;

				for (Enumeration ips = iface.getInetAddresses(); ips.hasMoreElements(); )
				{
					ia = (InetAddress)ips.nextElement();
					ip = ia.getHostAddress();

					if (ip != null)
					{
						/********************************
						 * 1. Loopback IP가 아니어야 한다.
						 * 2. Invalid IP는 아니된다.
						 ********************************/
						if (!ip.equals("127.0.0.1") && !ip.equals("0.0.0.0"))
						{
							isExit = true;
							break;
						}
					}
					else
					{
						ip = "";
					}
				}

				if (isExit == true)
				{
					break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return ip;
	}

	/**
	 * 현재 네트워크에서 해당 IP가 존재하는지를 확인한다.
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isExistIP(String compareip)
	{
		if (compareip == null || "".equals(compareip))
		{
			return true;
		}

		NetworkInterface iface = null;

		try
		{
			for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); )
			{
				iface = (NetworkInterface)ifaces.nextElement();
				InetAddress ia = null;
				String ip;

				for (Enumeration ips = iface.getInetAddresses(); ips.hasMoreElements(); )
				{
					ia = (InetAddress)ips.nextElement();
					ip = ia.getHostAddress();

					if (ip != null && ip.equals(compareip))
					{
						return true;
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 현재 연결된 WiFi SSID 값을 반환한다.
	 * @param context
	 * @return
	 */
	public static String getWiFiSSID(Context context)
	{
		String ssid = "";
		try{
			WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wm.getConnectionInfo();
//			String ssid;

//			if (wifiInfo == null)
//			{
//				ssid = "";
//			}
//			else if ((ssid = wifiInfo.getSSID()) == null)
//			{
//				ssid = "";
//			}
			if (wifiInfo == null)
			{
				return ssid;
			}

			ssid = wifiInfo.getSSID();
			if (ssid == null || ssid.length() == 0 || ssid.equals(""))
			{
				return ssid;
			}
			return ssid;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return ssid;
	}

	/**
	 * 현재 연결된 WiFi IP값을 반환한다.
	 * @param context
	 * @return
	 */
	public static String getWiFiIP(Context context)
	{
		WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wm.getConnectionInfo();
		String ip;

		if (wifiInfo == null)
		{
			ip = "";
		}
		else
		{
			int i = wifiInfo.getIpAddress();	// 빅 엔디언 바이트 오더링
			ip = (i & 0xff) + "." + (i >> 8 & 0xff) + "." + (i >> 16 & 0xff) + "." + (i >> 24 & 0xff);
		}

		return ip;
	}
}