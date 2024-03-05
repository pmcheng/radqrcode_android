package pmcheng.radqrcode;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class RadQRCodeActivity extends Activity implements OnItemClickListener {

	private static final String TAG = "RadQRCodeActivity";

	private ListView listCases;
	private Cursor cursor;

	//private static final int DIALOG_LOAD_FILE = 1000;
	private static final int SCAN_RESULT_CODE = 0;
	private static final int PICK_CSV_FILE = 1;
	private static final int CREATE_CSV_FILE = 2;
	static final String[] FROM = { CaseData.C_MRN, CaseData.C_DATE,
			CaseData.C_DESC };
	static final int[] TO = { R.id.textView1, R.id.textView2, R.id.textView3 };
	
	long case_id;
	RadQRCodeApp caseApp;
	
	boolean searchResults=false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		caseApp= (RadQRCodeApp) super.getApplication();

		Log.v(TAG, "onCreate");

		if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
				== PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(RadQRCodeActivity.this, new String[] {Manifest.permission.CAMERA}, 0);
		}

		Button buttonScan = findViewById(R.id.buttonScan);
		buttonScan.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.v(TAG, "ButtonScan");

				Intent intent = new Intent("pmcheng.radqrcode.SCAN");
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				startActivityForResult(intent, 0);
			}
		});

		Button buttonSearch = findViewById(R.id.buttonSearch);
		buttonSearch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSearchRequested();
			}
		});

		listCases = findViewById(R.id.listViewCases);
		registerForContextMenu(listCases);

		handleIntent(getIntent());

	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    setIntent(intent);
	    handleIntent(intent);
	}
	
	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			searchResults=true;
			setupList(query);
		} else {
			setupList();
		}		
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode!=RESULT_OK) return;
		switch(requestCode) {
			case SCAN_RESULT_CODE:

				String contents = intent.getStringExtra("SCAN_RESULT");
				// String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

				Case radcase = new Case(contents);

				long id = caseApp.getCaseData().insert(radcase);
				if (id >= 0) {
					Intent i = new Intent(this, EditActivity.class);
					i.putExtra("id", id);
					i.putExtra("scanned", true);
					startActivity(i);
				}
				break;

			case PICK_CSV_FILE:
				new ImportTask(this).execute(intent.getData());
				break;

			case CREATE_CSV_FILE:
				new ExportTask(this).execute(intent.getData());
				break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");
		// setupList();
	}

	
	@Override
    public void onBackPressed() {
         if (!searchResults) {
             super.onBackPressed();
             return;
         } 
         searchResults=false;
         setupList();
    } 
	
	
	// Responsible for fetching data and setting up the list and the adapter
	private void setupList() {
		setupList("");
	}

	private void setupList(String query) {
		// Get the data from the database
		this.cursor = caseApp.getCaseData().getCases(query);
		startManagingCursor(this.cursor);

		// Setup the adapter
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO);

		// Add a ViewBinder to color code LACUSC and follow-up studies
		final SimpleCursorAdapter.ViewBinder VIEW_BINDER = new SimpleCursorAdapter.ViewBinder() {
		    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		        String name = cursor.getColumnName(columnIndex);
		        if (name.equals(CaseData.C_MRN)) {
		        	String mrn=cursor.getString(columnIndex);
		        	TextView tv=(TextView) view;
		        	tv.setText(mrn);
					int sel=cursor.getColumnIndex(CaseData.C_LOC);
					if (sel>=0) {
						String loc = cursor.getString(sel);
						if (loc.equals("LACUSC") || loc.equals("LAG")) {
							((TextView) view).setTextColor(Color.GREEN);
						} else {
							((TextView) view).setTextColor(Color.WHITE);
						}
						return true;
					}
		        }
		        if (name.equals(CaseData.C_DESC)) {
		        	String desc=cursor.getString(columnIndex);
		        	TextView tv=(TextView) view;
		        	tv.setText(desc);
					int sel=cursor.getColumnIndex(CaseData.C_FOLLOW_UP);
					if (sel>=0) {
						String fu = cursor.getString(sel);
						if (fu.equals("1")) {
							((TextView) view).setTextColor(Color.rgb(0xff, 0xc0, 0x40));
						} else {
							((TextView) view).setTextColor(Color.WHITE);
						}
						return true;
					}
		        }
		        return false;
		    }
		};
		adapter.setViewBinder(VIEW_BINDER);
		
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

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder = new Builder(this);

		switch (id) {
//		case DIALOG_LOAD_FILE:
//			builder.setTitle("Choose your file");
//			if (mFileList == null) {
//				Log.e(TAG, "Showing file picker before loading the file list");
//				dialog = builder.create();
//				return dialog;
//			}
//			builder.setItems(mFileList, new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					mChosenFile = new File(mPath.getAbsolutePath(),mFileList[which]);
//					new ImportTask(RadQRCodeActivity.this).execute();
//				}
//			});
//			break;
		}
		dialog = builder.show();
		return dialog;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	private void pickCSVFile() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("text/comma-separated-values");
		startActivityForResult(intent, PICK_CSV_FILE);
	}

	private void createCSVFile() {
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("text/comma-separated-values");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HHmm", Locale.US);
		Date now = new Date();
		String fileName = "cases_" + formatter.format(now) + ".csv";
		intent.putExtra(Intent.EXTRA_TITLE, fileName);
		startActivityForResult(intent, CREATE_CSV_FILE);
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
			pickCSVFile();
			break;
		case R.id.export_cases:
			createCSVFile();
			break;
		case R.id.delete_db:
			AlertDialog.Builder builder = new AlertDialog.Builder(
					RadQRCodeActivity.this);
			builder.setCancelable(false)
					.setTitle("Delete all data?")
					.setIcon(R.drawable.ic_dialog_alert)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
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

	public class ImportTask extends AsyncTask<Uri, Integer, Boolean> {
		private ProgressDialog dialog;
		public int count;

		public ImportTask(Activity activity) {
			dialog = new ProgressDialog(RadQRCodeActivity.this);
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
				Toast.makeText(RadQRCodeActivity.this,
						"Imported " + count + " entries.", Toast.LENGTH_SHORT)
						.show();

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			this.dialog.setMessage("Loading " + values[0] + " cases");
		}

		protected Boolean doInBackground(final Uri... args) {
			CSVReader reader;
			count = 0;
			try {
				Uri mChosenURI = args[0];
				InputStream inputStream=getContentResolver().openInputStream(mChosenURI);
				reader = new CSVReader(new InputStreamReader(inputStream));
				reader.readNext();
				String[] nextLine;

				while ((nextLine = reader.readNext()) != null) {
					if (nextLine.length < Case.LENGTH)
						continue;
					String[] subset = new String[Case.LENGTH];
					final int OFFSET = 1; // skip ID field
					for (int i = 0; i < subset.length; i++) {
						subset[i] = nextLine[i + OFFSET];
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

	public class ExportTask extends AsyncTask<Uri, Void, Boolean> {
		private ProgressDialog dialog;
		public int count;

		public ExportTask(Activity activity) {
			dialog = new ProgressDialog(RadQRCodeActivity.this);
			this.dialog.setMessage("Saving...");
			this.dialog.show();
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			if (success)
				Toast.makeText(RadQRCodeActivity.this,
						"Exported " + count + " entries.", Toast.LENGTH_LONG)
						.show();
			else 
				Toast.makeText(RadQRCodeActivity.this,
						"Could not export", Toast.LENGTH_LONG)
						.show();
		}

		protected Boolean doInBackground(final Uri... args) {
			try {
				Uri mChosenURI=args[0];
				OutputStream outputStream = getContentResolver().openOutputStream(mChosenURI);
				CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream));
				cursor.moveToFirst();
				String[] header = { "id", "loc", "MRN", "study", "date",
						"desc", "follow_up" };
				writer.writeNext(header);
				while (!cursor.isAfterLast()) {
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

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId()) {
	        case R.id.edit:
	    		Intent i = new Intent(this, EditActivity.class);
	    		i.putExtra("id", info.id);
	    		startActivity(i);
	            return true;
	        case R.id.delete:
	        	case_id=info.id;
				AlertDialog.Builder builder = new AlertDialog.Builder(
						RadQRCodeActivity.this);
				builder.setTitle("Delete this case?")
						.setCancelable(false)
						.setIcon(R.drawable.ic_dialog_alert)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										caseApp.getCaseData().deleteCaseById(
												case_id);
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
	            return true;
	        case R.id.create_qr:
				Case radcase=caseApp.getCaseData().getCaseById(info.id);
				Intent intent = new Intent("pmcheng.radqrcode.ENCODE");
				intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");
				intent.putExtra("ENCODE_DATA", radcase.concatenate());
				intent.putExtra("ENCODE_FORMAT", "QR_CODE");
				startActivity(intent);
	        	return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
}