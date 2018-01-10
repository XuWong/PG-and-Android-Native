package fr.nconcepts.plugin;
 
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class Echo extends CordovaPlugin {
   public static final String SEND_ECHO = "sendEcho";
   
   @Override
   public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
 
       try {
          if (SEND_ECHO.equals(action)) {
             JSONObject arg_object = args.getJSONObject(0);
				   
		     arg_object.getString("message");
				   
             callbackContext.success(arg_object.getString("message"));
             return true;
          }
          callbackContext.error("Invalid action");
          return false;
      } catch(Exception e) {
          System.err.println("Exception: " + e.getMessage());
          callbackContext.error(e.getMessage());
          return false;
      } 
 
   }
}