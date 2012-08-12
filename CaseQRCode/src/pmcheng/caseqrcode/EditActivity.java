package pmcheng.caseqrcode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class EditActivity extends Activity {
	private static final String TAG = "EditActivity";
	long case_id;
	EditText e_mrn, e_loc, e_study, e_date, e_desc;
	CheckBox cb_fu;
	CaseQRCodeApp caseApp;
	boolean newCase = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");

		setContentView(R.layout.editrow);
		e_mrn = (EditText) findViewById(R.id.editMRN);
		e_loc = (EditText) findViewById(R.id.editLoc);
		e_study = (EditText) findViewById(R.id.editStudy);
		e_date = (EditText) findViewById(R.id.editDate);
		e_desc = (EditText) findViewById(R.id.editDesc);
		cb_fu = (CheckBox) findViewById(R.id.checkBoxFollow);

		Intent i = getIntent();
		case_id = i.getLongExtra("id", -1);
		caseApp = (CaseQRCodeApp) super.getApplication();
		Cursor c = null;
		if (case_id == -1) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String date = dateFormat.format(new Date());
			e_date.setText(date);

			newCase = true;

		} else {

			c = caseApp.getCaseData().getCaseById(case_id);
			if (c.moveToNext()) {
				String mrn = c.getString(c.getColumnIndex(CaseData.C_MRN));
				e_mrn.setText(mrn);

				String loc = c.getString(c.getColumnIndex(CaseData.C_LOC));
				e_loc.setText(loc);

				String study = c.getString(c.getColumnIndex(CaseData.C_STUDY));
				e_study.setText(study);

				String date = c.getString(c.getColumnIndex(CaseData.C_DATE));
				e_date.setText(date);

				String desc = c.getString(c.getColumnIndex(CaseData.C_DESC));
				e_desc.setText(desc);

				Integer fu = c.getInt(c.getColumnIndex(CaseData.C_FOLLOW_UP));
				cb_fu.setChecked(fu == 1);
			}
		}

		Button btnUpdate = (Button) findViewById(R.id.btnUpdate);
		btnUpdate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String[] case_array = new String[6];
				case_array[0] = e_mrn.getText().toString();
				case_array[1] = e_desc.getText().toString();
				case_array[2] = e_date.getText().toString();
				case_array[3] = e_loc.getText().toString();
				case_array[4] = cb_fu.isChecked() ? "1" : "0";
				case_array[5] = e_study.getText().toString();

				Case radcase = new Case(case_array);

				if (newCase) {
					caseApp.getCaseData().insert(radcase);
				} else {
					caseApp.getCaseData().update(case_id, radcase);
				}

				finish();
			}
		});

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
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

	}
}
