package pmcheng.caseqrcode;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CaseData {
	private static final String TAG = "CaseData";

	static final String DB_NAME = "cases.db";
	static final int DB_VERSION = 1;
	static final String TABLE = "cases";
	static final String C_ID = "_id";
	// static final String C_ACC = "accession";
	static final String C_MRN = "mrn";
	static final String C_LOC = "loc";
	static final String C_DATE = "date";
	static final String C_STUDY = "study";
	static final String C_DESC = "description";
	static final String C_FOLLOW_UP = "follow_up";

	private DbHelper dbHelper;

	/** Constructor */
	public CaseData(Context context) {
		// context.deleteDatabase("cases.db");
		dbHelper = new DbHelper(context);

	}

	/** Inserts the case into the database. */
	public long insert(Case radcase) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		// Create content values from status object
		ContentValues values = new ContentValues();
		values.put(C_MRN, radcase.MRN);
		values.put(C_DESC, radcase.desc);
		values.put(C_STUDY, radcase.study);
		values.put(C_LOC, radcase.loc);
		values.put(C_DATE, radcase.date);
		values.put(C_FOLLOW_UP, radcase.follow_up);

		return db.insert(TABLE, null, values);
	}

	public String[] getCase(Cursor cursor) {
		String[] entries = new String[Case.LENGTH + 1];
		for (int i = 0; i < entries.length; i++) {
			entries[i] = cursor.getString(i);
		}
		return entries;
	}

	public long update(long id, Case radcase) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(C_MRN, radcase.MRN);
		values.put(C_DESC, radcase.desc);
		values.put(C_STUDY, radcase.study);
		values.put(C_LOC, radcase.loc);
		values.put(C_DATE, radcase.date);
		values.put(C_FOLLOW_UP, radcase.follow_up);

		return db.update(TABLE, values, C_ID + "=" + id, null);
	}

	public long deleteCaseById(long id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(TABLE, C_ID + "=" + id, null);
	}
	
	public void deleteAll() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(TABLE, null, null);
	}

	public Cursor getCases() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(TABLE, null, null, null, null, null, C_DATE+" DESC");
	}
	
	public ArrayList<String> getLocs() {
		ArrayList<String> locList=new ArrayList<String>();
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		String query=String.format("SELECT DISTINCT %s FROM %s ORDER BY %s",C_LOC, TABLE, C_LOC); 
		Cursor cursor=db.rawQuery(query,null);

		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {
			locList.add(cursor.getString(0));
			cursor.moveToNext();
		}
		return locList;
	}
	
	public Cursor getCases(String query) {
		Log.d(TAG,"Query: "+query);
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		if (query.equals("")) {
			return db.query(TABLE, null, null, null, null, null, C_DATE+" DESC");
		}
		return db.query(TABLE, null, C_DESC + " LIKE '%"+query+"%'",
				null, null, null, null);
	}

	public Case getCaseById(long id) {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		Cursor c=db.query(TABLE, null, C_ID + "=" + id, null, null, null, null);
		Case radcase=new Case();
		if (c.moveToNext()) {
			radcase.MRN = c.getString(c.getColumnIndex(CaseData.C_MRN));
			radcase.loc= c.getString(c.getColumnIndex(CaseData.C_LOC));
			radcase.study=c.getString(c.getColumnIndex(CaseData.C_STUDY));
			radcase.date=c.getString(c.getColumnIndex(CaseData.C_DATE));
			radcase.desc=c.getString(c.getColumnIndex(CaseData.C_DESC));
			radcase.follow_up= c.getInt(c.getColumnIndex(CaseData.C_FOLLOW_UP));
		}
		return radcase;
	}

	/**
	 * Deletes ALL the data
	 */
	public void delete() {
		// Open Database
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		// Delete the data
		db.delete(TABLE, null, null);

		// Close Database
		db.close();
	}

	class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			Log.d(TAG, "DbHelper() instantiated");
		}

		/** Called only once, first time we create the database file. */
		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = String
					.format("CREATE TABLE %s "
							+ "( %s INTEGER PRIMARY KEY AUTOINCREMENT, "
							+ "%s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s INTEGER, "
							+ "UNIQUE (%s, %s, %s) ON CONFLICT REPLACE)",
							TABLE, C_ID, C_LOC, C_MRN, C_STUDY, C_DATE, C_DESC, C_FOLLOW_UP,
							C_LOC,C_MRN,C_DATE);
			Log.d(TAG, "onCreate with SQL: " + sql);
			db.execSQL(sql);
		}

		/** Called when the old schema is different then new schema. */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Typically SQL such as: ALTER TABLE ADD COLUMN ...
			db.execSQL("DROP TABLE IF EXISTS " + TABLE);
			onCreate(db);
		}
	}

}
