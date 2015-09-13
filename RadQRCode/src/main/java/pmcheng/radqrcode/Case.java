package pmcheng.radqrcode;

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
			study = elements[2];
			date = convertDate(elements[3]);
			desc = elements[4];
			follow_up = (elements[5].equals("1")) ? 1 : 0;
		}
	}

	public Case(String[] input) {
		loc = input[0];
		MRN = input[1];
		study = input[2];
		date = convertDate(input[3]);
		desc = input[4];	
		follow_up = (input[5].equals("1")) ? 1 : 0;
	}

	public String concatenate() {
		String output_string=loc+"|"+
				MRN+"|"+
				study+"|"+
				date+"|"+
				desc+"|"+
				follow_up;
		return output_string;
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
