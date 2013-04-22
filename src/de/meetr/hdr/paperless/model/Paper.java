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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Paper {
	public static final String FILENAME_EXTENSION = ".plf";
	
	private long creationDate;
	private long lastModified;
	private String documentName;
	private int numberOfPages;
	
	private boolean changed = false;
	
	public long getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
		this.changed = true;
	}
	public long getLastModified() {
		return lastModified;
	}
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
		this.changed = true;
	}
	public String getDocumentName() {
		return documentName;
	}
	public void setDocumentName(String documentName) {
		this.documentName = documentName;
		this.changed = true;
	}
	public int getNumberOfPages() {
		return numberOfPages;
	}
	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
		this.changed = true;
	}

	public void saveToFile(File f) throws ParserConfigurationException, IOException, TransformerException {
		// Build the meta file
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		// Root element
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("paperless");
		doc.appendChild(rootElement);
		
		// Meta-data
		// CreationDate
		Element creationDate = doc.createElement("creationDate");
		creationDate.appendChild(doc.createTextNode("" + this.creationDate));
		rootElement.appendChild(creationDate);
		
		// LastModified (NOW)
		Element lastModified = doc.createElement("lastModified");
		lastModified.appendChild(doc.createTextNode("" + System.currentTimeMillis()));
		rootElement.appendChild(lastModified);
		
		// DocumentName
		Element documentName = doc.createElement("documentName");
		documentName.appendChild(doc.createTextNode(this.documentName));
		rootElement.appendChild(documentName);
		
		// NumberOfPages
		Element numberOfPages = doc.createElement("numberOfPages");
		numberOfPages.appendChild(doc.createTextNode("" + this.numberOfPages));
		rootElement.appendChild(numberOfPages);
		
		// Order of pages
		Element pages = doc.createElement("pages");
		// TODO: add pages
		rootElement.appendChild(pages);
		
		// Write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		
		ZipOutputStream os = new ZipOutputStream(new FileOutputStream(f));
		os.putNextEntry(new ZipEntry("meta.xml"));
		
		StreamResult result =  new StreamResult(os);
		transformer.transform(source, result);
		
		os.close();
	}
	
	public static Paper openFile(File f) throws Exception {
		ZipFile zipFile = new ZipFile(f);
		ZipEntry entry = zipFile.getEntry("meta.xml");
		InputStream is = zipFile.getInputStream(entry);
		
		// TODO: ..
		
		return null;
	}
}
