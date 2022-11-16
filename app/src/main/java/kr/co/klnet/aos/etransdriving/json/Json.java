package kr.co.klnet.aos.etransdriving.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

//import android.util.Log;

/**
 * JSON 파서
 */
@SuppressWarnings("serial")
public class Json extends HashMap<String, String>
{
	/**
	 * 입력된 Key에 대응되는 value 반환
	 */
	@Override
	public String get(Object key)
	{
		return super.get(key) != null ? super.get(key) : "null";
	}

	/**
	 * JSON 형식의 데이터를 파서
	 * 
	 * @param _jsonString
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public void paser(String _jsonString)
	{
		try {


			if (_jsonString.length() > 0) {

				JSONObject jObject = new JSONObject(_jsonString);

				for (Iterator<String> names = jObject.keys(); names.hasNext(); ) {
					String name = names.next();

					if (name != null) {
						try {
							jObject.getString(name).charAt(0);
						} catch (Exception e) {
							continue;
						}

						if (jObject.getString(name).charAt(0) == '[') {
							JSONArray jSONArray = jObject.getJSONArray(name);
							put(name + "_size", "" + jSONArray.length());
							//Log.d("JSON", name + "_size : " + jSONArray.length());

							for (int i = 0; i < jSONArray.length(); i++) {
								for (Iterator<String> jSONArrayNames = jSONArray.getJSONObject(i).keys(); jSONArrayNames.hasNext(); ) {
									String jSONArrayName = jSONArrayNames.next();
									put(name + "_" + i + "_" + jSONArrayName, jSONArray.getJSONObject(i).getString(jSONArrayName));
									//Log.d("JSON", name + "_" + i + "_" + jSONArrayName + " : " + jSONArray.getJSONObject(i).getString(jSONArrayName));
								}
							}
						} else {
							try {
								jObject.getString(name).charAt(0);
							} catch (Exception e) {
								continue;
							}

							if (jObject.getString(name).charAt(0) == '{') {
								paser(jObject.getString(name));
							} else {
								put(name, jObject.getString(name));
							}
						}
					}
				}

			}
		}
	catch(Exception e)
		{
			e.printStackTrace();
//			Log.d("JSON", "jsonPaser Error : " + e.getMessage());
		}

	}
}