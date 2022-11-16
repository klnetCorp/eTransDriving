package kr.co.klnet.aos.etransdriving.util;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtil {

	public static String getEncode(String text){
		Key skey = makeAESKey("abcdefghijklmnop");
	
		text = padding16(text);
		byte[] enc = aesEncode(text.getBytes(), skey);
	
		return byte2hex(enc);	
	}
	
	public static String getDecode(String text){
		Key skey = makeAESKey("abcdefghijklmnop");
		byte[] enc = hexToByteArray(text);
		byte[] dec = aesDecode(enc, skey);	
	
		return new String(dec);
	
	}
	
		public static String byte2hex (byte []b) {
			String hs ="";
			String stmp ="";
			for (int n =0 ;n <b.length ;n ++) {
				stmp =(java.lang.Integer.toHexString (b [n ]&0XFF ));
				if (stmp.length ()==1 )hs =hs +"0"+stmp ;
				else hs =hs +stmp ;
				if (n <b.length -1 ) hs = hs;
			}
			return hs;
		}
	
		 public static byte[] hexToByteArray(String hex) {
	        if (hex == null || hex.length() == 0) {
	            return null;
	        }
	
	        byte[] ba = new byte[hex.length() / 2];
	        for (int i = 0; i < ba.length; i++) {
	            ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
	        }
	        return ba;
	    }
	
		public static Key makeAESKey(String sKey) {
			final byte[] key = sKey.getBytes();
			return new SecretKeySpec(key, "AES");
		}
	
		public static byte[] aesEncode(byte[] src, Key skey) {
			try {
				Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
				cipher.init(Cipher.ENCRYPT_MODE, skey);
				return cipher.doFinal(src);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	
		public static byte[] aesDecode(byte[] src, Key skey) {
			try {
				Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
				cipher.init(Cipher.DECRYPT_MODE, skey);
				return cipher.doFinal(src);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	
		public static String padding16(String args){
	
			int nCount = 16 - (args.length() % 16);
			for(int i=0; i<nCount; i++){
				args += ' ';
			}
			return args;
	
		}
}
