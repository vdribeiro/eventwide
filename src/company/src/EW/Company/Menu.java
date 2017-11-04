package EW.Company;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Menu extends Activity {
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        
        // load username
        final TextView M_user = (TextView) findViewById(R.id.textViewMenuUser);
        final TextView M_comp = (TextView) findViewById(R.id.textViewMenuCompany);
        final TextView M_cal = (TextView) findViewById(R.id.textViewMenuCalendar);
        M_user.setText("User: " + EditUser.user);
        M_comp.setText("Company: " + EditUser.name);
        M_cal.setText("Calendar: " + EditUser.gc_name);
        
    }
    
    public void quit(View view) {
        finish();
    }
    
    public void syncAction(View view) throws Exception {
    	// HTTP values 
    	HttpResponse result=null;
    	int statuscode=400;
    	//InputStream instream=null;
    	
    	// prepare the loading dialog box
        ProgressDialog dialog = new ProgressDialog(this);
        // make the progress bar cancelable
        dialog.setCancelable(true);
        // set a message text
        dialog.setMessage("Synchronization in progress...");
        // show it
        dialog.show();
    	
    	// send sync
    	result=sendSync();
    	
        if (result==null) {
        	Toast.makeText(getApplicationContext(), "Synchronization Error", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        	return;
        }
        
        // print result values
        //instream = result.getEntity().getContent();
        //Toast.makeText(getApplicationContext(), result.getStatusLine().toString(), Toast.LENGTH_SHORT).show();
        //Toast.makeText(getApplicationContext(), "ENTITY/BODY: " + read(instream), Toast.LENGTH_LONG).show();
        
        // obtain status code and user info 
        statuscode=result.getStatusLine().getStatusCode();
        dialog.dismiss();
        
        if (statuscode==200) {
            // success
        	Toast.makeText(getApplicationContext(), "Synchronization Successful", Toast.LENGTH_LONG).show();
        } 
        else if(statuscode == 409)
        	Toast.makeText(getApplicationContext(), "Google Calendar's credentials are invalid.", Toast.LENGTH_SHORT).show();
        else if(statuscode ==204)
        	Toast.makeText(getApplicationContext(), "Invalid calendar name.", Toast.LENGTH_SHORT).show();
        else if(statuscode == 500)
        	Toast.makeText(getApplicationContext(), "Server Internal Error.", Toast.LENGTH_SHORT).show();
        else {
        	// unknown code
        	Toast.makeText(getApplicationContext(), "Unknown Response Code", Toast.LENGTH_SHORT).show();
        	return;
        }
        
        return;
        
    }
 
    public void editmenuAction(View view) {
     
     // Change to edituser activity
     Intent myIntentM = new Intent(this, EditUser.class);
     this.startActivity(myIntentM);
     
     
    }
    
    //send sync
    public HttpResponse sendSync() throws JSONException {
    	
    	HttpResponse response=null;
        try {
            String url = Login.IP + "/events";
            final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("token", Login.token);
            JSONObject jsonobj=new JSONObject();
            jsonobj.put("oper", "import");
            String POSTText = jsonobj.toString();
            StringEntity entity = new StringEntity(POSTText, "UTF-8");
            BasicHeader basicHeader = new BasicHeader(HTTP.CONTENT_TYPE, "application/json");
            httpPost.getParams().setBooleanParameter("http.protocol.expect-continue", false);
            entity.setContentType(basicHeader);
            httpPost.setEntity(entity);
            response = httpClient.execute(httpPost);
        } catch (IOException ex) {
            //Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        	Toast.makeText(getApplicationContext(), "Error on HTTP", Toast.LENGTH_SHORT).show();
        }
		return response;
    }
    
}
