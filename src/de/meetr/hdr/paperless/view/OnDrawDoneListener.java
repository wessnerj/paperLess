/**
 * paperLess - Android App for taking notes in PDFs
 * Copyright (C) 2014 Joseph Wessner
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

package de.meetr.hdr.paperless.view;

import java.util.List;
import de.meetr.hdr.paperless.model.Point2d;

/**
 * Interface, which is called by the draw view, when a user drawing is done.
 * 
 * @author Joseph Wessner
 */
public interface OnDrawDoneListener {
	/**
	 * This function is called, when the user input drawing is done.
	 * 
	 * @param points	List of drawn points
	 * @return			True if the listener has consumed the event, false otherwise.
	 */
	public abstract boolean onDrawDone(List<Point2d> points);
}
