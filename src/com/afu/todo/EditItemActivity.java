package com.afu.todo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class EditItemActivity extends Activity {
	
	int pos =-1;
	String toEditStr;
	EditText editTxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_item);
		
		Intent i = this.getIntent();
		pos = i.getIntExtra("position", -1);
		toEditStr = i.getStringExtra("editMe");
		
		editTxt = (EditText) findViewById(R.id.editItem);
		editTxt.setText(toEditStr);
	}
	
	public void handlePress(View v){      
       // this method handles the "save" button press.  
	   // It passes the newly edited text back to main Activity for saving there
       Intent retVal = new Intent();
       toEditStr = editTxt.getText().toString();
       retVal.putExtra("edited", toEditStr);

       setResult(RESULT_OK, retVal); // set result code and bundle data for response
       finish();
	}

}