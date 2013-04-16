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

package de.meetr.hdr.paperless.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class BitmapView extends ImageView {
	private Matrix scaleMatrix = new Matrix();;
	
	private float scaleFactor = 1.f;
	
	private int upperLeftX = 0;
	private int upperLeftY = 0;
	
	public BitmapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.setScaleType(ScaleType.MATRIX);
	}
	
	public boolean setImageScale(float s) {
//		if ((this.getWidth() * s) > this.imageWidth && (this.getHeight() * s) > this.imageHeight)
//			return false;
		
		this.scaleFactor = s;
		this.resetMatrix();
		
		return true;
	}
	
	public float getImageScale() {
		return this.scaleFactor;
	}
	
	public boolean setImagePos(int x, int y) {
		this.upperLeftX = x;
		this.upperLeftY = y;
		
		this.resetMatrix();
		
		return true;
	}
	
	public int getImagePosX() {
		return this.upperLeftX;
	}
	
	public int getImagePosY() {
		return this.upperLeftY;
	}
	
	public int getImageForViewPosX(int x) {
		return (int) ((this.scaleFactor * x) + this.upperLeftX);
	}
	
	public int getImageForViewPosY(int y) {
		return (int) ((this.scaleFactor * y) + this.upperLeftY);
	}
	
	public int getDisplayedWidth() {
		return (int) (this.getWidth() / this.scaleFactor);
	}
	
	public int getDisplayedHeight() {
		return (int) (this.getHeight() / this.scaleFactor);
	}
	
	public void scrollTo(int x, int y) {
//		super.scrollTo(x, y);
		
		this.setImagePos(x, y);
	}
	
	public void scrollBy(int x, int y) {
		this.scrollTo(this.upperLeftX + x, this.upperLeftY + y);
	}
	
	private void resetMatrix() {
		this.scaleMatrix.reset();
		
		final int viewWidth = this.getWidth();
		final int viewHeight = this.getHeight();
		
		RectF srcRect = new RectF(
				(float) this.upperLeftX, 
				(float) this.upperLeftY, 
				this.upperLeftX + viewWidth / this.scaleFactor,
				this.upperLeftY + viewHeight / this.scaleFactor
			);
		RectF destRect = new RectF(0, 0, (float) viewWidth, (float) viewHeight);
		
		Log.d("viewWidth", "" + viewWidth);
		Log.d("scale", "" + this.scaleFactor);
		Log.d("From", "(" + srcRect.left + "|" + srcRect.top + ") (" + srcRect.right + "|" + srcRect.bottom + ")");
		Log.d("To", "(" + destRect.left + "|" + destRect.top + ") (" + destRect.right + "|" + destRect.bottom + ")");
		
		this.scaleMatrix.setRectToRect(srcRect, destRect, Matrix.ScaleToFit.FILL);
		this.setImageMatrix(this.scaleMatrix);
	}
}
