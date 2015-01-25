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

package de.meetr.hdr.paperless.paper;

import de.meetr.hdr.paperless.model.Page;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class PageFactory {
	public enum PaperType {
		SQUARED, LINED, BLANK
	}
	
	/**
	 * Default resolution in ppi
	 */
	public static final int RESOLUTION = 100;
	/**
	 * Default width in pixel (209,97mm = DIN A4)
	 */
	public static final int DEFAULT_WIDTH = 823;
	/**
	 * Default height in pixel (297,01mm = DIN A4)
	 */
	public static final int DEFAULT_HEIGHT = 1169;
	/**
	 * Number of pixel per mm.
	 */
	public static final float MM_TO_PX = 3.937f;
	
	/**
	 * Singleton instance
	 */
	private static PageFactory instance = null;
	
	/**
	 * Paint used for painting
	 */
	private Paint paint = null;
	
	/**
	 * Private constructor to avoid calling from outside.
	 */
	private PageFactory() {
		this.paint = new Paint();
		this.paint.setColor(Color.GRAY);
		this.paint.setStrokeWidth(1);
		this.paint.setAntiAlias(true);
		this.paint.setStrokeCap(Paint.Cap.ROUND);
	}
	
	/**
	 * Getter for the singleton instance.
	 * 
	 * @return
	 */
	private static PageFactory getInstance() {
		if (null == instance) {
			instance = new PageFactory();
		}
		
		return instance;
	}
	
	public static Bitmap getDINA4Page() {
		return getDINA4Page(PaperType.BLANK);
	}
	
	public static Bitmap getDINA4Page(PaperType type) {
		return getInstance().getPage(type);
	}
	
	private Bitmap getPage(PaperType type) {
		Bitmap b = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT,
		        Bitmap.Config.RGB_565);
		
		Canvas c = new Canvas(b);		
		c.drawColor(Color.WHITE);
		
		final float offset = 50.f;
		
		if (PaperType.LINED == type) {
			// draw horizontal line every 1cm
			int counter = 1;
			float pos = 0.f;
			
			final float startX = offset;
			final float endX = DEFAULT_WIDTH - offset;
			
			do {
				// draw line every 1cm
				pos = counter * 10.f * MM_TO_PX + offset;
				c.drawLine(startX, pos, endX, pos, this.paint);
				counter++;
			} while (pos < DEFAULT_HEIGHT);
		} else if (PaperType.SQUARED == type) {
			// TODO
		}
		
		return b;
	}
}
