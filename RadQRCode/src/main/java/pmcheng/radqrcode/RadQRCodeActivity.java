package pmcheng.radqrcode;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pmcheng.radqrcode.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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

public class RadQRCodeActivity extends Activity implements OnItemClickListener {

	private static final String TAG = "RadQRCodeActivity";

	private ListView listCases;
	private Cursor cursor;
	private SimpleCursorAdapter adapter;

	private String[] mFileList;
	private File mPath;
	private String mChosenFile;
	private static final int DIALOG_LOAD_FILE = 1000;

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

		Button buttonScan = (Button) findViewById(R.id.buttonScan);
		buttonScan.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.v(TAG, "ButtonScan");
				Intent intent = new Intent("pmcheng.radqrcode.SCAN");
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
		registerForContextMenu(listCases);

		handleIntent(getIntent());

		mPath = new File(getStorageDir());
		
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
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
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
		adapter = new SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO);

		// Add a ViewBinder to color code LACUSC and follow-up studies
		final SimpleCursorAdapter.ViewBinder VIEW_BINDER = new SimpleCursorAdapter.ViewBinder() {
		    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		        String name = cursor.getColumnName(columnIndex);
		        if (name.equals(CaseData.C_MRN)) {
		        	String mrn=cursor.getString(columnIndex);
		        	TextView tv=(TextView) view;
		        	tv.setText(mrn);
		        	String loc=cursor.getString(cursor.getColumnIndex(CaseData.C_LOC));
		            if (loc.equals("LACUSC")) {
		            	((TextView) view).setTextColor(Color.GREEN);
		            } else {
		            	((TextView) view).setTextColor(Color.WHITE);
		            }
		            return true;
		        }
		        if (name.equals(CaseData.C_DESC)) {
		        	String desc=cursor.getString(columnIndex);
		        	TextView tv=(TextView) view;
		        	tv.setText(desc);
		        	String fu=cursor.getString(cursor.getColumnIndex(CaseData.C_FOLLOW_UP));
		            if (fu.equals("1")) {
		            	((TextView) view).setTextColor(Color.rgb(0xff,0xc0,0x40));
		            } else {
		            	((TextView) view).setTextColor(Color.WHITE);
		            }
		            return true;
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

	private void loadFileList() {
		if (mPath.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					return filename.contains(".csv");
				}
			};
			mFileList = mPath.list(filter);
		} else {
			mFileList = new String[0];
		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new Builder(this);

		switch (id) {
		case DIALOG_LOAD_FILE:
			builder.setTitle("Choose your file");
			if (mFileList == null) {
				Log.e(TAG, "Showing file picker before loading the file list");
				dialog = builder.create();
				return dialog;
			}
			builder.setItems(mFileList, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mChosenFile = new File(mPath.getAbsolutePath(),mFileList[which]).toString();
					new ImportTask(RadQRCodeActivity.this).execute();
				}
			});
			break;
		}
		dialog = builder.show();
		return dialog;
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
			loadFileList();
			if (mFileList.length > 0) {
				showDialog(DIALOG_LOAD_FILE);
			} else {
				Toast.makeText(RadQRCodeActivity.this,
						"No .csv files found to import.", Toast.LENGTH_SHORT)
						.show();
			}

			break;
		case R.id.export_cases:
			new ExportTask(this).execute();
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

	public String getStorageDir() {
		//String path = Environment.getExternalStorageDirectory()
		//		.getAbsolutePath();
		//File storagedir=new File(path,"radqrcode");
		//if (!storagedir.exists()) storagedir.mkdirs();
		File storagedir = this.getExternalFilesDir(null);
		String path=storagedir.toString();
		Log.v(TAG, path);
		return path;
	}

	public class ImportTask extends AsyncTask<String, Integer, Boolean> {
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

		protected Boolean doInBackground(final String... args) {
			CSVReader reader;
			count = 0;
			try {
				reader = new CSVReader(new FileReader(mChosenFile));
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

	public class ExportTask extends AsyncTask<String, Void, Boolean> {
		private ProgressDialog dialog;
		public int count;
		private String filePath;

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
						"Exported " + count + " entries to "+filePath, Toast.LENGTH_LONG)
						.show();
			else 
				Toast.makeText(RadQRCodeActivity.this,
						"Could not export to  "+filePath, Toast.LENGTH_LONG)
						.show();
				
		}

		protected Boolean doInBackground(final String... args) {
			CSVWriter writer;
			try {

				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy_MM_dd_HHmm", Locale.US);
				Date now = new Date();
				String fileName = "cases_" + formatter.format(now) + ".csv";
				filePath= new File(getStorageDir(),fileName).toString();
				writer = new CSVWriter(new FileWriter(filePath));

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