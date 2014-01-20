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

import java.util.ArrayList;
import java.util.List;

import de.meetr.hdr.paperless.R;
import de.meetr.hdr.paperless.misc.IntentHelper;
import de.meetr.hdr.paperless.model.ColorModel;
import de.meetr.hdr.paperless.model.Page;
import de.meetr.hdr.paperless.model.Paper;
import de.meetr.hdr.paperless.model.Point2d;
import de.meetr.hdr.paperless.paper.PageFactory;
import de.meetr.hdr.paperless.view.BitmapView;
import de.meetr.hdr.paperless.view.ColorSpinnerAdapter;
import de.meetr.hdr.paperless.view.DrawView;
import de.meetr.hdr.paperless.view.OnDrawDoneListener;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;

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
	 * View used for drawing
	 */
	private DrawView drawView;

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

	/**
	 * Ratio of the zoom level between zoomed and main view
	 */
	private float zoomRatio = 2.f;

	private List<ColorModel> availableColors;

	private float lastX, lastY;
	private int frameOffX, frameOffY;

	/**
	 * Offset of the zoomed view
	 */
	private int zoomedViewOffX, zoomedViewOffY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_editor);

		// Set color spinner
		Spinner colorSpinner = (Spinner) findViewById(R.id.spinner_color);
		this.availableColors = new ArrayList<ColorModel>();
		this.availableColors.add(new ColorModel(Color.BLACK, getResources()
				.getString(R.string.color_black)));
		this.availableColors.add(new ColorModel(Color.WHITE, getResources()
				.getString(R.string.color_white)));
		this.availableColors.add(new ColorModel(Color.RED, getResources()
				.getString(R.string.color_red)));
		this.availableColors.add(new ColorModel(Color.BLUE, getResources()
				.getString(R.string.color_blue)));
		ColorSpinnerAdapter colorAdapter = new ColorSpinnerAdapter(this,
				R.layout.color_spinner_row, this.availableColors);
		colorSpinner.setAdapter(colorAdapter);
		// Listener called when spinner item selected
		colorSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				paint.setColor(availableColors.get(arg2).getColor());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// Nothing todo than
			}

		});

		// Click event for add page
		Button addPageButton = (Button) this.findViewById(R.id.button_add_page);
		addPageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				addPage();
			}
		});

		// Get Paper from IntentHelper
		this.currentPaper = (Paper) IntentHelper
				.getObjectForKey("selectedPaper");

		// Get all needed views
		this.mainPaperView = (BitmapView) this.findViewById(R.id.mainPaperView);
		this.zoomedPaperView = (BitmapView) this
				.findViewById(R.id.zoomedPaperView);
		this.zoomedPaperFrame = (View) this.findViewById(R.id.zoomedPaperFrame);

		// Add listeners
		this.mainPaperView.setOnTouchListener(this);
		this.zoomedPaperFrame.setOnTouchListener(this);
		this.zoomedPaperView.setOnTouchListener(this);

		// Get paint for drawing
		this.paint = new Paint();
		this.paint.setColor(Color.RED);
		this.paint.setStrokeWidth(3);
		this.paint.setStrokeJoin(Paint.Join.ROUND);
		this.paint.setStyle(Paint.Style.STROKE);
		this.paint.setAntiAlias(true);

		// Register for draw done event
		this.drawView = (DrawView) this.findViewById(R.id.drawView1);
		this.drawView.setOnDrawDoneListener(new OnDrawDoneListener() {
			@Override
			public boolean onDrawDone(List<Point2d> points) {
				saveDrawing(points);
				return true;
			}
		});
		// Set Paint for drawView
		this.drawView.setPaint(this.paint);
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
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
		case R.id.mainPaperView:
			this.onMainPaperViewTouch(event);
			break;
		case R.id.zoomedPaperFrame:
			this.onZoomedPaperFrame(event);
			break;
		default:
			return false;
		}

		return true;
	}

	/**
	 * This method is called, if the paper is moved. The method than updates the
	 * other views.
	 * 
	 * @param event
	 */
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

		// Update zoomed view
		this.updateZoomedPositionFromFrame();
	}

	/**
	 * Zoom frame is moved -> update zoomed view.
	 * 
	 * @param event
	 */
	public void onZoomedPaperFrame(MotionEvent event) {
		// Getting coordinate relative to frame view
		final int frameX = (int) event.getX();
		final int frameY = (int) event.getY();

		// Get layout parameters of the frame
		LayoutParams params = (LayoutParams) this.zoomedPaperFrame
				.getLayoutParams();

		// Compute coordinate relative to image view
		final int imageViewX = frameX + params.leftMargin - this.frameOffX;
		final int imageViewY = frameY + params.topMargin - this.frameOffY;

		params.width = (int) (this.zoomedPaperView.getDisplayedWidth() / this.mainPaperView
				.getScaleX());
		params.height = (int) (this.zoomedPaperView.getDisplayedHeight() / this.mainPaperView
				.getScaleY());
		this.zoomedPaperFrame.setLayoutParams(params);

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

		this.updateZoomedPositionFromFrame();
	}

	/**
	 * Shows the page adding dialog.
	 */
	private void addPage() {
		String[] items = { getString(R.string.papertype_lined),
				getString(R.string.papertype_squared),
				getString(R.string.papertype_blank) };

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

	/**
	 * Actually adds a new page to the paper.
	 * 
	 * @param type
	 *            Type of the new page
	 */
	private void addPage(PageFactory.PaperType type) {
		this.closePage();

		ProgressDialog dialog = ProgressDialog.show(this,
				getString(R.string.saving), getString(R.string.wait)
						+ "adding...", true);

		this.backgroundBitmap = PageFactory.getDINA4Page(type);
		this.foregroundBitmap = Bitmap.createBitmap(
				this.backgroundBitmap.getWidth(),
				this.backgroundBitmap.getHeight(), Bitmap.Config.ARGB_8888);
		this.foregroundCanvas = new Canvas(this.foregroundBitmap);
		this.foregroundCanvas.drawARGB(0, 0, 0, 0);

		this.currentPage = this.currentPaper.addNewPage(
				this.backgroundBitmap.getWidth(),
				this.backgroundBitmap.getHeight());

		Drawable[] layers = new Drawable[2];
		layers[0] = new BitmapDrawable(this.getResources(),
				this.backgroundBitmap);
		layers[1] = new BitmapDrawable(this.getResources(),
				this.foregroundBitmap);
		LayerDrawable layerDrawable = new LayerDrawable(layers);

		this.mainPaperView.setImageDrawable(layerDrawable);
		this.zoomedPaperView.setImageDrawable(layerDrawable);

		this.mainPaperView.setVisibility(View.VISIBLE);
		this.zoomedPaperView.setVisibility(View.VISIBLE);
		this.zoomedPaperFrame.setVisibility(View.VISIBLE);

		dialog.dismiss();
	}

	/**
	 * Closes the current page.
	 */
	private void closePage() {
		if (null == this.currentPage)
			return;

		ProgressDialog dialog = ProgressDialog.show(this,
				getString(R.string.saving), getString(R.string.wait), true);

		try {
			this.currentPage.setBackground(this.backgroundBitmap);
			this.currentPage.updateForeground(this.foregroundBitmap);
		} catch (Exception e) {
			e.printStackTrace();
		}

		dialog.dismiss();

		this.clearBitmaps();
	}

	/**
	 * Clear both foreground and background bitmaps.
	 */
	private void clearBitmaps() {
		if (null != this.foregroundBitmap)
			this.foregroundBitmap.recycle();
		this.foregroundBitmap = null;

		if (null != this.backgroundBitmap)
			this.backgroundBitmap.recycle();
		this.backgroundBitmap = null;
	}

	/**
	 * Updates the zoomed view according to the position of the frame.
	 */
	private void updateZoomedPositionFromFrame() {
		// Get layout parameters of the frame
		LayoutParams params = (LayoutParams) this.zoomedPaperFrame
				.getLayoutParams();

		// Compute upper left corner in image coordinates
		this.zoomedViewOffX = (int) (this.mainPaperView.getImagePosX() + params.leftMargin
				* this.mainPaperView.getScaleY());
		this.zoomedViewOffY = (int) (this.mainPaperView.getImagePosY() + params.topMargin
				* this.mainPaperView.getScaleY());

		this.zoomedPaperView.setImageScale(this.zoomRatio);
		this.zoomedPaperView.scrollTo(this.zoomedViewOffX, this.zoomedViewOffY);
	}

	/**
	 * DrawView has detect a new drawing -> save
	 * 
	 * @param points
	 *            Points, which have been drawn.
	 */
	private void saveDrawing(List<Point2d> points) {
		boolean first = true;
		float prevX = 0.f, prevY = 0.f, curX, curY;
		for (Point2d cur : points) {
			curX = this.zoomedViewOffX + (cur.x / this.zoomedPaperView.getImageScale());
			curY = this.zoomedViewOffY + (cur.y / this.zoomedPaperView.getImageScale());

			if (!first)
				this.foregroundCanvas.drawLine(prevX, prevY, curX, curY,
						this.paint);
			else
				first = false;

			prevX = curX;
			prevY = curY;
		}

		// Clear draw view itself
		this.drawView.clear();
	}
}
