package pmcheng.caseqrcode;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class CaseQRCodeActivity extends Activity implements OnItemClickListener {

	private static final String TAG = "CaseQRCodeActivity";

	ListView listCases;
	Cursor cursor;
	SimpleCursorAdapter adapter;

	static final String[] FROM = { CaseData.C_MRN, CaseData.C_DATE,
			CaseData.C_DESC };
	static final int[] TO = { R.id.textView1, R.id.textView2, R.id.textView3 };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Log.v(TAG, "onCreate");

		Button buttonScan = (Button) findViewById(R.id.buttonScan);
		buttonScan.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent("pmcheng.caseqrcode.SCAN");
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				startActivityForResult(intent, 0);
			}
		});

		Button buttonSearch = (Button) findViewById(R.id.buttonSearch);
		buttonSearch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSearchRequested();
			}
		});

		listCases = (ListView) findViewById(R.id.listViewCases);

		// Get the intent, verify the action and get the query
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			setupList(query);
		} else {
			setupList();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				// String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

				Case radcase = new Case(contents);
				CaseQRCodeApp caseApp = (CaseQRCodeApp) super.getApplication();
				long id=caseApp.getCaseData().insert(radcase);
				if (id>=0) {
					Intent i = new Intent(this, EditActivity.class);
					i.putExtra("id", id);
					i.putExtra("scanned", true);
					startActivity(i);
				}

				// Handle successful scan
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");
		// setupList();
	}

	// Responsible for fetching data and setting up the list and the adapter
	private void setupList() {
		setupList("");
	}

	private void setupList(String query) {
		// Get the data from the database
		CaseQRCodeApp caseApp = (CaseQRCodeApp) super.getApplication();
		this.cursor = caseApp.getCaseData().getCases(query);
		startManagingCursor(this.cursor);

		// Setup the adapter
		adapter = new SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO);

		listCases.setAdapter(adapter);
		listCases.setOnItemClickListener(this);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.v("listCases", "in onItemClick with position=" + position
				+ " and id=" + id);
		Intent i = new Intent(this, EditActivity.class);
		i.putExtra("id", id);
		startActivity(i);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about:
			AboutDialog about = new AboutDialog(this);
			// about.setTitle("About");
			about.show();
			break;
		case R.id.new_case:
			Intent i = new Intent(this, EditActivity.class);
			startActivity(i);
			break;
		case R.id.import_cases:
			new ImportTask(this).execute();
			break;
		case R.id.export_cases:
			new ExportTask(this).execute();
			break;
		case R.id.delete_db:
			AlertDialog.Builder builder = new AlertDialog.Builder(
					CaseQRCodeActivity.this);
			builder.setCancelable(false)
					.setTitle("Delete all data?")
					.setIcon(R.drawable.ic_dialog_alert)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									CaseQRCodeApp caseApp = (CaseQRCodeApp) getApplication();
									caseApp.getCaseData().deleteAll();
									setupList();
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			builder.show();
			break;
		}
		return true;
	}

	public String getStorageDir() {
		String url = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator;
		if (android.os.Build.DEVICE.toLowerCase().contains("samsung")
				|| android.os.Build.MANUFACTURER.toLowerCase().contains(
						"samsung")) {
			url = url + "external_sd" + File.separator;
		}

		return url;
	}

	public class ImportTask extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog dialog;
		public int count;

		public ImportTask(Activity activity) {
			dialog = new ProgressDialog(CaseQRCodeActivity.this);
			this.dialog.setMessage("Loading...");
			this.dialog.show();
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			setupList();
			if (success)
				Toast.makeText(CaseQRCodeActivity.this,
						"Imported " + count + " entries.", Toast.LENGTH_SHORT)
						.show();

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			this.dialog.setMessage("Loading " + values[0] + " cases");
		}

		protected Boolean doInBackground(final String... args) {
			CSVReader reader;
			count = 0;
			try {
				reader = new CSVReader(new FileReader(getStorageDir()
						+ "cases.csv"));
				reader.readNext();
				String[] nextLine;
				CaseQRCodeApp caseApp = (CaseQRCodeApp) getApplication();
				
				while ((nextLine = reader.readNext()) != null) {
					if (nextLine.length<Case.LENGTH) continue;
					String[] subset=new String[Case.LENGTH];
					final int OFFSET=1;
					for (int i = 0; i < subset.length; i++) {
						subset[i]=nextLine[i+OFFSET];
					}
					Case radcase = new Case(subset);
					caseApp.getCaseData().insert(radcase);
					count++;
					publishProgress(count);

				}

				return true;
			} catch (Exception e) {
				Log.d(TAG, "Import Task", e);
				return false;
			}
		}
	}

	public class ExportTask extends AsyncTask<String, Void, Boolean> {
		private ProgressDialog dialog;
		public int count;

		public ExportTask(Activity activity) {
			dialog = new ProgressDialog(CaseQRCodeActivity.this);
			this.dialog.setMessage("Saving...");
			this.dialog.show();
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			if (success)
				Toast.makeText(CaseQRCodeActivity.this,
						"Exported " + count + " entries.", Toast.LENGTH_SHORT)
						.show();
		}

		protected Boolean doInBackground(final String... args) {
			CSVWriter writer;
			CaseQRCodeApp caseApp = (CaseQRCodeApp) getApplication();
			try {
				writer = new CSVWriter(new FileWriter(getStorageDir()
						+ "cases_export.csv"));
				cursor.moveToFirst();
				String[] header = { "id", "loc", "MRN", "study", "date",
						"desc", "follow_up" };
				writer.writeNext(header);
				while (cursor.isAfterLast() == false) {
					String[] entries = caseApp.getCaseData().getCase(cursor);
					writer.writeNext(entries);
					count++;
					cursor.moveToNext();
				}
				writer.close();
				return true;
			} catch (Exception e) {
				Log.d(TAG, "Export Task", e);
				return false;
			}
		}
	}

}