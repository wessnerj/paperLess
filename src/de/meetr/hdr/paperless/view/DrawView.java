/**
 * paperLess - Android App for taking notes in PDFs
 * Copyright (C) 2014 Joseph Wessner
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

/**
 * This class is based on SignatureView
 * (http://corner.squareup.com/2010/07/smooth-signatures.html)
 */

package de.meetr.hdr.paperless.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import de.meetr.hdr.paperless.model.Point2d;

/**
 * View for drawing on the screen.
 * 
 * @author Joseph Wessner
 */
public class DrawView extends View {
	/**
	 * Paint used for drawing.
	 */
	private Paint paint = null;
	
	/**
	 * Need to track this so the dirty region can accommodate the stroke.
	 */
	private float halfStrokeWidth = 0.f;
	
	/**
	 * Keep track of the points to draw.
	 */
	private List<Point2d> pointList = new ArrayList<Point2d>();
	
	/**
	 * List of listeners, which are waiting for draw done event.
	 */
	private List<OnDrawDoneListener> doneListener = new ArrayList<OnDrawDoneListener>();

	/**
	 * Optimizes painting by invalidating the smallest possible area.
	 */
	private float lastTouchX;
	private float lastTouchY;
	private final RectF dirtyRect = new RectF();
	
	/**
	 * Default constructor, takes the context and attribute set as arguments.
	 * 
	 * @param context
	 * @param attrs
	 */
	public DrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * Default constructor, takes the context, attribute set and defStyle as arguments.
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public DrawView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Setter for the paint, used to draw on the view.
	 * 
	 * @param p		New painter, which should be used
	 */
	public void setPaint(Paint p) {
		this.paint = p;
		halfStrokeWidth = p.getStrokeWidth() / 2;
	}

	/**
	 * Clears the view and start over again.
	 */
	public void clear() {
		this.pointList.clear();

		// Repaints the entire view.
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (null == paint)
			return;

		Point2d prev = null;
		for (Point2d cur: this.pointList) {
			if (null == prev) {
				prev = cur;
				continue;
			}
			canvas.drawLine(prev.x, prev.y, cur.x, cur.y, paint);
			prev = cur;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float eventX = event.getX();
		float eventY = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			this.pointList.clear();
			this.pointList.add(new Point2d(eventX, eventY));
			this.lastTouchX = eventX;
			this.lastTouchY = eventY;

			// There is no end point yet, so don't waste cycles invalidating.
			return true;
		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_UP:
			// Start tracking the dirty region.
			resetDirtyRect(eventX, eventY);

			// When the hardware tracks events faster than they are delivered,
			// the event will contain a history of those skipped points.
			final int historySize = event.getHistorySize();
			for (int i = 0; i < historySize; i++) {
				final float historicalX = event.getHistoricalX(i);
				final float historicalY = event.getHistoricalY(i);
				expandDirtyRect(historicalX, historicalY);
				this.pointList.add(new Point2d(historicalX, historicalY));
			}

			// After replaying history, connect the line to the touch point.
			this.pointList.add(new Point2d(eventX, eventY));
			break;
		default:
			Log.d("default", "Ignored touch event: " + event.toString());
			return false;
		}

		// Include half the stroke width to avoid clipping.
		invalidate((int) (dirtyRect.left - halfStrokeWidth),
				(int) (dirtyRect.top - halfStrokeWidth),
				(int) (dirtyRect.right + halfStrokeWidth),
				(int) (dirtyRect.bottom + halfStrokeWidth));
		lastTouchX = eventX;
		lastTouchY = eventY;
		
		if (event.getAction() == MotionEvent.ACTION_UP) {
			this.drawDone();
			this.pointList.clear();
		}

		return true;
	}
	
	/**
	 * Adds a listener for on draw done event.
	 * 
	 * @param l		Listener to add
	 */
	public void setOnDrawDoneListener(OnDrawDoneListener l) {
		this.doneListener.add(l);
	}
	
	/**
	 * Call all onDrawDone listeners.
	 */
	private void drawDone() {
		for (final OnDrawDoneListener l : this.doneListener) {
			if (l.onDrawDone(this.pointList)) {
				return; // Stop, when the fist listener returns true
			}
		}
	}

	/**
	 * Resets the dirty region when the motion event occurs.
	 */
	private void resetDirtyRect(float eventX, float eventY) {
		// The lastTouchX and lastTouchY were set when the ACTION_DOWN
		// motion event occurred.
		dirtyRect.left = Math.min(lastTouchX, eventX);
		dirtyRect.right = Math.max(lastTouchX, eventX);
		dirtyRect.top = Math.min(lastTouchY, eventY);
		dirtyRect.bottom = Math.max(lastTouchY, eventY);
	}

	/**
	 * Called when replaying history to ensure the dirty region includes all
	 * points.
	 */
	private void expandDirtyRect(float historicalX, float historicalY) {
		if (historicalX < dirtyRect.left) {
			dirtyRect.left = historicalX;
		} else if (historicalX > dirtyRect.right) {
			dirtyRect.right = historicalX;
		}
		if (historicalY < dirtyRect.top) {
			dirtyRect.top = historicalY;
		} else if (historicalY > dirtyRect.bottom) {
			dirtyRect.bottom = historicalY;
		}
	}
}
