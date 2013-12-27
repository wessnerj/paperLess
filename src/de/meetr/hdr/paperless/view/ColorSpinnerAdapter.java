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

package de.meetr.hdr.paperless.view;

import java.util.List;

import de.meetr.hdr.paperless.R;
import de.meetr.hdr.paperless.model.ColorModel;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ColorSpinnerAdapter extends ArrayAdapter<ColorModel> {
	private LayoutInflater inflater;
	private List<ColorModel> modelList;

	public ColorSpinnerAdapter(Context context, int textViewResourceId, List<ColorModel> objects) {
		super(context, textViewResourceId, objects);
		
		this.modelList = objects;

		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	// This funtion called for each row ( Called data.size() times )
	public View getCustomView(int position, View convertView, ViewGroup parent) {
		View row = inflater.inflate(R.layout.color_spinner_row, parent, false);
		TextView colorBox = (TextView)row.findViewById(R.id.color_box);
		TextView colorText = (TextView)row.findViewById(R.id.color_text);
		
		ColorModel m = this.modelList.get(position);
		colorBox.setBackgroundColor(m.getColor());
		colorText.setText(m.getName());
		
		return row;
	}

}
