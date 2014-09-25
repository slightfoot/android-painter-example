package com.dd.painter;

import android.content.res.Resources;

import android.content.res.Configuration;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


public class PlayActivity extends Activity
{
	private DrawingView mDrawingView;
	private ImageButton mEraserTool;
	private ImageButton mClearTool;
	
	// see: http://stackoverflow.com/questions/25758294/how-to-fill-different-color-on-same-area-of-imageview-color-over-another-color/
	static int[] COLORS = {
		Color.rgb(255,  51, 255), // DARK PINK
		Color.rgb(255, 230, 102), // LIGHT YELLOW
		Color.rgb(148,  66,  50), // DARK MAROON
		Color.rgb(186, 123,  68), // LIGHT MAROON
		Color.rgb(252,  20,  20), // RED
		Color.rgb(102, 255, 255), // LIGHT BLUE
		
		Color.rgb( 70,  78, 202), // DARK BLUE
		Color.rgb(190, 255,  91), // LIGHT GREEN
		Color.rgb( 15, 230,   0), // DARK GREEN
		Color.rgb(123,   0, 230), // JAMBLI
		Color.rgb(255, 187,  50), // ORANGE
		Color.rgb(  7,   5,   0), // BLACK
		
		Color.rgb(129, 128, 127), // GRAY
		Color.rgb(255,   4, 139), // PINK RED
		Color.rgb( 51, 204, 255), // NAVY BLUE
		Color.rgb(102, 255, 204), // BRIGHT GREEN
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(createCheckerBoard(getResources(), 16));
		
		setContentView(R.layout.activity_play);
		
		mDrawingView = (DrawingView)findViewById(R.id.drawing_view);
		mDrawingView.setShape(R.drawable.img_a_inner, R.drawable.img_a);
		
		int rowLimit = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 6;
		
		TableLayout controlsPanel = (TableLayout)findViewById(R.id.controls_panel);
		TableRow tableRow = null;
		for(int i = 0; i < COLORS.length; i++){
			if((i % rowLimit) == 0){
				tableRow = new TableRow(this);
				controlsPanel.addView(tableRow, new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
			}
			tableRow.addView(createToolButton(tableRow, R.drawable.paint_spot, i));
		}
		mEraserTool = createToolButton(tableRow, R.drawable.paint_eraser, -1);
		tableRow.addView(mEraserTool);
		mClearTool  = createToolButton(tableRow, R.drawable.paint_clear, -1);
		tableRow.addView(mClearTool);
	}
		
	private ImageButton createToolButton(ViewGroup parent, int drawableResId, int index)
	{
		ImageButton button = (ImageButton)getLayoutInflater().inflate(R.layout.button_paint_spot, parent, false);
		button.setImageResource(drawableResId);
		button.setOnClickListener(mButtonClick);
		if(index != -1){
			button.setTag(Integer.valueOf(index));
			button.setColorFilter(COLORS[index]);
		}
		return button;
	}
	
	private View.OnClickListener mButtonClick = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(v == mEraserTool){
				mDrawingView.enableEraser();
			}
			else if(v == mClearTool){
				mDrawingView.clearDrawing();
			}
			else{
				mDrawingView.setDrawingColor(COLORS[((Integer)v.getTag()).intValue()]);
			}
		}
	};
	
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
