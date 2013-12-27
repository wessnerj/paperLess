package de.meetr.hdr.paperless.model;

public class ColorModel {
	private int color;
	private String colorStr;
	
	public ColorModel(int c, String s) {
		this.color = c;
		this.colorStr = s;
	}
	
	public int getColor() {
		return this.color;
	}
	
	public String getName() {
		return this.colorStr;
	}
}
