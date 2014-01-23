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

package de.meetr.hdr.paperless.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Page {
	/**
	 * Maximal number of saved foreground images for history
	 */
	private static final int MAX_HISTORY = 8;
	
	private final Paper paper;
	private final long identifier;
	private final int width;
	private final int height;
	private int history;
	
	private Bitmap foreground = null;
	
	private final SQLiteDatabase db;
	
	public Page(Paper p, long id, int w, int h, int history, SQLiteDatabase db) {
		this.paper = p;
		this.identifier = id;
		this.width = w;
		this.height = h;
		this.history = history;
		this.db = db;
	}
	
	public long getIdentifier() {
		return this.identifier;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	/**
	 * Sets the background of the Paper. This methods saves the new
	 * background to file.
	 * 
	 * Note: height and width of the background has to match with the
	 * 		dimensions of the page.
	 * 
	 * @param b 			Bitmap of the background image
	 * @throws Exception	if any error occurs and saving failed
	 */
	public void setBackground(Bitmap b) throws Exception {
		// Check dimensions
		if (this.height != b.getHeight() || this.width != b.getWidth())
			throw new Exception("Dimension mismatch.");
		
		// Update db
		ContentValues values = new ContentValues(); 
		values.put(Paper.PAGE_FIELD_BACKGROUND , this.bitmapToBlob(b));
		
		// Which row to update, based on the ID
		String selection = Paper.PAGE_FIELD_ID + " = ?";
		String[] selectionArgs = { "" + this.identifier };

		if (1 != db.update(Paper.PAGE_TABLE, values, selection, selectionArgs)) {
			throw new Exception("Background save failed.");
		}
	}
	
	/**
	 * Gets the background image from file.
	 * 
	 * @return		Bitmap of the background image.
	 */
	public Bitmap getBackground() {
		final String[] fields = { Paper.PAGE_FIELD_BACKGROUND };
		final String where = Paper.PAGE_FIELD_ID + " = ?";
		final String[] whereArgs = { "" + this.identifier };
		
		// Query database for background blob
		Cursor c = db.query(Paper.PAGE_TABLE, fields, where, whereArgs, null, null, null, "1");
		c.moveToFirst();
		
		if (!c.isAfterLast()) {
			Bitmap b = this.blobToBitmap(c.getBlob(c.getColumnIndex(Paper.PAGE_FIELD_BACKGROUND)), false);
			c.close();
			return b;
		}
		
		c.close();
		return null;
	}
	
	/**
	 * Updates the foreground image and saves current one for history.
	 * 
	 * Note: height and width of the background has to match with the
	 * 		dimensions of the page.
	 * 
	 * @param b				Bitmap of the new foreground image
	 * @throws Exception	If any error during saving occurs
	 */
	public void updateForeground(Bitmap b) throws Exception {
		// Check dimensions
		if (this.height != b.getHeight() || this.width != b.getWidth())
			throw new Exception("Dimension mismatch.");
		
		// First let's save the current content
		this.saveCurrentForegroundToHistory();
		this.history++;
		
		this.setForeground(b);
	}
	
	/**
	 * Sets new foreground bitmap. Does NOT save the current one to history, use
	 * updateForeground instead.
	 * 
	 * Note: height and width of the background has to match with the
	 * 		dimensions of the page.
	 * 
	 * @param b				Bitmap of the new foreground image
	 * @throws Exception	If any error during saving occurs
	 */
	public void setForeground(Bitmap b) throws Exception {
		// Check dimensions
		if (this.height != b.getHeight() || this.width != b.getWidth())
			throw new Exception("Dimension mismatch.");
		
		// Set new foreground bitmap
		this.foreground = b;

		// Update db
		ContentValues values = new ContentValues();
		values.put(Paper.PAGE_FIELD_FOREGROUND,
				this.bitmapToBlob(this.foreground));
		values.put(Paper.PAGE_FIELD_CURRENT_HISTORY, this.history);

		// Which row to update, based on the ID
		String selection = Paper.PAGE_FIELD_ID + " = ?";
		String[] selectionArgs = { "" + this.identifier };

		if (1 != db.update(Paper.PAGE_TABLE, values, selection, selectionArgs)) {
			throw new Exception("Foreground save failed.");
		}
	}
	
	/**
	 * Gets the foreground image from file.
	 * 
	 * @return		Bitmap of the foreground image.
	 */
	public Bitmap getForeground() {
		final String[] fields = { Paper.PAGE_FIELD_FOREGROUND };
		final String where = Paper.PAGE_FIELD_ID + " = ?";
		final String[] whereArgs = { "" + this.identifier };
		
		// Query database for background blob
		Cursor c = db.query(Paper.PAGE_TABLE, fields, where, whereArgs, null, null, null, "1");
		c.moveToFirst();
		
		if (!c.isAfterLast()) {
			this.foreground = this.blobToBitmap(c.getBlob(c.getColumnIndex(Paper.PAGE_FIELD_FOREGROUND)), false);
		} else {
			this.foreground = null;
		}
		
		c.close();
		return this.foreground;
	}
	
	private byte[] bitmapToBlob(Bitmap b) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		b.compress(Bitmap.CompressFormat.PNG, 100, outStream);
		outStream.close(); // has no effect on ByteArrayOutputStream
		
		return outStream.toByteArray();
	}
	
	private Bitmap blobToBitmap(byte[] blob) {
		return blobToBitmap(blob, true);
	}
	
	private Bitmap blobToBitmap(byte[] blob, boolean foreground) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (foreground)
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		else
			options.inPreferredConfig = Bitmap.Config.RGB_565;
		
		return BitmapFactory.decodeByteArray(blob, 0, blob.length, options);
	}
	
	private void saveCurrentForegroundToHistory() throws IOException {
		// Delete all history items, which have greater or equal history number
		// or are too old (number <= this.history - MAX_HISTORY)
		String[] whereArgs = { "" + this.identifier, "" + this.history,
				"" + (this.history - MAX_HISTORY) };
		this.db.delete(Paper.HISTORY_FIELD_PAGE, Paper.HISTORY_FIELD_PAGE
				+ " = ? AND (" + Paper.HISTORY_FIELD_NUMBER + " >= ? OR "
				+ Paper.HISTORY_FIELD_NUMBER + " <= ?", whereArgs);
		
		// Add current foreground to history
		ContentValues values = new ContentValues();
		values.put(Paper.HISTORY_FIELD_PAGE, this.identifier);
		values.put(Paper.HISTORY_FIELD_NUMBER, this.history);
		if (null != this.foreground)
			values.put(Paper.HISTORY_FIELD_DATA, this.bitmapToBlob(this.foreground));
		this.db.insert(Paper.HISTORY_TABLE, null, values);
	}
}
