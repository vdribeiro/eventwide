package EW.Company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {
	
	static String IP="http://255.255.255.255:8080";
	static String token="";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main); 
        
        final EditText serverip = (EditText) findViewById(R.id.editTextIP);
        
        //get own IP
        /*
		try {
			Enumeration e = NetworkInterface.getNetworkInterfaces();

			while(e.hasMoreElements()) {
			    NetworkInterface ni = (NetworkInterface) e.nextElement();
			    //Toast.makeText(getApplicationContext(), "Net interface: " + ni.getName(), Toast.LENGTH_SHORT).show();
			    //System.out.println("Net interface: "+ni.getName());
			
				Enumeration e2 = ni.getInetAddresses();
				
				while (e2.hasMoreElements()){
				   InetAddress ip = (InetAddress) e2.nextElement();
				   //Toast.makeText(getApplicationContext(), "IP address: "+ ip.toString(), Toast.LENGTH_SHORT).show();
				   //System.out.println("IP address: "+ ip.toString());
				}
			}

		    //IP="http://" + hostname + ":8080";
		} catch (Exception ex) {
			ex.printStackTrace();
		}*/
        
        serverip.setText(IP);
    }
    
    public void loginAction(View view) throws Exception {
    	// HTTP values 
    	HttpResponse result=null;
    	int statuscode=400;
    	InputStream instream=null;
    	
    	boolean logflag=false;
    	String temp;

    	// String values
    	String user = "", name = "", pass = "", 
    	desc = "", addr = "", city = "", tel[], 
    	gc_name = "", gc_user = "", gc_pass = "";
    	
    	// load text values
    	final AutoCompleteTextView username = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewUsername);
    	final EditText password = (EditText) findViewById(R.id.editTextPassword);
    	final EditText serverip = (EditText) findViewById(R.id.editTextIP);
        user = username.getText().toString();
        pass = password.getText().toString();
        IP=serverip.getText().toString();
        
        // prepare the loading dialog box
        ProgressDialog dialog = new ProgressDialog(this);
        // make the progress bar cancelable
        dialog.setCancelable(true);
        // set a message text
        dialog.setMessage("Authenticating...");
        // show it
        dialog.show();
       
        // send login
        result=login(user,pass);
        if (result==null) {
        	Toast.makeText(getApplicationContext(), "Login Error", Toast.LENGTH_SHORT).show();
        	//close loading dialog
            dialog.dismiss();
        	return;
        }
        
        // print result values
        instream = result.getEntity().getContent();
        temp=read(instream);
        //Toast.makeText(getApplicationContext(), result.getStatusLine().toString(), Toast.LENGTH_SHORT).show();
        //Toast.makeText(getApplicationContext(), "ENTITY/BODY: " + temp, Toast.LENGTH_LONG).show();
        
        // obtain status code 
        statuscode=result.getStatusLine().getStatusCode();
        
        // close loading dialog
        dialog.dismiss();
        
        // analyze if login is successful
        if (statuscode==200) {
            // success
        	Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
        	//obtain token 
            token=temp.substring(temp.indexOf("=")+1);
            //Toast.makeText(getApplicationContext(), token, Toast.LENGTH_LONG).show();
        } else if (statuscode==403) {
        	// show error if login is incorrect
            // prepare the alert box
            AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
            // set the message to display
            alertbox.setMessage("Incorrect Login");
            // add a neutral button to the alert box and assign a click listener
            alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                // click listener on the alert box
                public void onClick(DialogInterface arg0, int arg1) {
                    // the button was clicked
                    Toast.makeText(getApplicationContext(), "Insert New Login", Toast.LENGTH_LONG).show();
                }
            });
            // show it
            alertbox.show();
        	return;
        } else {
        	// unknown code
        	Toast.makeText(getApplicationContext(), "Unknown Response Code", Toast.LENGTH_SHORT).show();
        	return;
        }
        
        // request user information
        dialog.setMessage("Requesting User Information");
        dialog.show();
        
        result=requestuserinfo(user);
        if (result==null) {
        	Toast.makeText(getApplicationContext(), "User Information Request Error", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        	return;
        }
        
        // print result values
        instream = result.getEntity().getContent();
        JSONObject body = new JSONObject(read(instream));
        JSONArray tels = new JSONArray();
        //Toast.makeText(getApplicationContext(), result.getStatusLine().toString(), Toast.LENGTH_SHORT).show();
        //Toast.makeText(getApplicationContext(), "ENTITY/BODY: " + temp, Toast.LENGTH_LONG).show();
        
        // obtain status code and user info 
        statuscode=result.getStatusLine().getStatusCode();
        gc_name=(String) body.get("gc_nome");
        gc_user=(String) body.get("gc_username");
        gc_pass=(String) body.get("gc_password");
        name=(String) body.get("nome");
        desc=(String) body.get("descricao");
        addr=(String) body.get("morada");
        city=(String) body.get("cidade");
        tels = body.getJSONArray("telefones");
        
        tel=new String[tels.length()];
        for (int i=0; i<tels.length();i++) {
        	tel[i]=new String (Integer.toString(tels.getInt(i)));
        }
        
        if ( 
        		(gc_name.compareTo("")==0) ||
        		(gc_user.compareTo("")==0) ||
        		(gc_pass.compareTo("")==0) ||
        		(user.compareTo("")==0) ||
        		(name.compareTo("")==0) ||
        		(pass.compareTo("")==0)
        	){
        	logflag=false;
        } else {
        	logflag=true;
        }
        
        dialog.dismiss();
        
        EditUser.gc_name=gc_name;
        EditUser.gc_user=gc_user;
        EditUser.gc_pass=gc_pass;
        EditUser.user=user;
        EditUser.name=name;
        EditUser.pass=pass;
        EditUser.desc=desc;
        EditUser.addr=addr;
        EditUser.city=city;
        EditUser.tel = new String[3];
        for (int i=0; i<3;i++) {
        	EditUser.tel[i]="";
        }
        for (int i=0; i<tels.length();i++) {
        	EditUser.tel[i]=tel[i];
        }
        
        
        /*IntentFirstEdit.putExtra("user", user);
        IntentFirstEdit.putExtra("name", name);
        IntentFirstEdit.putExtra("pass", pass);
        IntentFirstEdit.putExtra("desc", desc);
        IntentFirstEdit.putExtra("addr", addr);
        IntentFirstEdit.putExtra("city", city);
        IntentFirstEdit.putExtra("tel", tel);
        IntentFirstEdit.putExtra("gc_name", gc_name);
        IntentFirstEdit.putExtra("gc_user", gc_user);
        IntentFirstEdit.putExtra("gc_pass", gc_pass);*/
        
        // analyze result and show next layout depending on user info
        if (logflag) {
        	Intent IntentFirstMenu = new Intent(this, Menu.class);
            this.startActivity(IntentFirstMenu);
        } else {
        	Intent IntentFirstEdit = new Intent(this, EditUser.class);
            this.startActivity(IntentFirstEdit);
        }
    }
    
    //send HTTP login
    public HttpResponse login(String user, String pass) throws JSONException {
    	HttpResponse response=null;
        try {
            String url = IP + "/accounts";
            final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
            HttpPost httpPost = new HttpPost(url);
            JSONObject jsonoper=new JSONObject();
            JSONObject jsonlogin=new JSONObject();
            jsonlogin.put("pass", pass);
            jsonlogin.put("username", user);
            jsonoper.put("account", jsonlogin);
            jsonoper.put("oper", "login");
            String POSTText = jsonoper.toString();
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
    
    // send HTTP user info request
    public HttpResponse requestuserinfo(String user) throws JSONException {
    	HttpResponse response=null;
        try {
            String url = IP + "/companies?oper=getcompany";
            final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("token", token);
            response = httpClient.execute(httpget);
        } catch (IOException ex) {
            //Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        	Toast.makeText(getApplicationContext(), "Error on HTTP", Toast.LENGTH_SHORT).show();
        }
		return response;
    }
    
    //convert inputstream to string
    private String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }
}