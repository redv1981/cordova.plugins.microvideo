package cordova.plugins.microvideo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

public class MicroVideo extends CordovaPlugin {

	private CallbackContext m_callbackContext;

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (action.equals("record")) {
			return record(callbackContext);
		}

		return false;
	}

	protected boolean record(CallbackContext callbackContext) {
		m_callbackContext = callbackContext;
		Intent intent = new Intent().setClassName(cordova.getActivity(), "cordova.plugins.microvideo.MicroVideoActivity");
		cordova.startActivityForResult(this, intent, 1);

		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (resultCode) {
			case Activity.RESULT_OK:
				Bundle bundle = intent.getExtras();
				ArrayList<String> pathArray = bundle.getStringArrayList("result");
				m_callbackContext.success(new JSONArray(pathArray));
				break;

			case Activity.RESULT_CANCELED:
			default:
				m_callbackContext.success();
				break;
		}
	}

}
