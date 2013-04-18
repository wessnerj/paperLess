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
	
	public List<String> getSubFolderNames() throws Exception {
		return this.getSubFolderNames("");
	}
	
	public List<String> getSubFolderNames(String path) throws Exception {
		return this.getDirectoryContentList(path, true);
	}
	
	public List<String> getPaperNames() throws Exception {
		return this.getPaperNames("");
	}
	
	public List<String> getPaperNames(String path) throws Exception {
		return this.getDirectoryContentList(path, false);
	}
	
	private List<String> getDirectoryContentList(String path, boolean subFolders) throws Exception {
		String strDirPath = this.externalDirPath + "/";
		
		if (path.startsWith("/"))
			strDirPath += PAPER_DIR + path;
		else
			strDirPath += PAPER_DIR + "/" + path;
		
		File directory = new File(strDirPath);
		
		if (null == directory || !directory.exists() || !directory.canRead())
			throw new Exception("Directory not readable");
		
		List<String> items = new ArrayList<String>();
		
		for (final File f : directory.listFiles()) {
			if (f.toString().equals(".") || f.toString().equals(".."))
				continue;
			
			if (f.isDirectory() && subFolders) {
				items.add(f.getName());
			} else if (!f.isDirectory() && !subFolders && f.toString().endsWith(Paper.FILENAME_EXTENSION)) {
				items.add(f.getName());
			}	
		}
		
		return items;
	}
}
