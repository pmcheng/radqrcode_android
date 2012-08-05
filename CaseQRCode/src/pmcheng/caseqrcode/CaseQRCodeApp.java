package pmcheng.caseqrcode;

import android.app.Application;
import android.util.Log;

public class CaseQRCodeApp extends Application {

	private static final String TAG = "CaseQRCodeApp";
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
