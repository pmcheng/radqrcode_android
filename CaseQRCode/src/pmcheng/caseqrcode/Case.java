package pmcheng.caseqrcode;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class Case {
	private static final String TAG = "Case";
	public static final int LENGTH = 6;
	public String loc = "";
	public String MRN = "";
	public String date = "";
	public String study = "";
	public String desc = "";
	public int follow_up = 0;

	public Case() {
	}

	public Case(String input) {
		String[] elements = input.split("\\|");
		Log.d("Case", elements.length + "");
		if (elements.length != LENGTH) {
			desc = input;
		} else {
			loc = elements[0];
			MRN = elements[1];
			date = convertDate(elements[2]);
			study = elements[3];
			follow_up = (elements[4].equals("1")) ? 1 : 0;
			desc = elements[5];
		}
	}

	public Case(String[] input) {
		loc = input[1];
		MRN = input[2];
		study = input[3];
		date = convertDate(input[4]);
		desc = input[5];
		follow_up = (input[6].equals("1")) ? 1 : 0;

	}

	public String convertDate(String dateString) {
		Log.v(TAG, "convertDate");
		String[] formatStrings = { "M/d/yyyy", "yyyy-M-d", "M-d-yyyy",
				"yyyy/m/d" };

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date output = new Date();

		for (String formatString : formatStrings) {
			try {
				output = new SimpleDateFormat(formatString).parse(dateString);
				break;
			} catch (ParseException e) {
			}
		}

		return df.format(output);
	}
}
