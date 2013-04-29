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
import de.meetr.hdr.paperless.misc.IntentHelper;
import de.meetr.hdr.paperless.model.Page;
import de.meetr.hdr.paperless.model.Paper;
import de.meetr.hdr.paperless.paper.PageFactory;
import de.meetr.hdr.paperless.view.BitmapView;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout.LayoutParams;

/**
 * (Main) Activity for viewing/editing Paper(s).
 * 
 * @author Joseph Wessner <joseph@wessner.org>
 */
public class EditorActivity extends Activity implements OnTouchListener {
	/**
	 * The paper, which is edited
	 */
	private Paper currentPaper;
	/**
	 * Current page, which is displayed
	 */
	private Page currentPage = null;
	
	/**
	 * MainView for the Page 
	 */
	private BitmapView mainPaperView;
	/**
	 * ZoomedView for the Page
	 */
	private BitmapView zoomedPaperView;
	/**
	 * Frame which shows the dimension of the zoomed view in the main view
	 */
	private View zoomedPaperFrame;
	
	/**
	 * Bitmap for the background (Paper)
	 */
	private Bitmap backgroundBitmap = null;
	/**
	 * Bitmap for the user's drawing
	 */
	private Bitmap foregroundBitmap = null;
	
	/**
	 * Canvas to draw on the foregroundBitmap
	 */
	private Canvas foregroundCanvas;
	/**
	 * Paint used to draw on Bitmaps
	 */
	private Paint paint;
	
	private float lastX, lastY;
	private int frameOffX, frameOffY;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editor);
		
		// Get Paper from IntentHelper
		this.currentPaper = (Paper) IntentHelper.getObjectForKey("selectedPaper");
		
		// Get all needed views
		this.mainPaperView = (BitmapView) this.findViewById(R.id.mainPaperView);
		this.zoomedPaperView = (BitmapView) this.findViewById(R.id.zoomedPaperView);
		this.zoomedPaperFrame = (View) this.findViewById(R.id.zoomedPaperFrame);
		
		// Add listeners
		this.mainPaperView.setOnTouchListener(this);
	    this.zoomedPaperFrame.setOnTouchListener(this);
	    this.zoomedPaperView.setOnTouchListener(this);

//		this.backgroundBitmap = PaperFactory.getDINA4Page(PaperFactory.PaperType.LINED);
//		this.foregroundBitmap = Bitmap.createBitmap(
//				this.backgroundBitmap.getWidth(),
//				this.backgroundBitmap.getHeight(), Bitmap.Config.ARGB_8888);
//		Canvas c = new Canvas(this.foregroundBitmap);
//		c.drawARGB(0, 0, 0, 0);
//		
//		Drawable[] layers = new Drawable[2];
//		layers[0] = new BitmapDrawable(this.getResources(), this.backgroundBitmap);
//		layers[1] = new BitmapDrawable(this.getResources(), this.foregroundBitmap);
//		LayerDrawable layerDrawable = new LayerDrawable(layers);
//		
//		this.foregroundCanvas = new Canvas(foregroundBitmap);
//	    this.paint = new Paint();
//	    this.paint.setColor(Color.RED);
//	    this.paint.setStrokeWidth(3);
//	    this.paint.setAntiAlias(true);
//	    this.paint.setDither(true);
//	    this.paint.setStrokeCap(Paint.Cap.ROUND);
//	    
//	    this.mainPaperView.setImageDrawable(layerDrawable);
//	    this.zoomedPaperView.setImageDrawable(layerDrawable);
	}
	
	public void onStart() {
		super.onStart();
		
		Log.d("onStart", "ImageView width: " + this.mainPaperView.getWidth());
	}
	
	public void onResume() {
		super.onResume();
		
		Log.d("onResume", "ImageView width: " + this.mainPaperView.getWidth());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.write, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_page:
			this.addPage();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
		case R.id.mainPaperView:
			this.onMainPaperViewTouch(event);
			break;
		case R.id.zoomedPaperView:
			this.onZoomedPaperView(event);
			break;
		case R.id.zoomedPaperFrame:
			this.onZoomedPaperFrame(event);
			break;
		default:
			return false;
		}
		
		return true;
	}
	
	public void onMainPaperViewTouch(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			this.mainPaperView.scrollBy(
					(int) ((this.lastX - event.getX()) * this.mainPaperView
							.getScaleX()),
					(int) ((this.lastY - event.getY()) * this.mainPaperView
							.getScaleY()));

		case MotionEvent.ACTION_DOWN:
			this.lastX = event.getX();
			this.lastY = event.getY();
			break;
		default:
			break;
		}
	}

	public void onZoomedPaperView(MotionEvent event) {
		// Getting coordinate relative to frame view
					final int frameX = (int) event.getX();
					final int frameY = (int) event.getY();
					
					// Get layout parameters of the frame
					LayoutParams params = (LayoutParams) this.zoomedPaperFrame.getLayoutParams();
					
					// Compute coordinate relative to image view
					final int imageViewX = frameX + params.leftMargin - this.frameOffX;
					final int imageViewY = frameY + params.topMargin - this.frameOffY;
					
					params.width = (int) (this.zoomedPaperView.getDisplayedWidth() / this.mainPaperView.getScaleX());
					params.height = (int) (this.zoomedPaperView.getDisplayedHeight() / this.mainPaperView.getScaleY());
					this.zoomedPaperFrame.setLayoutParams(params);
					
					// Compute upper left corner in image coordinates
//					final int imageX = (int) ((this.imageView.getScrollX() + imageViewX)); //  * this.imageView.getScaleX());
					final int imageX = (int) (this.mainPaperView.getImagePosX() + imageViewX * this.mainPaperView.getScaleX());
					final int imageY = (int) (this.mainPaperView.getImagePosY() + imageViewY * this.mainPaperView.getScaleY());
					
					Log.d("scrollX", "" + this.mainPaperView.getImagePosX());
					Log.d("imageX", "" + imageX);
					
//					this.zoomedView.setScaleX(this.imageView.getScaleX() * 2f);
//					this.zoomedView.setScaleY(this.imageView.getScaleY() * 2f);
					
//					matrix.setScale(this.imageView.getScaleX() * 2f, this.imageView.getScaleY() * 2f, imageX, imageY);
//					this.zoomedView.setImageMatrix(matrix);
					this.zoomedPaperView.setImageScale(2.5f);
					this.zoomedPaperView.scrollTo(imageX, imageY);
					
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						this.frameOffX = frameX;
						this.frameOffY = frameY;
						break;
					case MotionEvent.ACTION_MOVE:
						params.leftMargin = (int) imageViewX;
						params.topMargin = (int) imageViewY;
						this.zoomedPaperFrame.setLayoutParams(params);
						break;
					default:
						break;
					}			
	}

	public void onZoomedPaperFrame(MotionEvent event) {
		final float imageX = event.getX() / this.zoomedPaperView.getImageScale() + this.zoomedPaperView.getImagePosX();
		final float imageY = event.getY() / this.zoomedPaperView.getImageScale() + this.zoomedPaperView.getImagePosY();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			this.foregroundCanvas.drawLine(this.lastX, this.lastY, imageX, imageY, this.paint);
			this.mainPaperView.invalidate();
			this.zoomedPaperView.invalidate();
		case MotionEvent.ACTION_DOWN:
			this.lastX = imageX;
			this.lastY = imageY;
		}
	}
	
	private void addPage() {
		String[] items = {
				getString(R.string.papertype_lined),
				getString(R.string.papertype_squared),
				getString(R.string.papertype_blank)
			};

		// Setup AlertDialog for name request
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.add_page);
		// alert.setMessage(R.string.add_page);
		alert.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// The 'which' argument contains the index position
				// of the selected item
				PageFactory.PaperType type;
				switch (which) {
				case 0:
					type = PageFactory.PaperType.LINED;
					break;
				case 1:
					type = PageFactory.PaperType.SQUARED;
					break;
				default:
					type = PageFactory.PaperType.BLANK;
				}
				
				addPage(type);
			}
		});

		// Display the dialog
		alert.show();
	}
	
	private void addPage(PageFactory.PaperType type) {
		this.closePage();
		
		this.backgroundBitmap = PageFactory.getDINA4Page(type);
		this.foregroundBitmap = Bitmap.createBitmap(
				this.backgroundBitmap.getWidth(),
				this.backgroundBitmap.getHeight(), 
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(this.foregroundBitmap);
		c.drawARGB(0, 0, 0, 0);
		
		this.currentPage = this.currentPaper.addNewPage(this.backgroundBitmap.getWidth(), this.backgroundBitmap.getHeight());
		
		Drawable[] layers = new Drawable[2];
		layers[0] = new BitmapDrawable(this.getResources(), this.backgroundBitmap);
		layers[1] = new BitmapDrawable(this.getResources(), this.foregroundBitmap);
		LayerDrawable layerDrawable = new LayerDrawable(layers);
		
		this.mainPaperView.setImageDrawable(layerDrawable);
		this.zoomedPaperView.setImageDrawable(layerDrawable);
		
		this.mainPaperView.setVisibility(View.VISIBLE);
		this.zoomedPaperView.setVisibility(View.VISIBLE);
	}
	
	private void closePage() {
		if (null == this.currentPage)
			return;
		
		try {
			this.currentPage.saveLayer(this.backgroundBitmap, 0);
			this.currentPage.saveLayer(this.foregroundBitmap, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.clearBitmaps();
	}
	
	private void clearBitmaps() {
		if (null != this.foregroundBitmap)
			this.foregroundBitmap.recycle();
		this.foregroundBitmap = null;
		
		if (null != this.backgroundBitmap)
			this.backgroundBitmap.recycle();
		this.backgroundBitmap = null;
	}
}
