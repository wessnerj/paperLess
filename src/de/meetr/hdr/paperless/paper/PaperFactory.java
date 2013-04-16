package de.meetr.hdr.paperless.paper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class PaperFactory {
	public enum PaperType {
		SQUARED, LINED, BLANK
	}
	
	/**
	 * Default resolution in ppi
	 */
	public static final int RESOLUTION = 150;
	/**
	 * Default width in pixel (209,97mm = DIN A4)
	 */
	public static final int DEFAULT_WIDTH = 1240;
	/**
	 * Default height in pixel (297,01mm = DIN A4)
	 */
	public static final int DEFAULT_HEIGHT = 1754;
	/**
	 * Number of pixel per mm.
	 */
	public static final float MM_TO_PX = 5.9055f;
	
	/**
	 * Singleton instance
	 */
	private static PaperFactory instance = null;
	
	/**
	 * Paint used for painting
	 */
	private Paint paint = null;
	
	/**
	 * Private constructor to avoid calling from outside.
	 */
	private PaperFactory() {
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
	private static PaperFactory getInstance() {
		if (null == instance) {
			instance = new PaperFactory();
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
