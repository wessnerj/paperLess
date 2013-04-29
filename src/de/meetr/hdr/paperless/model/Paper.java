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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

public class Paper {
	public static final String FILENAME_EXTENSION = ".plf";
	
	private static final String XML_ROOT = "paperless";
	private static final String XML_CREATION = "creationDate";
	private static final String XML_MODIFIED = "lastModified";
	private static final String XML_NAME = "documentName";
	private static final String XML_NUMPAGES = "numberOfPages";
	private static final String XML_PAGES = "pages";
	
	private long creationDate;
	private long lastModified;
	private String documentName;
	private int numberOfPages;
	
	private List<Page> pages = new ArrayList<Page>();
	
	private File file;
	
	private boolean changed = false;
	
	public Paper(String documentName) {
		this.creationDate = this.lastModified = System.currentTimeMillis();
		this.documentName = documentName;
		this.numberOfPages = 0;
	}
	
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
	
	public Page addNewPage(int w, int h) {
		return this.addNewPage(w, h, this.numberOfPages+1);
	}
	
	public Page addNewPage(int w, int h, int pageNumber) {
		Page p = new Page(this, w, h);
		this.pages.add(p);
		
		return p;
	}
	
	public OutputStream getOutputStreamForPage(long identifier, int layer) throws Exception {		
		ZipOutputStream os = new ZipOutputStream(new FileOutputStream(this.file));
		os.putNextEntry(new ZipEntry(identifier + "_" + layer + ".png"));
		
		return os;
	}

	public void saveToFile(File f) throws ParserConfigurationException, IOException, TransformerException {
		// Build the meta file
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		// Root element
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(XML_ROOT);
		doc.appendChild(rootElement);
		
		// Meta-data
		// CreationDate
		Element creationDate = doc.createElement(XML_CREATION);
		creationDate.appendChild(doc.createTextNode("" + this.creationDate));
		rootElement.appendChild(creationDate);
		
		// LastModified (NOW)
		Element lastModified = doc.createElement(XML_MODIFIED);
		lastModified.appendChild(doc.createTextNode("" + System.currentTimeMillis()));
		rootElement.appendChild(lastModified);
		
		// DocumentName
		Element documentName = doc.createElement(XML_NAME);
		documentName.appendChild(doc.createTextNode(this.documentName));
		rootElement.appendChild(documentName);
		
		// NumberOfPages
		Element numberOfPages = doc.createElement(XML_NUMPAGES);
		numberOfPages.appendChild(doc.createTextNode("" + this.numberOfPages));
		rootElement.appendChild(numberOfPages);
		
		// Order of pages
		Element pages = doc.createElement(XML_PAGES);
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
		
		this.file = f;
	}
	
	public static Paper openFile(File f) throws Exception {
		ZipFile zipFile = new ZipFile(f);
		ZipEntry entry = zipFile.getEntry("meta.xml");
		InputStream is = zipFile.getInputStream(entry);
		
		String documentName = null;
		long creationDate = 0;
		long lastModified = 0;
		int numberOfPages = 0;
		
		XmlPullParser parser = Xml.newPullParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(is, null);
		parser.nextTag();
		
		parser.require(XmlPullParser.START_TAG, null, XML_ROOT);
		
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
			
			final String name = parser.getName();
			
			if (name.equals(XML_NAME)) {
				if (parser.next() == XmlPullParser.TEXT)
					documentName = parser.getText();
			} else if (name.equals(XML_CREATION)) {
				if (parser.next() == XmlPullParser.TEXT)
					creationDate = Long.parseLong(parser.getText());
			} else if (name.equals(XML_MODIFIED)) {
				if (parser.next() == XmlPullParser.TEXT)
					lastModified = Long.parseLong(parser.getText());
			} else if (name.equals(XML_NUMPAGES)) {
				if (parser.next() == XmlPullParser.TEXT)
					numberOfPages = Integer.parseInt(parser.getText());
			} else {
				skip(parser);
				continue;
			}
			
			parser.nextTag();
		}
		
		if (null == documentName)
			return null;
		
		Paper p = new Paper(documentName);
		p.creationDate = creationDate;
		p.lastModified = lastModified;
		p.numberOfPages = numberOfPages;
		p.file = f;
		
		return p;
	}
	
	private static void skip(XmlPullParser parser) throws Exception {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}
	
}
