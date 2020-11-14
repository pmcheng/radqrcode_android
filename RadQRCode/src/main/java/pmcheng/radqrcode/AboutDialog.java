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

		TextView tv = (TextView)findViewById(R.id.legal_text);
		tv.setText(Html.fromHtml("Uses the ZXing Barcode Reader<br>https://github.com/zxing/zxing"));
		Linkify.addLinks(tv, Linkify.ALL);
		
		tv = (TextView)findViewById(R.id.info_text);
		tv.setText(Html.fromHtml("<h3>RadQRCode</h3><br>by Phillip Cheng, MD MS<br><br>phillip.cheng@med.usc.edu"));
		tv.setLinkTextColor(Color.WHITE);
		Linkify.addLinks(tv, Linkify.ALL);
	}

}
