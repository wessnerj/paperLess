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

import de.meetr.hdr.paperless.R;
import de.meetr.hdr.paperless.model.FileResource;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListAdapter extends ArrayAdapter<FileResource> {
	private final Activity context;
	private final FileResource[] values;

	static class ViewHolder {
		public TextView text;
		public ImageView image;
	}

	public FileListAdapter(Activity context, FileResource[] values) {
		super(context, R.layout.start_rowlayout, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.start_rowlayout, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) rowView.findViewById(R.id.textView1);
			viewHolder.image = (ImageView) rowView.findViewById(R.id.imageView1);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		
		FileResource r = values[position];
		holder.text.setText(r.getName());
		
		// Change icon
		if (r.isSubDirectory())
			holder.image.setImageResource(R.drawable.icon_folder);
		else
			holder.image.setImageResource(R.drawable.icon_document);

		return rowView;
	}
}
