// initial exercise, Peter van der Linden, pvdl@afu.com, 
// a simple todo list application for CodePath
// May 5 2014

// have enhanced this app:
// * hide the soft keyboard when returning to main Activity after editing
// * fixed the bug where you need to save the files on adds, as well as deletes.
// * added Commons IO library for better IO from http://commons.apache.org/proper/commons-io 
// * added list footer for when list starts to encroach on "Add" field!


// ideas to enhance this app:
// * write in smaller steps than "write entire file when anything added/deleted"
//   the simplest way to do that, is probably to store list items in an sql database
// * add a check off mark to each item, that can be set/unset
// * maintain a count of number of items in the list, in the Action Bar
// * resolve the logged issue in EditItemActivity.  You get this sometimes when deleting chars in the list item you're editing.
//   it doesn't affect the app running, but it would be good to tie up loose end
//   05-07 10:01:24.758: E/SpannableStringBuilder(23869): SPAN_EXCLUSIVE_EXCLUSIVE spans cannot have a zero length
//   05-07 10:01:25.509: W/InputEventReceiver(23869): Attempted to finish an input event but the input event receiver has already been disposed.
//   05-07 10:01:25.509: W/InputEventReceiver(23869): Attempted to finish an input event but the input event receiver has already been disposed.
//   05-07 10:01:25.509: W/ViewRootImpl(23869): Dropping event due to root view being removed: MotionEvent { action=ACTION_MOVE, id[0]=0, x[0]=1003.0, y[0]=1202.0, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=134558309, downTime=134558297, deviceId=20, source=0x1002 }
//   stackoverflow suggests it is due to using a non-Google soft keyboard.  But I am not doing that.

package com.afu.todo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.exception.app.AppException;
import com.kii.cloud.storage.query.KiiQueryResult;

public class MainActivity extends Activity {
	private ArrayList<String> items;
	private ArrayAdapter<String> itemsAdapter;
	private ListView lvItems;
	private Context ctx;
	private int savedPos;
	private static String username;
	private static String password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		items = new ArrayList<String>(100);
		setContentView(R.layout.activity_main);
		Drawable background = getResources().getDrawable(R.drawable.background);
		background.setAlpha(160);   // 0 = transparent, 255 = opaque

	    ctx = getBaseContext();  // need this for Intent later
	    Intent i = getIntent();
		username = i.getStringExtra("username");
		password = i.getStringExtra("password");

        // Kii SDK has already been initialized as part of login.
        lvItems = (ListView) findViewById(R.id.lvItems);
        //View footer = getLayoutInflater().inflate(R.layout.listfooter, null);
        //lvItems.addFooterView(footer);

		readItems();
		getWindow().setSoftInputMode(   // put keyboard away
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		

	}
	
	private void setupListViewListener() {
		lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			// a long click means we must delete this item from list
			@Override
			public boolean onItemLongClick(AdapterView<?> aview,
					View item, int pos, long id) {
				items.remove(pos);
				itemsAdapter.notifyDataSetChanged();
                saveItems();
				return true;
			}
		});
		
		lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			// a short click means we need to start the editItem activity
			@Override
			public void onItemClick(AdapterView<?> aview, View item, int pos, long id) {
				
				savedPos = pos;  // keep this around, to later put updated String value at this position into List
		    	Intent i = new Intent(ctx, EditItemActivity.class);

				// get the string from the clicked item, put into extra
				String s = ((TextView) item).getText().toString();
				i.putExtra("editMe", s);				
				startActivityForResult(i, 0);
				return;
			}
		});

	}
	
	static KiiObject ko = null;
	    
	class ReadFromCloud extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... p) { 
			Log.e("Todo", "readItems from cloud in Async task ");
			ko = Kii.bucket(username).object();
			// Get the object
			try {			
				KiiQueryResult<KiiObject> result = Kii.bucket(username).query(null); //null=all obj's

				List<KiiObject> objLists = result.getResult();
				int i = 0;

				try {
					for (KiiObject obj : objLists) {
						String s = obj.getString(""+i);
						Log.e("Todo", "have read in from cloud, about to set item "+ i + ", " + s);
						items.add(i, s);
						i++; 
					}
				} catch (Exception e) {
					Log.e("Todo", "excpn on write to cloud: " + e.getMessage() );
				}

				if (items==null) { // list adapter needs at least 1 item
					items = new ArrayList<String>(100);
					items.add("make list");  
					Log.e("Todo", "make list with 1 entry");
				}
				Log.e("Todo", "#items is " +i);
				Log.e("Todo", "items is " +items);

			} catch (IOException e) {
				e.printStackTrace();
				Log.e("Todo", "IO Excpn writing to Kii cloud");
			} catch (AppException e) {
				Log.e("Todo", "App Excpn writing to Kii cloud");
				e.printStackTrace();
			}
			return null;
		}                 

		@Override
		 protected void onPostExecute(String s) {
			
			itemsAdapter = new ArrayAdapter<String>(ctx,
					android.R.layout.simple_list_item_1, items);
			
			Log.e("Todo", "in post execute, setting adapter. items = " + items);
			Log.e("Todo", "items is " +items);
			Log.e("Todo", "itemsAdapter = " + itemsAdapter);
			Log.e("Todo", "lvItems = " + lvItems);
			
			// set the adapter
			lvItems.setAdapter(itemsAdapter);
			setupListViewListener();
		}

	}
	
	
	private void readItems() {
		// read/write into the Kii cloud (soon)
		Log.e("Todo", "about to read from Kii cloud for user "+username);
	    ko = Kii.bucket(username).object();
	    ReadFromCloud task = new ReadFromCloud();
	    task.execute(new String[] { "dummyarg" });
        return;
        
	}
	
	private void saveItems() {
		// read/write into the Kii cloud
		int count =0;
		for (String it: items) {
			ko.set( ""+ count++, it); //  for all objects in array, key is the int offset in array.
			Log.e("Todo", "about to add to ko, item " + it);
		}

		// do the cloud IO in a separate task	
		Runnable runInBackgrd = new Runnable() {
			@Override
			public void run() {
				// Save the object
				try {
					ko.saveAllFields(true);
					Log.e("Todo", "WRITTEN to Kii cloud");

				} catch (IOException e) {
					e.printStackTrace();
					Log.e("Todo", "IO Excpn writing to Kii cloud");
				} catch (AppException e) {
					Log.e("Todo", "App Excpn writing to Kii cloud");
					e.printStackTrace();
				}
			};                 
		};
		new Thread(runInBackgrd).start();
		Toast.makeText(this, "saved to Kii cloud", Toast.LENGTH_SHORT).show();

		return;
        // code below uses local file instead of cloud
		//		File filesDir = getFilesDir();
		//		File todoFile = new File(filesDir, "todo.txt");
		//		try {
		//			FileUtils.writeLines(todoFile, items);
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
	}
	
	public void addTodoItem(View v) {
		// this is the button handler, expected by XML!
		EditText etNewItem = (EditText)
				findViewById(R.id.etNewItem);
		itemsAdapter.add(etNewItem.getText().toString());
		etNewItem.setText("");
        saveItems();     
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// now we finished editing, get rid of keyboard (Got this from stackoverflow.com)
		// http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		if (resultCode == RESULT_OK) {
			// Extract name value from result extras
			String name = data.getExtras().getString("edited");
			
			items.set(savedPos, name);
			itemsAdapter.notifyDataSetChanged();

			saveItems();
		}
	}

}