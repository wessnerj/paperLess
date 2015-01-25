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

import com.artifex.mupdfdemo.MuPDFCore;

import de.meetr.hdr.paperless.R;
import de.meetr.hdr.paperless.misc.IntentHelper;
import de.meetr.hdr.paperless.model.FileManager;
import de.meetr.hdr.paperless.model.FileResource;
import de.meetr.hdr.paperless.model.Page;
import de.meetr.hdr.paperless.model.Paper;
import de.meetr.hdr.paperless.paper.PageFactory;
import de.meetr.hdr.paperless.view.FileListAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

/**
 * Startup Activity (Filebrowser)
 * 
 * @author Joseph Wessner <joseph@wessner.org>
 */
public class StartActivity extends Activity implements OnItemClickListener {
	private static final int REQUEST_FILE_CHOOSER = 7754;
	
	/**
	 * Final reference to this, can be used in subclasses.
	 */
	final Context context = this;
	private String currentPath = "/";
	
	/**
	 * FileManager is used for file access.
	 */
	private FileManager fileManager = null;
	/**
	 * Adapter for file listView.
	 */
	private FileListAdapter listAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Call parent onCreate and set layout
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		
		// Init fileManager
		try {
			this.fileManager = new FileManager(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
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
	
	/**
	 * This method will ask the user for a folder name and afterwards create the
	 * new folder.
	 */
	private void addFolder() {
		// Setup AlertDialog for name request
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.add_folder);
		alert.setMessage(R.string.enter_folder_name);

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		// Add action for "OK" button
		alert.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Get the value from input
						final String value = input.getEditableText().toString();
						
						// Try to add new folder
						if (fileManager.addFolder(currentPath + "/" + value)) {
							// Display success message
							Toast.makeText(context, R.string.folder_add_success, Toast.LENGTH_SHORT).show();
						} else {
							// Display error message
							Toast.makeText(context, R.string.folder_add_failure, Toast.LENGTH_LONG).show();
						}
						
						// Update the listView (display new folder)
						updateListView();
					}
				});

		// Add action for "Cancel" button
		alert.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled -> do nothing
					}
				});

		// Display the dialog
		alert.show();
	}
	
	/**
	 * This method will ask the user for a paper name and afterwards create the
	 * new paper.
	 */
	private void addPaper() {
		// Setup AlertDialog for name request
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.add_paper);
		alert.setMessage(R.string.enter_paper_name);

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		// Add action for "OK" button
		alert.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Get the value from input
						final String value = input.getEditableText().toString();

						if (!saveNewPaper(value)) {
							// Display error message
							Toast.makeText(context, R.string.paper_add_failure, Toast.LENGTH_LONG).show();
						}

						// Update the listView (display new paper)
						updateListView();
						
						// TODO: switch to editorActivity
					}
				});

		// Add action for "Cancel" button
		alert.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled -> do nothing
					}
				});
		
		// Display the dialog
		alert.show();
	}
	
	/**
	 * Saves a new paper with given name.
	 * 
	 * @param name	documentName of the new paper
	 * @return		true, if the paper could be created.
	 */
	private boolean saveNewPaper(String name) {	
		// Test if file with that name already exists
		File f = this.fileManager.getFile(this.currentPath, name);
		if (f.exists()) {
			// File already exists -> abort
			return false;
		}
		
		// Make new Paper object
		Paper newPaper = Paper.createNewPaperFile(f, name);
		
		if (null == newPaper)
			return false;
		
		// Close the new Paper as we do not open it immediately
		newPaper.close();
		return true;
	}
	
	/**
	 * Import a PDF file as new paper.
	 * TODO: implement
	 */
	private void importPdf() {
		Toast.makeText(this, "Import PDF", Toast.LENGTH_SHORT).show();
		
		// Create the ACTION_GET_CONTENT Intent
		final Intent getContentIntent = new Intent(Intent.ACTION_GET_CONTENT);
		// The MIME data type filter
		getContentIntent.setType("application/pdf");
        // Only return URIs that can be opened with ContentResolver
        getContentIntent.addCategory(Intent.CATEGORY_OPENABLE);

	    Intent intent = Intent.createChooser(getContentIntent, "Select a file");
	    startActivityForResult(intent, REQUEST_FILE_CHOOSER);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_FILE_CHOOSER:
			if (resultCode == RESULT_OK) {

				final Uri uri = data.getData();

				// Get the File path from the Uri
//				String path = FileUtils.getPath(this, uri);
				String path = getRealPathFromURI(this, uri);
				
				Toast.makeText(this, "Selected file: " + path, Toast.LENGTH_LONG).show();
				
				MuPDFCore mc = null;
				try {
					mc = new MuPDFCore(this, path);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// Get basename
				int idx = path.lastIndexOf("/");
				String filename = (-1 == idx)? path: path.substring(idx+1);
				idx = filename.lastIndexOf(".");
				String basename = (-1 == idx)? filename: filename.substring(0, idx);
				
				// Save new paper
				saveNewPaper(basename);
				
				// Get paper
				FileResource f = new FileResource(this.fileManager.getFile(this.currentPath, basename));
				Paper p = f.getPaper();
				
				int nPages = mc.countPages();
				for (int i = 0; i < nPages; i++) {
					PointF pageSize = mc.getPageSize(i);
					float width = pageSize.x;
					float height = pageSize.y;
					
					while (width > 1200 || height > 1200) {
						width /= 2.f;
						height /= 2.f;
					}
					
					Bitmap backgroundBitmap = mc.drawPage(i, (int) width, (int) height, 0, 0, (int) pageSize.x, (int) pageSize.y);
					Bitmap foregroundBitmap = Bitmap.createBitmap(
							backgroundBitmap.getWidth(),
							backgroundBitmap.getHeight(), Bitmap.Config.ARGB_8888);
					
					Page page = p.addNewPage(
							backgroundBitmap.getWidth(),
							backgroundBitmap.getHeight());
					try {
						page.setBackground(backgroundBitmap);
						page.setForeground(foregroundBitmap);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					foregroundBitmap.recycle();
					backgroundBitmap.recycle();
				}
				Toast.makeText(this, "Pages: " + nPages, Toast.LENGTH_LONG).show();

				p.close();
				// Alternatively, use FileUtils.getFile(Context, Uri)
//				if (path != null && FileUtils.isLocal(path)) {
//					File file = new File(path);
//				}
			}
			break;
		}
	}
	
	/**
	 * Deletes a selected item.
	 * 
	 * @param position of the selected item
	 */
	private void deleteItem(int position) {
		// Get corresponding FileResource
		final FileResource selectedResource = this.listAdapter.getItem(position);

		// Build "Are you sure?" dialog
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.delete);
		alert.setMessage(String.format(getString(R.string.delete_sure),
				selectedResource.getName()));

		// Add action for "OK" button
		alert.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Delete the item
						fileManager.deleteResource(selectedResource);
						
						// Update listView
						updateListView();
					}
				});
		
		// Add action for "Cancel" button
		alert.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled -> do nothing
					}
				});

		// Display dialog
		alert.show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		FileResource r = this.listAdapter.getItem(position);
		
		// Open paper in editorView, if item is document
		if (r.isDocument())
			this.openPaper(r);
		else {
			// Change directory, if item is directory
			String newPath = this.fileManager.changeDirectory(r);
			if (null == newPath) {
				// Something went wrong
				Toast.makeText(context, R.string.folder_change_failure, Toast.LENGTH_LONG).show();
				return;
			} else {
				// Change path and update view
				this.currentPath = newPath;
				this.updateListView();
			}
		}
	}
	
	/**
	 * Opens the given file resource with the EditorActivity
	 * 
	 * @param r	FileResource to open
	 */
	private void openPaper(FileResource r) {
		Paper p = r.getPaper();
		
		if (null == p) {
			// Opening the paper failed -> display error
			Toast.makeText(context, R.string.paper_open_failure, Toast.LENGTH_SHORT).show();
			return;
		}
		
		// Pass the paper object to the editor.
		IntentHelper.addObjectForKey("selectedPaper", p);
		
		// Switch to EditorActivity
		Intent myIntent = new Intent(this, EditorActivity.class);
		startActivity(myIntent);
	}
	
	/**
	 * Updates the listView
	 */
	private void updateListView() {
		final GridView listview = (GridView) findViewById(R.id.gridView1);	
		
		// Get the item from FileManager
		List<FileResource> items = new LinkedList<FileResource>();
		try {
			items = this.fileManager.getContents(this.currentPath);
		} catch (Exception e) {
			Toast.makeText(context, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
		}
		
		// Set new listAdapter for listView
		this.listAdapter = new FileListAdapter(this, items.toArray(new FileResource[0]));
		listview.setAdapter(this.listAdapter);
	}
	
	private static String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null,
					null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
}
