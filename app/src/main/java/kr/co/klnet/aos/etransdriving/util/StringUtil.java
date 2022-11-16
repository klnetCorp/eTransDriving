package kr.co.klnet.aos.etransdriving.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class StringUtil {

	public static String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(
					"Unsupported encoding?  UTF-8?  That's unpossible.");
		}
	}

	public static String urlDecode(String value) {
		try {
			return URLDecoder.decode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(
					"Unsupported encoding?  UTF-8?  That's unpossible.");
		}
	}

	/**
	 * String의 앞쪽에 주어진 char를 채워 주어진 길이(len)의 스트링을 반환한다.
	 * 
	 * @param str
	 *            대상 문자열.
	 * @param fc
	 *            채울 문자.
	 * @param len
	 *            반환될 문자열의 길이.
	 * @return 변환된 문자열.
	 */
	public static String lPad(String str, String fc, int len) {
		if (str == null) {
			str = "";
		}
		if (String.valueOf(fc).length() <= 0) {
			throw new IllegalArgumentException("fc must provied");
		}
		if (len <= 0) {
			throw new IllegalArgumentException("length must be positive");
		}

		for (; str.length() < len; str = fc + str)
			;

		return str;
	}

	/**
	 * String의 뒤쪽에 주어진 char를 채워 주어진 길이(len)의 스트링을 반환한다.
	 * 
	 * @param str
	 *            대상 문자열.
	 * @param fc
	 *            채울 문자.
	 * @param len
	 *            반환될 문자열의 길이.
	 * @return 변환된 문자열.
	 */
	public static String rPad(String str, String fc, int len) {
		if (str == null) {
			str = "";
		}
		if (String.valueOf(fc).length() <= 0) {
			throw new IllegalArgumentException("Padding char fc must provied");
		}
		if (len <= 0) {
			throw new IllegalArgumentException(
					"Length to pad len must be positive");
		}

		for (; str.length() < len; str = str + fc)
			;

		return str;
	}

	/**
	 * 문자열에서 숫자만 뽑은 새로운 문자열로 반환한다.
	 * 
	 * @param str
	 *            대상 문자열.
	 * @return 변환된 문자열.
	 */
	public static String getOnlyDigit(String str) {
		if (str == null) {
			return null;
		}

		char[] charArr = str.toCharArray();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < charArr.length; i++) {
			if (Character.isDigit(charArr[i])) {
				sb.append(charArr[i]);
			}
		}

		return sb.toString();
	}

	/**
	 * 문자열에서 문자만 뽑은 새로운 문자열로 반환한다.
	 * 
	 * @param str
	 *            대상 문자열.
	 * @return 변환된 문자열.
	 */
	public static String getOnlyLetter(String str) {
		if (str == null) {
			return null;
		}

		char[] charArr = str.toCharArray();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < charArr.length; i++) {
			if (Character.isLetter(charArr[i])) {
				sb.append(charArr[i]);
			}
		}

		return sb.toString();
	}

	/**
	 * 문자열에서 숫자와 문자만 뽑은 새로운 문자열로 반환한다.
	 * 
	 * @param str
	 *            대상 문자열.
	 * @return 변환된 문자열.
	 */
	public static String getOnlyLetterOrDigit(String str) {
		if (str == null) {
			return null;
		}

		char[] charArr = str.toCharArray();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < charArr.length; i++) {
			if (Character.isLetterOrDigit(charArr[i])) {
				sb.append(charArr[i]);
			}
		}

		return sb.toString();
	}

	/**
	 * map을 참조하여 주어진 문자열의 내용을 치환한다.
	 * <p>
	 * 사용예 :
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 * String a = &quot;This is a String&quot;;
	 * String[][] map = {{&quot;s&quot;,&quot;t&quot;},{&quot;i&quot;,&quot;k&quot;},{&quot;S&quot;,&quot;o&quot;}};
	 * out.print(StringUtil.replace(a, map));
	 * ===&gt; Thkt kt a otrkng
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param str
	 *            대상 문자열.
	 * @param map
	 *            변환할 내용을 담은 2차원 문자열배열.
	 * @return 변환된 문자열.
	 */
	public static String replace(String str, String map[][]) {
		return replace(str, map, true);
	}

	/**
	 * 주어진 문자열의 내용을 치환한다.
	 * 
	 * @param str
	 *            대상 문자열.
	 * @param find
	 *            찾을 문자열.
	 * @param to
	 *            치환할 문자열.
	 * @return 변환된 문자열.
	 */
	public static String replace(String str, String find, String to) {
		return replace(str, new String[][] { new String[] { find, to } });
	}

	/**
	 * 주어진 문자열의 내용을 치환한다. Case에 Insenstive 하다.
	 * 
	 * @param str
	 *            대상 문자열.
	 * @param find
	 *            찾을 문자열.
	 * @param to
	 *            치환할 문자열.
	 * @return 변환된 문자열.
	 */
	public static String replaceIgnoreCase(String str, String find, String to) {
		return replace(str, new String[][] { new String[] { find, to } }, false);
	}

	/**
	 * map을 참조하여 주어진 문자열의 내용을 치환한다. Case에 Insenstive 하다.
	 * 
	 * @param str
	 *            대상 문자열.
	 * @param map
	 *            변환할 내용을 담은 2차원 문자열배열.
	 * @param caseSensitive
	 *            Case에 Sensitive한지 여부. true 면 Case Senstive이다.
	 * @return 변환된 문자열.
	 */
	public static String replace(String str, String[][] map,
                                 boolean caseSensitive) {
		if (str == null) {
			return null;
		}
		if (map.length <= 0) {
			throw new IllegalArgumentException("mapping array cannot be null");
		}

		String original = str;
		if (!caseSensitive) {
			str = str.toUpperCase();
		}

		StringBuffer sb = new StringBuffer();
		int nextCmpPoint = 0;

		do {
			int matchIndex = -1;
			int fastestMatchPoint = str.length();
			String from;
			for (int i = 0; i < map.length; i++) {
				from = map[i][0];
				if (!caseSensitive) {
					from = from.toUpperCase();
				}
				int matchPoint = str.indexOf(from, nextCmpPoint);
				if (matchPoint > -1 && matchPoint <= fastestMatchPoint) {
					fastestMatchPoint = matchPoint;
					matchIndex = i;
				}
			}

			sb.append(original.substring(nextCmpPoint, fastestMatchPoint));
			if (matchIndex < 0) {
				break;
			}
			from = map[matchIndex][0];
			String to = map[matchIndex][1];
			sb.append(to);
			nextCmpPoint = fastestMatchPoint + from.length();
		} while (nextCmpPoint < str.length());

		return sb.toString();
	}

	/**
	 * 문자열을 delimiter로 분리해서 문자열 배열 형태로 반환한다.
	 * 
	 * @param str
	 *            대상 문자열.
	 * @param delimiter
	 *            구분자.
	 * @return 나눠진 문자열의 배열.
	 */
	public static String[] split(String str, String delimiter) {
		if (str == null) {
			return null;
		}
		if (delimiter == null) {
			throw new NullPointerException("delimiter cannot be null");
		}

		StringTokenizer st = new StringTokenizer(str, delimiter);
		String[] strarr = new String[st.countTokens()];

		for (int i = 0; i < strarr.length; i++) {
			strarr[i] = st.nextToken();
		}

		return strarr;
	}

	public static String join(String... strarr) {
		if (strarr == null) {
			return null;
		}

		return join(strarr, "");
	}

	/**
	 * 문자열 배열을 delimiter로 조합하여 하나의 문자열로 반환다.
	 * 
	 * @param strarr
	 *            대상 문자열 배열.
	 * @param delimiter
	 *            구분자.
	 * @return 합쳐진 문자열.
	 */
	public static String join(String[] strarr, String delimiter) {
		if (strarr == null) {
			return null;
		}
		if (delimiter == null) {
			throw new NullPointerException("delimiter cannot be null");
		}

		String str = "";

		for (int i = 0; i < strarr.length; i++) {
			str += strarr[i];
			if (i < (strarr.length - 1)) {
				str += delimiter;
			}
		}

		return str;
	}

	public static String combineParts(Object... messageParts) {
		StringBuilder builder = new StringBuilder(128);
		for (Object part : messageParts) {
			if (part != null && part.getClass().isArray()) {
				builder.append(Arrays.toString(CollectionUtil
						.asObjectArray(part)));
			} else {
				builder.append(part);
			}
		}

		return builder.toString();
	}

	/**
	 * 문자열이 값이 있는지 검사한다.
	 * 
	 * @param str
	 *            대상 문자열.
	 * @return 문자열이 null 이거나 길이가 0인 경우 false를, 나머지 경우는 true를 리턴한다.
	 */
	public static boolean hasValue(String str) {
		try{
			if (str != null && str.length() > 0 & !str.equals("null")) {
				return true;
			} else {
				return false;
			}
		}catch (Exception e){return false;}

	}

	/**
	 * 문자열이 null이면 주어진 기본값으로 치환한다.
	 * 
	 * @param str
	 *            대상 문자열.
	 * @param defaultValue
	 *            null일 경우 치환할 문자열.
	 * @return 문자열이 null 이거나 길이가 0인 경우 false를, 나머지 경우는 true를 리턴한다..
	 */
	public static String fixNull(String str, String defaultValue) {
		return str == null ? defaultValue : str;
	}

	/**
	 * 문자열이 null 또는 공백 이면 주어진 기본값으로 치환한다.
	 * 
	 * @param str
	 *            대상 문자열.
	 * @param defaultValue
	 *            null일 경우 치환할 문자열.
	 * @return 문자열이 null 이거나 길이가 0인 경우 false를, 나머지 경우는 true를 리턴한다..
	 */
	public static String fixEmpty(String str, String defaultValue) {
		return isEmpty(str) ? defaultValue : str;
	}

	/**
	 * 문자열이 null이면 ""(빈문자열)로 치환한다.
	 * 
	 * @param str
	 *            대상 문자열.
	 * @return 문자열이 null 이거나 길이가 0인 경우 false를, 나머지 경우는 true를 리턴한다.
	 */
	public static String fixNull(String str) {
		return fixNull(str, "");
	}

	/**
	 * 문자열의 길이를 반환한다. 한글인 경우 2 byte로 계산한다.
	 * 
	 * @param str
	 *            대상 문자열.
	 * @return 문자열의 길이.
	 */
	public static int getLength(String str) {
		if (str == null) {
			return 0;
		}

		int strlen = 0;
		char c;

		for (int i = 0; i < str.length(); i++) {
			c = str.charAt(i);

			if (c < 0xac00 || 0xd7a3 < c) {
				strlen++;
			} else {
				strlen += 2;
			}
		}

		return strlen;
	}

	/**
	 * byte[]을 Hex String으로 변환
	 * 
	 * @param b
	 *            대상 문자열.
	 * @return 변환된 문자열.
	 */
	public static String toHex(byte b[]) {
		if (b == null) {
			return null;
		}

		String rslt = "";

		for (int i = 0; i < b.length; i++) {
			String es = "" + Integer.toHexString(b[i] & 0x000000ff);
			if (es.length() < 2)
				es = "0" + es;
			rslt += es;
		}

		return rslt;
	}

	/**
	 * 파라미터가 null 또는 공백인가를 점검한다.
	 * 
	 * @param val
	 * @return
	 */
	public static boolean isEmpty(final String val) {
		if (val == null || "".equals(val)) {
			return true;
		}
		return false;
	}

	/**
	 * 입력받은 문자열의 왼쪽 공백을 제거하고 리턴한다.
	 * 
	 * @param val
	 * @return
	 */
	public static String lTrim(final String val) {
		if (val == null) {
			return val;
		}
		int pos = 0;
		for (int i = 0; i < val.length(); i++) {
			if (val.charAt(i) == ' ') {
				pos++;
			} else {
				break;
			}
		}
		return val.substring(pos);
	}

	/**
	 * 입력받은 문자열의 오른쪽 공백을 제거하고 리턴한다.
	 * 
	 * @param val
	 * @return
	 */
	public static String rTrim(final String val) {
		if (val == null) {
			return val;
		}
		int pos = val.length();
		for (int i = val.length() - 1; i > 0; i--) {
			if (val.charAt(i) == ' ') {
				pos--;
			} else {
				break;
			}
		}
		return val.substring(0, pos);
	}

	/**
	 * 입력받은 문자열의 좌/우측 공백을 제거하고 리턴한다.
	 * 
	 * @param val
	 * @return
	 */
	public static String trim(final String val) {
		String tmp = null;
		tmp = lTrim(val);
		return rTrim(tmp);
	}

	/**
	 * 입력받은 숫자형문자를 통화형 포맷으로 변경하여 문자열로 리턴한다.
	 * 
	 * @param strNumber
	 * @return
	 */
	public static String toCurrency(final String strNumber) {
		String rtnVal = "";
		try {
			NumberFormat nf = NumberFormat.getCurrencyInstance();
			BigDecimal bd = new BigDecimal(strNumber);
			rtnVal = nf.format(bd);
		} catch (Exception e) {
		}
		return rtnVal;
	}

	/**
	 * 입력받은 숫자형문자를 숫자형 포맷으로 변경하여 문자열로 리턴한다.
	 * 
	 * @param strNumber
	 * @return
	 */
	public static String formatNumber(final String strNumber) {
		String rtnVal = "";
		try {
			NumberFormat nf = NumberFormat.getNumberInstance();
			BigDecimal bd = new BigDecimal(strNumber);
			rtnVal = nf.format(bd);
		} catch (Exception e) {
		}
		return rtnVal;
	}

	/**
	 * Http Header를 이용한 폰 정보를 추출하는 class. 작성자 : 박 현 우
	 */
	/**
	 * SKT - 폰 번호를 추출하여 반환하는 메소드.
	 * 
	 * @param user_agent
	 * @param client_id
	 * @return phn_no
	 */
	public String get_phnNum_skt(String user_agent, String client_id) {
		String phn_no = "0000000000";
		if (client_id == null) {
			String dev_prefix = user_agent.substring(0, 3);
			String dev_prefix_num = "000";

			if (dev_prefix.equals("010")) {
				dev_prefix_num = "010";
			} else if (dev_prefix.equals("SKT")) {
				dev_prefix_num = "011";
			} else if (dev_prefix.equals("STI")) {
				dev_prefix_num = "017";
			} else if (dev_prefix.equals("KTF")) {
				dev_prefix_num = "016";
			} else if (dev_prefix.equals("HSP")) {
				dev_prefix_num = "018";
			} else if (dev_prefix.equals("LGT")) {
				dev_prefix_num = "019";
			} else {
				if (dev_prefix.substring(0, 1).equals("I")) {
					dev_prefix_num = dev_prefix;
				}
			}

			// 번호 추출
			Integer num_suffix_loc = user_agent.indexOf(";");
			String num_suffix = user_agent.substring(25, num_suffix_loc);

			if (num_suffix.substring(0, 1).equals("0")) {
				phn_no = dev_prefix_num + num_suffix.substring(1); // 앞에 0빼고
																	// 조합(10자리
																	// 번호)
			} else {
				phn_no = dev_prefix_num + num_suffix;
			}

		} else {
			Integer loc1 = client_id.indexOf("+");
			phn_no = client_id.substring(0, loc1);
		}
		return phn_no;
	}

	/**
	 * KTF - 폰 정보를 추출하여 반환하는 메소드
	 * 
	 * @param phone_number
	 * @return phn_no
	 */
	public String get_phnNum_ktf(String phone_number) {

		/*
		 * 폰번호 추출(HTTP_PHONE_NUMBER 를 이용한 폰번호 추출 / KUN에만 있음) HTTP_PHONE_NUMBER:
		 * 82016100XXXX(3자리 국번) HTTP_PHONE_NUMBER: 820161000XXXX(4자리 국번)
		 */
		String phn_no = "00000000000";
		if (phone_number != null) {
			if (phone_number.length() < 13) { // 10자리 국번
				phn_no = phone_number.substring(2, 5) + "0"
						+ phone_number.substring(5);
				// out.println("phn_no1="+phn_no);
			} else { // 11자리 국번
				phn_no = phone_number.substring(2);
				// out.println("phn_no2="+phn_no);
			}
		}
		return phn_no;
	}

	/**
	 * LGT - 폰 정보를 추출하여 반환하는 메소드 .
	 * 
	 * @param wap_userInfo
	 * @return phn_no
	 */
	public String get_phnNum_lgt(String wap_userInfo) {
		String phn_no = "00000000000";
		if (wap_userInfo != null) {
			String[] temp1 = new String[2]; // Min=01195326026;
											// SubID=EB10-20040209-0083884;
			String[] temp2 = new String[2]; // Min=01195326026;

			temp1 = wap_userInfo.split(";");
			if (temp1.length > 0) {
				temp2 = temp1[0].split("=");
				if (temp2.length > 0) {
					phn_no = temp2[1];
				}
			}
		}
		return phn_no;
	}

	/**
	 * 문자열에서 공백을 제거한 새로운 문자열로 반환한다.
	 * 
	 * @param str
	 *            대상 문자열.
	 * @return 변환된 문자열.
	 */
	public static String getSpaceRemove(String str) {
		if (str == null) {
			return null;
		}

		char[] charArr = str.toCharArray();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < charArr.length; i++) {
			if (!Character.isWhitespace(charArr[i])) {
				sb.append(charArr[i]);
			}
		}

		return sb.toString();
	}

	/**
	 * HsahMap null처리 한반에....
	 * 
	 * @param map
	 *            Map.
	 * @return 변환된 map.
	 */
	public static HashMap nullToBlankInHash(HashMap map) {

		Set set = map.entrySet();
		Iterator it = set.iterator();

		while (it.hasNext()) {
			Map.Entry e = (Map.Entry) it.next();
			map.put(e.getKey(),
					e.getValue() == null ? "" : (String) e.getValue());
		}

		return map;

	}

	/**
	 * 문자열 처리가 가능하도록 문자열을 변경한다.
	 * 
	 * @param input
	 *            검사할 문자열
	 * @return 문자열이 null 인 경우는 빈 문자열(""), 그렇지 않으면 좌우 공백이 제거된 문자열
	 */
	public static String verify(String input) {
		if (input == null) {
			return "";
		}
		return input.trim();
	}

	/***************************************************************************************
	 * 설명 : 오늘날짜
	 **************************************************************************************/

	public static String getTodayDateString() {
		Date today = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		return "" + dateFormat.format(today);
	}

	/***************************************************************************************
	 * 설명 : 컨테이너번호검사한다.
	 **************************************************************************************/
	public static boolean checkContainerNo(String cntrNo) {

		if (cntrNo == null || cntrNo.trim().equals("")) {
			return false;
		}
		if (cntrNo.length() != 11) {
			return false;
		}

		int[] alphaCode = new int[26];
		int[] charWeight = new int[10];

		String cntrText = cntrNo;
		int cntrLen = cntrText.length();
		long result = 0;
		long sum = 0;
		String checkDigit = cntrText.substring(10, 11);

		if (cntrLen != 11) {
			return false;
		}

		// 컨테이너번호의 다섯번째 자리 이후의 문자가 숫자인지 체크
		for (int i = 4; i < 10; i++) {
			String chr = cntrText.substring(i, i + 1);
			if (toInt(chr) == 0 && !chr.equals("0")) {
				return false;
			}
		}

		// 알파벳문자별 값셋팅
		int j = 10;
		for (int i = 0; i < 26; i++) {
			if (j == 11 || j == 22 || j == 33) {
				j++;
			}
			alphaCode[i] = j;
			j++;
		}

		// 자리별 가중치 값셋팅
		for (int i = 0; i < 10; i++) {
			charWeight[i] = (int) Math.pow(2, i);
		}

		int h = 0;
		if (cntrLen == 11) {
			for (int i = 0; i < cntrLen - 1; i++) {

				if (i < 4) {
					h = cntrText.charAt(i);
					h = h - 65;
					sum = sum + alphaCode[h] * charWeight[i];
				} else {
					sum = sum + Integer.parseInt(cntrText.substring(i, i + 1))
							* charWeight[i];
				}
			}

			result = sum % 11;
			if (result == 10)
				result = 0;

			// 실제값과 올바른 Check Digit과의 값 비교
			if (result != Integer.parseInt(checkDigit)) {
				return false;
			}

		} else {
			return false;
		}
		return true;
	}

	/**
	 * param String input을 int로 캐스팅하여 리턴함. 캐스팅시에 Exception 발생시 return 0
	 * 
	 * @param input
	 * @return int
	 */
	public static int toInt(String input) {
		int retVal = 0;
		if(input != null){
			try {
				retVal = Integer.parseInt(input);
			} catch (Exception e) {
				retVal = 0;
			}
		}
		
		return retVal;
	}

	/**
	 * param Object input을 int로 캐스팅하여 리턴함. 캐스팅시에 Exception 발생시 return 0
	 * 
	 * @param input
	 * @return int
	 */
	public static int toInt(Object input) {
		String inputClass = input.getClass().getName();
		int retVal = 0;
		try {
			if (inputClass.indexOf("Double") > 0) {
				retVal = ((Double) input).intValue();
			} else if (inputClass.indexOf("Integer") > 0) {
				retVal = (Integer) input;
			} else if (inputClass.indexOf("String") > 0) {
				retVal = Integer.valueOf((String) input);
			} else if (inputClass.indexOf("Float") > 0) {
				retVal = ((Float) input).intValue();
			}

		} catch (Exception e) {
			retVal = 0;
		}
		return retVal;
	}

	/**
	 * object -> string 변환 , null, "null" 은 ""으로 변환
	 * 
	 * @param src
	 * @return
	 */
	public static String nullConvert(Object src) {
		try{
			// if (src != null &&
			// src.getClass().getName().equals("java.math.BigDecimal")) {
			if (src != null && src instanceof java.math.BigDecimal) {
				return ((BigDecimal) src).toString();
			} else if (src != null && src instanceof java.lang.Double) {
				return String.valueOf(src);
			} else if (src != null && src instanceof  java.lang.Float ) {
				return String.valueOf(src);
			}

			if (src == null || src.equals("null")) {
				return "";
			} else {
				return ((String) src).trim();
			}
		}catch(Exception e){
			return "";
		}

	}

	/**
	 * 미터 -> 키로미터 변환
	 *
	 * @param preDis
	 * @return
	 */
	public static String convertMeter(String preDis) {
		try{
			double d = Double.valueOf(preDis);
			int numDis = (int) d;

			numDis = Math.round(numDis / 1000);

			String rtnStr = String.valueOf(numDis) + "km";

			return rtnStr;

		}catch(Exception e){
			return "0km";
		}

	}


	/**
	 * object -> string 변환 , null, "null" 은 defaultStr 으로 변환
	 *
	 * @param src
	 * @return
	 */
	public static String nullConvert(Object src, String defaultStr) {
		// if (src != null &&
		// src.getClass().getName().equals("java.math.BigDecimal")) {
		if (src != null && src instanceof java.math.BigDecimal) {
			return ((BigDecimal) src).toString();
		} else if (src != null && src instanceof java.lang.Double) {
			return String.valueOf(src);
		}

		if (src == null || src.equals("null")){
			return defaultStr;
		} else {
			return StringUtil.hasValue((String) src) ? ((String) src).trim() : defaultStr;
		}
	}

}
