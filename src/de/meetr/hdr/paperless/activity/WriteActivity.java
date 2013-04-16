/**
 * paperLess - Android App for taking notes in PDFs
 * Copyright (C) 2013 Joseph Wessner
 * 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.meetr.hdr.paperless.activity;

import de.meetr.hdr.paperless.R;
import de.meetr.hdr.paperless.paper.PaperFactory;
import de.meetr.hdr.paperless.view.BitmapView;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout.LayoutParams;

public class WriteActivity extends Activity implements OnTouchListener {
	public static final int DEFAULT_WIDTH = 1240;	// 209,97mm with 150 ppi (DIN A4)
	public static final int DEFAULT_HEIGHT = 1754;	// 297,01mm with 150 ppi (DIN A4)
	public static final int RESOLUTION = 150;		// 150 ppi
	public static final float MM_TO_PX = 150 / 254;
	
	private BitmapView imageView;
	private BitmapView zoomedView;
	
	private View frameView;
	
	private Bitmap backgroundBitmap;
	private Bitmap foregroundBitmap;
	
	private Canvas canvas;
	private Paint paint;
	
	private float lastX, lastY;
	private int frameOffX, frameOffY;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_write);
		
		this.imageView = (BitmapView) this.findViewById(R.id.imageView1);
		this.zoomedView = (BitmapView) this.findViewById(R.id.imageView2);
		this.frameView = (View) this.findViewById(R.id.view1);

		this.backgroundBitmap = PaperFactory.getDINA4Page(PaperFactory.PaperType.LINED);
		this.foregroundBitmap = Bitmap.createBitmap(
				this.backgroundBitmap.getWidth(),
				this.backgroundBitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(this.foregroundBitmap);
		c.drawARGB(0, 0, 0, 0);
		
		Drawable[] layers = new Drawable[2];
		layers[0] = new BitmapDrawable(this.getResources(), this.backgroundBitmap);
		layers[1] = new BitmapDrawable(this.getResources(), this.foregroundBitmap);
		LayerDrawable layerDrawable = new LayerDrawable(layers);
		
		this.canvas = new Canvas(foregroundBitmap);
	    this.paint = new Paint();
	    this.paint.setColor(Color.RED);
	    this.paint.setStrokeWidth(3);
	    this.paint.setAntiAlias(true);
	    this.paint.setDither(true);
	    this.paint.setStrokeCap(Paint.Cap.ROUND);
	    
//	    this.imageView.setImageBitmap(this.backgroundBitmap);
//	    this.zoomedView.setImageBitmap(this.backgroundBitmap);
	    
	    this.imageView.setImageDrawable(layerDrawable);
	    this.zoomedView.setImageDrawable(layerDrawable);
	    
	    // Paint lines
	    
	    // Paint blue lines on upper half
	    this.paint.setColor(Color.BLUE);
	    this.canvas.drawLine(100.0f, 0.25f*DEFAULT_HEIGHT, DEFAULT_WIDTH-100.0f, 0.25f*DEFAULT_HEIGHT, this.paint);
	    
	    // Paint green lines on bottom half
	    this.paint.setColor(Color.GREEN);
	    this.canvas.drawLine(100.0f, 0.5f*DEFAULT_HEIGHT, DEFAULT_WIDTH-100.0f, 0.5f*DEFAULT_HEIGHT, this.paint);
	    
	    // Paint red lines on bottom half
	    this.paint.setColor(Color.RED);
	    this.canvas.drawLine(100.0f, 0.75f*DEFAULT_HEIGHT, DEFAULT_WIDTH-100.0f, 0.75f*DEFAULT_HEIGHT, this.paint);

	    this.imageView.setOnTouchListener(this);
	    this.frameView.setOnTouchListener(this);
	    this.zoomedView.setOnTouchListener(this);
	}
	
	public void onStart() {
		super.onStart();
		
		Log.d("onStart", "ImageView width: " + this.imageView.getWidth());
	}
	
	public void onResume() {
		super.onResume();
		
		Log.d("onResume", "ImageView width: " + this.imageView.getWidth());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.write, menu);
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v == this.frameView) {
			// Getting coordinate relative to frame view
			final int frameX = (int) event.getX();
			final int frameY = (int) event.getY();
			
			// Get layout parameters of the frame
			LayoutParams params = (LayoutParams) this.frameView.getLayoutParams();
			
			// Compute coordinate relative to image view
			final int imageViewX = frameX + params.leftMargin - this.frameOffX;
			final int imageViewY = frameY + params.topMargin - this.frameOffY;
			
			params.width = (int) (this.zoomedView.getDisplayedWidth() / this.imageView.getScaleX());
			params.height = (int) (this.zoomedView.getDisplayedHeight() / this.imageView.getScaleY());
			this.frameView.setLayoutParams(params);
			
			// Compute upper left corner in image coordinates
//			final int imageX = (int) ((this.imageView.getScrollX() + imageViewX)); //  * this.imageView.getScaleX());
			final int imageX = (int) (this.imageView.getImagePosX() + imageViewX * this.imageView.getScaleX());
			final int imageY = (int) (this.imageView.getImagePosY() + imageViewY * this.imageView.getScaleY());
			
			Log.d("scrollX", "" + this.imageView.getImagePosX());
			Log.d("imageX", "" + imageX);
			
//			this.zoomedView.setScaleX(this.imageView.getScaleX() * 2f);
//			this.zoomedView.setScaleY(this.imageView.getScaleY() * 2f);
			
//			matrix.setScale(this.imageView.getScaleX() * 2f, this.imageView.getScaleY() * 2f, imageX, imageY);
//			this.zoomedView.setImageMatrix(matrix);
			this.zoomedView.setImageScale(2.5f);
			this.zoomedView.scrollTo(imageX, imageY);
			
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				this.frameOffX = frameX;
				this.frameOffY = frameY;
				break;
			case MotionEvent.ACTION_MOVE:
				params.leftMargin = (int) imageViewX;
				params.topMargin = (int) imageViewY;
				this.frameView.setLayoutParams(params);
				break;
			default:
				break;
			}			
		} else if (v == this.imageView) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				this.imageView.scrollBy((int) ((this.lastX - event.getX())*this.imageView.getScaleX()),
						(int) ((this.lastY - event.getY())*this.imageView.getScaleY()));
				
			case MotionEvent.ACTION_DOWN:
				this.lastX = event.getX();
				this.lastY = event.getY();
				break;
			default:
				break;
			}
		} else if (v == this.zoomedView) {
			final float imageX = event.getX() / this.zoomedView.getImageScale() + this.zoomedView.getImagePosX();
			final float imageY = event.getY() / this.zoomedView.getImageScale() + this.zoomedView.getImagePosY();
			
			switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				this.canvas.drawLine(this.lastX, this.lastY, imageX, imageY, this.paint);
				this.imageView.invalidate();
				this.zoomedView.invalidate();
			case MotionEvent.ACTION_DOWN:
				this.lastX = imageX;
				this.lastY = imageY;
			}
		}
		
//		int action = event.getAction();
//		switch (action) {
//		case MotionEvent.ACTION_MOVE:
//			this.canvas.drawLine(this.lastX, this.lastY, event.getX(),
//					event.getY(), this.paint);
//			imageView.invalidate();
//		case MotionEvent.ACTION_DOWN:
//			this.lastX = event.getX();
//			this.lastY = event.getY();
//			break;
////		case MotionEvent.ACTION_UP:
////			upx = event.getX();
////			upy = event.getY();
////			canvas.drawLine(downx, downy, upx, upy, paint);
////			imageView.invalidate();
////			break;
//		case MotionEvent.ACTION_CANCEL:
//			break;
//		default:
//			break;
//		}
		return true;
	}
}
