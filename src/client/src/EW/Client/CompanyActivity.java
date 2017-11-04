package EW.Client;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CompanyActivity extends Activity {
	
	static Company comp;
	
	Button FAV;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.company);
        
        TextView CName = (TextView) findViewById(R.id.CompanyName);
        TextView CCity = (TextView) findViewById(R.id.CompanyCity);
        TextView CAddr = (TextView) findViewById(R.id.CompanyAddress);
        TextView CContacts = (TextView) findViewById(R.id.CompanyContacts);
        TextView CDesc = (TextView) findViewById(R.id.CompanyDesc);
        
        FAV = (Button) findViewById(R.id.buttonFavc);
        
        String contacts = "Contacts: ";
        if(comp.tels.length != 0)
        	contacts += comp.tels[0];
        for(int i = 1; i < comp.tels.length; i++)
        	contacts += ", " + comp.tels[i];
        
        CName.setText(comp.name);
        CCity.setText("City: " + comp.city);
        CAddr.setText("Address: " + comp.address);
        CContacts.setText(contacts);
        CDesc.setText(comp.desc);
        
        if (Main.idcomps.contains(comp.idc)) {
        	FAV.setText("Remove from Favorites");
    	} else {
    		FAV.setText("Add to Favorites");
    	}
    }
    
    public void cancelAction(View view) throws Exception {
    	Intent intent = new Intent();
    	setResult(RESULT_CANCELED, intent);
    	finish();
    }
    
    public void addAction(View view) throws Exception {
    	Intent intent = new Intent();
        /*Bundle bundle = new Bundle();
        bundle.putString("ok", "ok");
        intent.putExtras(bundle);*/
        setResult(RESULT_OK, intent);
    	finish();
    }
    
    public void favAction(View view) throws Exception {
    	
    	if (Main.idcomps.contains(comp.idc)) { // remove    		
    		Main.idcomps.remove(comp.idc);
    		Main.ncomps.remove(comp.name);
    		FAV.setText("Add to Favorites");
    		Toast.makeText(getApplicationContext(), "Removed from Favorites", Toast.LENGTH_SHORT).show();
    	} else { // add
    		Main.idcomps.add(comp.idc);
    		Main.ncomps.add(comp.name);
    		FAV.setText("Remove from Favorites");
    		Toast.makeText(getApplicationContext(), "Added to Favorites", Toast.LENGTH_SHORT).show();
    	}
    	
    	try {
			FileOutputStream fos = openFileOutput("EWCfav", Context.MODE_PRIVATE);
			for (int i=0;i<Main.idcomps.size();i++) {
				String temp1 = Main.idcomps.get(i) + "\n";
				String temp2 = Main.ncomps.get(i) + "\n";
				fos.write(temp1.getBytes());
				fos.write(temp2.getBytes());
			}
			fos.close();
			//Toast.makeText(getApplicationContext(), "ok", Toast.LENGTH_SHORT).show();
		} catch (Exception e2) {
			return;
		}
    }
    
    public void viewCompanyEvents(View view) {
    	HttpResponse response = getCompanyEvents(comp.idc);
    	
    	if(response == null)
    		return;
    	try {
	    	InputStream instream = response.getEntity().getContent();
			JSONObject body = new JSONObject(read(instream));
			JSONArray events = body.getJSONArray("eventos");
			
			Event[] events_array = new Event[events.length()];
		        
	        for(int i=0;i<events.length();i++){
	        	JSONObject jsonevent = events.getJSONObject(i);
	        	
	        	Event e = new Event();
	        	e.Ide = jsonevent.getString("id");
	        	e.Place = jsonevent.getString("onde");
	        	e.Company = jsonevent.getString("nome_emp");
	        	e.Name = jsonevent.getString("nome");
	        	e.Start = jsonevent.getString("dinicio");
	        	
	        	events_array[i] = e;
	        }
	        
	        Main.events = events_array;
    	} catch (Exception e) {
    		return;
    	}
             
        Intent iListEvent = new Intent(this, ListEventResults.class);
        this.startActivity(iListEvent);
	
    	
    }
    
    private HttpResponse getCompanyEvents(String idc) {
		
		//String IP = "http://172.29.145.85:8080";
		String url = Main.IP + "/events";
		
		try {
            final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 5000);
            
            String target = url+"?oper=companyevents&idc=" + idc;

            HttpGet httpget = new HttpGet(target);
            HttpResponse response = httpClient.execute(httpget);
            int statusResponse=response.getStatusLine().getStatusCode();
            if(statusResponse!=200)	
					throw new Exception(Integer.toString(statusResponse));
           
            return response;
            
		} catch (Exception e) {
			
			int status_code = Integer.parseInt(e.getMessage());
			switch(status_code) {
				case HttpURLConnection.HTTP_NO_CONTENT:
					Toast.makeText(this, "This company has no events yet!", Toast.LENGTH_SHORT).show();
					break;
				default:
					Toast.makeText(this, "Search error: " + status_code, Toast.LENGTH_SHORT).show();
					break;
			}
				
			return null;
		}
		
	}
	
	// convert inputstream to string
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
