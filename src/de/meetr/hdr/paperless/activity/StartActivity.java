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

package de.meetr.hdr.paperless.activity;

import java.util.List;

import de.meetr.hdr.paperless.R;
import de.meetr.hdr.paperless.model.FileManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class StartActivity extends Activity {
	final Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		
		FileManager fm = new FileManager(this);
		
		try {
			List<String> files = fm.getPaperNames();
			Log.d("NumberOfFiles", "" + files.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Log.d("PATH", getExternalFilesDir(null).toString());
	} 
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.start, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_folder:
			this.addFolder();
			return true;
		case R.id.action_add_paper:
			this.addPaper();
			return true;
		case R.id.import_pdf:
			this.importPdf();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void addFolder() {
		Toast.makeText(this, "Add Folder", Toast.LENGTH_SHORT).show();
	}
	
	private void addPaper() {
		Toast.makeText(this, "Add Paper", Toast.LENGTH_SHORT).show();
	}
	
	private void importPdf() {
		Toast.makeText(this, "Import PDF", Toast.LENGTH_SHORT).show();
	}
}
