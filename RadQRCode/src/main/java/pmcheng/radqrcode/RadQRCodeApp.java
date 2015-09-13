package pmcheng.radqrcode;

import android.app.Application;
import android.util.Log;

public class RadQRCodeApp extends Application {

	private static final String TAG = "RadQRCodeApp";
	private CaseData caseData;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG,"onCreate");
		
		this.caseData = new CaseData(this);
	}

	public CaseData getCaseData() {
		return caseData;
	}

}
