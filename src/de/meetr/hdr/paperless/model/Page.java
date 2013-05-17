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
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

import android.graphics.Bitmap;

public class Page {
	private Paper paper;
	private long identifier;
	private int width;
	private int height;
	
	public Page(Paper p, int w, int h) {
		this.paper = p;
		this.identifier = System.currentTimeMillis();
		this.width = w;
		this.height = h;
	}
	
	public Page(Paper p, long id, int w, int h) {
		this.paper = p;
		this.identifier = id;
		this.width = w;
		this.height = h;
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
	
	public void saveLayer(Bitmap b, int layer) throws Exception {
		// Get outStream in-memory
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		b.compress(Bitmap.CompressFormat.PNG, 100, outStream);
		
		outStream.close(); // has no effect on ByteArrayOutputStream
		
		this.paper.savePage(100, layer, outStream.toByteArray());
	}
}
