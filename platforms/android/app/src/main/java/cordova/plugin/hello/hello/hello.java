package cordova.plugin.hello;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Message;
import android.telecom.Call;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.infologs.app.LNService;
import com.infologs.app.MainActivity;
import com.infologs.app.ServiceCallbacks;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * This class echoes a string called from JavaScript.
 */
public class hello extends CordovaPlugin implements ServiceCallbacks{
    String tag="uln";
    public void callBack(String content) {
        updateUI(content);
    }

    public interface updateUI {
        void updateUI(String content);
    }
    private void updateUI(String content) {
        String msg=content.trim();
        Log.i(tag, "Activity update ui with: " + msg);
        String jsCode="appText('"+msg+"');";
        Log.i(tag,jsCode);
        webView.sendJavascript(jsCode);
    }

    LNService lnService = null;
    boolean mBound = false;
    boolean init = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LNService.LocalBinder binder = (LNService.LocalBinder) service;
            lnService = binder.getService();
            mBound = true;
            lnService.setCallbacks(hello.this);
            Log.i(tag,"Bound true");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Log.i(tag,"Bound false");
        }
    };


    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("actionShow")) {
            String message = args.getString(0)+"modify";
            this.coolMethod(message, callbackContext);
            return true;
        }
        if (action.equals("cmd"))
        {
            String message = args.getString(0);
            Log.i(tag,"receive cmd:"+message);
//            this.coolMethod(message, callbackContext);
            Intent startIntent = new Intent(cordova.getActivity(), LNService.class);
            cordova.getActivity().bindService(startIntent, mConnection, BIND_AUTO_CREATE);
            if (mBound) {
                lnService.sendCommand(message);
            }
            this.coolMethod(message, callbackContext);
            return true;

        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
