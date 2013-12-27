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
	
	public FileManager(Context c) throws Exception {
//		this.context = c;
		final File dataDirectory = c.getExternalFilesDir(null);
		
		if (null == dataDirectory) {
			throw new Exception("No external space available");
		}
		
		this.externalDirPath = dataDirectory.toString();
		
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
	
	public File getFile(String path, String filename) {
		return this.getFile(path, filename, true);
	}
	
	public File getFile(String path, String filename, boolean escape) {
		File dir = new File(this.externalDirPath + "/" + PAPER_DIR + "/" + path);
		
		if (!dir.exists())
			return null;
		
		if (escape)
			filename = filename.replaceAll("[^a-zA-Z0-9_]+","");
		
		filename += Paper.FILENAME_EXTENSION;
		
		return new File(dir.getAbsolutePath() + "/" + filename);
	}
	
	public boolean deleteResource(FileResource f) {
		File file = f.getFile();
		this.deleteRecursivley(file);
		
		return !file.exists();
	}
	
	public String changeDirectory(FileResource f) {
		final File dir = f.getFile();
		
		if (!dir.exists() || !dir.isDirectory())
			return null;
		
		String dirPath = dir.getAbsolutePath();
		if (!dirPath.endsWith("/"))
			dirPath += "/";
		
		final String paperRootPath = this.externalDirPath + "/" + PAPER_DIR;
		if (!dirPath.startsWith(paperRootPath))
			return null;
		
		return dirPath.substring(paperRootPath.length());
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
		if (!directory.getAbsolutePath().equals(this.externalDirPath + "/" + PAPER_DIR)) {
			items.add(new FileResource(directory.getParentFile(), ".."));
		}
		
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
