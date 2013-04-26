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

public class FileResource implements Comparable<FileResource> {
	private File file;
	
	public FileResource(File f) {
		this.file = f;
	}
	
	public String getName() {
		return this.file.getName();
	}
	
	public File getFile() {
		return this.file;
	}
	
	public boolean isSubDirectory() {
		return (this.file.isDirectory() && !this.file.getName().equals(".."));
	}
	
	public boolean isDocument() {
		return (!this.file.isDirectory() && this.file.getName().endsWith(Paper.FILENAME_EXTENSION));
	}

	@Override
	public int compareTo(FileResource another) {
		if (this.file.isDirectory()) {
			if (another.file.isDirectory())
				return this.file.getName().compareTo(another.file.getName());
			
			return -1;
		} else {
			if (another.file.isDirectory())
				return +1;
			
			return this.file.getName().compareTo(another.file.getName());
		}
	}
}
