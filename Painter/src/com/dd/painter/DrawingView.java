package com.dd.painter;

import android.graphics.Shader;
import android.graphics.BitmapShader;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class DrawingView extends View
{
	private final Paint  mPaintSrcIn   = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
	private final Paint  mPaintDstIn   = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
	private final Paint  mPaintColor   = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint  mPaintEraser  = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private final Path   mPath         = new Path();
	
	private Bitmap mInnerShape,  mOuterShape;
	private Bitmap mBitmapLayer, mBitmapDraw;
	private Canvas mCanvasLayer, mCanvasDraw;
	
	private Paint mPaintDraw;
	
	
	public DrawingView(Context context)
	{
		this(context, null, 0);
	}
	
	public DrawingView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}
	
	public DrawingView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		
		mPaintSrcIn.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		mPaintDstIn.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		
		mPaintColor.setStrokeWidth(30);
		mPaintColor.setStyle(Paint.Style.STROKE);
		mPaintColor.setStrokeJoin(Paint.Join.ROUND);
		mPaintColor.setStrokeCap(Paint.Cap.ROUND);
		
		mPaintEraser.set(mPaintColor);
		
		//mPaintEraser.setColor(Color.WHITE);
		mPaintEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		//mPaintEraser.setColorFilter(new PorterDuffColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN));
		mPaintEraser.setMaskFilter(new BlurMaskFilter(5 * getResources().getDisplayMetrics().density,
			BlurMaskFilter.Blur.NORMAL));
		
		mPaintDraw = mPaintColor;
	}
	
	public void setShape(int inner, int outer)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ALPHA_8;
		setShape(BitmapFactory.decodeResource(getResources(), inner, options),
			BitmapFactory.decodeResource(getResources(), outer, options));
	}
	
	public void setShape(Bitmap inner, Bitmap outer)
	{
		mInnerShape = inner;
		mOuterShape = outer;
		replaceDrawingBitmap(outer.getWidth(), outer.getHeight());
		requestLayout();
		invalidate();
	}
	
	public void setDrawingColor(int color)
	{
		mPaintDraw = mPaintColor;
		mPaintDraw.setColor(color);
	}
	
	public void enableEraser()
	{
		mPaintDraw = mPaintEraser;
	}
	
	public void clearDrawing()
	{
		mCanvasDraw.drawColor(0, PorterDuff.Mode.CLEAR);
		invalidate();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		if(mOuterShape != null){
			setMeasuredDimension(
				resolveSize(mOuterShape.getWidth(), widthMeasureSpec),
				resolveSize(mOuterShape.getHeight(), heightMeasureSpec));
		}
		else{
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		mBitmapLayer = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mCanvasLayer = new Canvas(mBitmapLayer);
	}
	
	private void replaceDrawingBitmap(int w, int h)
	{
		Bitmap newBitmapDraw = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mCanvasDraw = new Canvas(newBitmapDraw);
		if(mBitmapDraw != null){
			mCanvasDraw.drawBitmap(mBitmapDraw, 0, 0, null);
		}
		mBitmapDraw = newBitmapDraw;
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		
		// NOTE: Without extra bitmap or layer.. but HW Acceleration does not support setMaskFilter which means
		// eraser has strong edges whilst drawing.
		// @see http://developer.android.com/guide/topics/graphics/hardware-accel.html#unsupported 
		/*
		canvas.drawBitmap(mOuterShape, 0, 0, null);
		canvas.saveLayer(null, mPaint, Canvas.FULL_COLOR_LAYER_SAVE_FLAG);
		canvas.drawColor(0, PorterDuff.Mode.CLEAR);
		canvas.drawBitmap(mInnerShape, 0, 0, null);
		canvas.saveLayer(null, mPaintSrcIn, Canvas.FULL_COLOR_LAYER_SAVE_FLAG);
		canvas.drawBitmap(mBitmapDraw, 0, 0, null);
		canvas.drawPath(mPath, mPaintDraw);
		canvas.restore();
		canvas.restore();
		*/
		
		// Clear software canvas
		mCanvasLayer.drawColor(0, PorterDuff.Mode.CLEAR);
		// Draw previously drawn lines
		mCanvasLayer.drawBitmap(mBitmapDraw, 0, 0, null);
		// Draw currently drawn line
		mCanvasLayer.drawPath(mPath, mPaintDraw);
		// Mask the drawing to the inner surface area of the shape
		mCanvasLayer.drawBitmap(mInnerShape, 0, 0, mPaintDstIn);
		
		// Draw orignal shape to view
		canvas.drawBitmap(mOuterShape, 0, 0, null);
		// Draw masked image to view
		canvas.drawBitmap(mBitmapLayer, 0, 0, null);
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		final float x = event.getX();
		final float y = event.getY();
		
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				mPath.moveTo(x, y);
				break;
			
			case MotionEvent.ACTION_MOVE:
				for(int i = 0; i < event.getHistorySize(); i++){
					mPath.lineTo(event.getHistoricalX(i), event.getHistoricalY(i));
				}
				mPath.lineTo(x, y);
				break;
			
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mPath.lineTo(x, y);
				mCanvasDraw.drawPath(mPath, mPaintDraw);
				mPath.reset();
				break;
		}
		
		invalidate();
		
		return true;
	}
	
	@Override
	protected Parcelable onSaveInstanceState()
	{
		return new SavedState(super.onSaveInstanceState(), mBitmapDraw);
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		SavedState savedState = (SavedState)state;
		super.onRestoreInstanceState(savedState.getSuperState());
		if(savedState.bitmap != null){
			mBitmapDraw = savedState.bitmap;
			mCanvasDraw = new Canvas(mBitmapDraw);
			invalidate();
		}
	}
	
	
	static class SavedState extends BaseSavedState
	{
		private Bitmap bitmap;
		
		
		public SavedState(Parcelable superState, Bitmap bitmap)
		{
			super(superState);
			this.bitmap = bitmap;
		}
		
		public SavedState(Parcel source)
		{
			super(source);
			bitmap = source.readParcelable(getClass().getClassLoader());
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			super.writeToParcel(dest, flags);
			dest.writeParcelable(bitmap, flags);
		}
		
		
		public static final Parcelable.Creator<SavedState> 
			CREATOR = new Parcelable.Creator<SavedState>()
		{
			public SavedState createFromParcel(Parcel in)
			{
				return new SavedState(in);
			}
			
			public SavedState[] newArray(int size)
			{
				return new SavedState[size];
			}
		};
	}
}
