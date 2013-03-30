package pmcheng.radqrcode;

import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import pmcheng.radqrcode.R;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

public class AboutDialog extends Dialog {
	private static final String TAG = "AboutDialog";
	
	Context mContext;
	public AboutDialog(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mContext=context;
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG,"onCreate");
		
		setContentView(R.layout.about);

		String buildDate="";
		try{
		     ApplicationInfo ai = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), 0);
		     ZipFile zf = new ZipFile(ai.sourceDir);
		     ZipEntry ze = zf.getEntry("classes.dex");
		     long time = ze.getTime();
		     buildDate= SimpleDateFormat.getInstance().format(new java.util.Date(time));

		} catch(Exception e) {
		}

		TextView tv = (TextView)findViewById(R.id.legal_text);
		tv.setText(Html.fromHtml("Uses the open source ZXing Barcode Reader<br>http://code.google.com/p/zxing/"));	
		Linkify.addLinks(tv, Linkify.ALL);
		
		tv = (TextView)findViewById(R.id.info_text);
		tv.setText(Html.fromHtml("<h3>RadQRCode</h3>"+buildDate+"<br>by Phillip Cheng, MD MS<br><br>phillip.cheng@usc.edu"));
		tv.setLinkTextColor(Color.WHITE);
		Linkify.addLinks(tv, Linkify.ALL);
	}

}
