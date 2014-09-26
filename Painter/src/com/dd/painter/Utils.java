package com.dd.painter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;


public final class Utils
{
	public static BitmapDrawable createCheckerBoard(Resources res, int size)
	{
		size *= res.getDisplayMetrics().density;
		
		BitmapShader shader = new BitmapShader(Bitmap.createBitmap(new int[] {
			0xFFFFFFFF, 0xFFCCCCCC, 0xFFCCCCCC, 0xFFFFFFFF }, 2, 2, Bitmap.Config.RGB_565), 
			BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT);
		Matrix matrix = new Matrix();
		matrix.setScale(size, size);
		shader.setLocalMatrix(matrix);
		
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setShader(shader);
		
		Bitmap bm2 = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.RGB_565);
		new Canvas(bm2).drawPaint(paint);
		
		BitmapDrawable drawable = new BitmapDrawable(res, bm2);
		drawable.setTileModeXY(BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT);
		
		return drawable;
	}
}
