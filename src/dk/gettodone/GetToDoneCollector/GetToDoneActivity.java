package dk.gettodone.GetToDoneCollector;

import dk.gettodone.GetToDoneCollector.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class GetToDoneActivity extends Activity {

	private static final int DIALOG_LOGIN = 0;
	private EditText mTextCollect;
	private HttpHelper httpHelper; 

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		httpHelper = new HttpHelper(this);

		setContentView(R.layout.main);

		mTextCollect = (EditText)findViewById(R.id.editTextCollect);
		mTextCollect.setText("Collect here!");		

		Button btnCollect = (Button)findViewById(R.id.buttonCollect);

		btnCollect.setOnClickListener(new Button.OnClickListener() {			
			public void onClick(View v) {
				httpHelper.collect(mTextCollect.getText().toString());
				mTextCollect.setText("");
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mTextCollect, InputMethodManager.SHOW_IMPLICIT);				
			}			
		});        

		mTextCollect.selectAll();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(mTextCollect, InputMethodManager.SHOW_IMPLICIT);

		if (httpHelper.getApiKey().isEmpty()) {
			showDialog(DIALOG_LOGIN);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.itemLogin:
			httpHelper.setApiKey("");
			showDialog(DIALOG_LOGIN);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog dialog;
		switch (id){
		case DIALOG_LOGIN:
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.login);
			dialog.setTitle("Log in to GetToDone");
			
			Button btnLogin = (Button)dialog.findViewById(R.id.buttonLogin);

			btnLogin.setOnClickListener(new Button.OnClickListener() {			
				public void onClick(View v) {
					EditText username = (EditText)dialog.findViewById(R.id.editTextUserName);
					EditText password = (EditText)dialog.findViewById(R.id.editTextPassword);
					
					httpHelper.setApiKey(httpHelper.login(username.getText().toString(), password.getText().toString()));

					dialog.dismiss();
				}
			});
			
			break;
		default:
			dialog = null;
		}
		
		return dialog;	
	}
}