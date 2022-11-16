package kr.co.klnet.aos.etransdriving.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * 터치 그리기
 */
public class PaintView extends View
{
	public class Vertex
	{
		float	x;
		float y;
		boolean	draw;

		public Vertex(float x, float y, boolean draw)
		{
			this.x = x;
			this.y = y;
			this.draw = draw;
		}
	}

	/**
	 * @uml.property  name="paint"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	Paint paint;
	
	Bitmap bitmap;
	/**
	 * @uml.property  name="lists"
	 * @uml.associationEnd  multiplicity="(0 -1)" inverse="this$0:com.klnet.ui.PaintView$Vertex"
	 */
	ArrayList<Vertex> lists;

	public PaintView(Context context)
	{
		super(context);

		lists = new ArrayList<Vertex>();
		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(15);// 선두께
		paint.setAntiAlias(true);// 선을 부드럽게
	}

	/**
	 * 캡쳐
	 * 
	 * @param _filename
	 */
	public Bitmap CaptureView(String _filename)
	{
		this.setDrawingCacheEnabled(true);
		Bitmap bm = this.getDrawingCache();

		try
		{
			File path = new File("/sdcard/capture");

			if (!path.isDirectory())
			{
				path.mkdirs();
			}

			FileOutputStream out = new FileOutputStream("/sdcard/capture/" + _filename);
			bm.compress(Bitmap.CompressFormat.JPEG, 50, out);
		}
		catch (FileNotFoundException e)
		{
			//Log.d("FileNotFoundException:", e.getMessage());
		}

		return bm;
	}

	public void clear()
	{
		lists = new ArrayList<Vertex>();
		invalidate();
	}

	/**
	 * 파일 로드
	 * 
	 * @param bm
	 */
	void LoadFileBitmap(Bitmap bm, String _filename)
	{
		try
		{
			File path = new File("/sdcard/capture");

			if (!path.isDirectory())
			{
				path.mkdirs();
			}

			FileOutputStream out = new FileOutputStream("/sdcard/capture/" + _filename);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
		}
		catch (FileNotFoundException e)
		{
			//Log.d("FileNotFoundException:", e.getMessage());
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
//		if(bitmap == null){
//			bitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
//		}
//		canvas.drawBitmap(bitmap, null,new RectF(0,0,getWidth(),getHeight()),null);

		canvas.drawColor(0xffffffff);
		// 정점을 순회하면서 선을 그린다.
		for (int i = 0; i < lists.size(); i++)
		{
			if (lists.get(i).draw)
			{
				canvas.drawLine(lists.get(i - 1).x, lists.get(i - 1).y, lists.get(i).x, lists.get(i).y, paint);
			}
		}
	}

	public void transparent(int x, int y) {
		Bitmap tmp = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(tmp);
		c.drawBitmap(bitmap, null, new RectF(0, 0, getWidth(), getHeight()),
				null);
		Path path = new Path();
		path.setFillType(FillType.WINDING);
		path.addOval((new RectF(x, y, x + 15, y + 15)), Path.Direction.CW);
		Paint paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
		paint.setAlpha(0);
		c.drawPath(path, paint);
		bitmap = tmp;
		invalidate();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		// 터치 이동시 정점을 추가
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			lists.add(new Vertex(event.getX(), event.getY(), false));
			//transparent((int)event.getX(), (int)event.getY());
			return true;
		}

		if (event.getAction() == MotionEvent.ACTION_MOVE)
		{
			lists.add(new Vertex(event.getX(), event.getY(), true));
			invalidate();
			return true;
		}

		return false;
	}
}