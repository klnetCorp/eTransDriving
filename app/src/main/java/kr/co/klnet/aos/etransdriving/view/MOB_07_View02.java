package kr.co.klnet.aos.etransdriving.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import kr.co.klnet.aos.etransdriving.R;

/**
 * 서명 받기 화면  (벌크)
 */
public class MOB_07_View02 extends FrameLayout
{
	
	private Context context;
	private PaintView paintView;
	private View view;

	public MOB_07_View02(Context _context)
	{
		super(_context);
		context = _context;
		
		LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT);
		
		this.setLayoutParams(params);
		paintView = new PaintView(context);
//		paintView.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
//		paintView.setBackgroundColor(Color.TRANSPARENT);
		
		paintView.setDrawingCacheEnabled(true);
		paintView.setLayoutParams(params);

		view = LayoutInflater.from(context).inflate(R.layout.mob_07_view02, null);
		view.setLayoutParams(params);
		addView(paintView);
		addView(view);
	}

	public void clear()
	{
		paintView.clear();
	}

	/**
	 * 캡쳐
	 * 
	 * @param _filename
	 */
	public Uri CaptureView(String _filename) {
		Bitmap bm = paintView.getDrawingCache();
		FileOutputStream out = null;
		try {
			File path = new File(Environment.getExternalStorageDirectory().toString()+"/klnet/");

			if (!path.isDirectory()) {
				path.mkdirs();
			}

			//서명 저장 및 서버로의 전송 오류 개선 (황용민) 12.10.19
			File f = new File(path + "/" + _filename);
			out = new FileOutputStream(f);

			bm.compress(Bitmap.CompressFormat.JPEG, 60, out);

			return Uri.fromFile(f);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch(Exception e) {
				
			}
		}

		return Uri.EMPTY;
	}
	
	public Bitmap getSignBitmap()
	{
		return paintView.getDrawingCache();
	}
}