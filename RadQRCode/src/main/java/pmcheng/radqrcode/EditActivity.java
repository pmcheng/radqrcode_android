package pmcheng.radqrcode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import pmcheng.radqrcode.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

public class EditActivity extends Activity {
	private static final String TAG = "EditActivity";
	long case_id;
	EditText e_mrn, e_loc, e_study, e_desc;
	Button b_date;
	CheckBox cb_fu;
	RadQRCodeApp caseApp;
	boolean newCase = false;
	boolean spinnerInitialized = false;

	private int mYear;
	private int mMonth;
	private int mDay;

	static final int DATE_DIALOG_ID = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");

		setContentView(R.layout.editrow);
		e_mrn = (EditText) findViewById(R.id.editMRN);
		e_loc = (EditText) findViewById(R.id.editLoc);
		e_study = (EditText) findViewById(R.id.editStudy);
		b_date = (Button) findViewById(R.id.btnDate);
		e_desc = (EditText) findViewById(R.id.editDesc);
		cb_fu = (CheckBox) findViewById(R.id.checkBoxFollow);

		Intent i = getIntent();
		case_id = i.getLongExtra("id", -1);
		boolean scanned = i.getBooleanExtra("scanned", false);
		caseApp = (RadQRCodeApp) super.getApplication();
		if (case_id == -1) {
			Calendar cal = Calendar.getInstance();
			mYear = cal.get(Calendar.YEAR);
			mMonth = cal.get(Calendar.MONTH);
			mDay = cal.get(Calendar.DAY_OF_MONTH);
			b_date.setText(mYear + "-" + (mMonth + 1) + "-" + mDay);

			newCase = true;

		} else {

			Case radcase = caseApp.getCaseData().getCaseById(case_id);

			e_mrn.setText(radcase.MRN);
			e_loc.setText(radcase.loc);
			e_study.setText(radcase.study);

			b_date.setText(radcase.date);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			try {
				Date d = sdf.parse(radcase.date);
				cal.setTime(d);
			} catch (ParseException e) {
				Log.v(TAG, e.getMessage());
			}
			mYear = cal.get(Calendar.YEAR);
			mMonth = cal.get(Calendar.MONTH);
			mDay = cal.get(Calendar.DAY_OF_MONTH);

			Log.v(TAG, "Parsed date = " + mYear + "-" + (mMonth + 1) + "-"
					+ mDay);

			e_desc.setText(radcase.desc);
			cb_fu.setChecked(radcase.follow_up == 1);

		}

		b_date.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		Button btnSave = (Button) findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Case radcase = makeCase();

				if (newCase) {
					caseApp.getCaseData().insert(radcase);
				} else {
					caseApp.getCaseData().update(case_id, radcase);
				}

				finish();
			}
		});

		ArrayList<String> locList = caseApp.getCaseData().getLocs();
		locList.add(0, "");

		Spinner locSpinner = (Spinner) findViewById(R.id.spinnerLoc);
		if (locList.size() > 1) {
			try {
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_spinner_item, locList);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				locSpinner.setAdapter(adapter);

				locSpinner
						.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
							public void onItemSelected(AdapterView<?> parent,
									View v, int pos, long id) {
								if (!spinnerInitialized) {
									spinnerInitialized = true;
								} else if (pos > 0) {
									e_loc.setText(parent.getItemAtPosition(pos)
											.toString());
								}
							}

							public void onNothingSelected(AdapterView<?> parent) {
								return;
							}
						});

			} catch (Exception e) {
				Log.v(TAG, e.getStackTrace().toString());
			}
		} else {
			locSpinner.setVisibility(View.GONE);
		}

		Button btnDelete = (Button) findViewById(R.id.btnDelete);
		if (newCase) {
			btnDelete.setVisibility(View.GONE);
		} else {
			btnDelete.setVisibility(View.VISIBLE);
		}

		btnDelete.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						EditActivity.this);
				builder.setTitle("Delete this case?")
						.setCancelable(false)
						.setIcon(R.drawable.ic_dialog_alert)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										caseApp.getCaseData().deleteCaseById(
												case_id);
										EditActivity.this.finish();
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
			}
		});

		Button btnCancel = (Button) findViewById(R.id.btnCancel);
		if (scanned) {
			btnCancel.setVisibility(View.GONE);
		} else {
			btnCancel.setVisibility(View.VISIBLE);
		}
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
					mDay);
		}
		return null;
	}

	private void updateDisplay() {
		String date = mYear + "-" + String.format("%02d", mMonth + 1) + "-"
				+ String.format("%02d", mDay);
		b_date.setText(date);
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateDisplay();
		}
	};

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit_menu, menu);
		return true;
	}

	public Case makeCase() {
		Case radcase = new Case();
		radcase.loc = e_loc.getText().toString();
		radcase.MRN = e_mrn.getText().toString();
		radcase.study = e_study.getText().toString();
		radcase.date = b_date.getText().toString();
		radcase.desc = e_desc.getText().toString();
		radcase.follow_up = cb_fu.isChecked() ? 1 : 0;

		return radcase;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.create_qr:
			Case radcase = makeCase();
			Intent intent = new Intent("pmcheng.radqrcode.ENCODE");
			intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");
			intent.putExtra("ENCODE_DATA", radcase.concatenate());
			intent.putExtra("ENCODE_FORMAT", "QR_CODE");
			startActivity(intent);
			break;
		}
		return true;
	}
}
