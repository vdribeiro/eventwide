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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditUser extends Activity {
	
	EditText ET_gc_name;
	EditText ET_gc_user;
	EditText ET_gc_pass;
	EditText ET_name;
	EditText ET_pass;
	EditText ET_desc;
	EditText ET_addr;
	EditText ET_city;
	EditText ET_tel;
	EditText ET_tel2;
	EditText ET_tel3;
	
	static String gc_name;
	static String gc_user;
	static String gc_pass;
	static String user;
	static String name;
	static String pass;
	static String desc;
	static String addr;
	static String city;
	static String[] tel;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Window on fullscreen mode
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.edituser);
        
        final TextView ET_user = (TextView) findViewById(R.id.TextViewEdit);
        ET_user.setText("User " + user + " Information");
    	
    	ET_gc_name = (EditText) findViewById(R.id.editTextGCNameEdit);
    	ET_gc_user = (EditText) findViewById(R.id.editTextGCUserEdit);
    	ET_gc_pass = (EditText) findViewById(R.id.editTextGCPassEdit);
    	ET_name = (EditText) findViewById(R.id.editTextNameEdit);
    	ET_pass = (EditText) findViewById(R.id.editTextPasswordEdit);
    	ET_desc = (EditText) findViewById(R.id.editTextDescEdit);
    	ET_addr = (EditText) findViewById(R.id.editTextAddrEdit);
    	ET_city = (EditText) findViewById(R.id.editTextCityEdit);
    	ET_tel = (EditText) findViewById(R.id.editTextTelEdit);
    	ET_tel2 = (EditText) findViewById(R.id.editTextTelEdit2);
    	ET_tel3 = (EditText) findViewById(R.id.editTextTelEdit3);
        
    	// load to textboxes
    	ET_name.setText(name);
    	ET_pass.setText(pass);
    	ET_desc.setText(desc);
    	ET_addr.setText(addr);
    	ET_city.setText(city);
    	if (tel.length!=0) {
    		if (tel.length>=1)
        		ET_tel.setText(tel[0]);
    		if (tel.length>=2)
        		ET_tel2.setText(tel[1]);
    		if (tel.length>=3)
        		ET_tel3.setText(tel[2]);
    	}
    	ET_gc_name.setText(gc_name);
    	ET_gc_user.setText(gc_user);
    	ET_gc_pass.setText(gc_pass);
    	
    	/*Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
        	ET_user.setText("User " + extras.getString("user") + " Information");
        	
        	ET_name.setText(extras.getString("name"));
        	ET_pass.setText(extras.getString("pass"));
        	ET_desc.setText(extras.getString("desc"));
        	ET_addr.setText(extras.getString("addr"));
        	ET_city.setText(extras.getString("city"));
        	ET_tel.setText(extras.getString("tel"));
        	ET_gc_name.setText(extras.getString("gc_name"));
        	ET_gc_user.setText(extras.getString("gc_user"));
        	ET_gc_pass.setText(extras.getString("gc_pass"));
        	
        }*/

    }
    
    public void cancelAction(View view) throws Exception {
    	// load to textboxes
    	ET_name.setText(name);
    	ET_pass.setText(pass);
    	ET_desc.setText(desc);
    	ET_addr.setText(addr);
    	ET_city.setText(city);
    	if (tel.length!=0) {
    		if (tel.length>=1)
        		ET_tel.setText(tel[0]);
    		if (tel.length>=2)
        		ET_tel2.setText(tel[1]);
    		if (tel.length>=3)
        		ET_tel3.setText(tel[2]);
    	}
    	ET_gc_name.setText(gc_name);
    	ET_gc_user.setText(gc_user);
    	ET_gc_pass.setText(gc_pass);
    	
    	// Change to previous activity
        //Intent myIntentMenu = new Intent(this, Menu.class);
        //this.startActivity(myIntentMenu);
    	finish();
    }
    
    public void editAction(View view) throws Exception {
    	// HTTP values 
    	HttpResponse result=null;
    	int statuscode=400;
    	//InputStream instream=null;
    	
    	// load and check text values
        if ( 
    		(ET_gc_name.getText().toString().compareTo("")==0) || 
    		(ET_gc_user.getText().toString().compareTo("")==0) ||
    		(ET_gc_pass.getText().toString().compareTo("")==0) ||
    		(ET_name.getText().toString().compareTo("")==0) ||
    		(ET_pass.getText().toString().compareTo("")==0)
        	) {
        	Toast.makeText(getApplicationContext(), "Information Missing", Toast.LENGTH_SHORT).show();
        	if  (ET_gc_name.getText().toString().compareTo("")==0) {
            	Toast.makeText(getApplicationContext(), "GC Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
    		if (ET_gc_user.getText().toString().compareTo("")==0) {
    			Toast.makeText(getApplicationContext(), "GC User cannot be empty", Toast.LENGTH_SHORT).show();
    		}
    		if (ET_gc_pass.getText().toString().compareTo("")==0) {
    			Toast.makeText(getApplicationContext(), "GC Password cannot be empty", Toast.LENGTH_SHORT).show();
    		}
    		if (ET_name.getText().toString().compareTo("")==0) {
    			Toast.makeText(getApplicationContext(), "Company Name cannot be empty", Toast.LENGTH_SHORT).show();
    		}
    		if (ET_pass.getText().toString().compareTo("")==0) {
    			Toast.makeText(getApplicationContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
    		}
    		return;
        } else {
        	
        	// prepare the loading dialog box
            ProgressDialog dialog = new ProgressDialog(this);
            // make the progress bar cancelable
            dialog.setCancelable(true);
            // set a message text
            dialog.setMessage("Update in progress...");
            // show it
            dialog.show();
            
            // update information
        	gc_name=ET_gc_name.getText().toString();
            gc_user=ET_gc_user.getText().toString();
            gc_pass=ET_gc_pass.getText().toString();
            name=ET_name.getText().toString();
            pass=ET_pass.getText().toString();
            desc=ET_desc.getText().toString();
            addr=ET_addr.getText().toString();
            city=ET_city.getText().toString();
            if (tel.length!=0) {
        		if (tel.length>=1)
        			tel[0]=ET_tel.getText().toString();
        		if (tel.length>=2)
        			tel[1]=ET_tel2.getText().toString();
        		if (tel.length>=3)
        			tel[2]=ET_tel3.getText().toString();
        	}
            
            /*getIntent().putExtra("name", ET_name.getText().toString());
        	getIntent().putExtra("pass", ET_pass.getText().toString());
        	getIntent().putExtra("desc", ET_desc.getText().toString());
        	getIntent().putExtra("addr", ET_addr.getText().toString());
        	getIntent().putExtra("city", ET_city.getText().toString());
        	getIntent().putExtra("tel", ET_tel.getText().toString());
        	getIntent().putExtra("gc_name", ET_gc_name.getText().toString());
        	getIntent().putExtra("gc_user", ET_gc_user.getText().toString());
        	getIntent().putExtra("gc_pass", ET_gc_pass.getText().toString());*/
            
        	// send data
        	result=sendInfo(gc_name, gc_user, gc_pass,
            		user, name, pass, desc,
            		addr, city, tel);
        	
            if (result==null) {
            	Toast.makeText(getApplicationContext(), "User Information Update Error", Toast.LENGTH_SHORT).show();
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
            	Toast.makeText(getApplicationContext(), "Update Successful", Toast.LENGTH_SHORT).show();
            } else {
            	// unknown code
            	Toast.makeText(getApplicationContext(), "Unknown Response Code", Toast.LENGTH_SHORT).show();
            	return;
            }

            // Change to menu activity
            Intent myIntentMenu = new Intent(this, Menu.class);
            this.startActivity(myIntentMenu);
        }
        
    }
    
    //send user info
    public HttpResponse sendInfo(String gc_name, String gc_user, String gc_pass,
    		String user, String name, String pass, String desc,
    		String addr, String city, String[] tel) throws JSONException {
    	
    	HttpResponse response=null;
        try {
            String url = Login.IP + "/companies";
            final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("token", Login.token);
            JSONObject jsonuser=new JSONObject();
            JSONArray tels = new JSONArray();
            for (int i=0; i<tel.length;i++) {
            	//Toast.makeText(getApplicationContext(), tel[i], Toast.LENGTH_SHORT).show();
            	if(!tel[i].equals(""))
            		tels.put(Integer.parseInt(tel[i]));
            }
            
            jsonuser.put("telefones", tels);
            jsonuser.put("cidade", city);
            jsonuser.put("morada", addr);
            jsonuser.put("descricao", desc);
            jsonuser.put("password", pass);
            jsonuser.put("nome", name);
            jsonuser.put("gc_password", gc_pass);
            jsonuser.put("gc_username", gc_user);
            jsonuser.put("gc_nome", gc_name);
            
            String POSTText = jsonuser.toString();
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
    
    //convert inputstream to string
    /*private String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }*/
}
