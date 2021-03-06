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

/**
 * Container for a 2 coordinate point
 * 
 * @author Joseph Wessner
 */
public class Point2d {
	/**
	 * X coordinate
	 */
	public float x;
	/**
	 * Y coordinate
	 */
	public float y;
	
	/**
	 * Default constructor, creates a point at 0.0|0.0.
	 */
	public Point2d() {
		x = .0f;
		y = .0f;
	}
	
	/**
	 * Constructor, which sets point to x|y.
	 * 
	 * @param x		X coordinate of the new point
	 * @param y		Y coordinate of the new point
	 */
	public Point2d(float x, float y) {
		this.x = x;
		this.y = y;
	}
}
