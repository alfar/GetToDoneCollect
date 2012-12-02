package dk.gettodone.GetToDoneCollector;

import java.util.ArrayList;
import java.util.List;

import dk.gettodone.GetToDoneCollector.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class GetToDoneActivity extends Activity {

	private static final int DIALOG_LOGIN = 0;
	private EditText mTextCollect;
	private ListView mListMatches;
	private HttpHelper httpHelper; 

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		httpHelper = new HttpHelper(this);

		setContentView(R.layout.main);
		mListMatches = (ListView)findViewById(R.id.listMatches);
		mListMatches.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {			
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				httpHelper.collect(mListMatches.getItemAtPosition(arg2).toString());
				mListMatches.setAdapter(null);
				return true;
			}
		});

		mListMatches.setOnItemClickListener(new ListView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {				
				mTextCollect.setText(mTextCollect.getText().replace(mTextCollect.getSelectionStart(), mTextCollect.getSelectionEnd(), mListMatches.getItemAtPosition(arg2).toString()));

				mListMatches.setAdapter(null);

				mTextCollect.setSelection(mTextCollect.getText().length());

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mTextCollect, InputMethodManager.SHOW_IMPLICIT);
			}			
		});

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

		Button speakButton = (Button)findViewById(R.id.buttonSpeak);

		// Check to see if a recognition activity is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(
				new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			speakButton.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					startVoiceRecognitionActivity();
				}
			});
		} else {
			speakButton.setEnabled(false);
			speakButton.setText("Recognizer not present");
		}

		if (getIntent().getBooleanExtra("EXTRA_SPEECH_TO_TEXT", false))
		{
			startVoiceRecognitionActivity();
		}
		else
		{
			mTextCollect.selectAll();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(mTextCollect, InputMethodManager.SHOW_IMPLICIT);
		}

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

	private void startVoiceRecognitionActivity()
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		// Specify the calling package to identify your application
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

		// Display an hint to the user about what he should say.
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "GetToDone Collection");

		// Given an hint to the recognizer about what the user is going to say
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

		// Specify how many results you want to receive. The results will be sorted
		// where the first result is the one with higher confidence.
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 7);

		startActivityForResult(intent, 1234);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1234 && resultCode == RESULT_OK) {
			// Fill the list view with the strings the recognizer thought it could have heard
			ArrayList<String> matches = data.getStringArrayListExtra(
					RecognizerIntent.EXTRA_RESULTS);
			mListMatches.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
					matches));
		}

		super.onActivityResult(requestCode, resultCode, data);
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