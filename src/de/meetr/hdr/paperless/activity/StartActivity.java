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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import de.meetr.hdr.paperless.R;
import de.meetr.hdr.paperless.model.FileManager;
import de.meetr.hdr.paperless.model.FileResource;
import de.meetr.hdr.paperless.model.Paper;
import de.meetr.hdr.paperless.view.FileListAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.FileA3D;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

public class StartActivity extends Activity implements OnItemClickListener {
	final Context context = this;
	private String currentPath = "/";
	
	private FileManager fileManager;
	private FileListAdapter listAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		
		this.fileManager = new FileManager(this);
		
		// Set Adapter for GridView
		this.updateListView();
		final GridView listview = (GridView) findViewById(R.id.gridView1);	
		
		// Add listener for gridview
		listview.setOnItemClickListener(this);
		
		// Add Context menu for gridview
		this.registerForContextMenu(listview);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.start_context, menu);
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
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		
		switch (item.getItemId()) {
		case R.id.delete:
			this.deleteItem(info.position);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	private void addFolder() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(R.string.add_folder);
		alert.setMessage(R.string.enter_folder_name);

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getEditableText().toString();
						
						if (fileManager.addFolder(currentPath + "/" + value)) {
							Toast.makeText(context, R.string.folder_add_success, Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(context, R.string.folder_add_failure, Toast.LENGTH_LONG).show();
						}
						
						updateListView();
					}
				});

		alert.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}
	
	private void addPaper() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(R.string.add_paper);
		alert.setMessage(R.string.enter_paper_name);

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getEditableText().toString();

						if (!saveNewPaper(value)) {
							Toast.makeText(context, R.string.paper_add_failure, Toast.LENGTH_LONG).show();
						}

						updateListView();
					}
				});

		alert.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}
	
	private boolean saveNewPaper(String name) {
		// Make new Paper object
		Paper newPaper = new Paper(name);
		
		// Test if file with that name already exists
		File f = this.fileManager.getFile(this.currentPath, name);
		if (f.exists()) {
			// File already exists -> abort
			return false;
		}
		
		// Save to file
		try {
			newPaper.saveToFile(f);
			return true;
		} catch (Exception e) {
			// Unexpected error -> return false
			return false;
		}
	}
	
	private void importPdf() {
		Toast.makeText(this, "Import PDF", Toast.LENGTH_SHORT).show();
	}
	
	private void deleteItem(int position) {
		final FileResource r = this.listAdapter.getItem(position);

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.delete);
		alert.setMessage(String.format(getString(R.string.delete_sure),
				r.getName()));

		alert.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						fileManager.deleteResource(r);
						
						updateListView();
					}
				});

		alert.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		FileResource r = this.listAdapter.getItem(position);
		
		if (r.isDocument())
			this.openPaper(r);
		
		Toast.makeText(context, "Clicked on: " + r.getName(), Toast.LENGTH_SHORT).show();
	}
	
	private void openPaper(FileResource r) {
		if (!r.isDocument())
			return;
		
		Intent myIntent = new Intent(this, EditorActivity.class);
		startActivity(myIntent);
	}
	
	private void updateListView() {
		final GridView listview = (GridView) findViewById(R.id.gridView1);	
		
		List<FileResource> items = new LinkedList<FileResource>();
		try {
			items = this.fileManager.getContents(this.currentPath);
		} catch (Exception e) {
			Toast.makeText(context, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
		}
		this.listAdapter = new FileListAdapter(this, items.toArray(new FileResource[0]));
		listview.setAdapter(this.listAdapter);
	}
}
