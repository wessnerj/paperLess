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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;

public class FileManager {
	private static final String PAPER_DIR = "papers";
	
//	private Context context;
	final private String externalDirPath;
	
	public FileManager(Context c) {
//		this.context = c;
		this.externalDirPath = c.getExternalFilesDir(null).toString();
		
		File f = new File(this.externalDirPath + "/" + PAPER_DIR);
		if (!f.exists()) {
			// try to create folder
			f.mkdirs();
		}
	}
	
	public boolean addFolder(String newDirPath) {
		File f = new File(this.externalDirPath + "/" + PAPER_DIR + "/" + newDirPath);
		if (!f.exists()) {
			// try to create folder
			return f.mkdirs();
		}
		
		return true;
	}
	
	public boolean deleteResource(FileResource f) {
		File file = f.getFile();
		this.deleteRecursivley(file);
		
		return !file.exists();
	}
	
	public List<FileResource> getContents(String path) throws Exception {
		String strDirPath = this.externalDirPath + "/";
		
		if (path.startsWith("/"))
			strDirPath += PAPER_DIR + path;
		else
			strDirPath += PAPER_DIR + "/" + path;
		
		File directory = new File(strDirPath);
		
		if (null == directory || !directory.exists() || !directory.canRead())
			throw new Exception("Directory not readable");
		
		List<FileResource> items = new ArrayList<FileResource>();
		for (final File f : directory.listFiles()) {
			if (f.getName().startsWith(".") && !f.getName().equals(".."))
				continue;
			
			if (f.isDirectory()) {
				items.add(new FileResource(f));
			} else if (f.getName().endsWith(Paper.FILENAME_EXTENSION)) {
				items.add(new FileResource(f));
			}
		}
		
		Collections.sort(items);
		return items;
	}
	
	private void deleteRecursivley(File f) {
		if (f.isDirectory())
	        for (final File child : f.listFiles())
	        	deleteRecursivley(child);

	    f.delete();
	}
}
