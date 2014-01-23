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

import java.io.File;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Paper {
	public static final String FILENAME_EXTENSION = ".plf";
	
	private static final String META_TABLE = "meta";
	private static final String META_FIELD_KEY = "key";
	private static final String META_FIELD_VALUE = "value";
	
	private static final String META_KEY_NAME = "documentName";
	private static final String META_KEY_NUMPAGES = "numberOfPages";
	private static final String META_KEY_CREATED = "created";
	private static final String META_KEY_MODIFIED = "modified";
	
	public static final String PAGE_TABLE = "pages";
	public static final String PAGE_FIELD_ID = "_id";
	public static final String PAGE_FIELD_NUMBER = "pageNumber";
	public static final String PAGE_FIELD_CURRENT_HISTORY = "curHistory";
	public static final String PAGE_FIELD_WIDTH = "width";
	public static final String PAGE_FIELD_HEIGHT = "height";
	public static final String PAGE_FIELD_BACKGROUND = "background";
	public static final String PAGE_FIELD_FOREGROUND = "foreground";
	
	public static final String HISTORY_TABLE = "history";
	public static final String HISTORY_FIELD_ID = "_id";
	public static final String HISTORY_FIELD_PAGE = "page_id";
	public static final String HISTORY_FIELD_NUMBER = "number";
	public static final String HISTORY_FIELD_DATA = "data";
	
	/**
	 * Sqlite database where the data is stored.
	 */
	private SQLiteDatabase db = null;
	
	/**
	 * Constructor, which takes the SQLite database as first argument.
	 * 
	 * @param db SQLiter database of this paper
	 */
	public Paper(SQLiteDatabase db) {
		this.db = db;
	}
	
	/**
	 * Closes the opened file
	 */
	public void close() {
		if (null != this.db)
			this.db.close();
	}
	
	/**
	 * Get the number of pages.
	 * 
	 * @return
	 */
	public int getNumberOfPages() {
		final String numberStr = this.getMetaInformation(META_KEY_NUMPAGES);
		
		if (null == numberStr)
			return 0;
		
		try {
			return Integer.parseInt(numberStr);
		}
		catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * Get the value for specified key from the meta info table.
	 * 
	 * @param key
	 * @return value for key
	 */
	private String getMetaInformation(String key) {
		final String[] fields = { META_FIELD_VALUE };
		final String where = META_FIELD_KEY + " = ?";
		final String[] whereArgs = { key };
		
		Cursor c = db.query(META_TABLE, fields, where, whereArgs, null, null, null, "1");
		c.moveToFirst();
		
		if (!c.isAfterLast()) {
			return c.getString(c.getColumnIndex(META_FIELD_VALUE));
		}
		
		c.close();
		return null;
	}
	
	/**
	 * Sets a meta information for this paper.
	 * 
	 * @param key		Which meta information should be set?
	 * @param value		Value of given meta information.
	 */
	private void setMetaInformation(String key, String value) {
		// Delete old information (if any)
		this.delMetaInformation(key);
		
		ContentValues values = new ContentValues(); 
		values.put(META_FIELD_KEY, key);
		values.put(META_FIELD_VALUE, value);
		this.db.insert(META_TABLE, null, values);
	}
	
	/**
	 * Deletes a specific meta information from the paper.
	 * 
	 * @param key		Meta information, which should be deleted
	 */
	private void delMetaInformation(String key) {
		final String where = META_FIELD_KEY + "=?";
		final String[] whereArgs = { key };
		this.db.delete(META_TABLE, where, whereArgs);
	}
	
	/**
	 * Get a page by page number.
	 * 
	 * @param pageNumber	Number of the page to get
	 * @return				Page with given number
	 */
	public Page getPage(int pageNumber) {
		final String[] fields = { PAGE_FIELD_ID, PAGE_FIELD_NUMBER, PAGE_FIELD_CURRENT_HISTORY, PAGE_FIELD_WIDTH, PAGE_FIELD_HEIGHT};
		final String where = PAGE_FIELD_NUMBER + "=?";
		final String[] whereArgs = { ""+pageNumber };
		
		Cursor c = db.query(PAGE_TABLE, fields, where, whereArgs, null, null, null, "1");
		c.moveToFirst();
		
		if (c.isAfterLast())
			return null;
		
		final long id = c.getLong(c.getColumnIndex(PAGE_FIELD_ID));
		final int history = c.getInt(c.getColumnIndex(PAGE_FIELD_CURRENT_HISTORY));
		final int w = c.getInt(c.getColumnIndex(PAGE_FIELD_WIDTH));
		final int h = c.getInt(c.getColumnIndex(PAGE_FIELD_HEIGHT));
		c.close();
		
		Page p = new Page(this, id, w, h, history, this.db);
		return p;
	}
	
	/**
	 * Add a new page at the end of the paper.
	 * 
	 * @param w			Width of the new page [px]
	 * @param h			Height of the new page [px]
	 * @return			Newly created page
	 */
	public Page addNewPage(int w, int h) {
		final int currentNumberOfPages = this.getNumberOfPages();
		if (0 > currentNumberOfPages)
			return null;
		
		return this.addNewPage(w, h, currentNumberOfPages+1);
	}
	
	/**
	 * Add a new page at a specific page number.
	 * 
	 * @param w				Width of the new page [px]
	 * @param h				Height of the new page [px]
	 * @param pageNumber	Page number of the new page.
	 * @return				Newly created page
	 */
	public Page addNewPage(int w, int h, int pageNumber) {
		// Add new page to database
		// First check if pageNumber already exists
		if (null != this.getPage(pageNumber)) {
			// pageNumber already exists
			// Shift all pages, starting with pageNumber one number higher
			this.db.execSQL(
					"UPDATE `" + PAGE_TABLE + "` "
					+ "SET `" + PAGE_FIELD_NUMBER + "` = `" + PAGE_FIELD_NUMBER + "`+1 "
					+ "WHERE `" + PAGE_FIELD_NUMBER + "` >= '" + pageNumber + "'");
		}
		
		// Add new page to page table
		ContentValues values = new ContentValues(); 
		values.put(PAGE_FIELD_NUMBER, pageNumber);
		values.put(PAGE_FIELD_WIDTH, w);
		values.put(PAGE_FIELD_HEIGHT, h);
		values.put(PAGE_FIELD_CURRENT_HISTORY, 0);
		
		// Insert
		final long id = db.insert(PAGE_TABLE, null, values);
		
		if (0 > id)
			return null; // Something went wrong
		
		// Increment the page count by one
		int numPages = this.getNumberOfPages();
		this.setMetaInformation(META_KEY_NUMPAGES, ""+(numPages+1));
		
		// Update modified date
		this.setMetaInformation(META_KEY_MODIFIED, ""+System.currentTimeMillis());
		
		return new Page(this, id, w, h, 0, this.db);
	}
	
	/**
	 * Created a new paperLess file
	 * 
	 * @param f File, where to save
	 * @param documentName name of the new document
	 * @return
	 */
	public static Paper createNewPaperFile(File f, String documentName) {
		SQLiteDatabase db = null;
		
		try {
			db = SQLiteDatabase.openOrCreateDatabase(f.getAbsolutePath(), null);
		} catch (Exception e) {
			return null;
		}
		
		if (null == db)
			return null;

		if (!createEmptyDatabase(db, documentName))
			return null;
		
		return new Paper(db);
	}
	
	/**
	 * Open a existing plf file
	 * @param f File to open
	 * @return
	 */
	public static Paper openFile(File f) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(f.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
		
		if (null == db)
			return null;
		
		return new Paper(db);
	}
	
	/**
	 * Creates a empty database for a new document file.
	 * 
	 * @param db
	 * @param documentName
	 * @return
	 */
	private static boolean createEmptyDatabase(SQLiteDatabase db, String documentName) {
		try {
			/*****************
			 * Create tables *
			 *****************/
			// Meta table
			db.execSQL("CREATE TABLE " + META_TABLE + "("
					+ META_FIELD_KEY + " TEXT PRIMARY KEY,"
					+ META_FIELD_VALUE + " TEXT)");
						
			// Pages table
			db.execSQL("CREATE TABLE " + PAGE_TABLE + "("
					+ PAGE_FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ PAGE_FIELD_NUMBER + " INTEGER UNIQUE,"
					+ PAGE_FIELD_CURRENT_HISTORY + " INTEGER,"
					+ PAGE_FIELD_WIDTH + " INTEGER,"
					+ PAGE_FIELD_HEIGHT + " INTEGER,"
					+ PAGE_FIELD_BACKGROUND + " BLOB,"
					+ PAGE_FIELD_FOREGROUND + " BLOB"
					+ ")");
			
			// History table
			db.execSQL("CREATE TABLE " + HISTORY_TABLE + "("
					+ HISTORY_FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ HISTORY_FIELD_PAGE + " INTEGER,"
					+ HISTORY_FIELD_NUMBER + " INTEGER,"
					+ HISTORY_FIELD_DATA + " BLOB"
					+ ")");
			
			/***************************
			 * Store default meta data *
			 ***************************/
			ContentValues values = new ContentValues(); 
			values.put(META_FIELD_KEY, META_KEY_CREATED);
			values.put(META_FIELD_VALUE, "" + System.currentTimeMillis());
			if (0 > db.insert(META_TABLE, null, values))
				return false;
			
			values = new ContentValues(); 
			values.put(META_FIELD_KEY, META_KEY_MODIFIED);
			values.put(META_FIELD_VALUE, "" + System.currentTimeMillis());
			if (0 > db.insert(META_TABLE, null, values))
				return false;
			
			values = new ContentValues(); 
			values.put(META_FIELD_KEY, META_KEY_NUMPAGES);
			values.put(META_FIELD_VALUE, "0");
			if (0 > db.insert(META_TABLE, null, values))
				return false;
			
			values = new ContentValues(); 
			values.put(META_FIELD_KEY, META_KEY_NAME);
			values.put(META_FIELD_VALUE, documentName);
			if (0 > db.insert(META_TABLE, null, values))
				return false;
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
}
