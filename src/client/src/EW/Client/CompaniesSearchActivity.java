package EW.Client;

import java.io.BufferedReader;
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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class CompaniesSearchActivity extends Activity{

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_companies);
	}
	
	public void searchAction(View view) throws Exception {
    	
    	// HTTP values 
    	HttpResponse result=null;
    	//int statuscode=400;
    	InputStream instream=null;
    	//String temp=null;
    
    	// Text Object
    	EditText EName = (EditText) findViewById(R.id.editTextCompanyName);

    	
    	if ( EName.getText().toString().compareTo("")==0 ) {
            Toast.makeText(this, "Please enter a name to search for.", Toast.LENGTH_SHORT).show();
        	return;
    	}
    	
        // send search
        result=search(EName.getText().toString());
        if (result==null) {
        	//close loading dialog
           // dialog.dismiss();
        	return;
        }
                
        instream = result.getEntity().getContent();
        JSONArray companies = new JSONArray(read(instream));
        
        Company[] companies_array = new Company[companies.length()];
        
        for(int i=0;i<companies.length();i++){
        	JSONObject comp = companies.getJSONObject(i);
        	
        	Company c = new Company();
        	c.idc = comp.getString("idc");
        	c.name = comp.getString("nome");
        	c.city = comp.getString("cidade");
        	
        	companies_array[i] = c;
        }
        
        Main.companies = companies_array;
        
        //for(int i = 0; i < Main.companies.length; i++)
        	//Toast.makeText(this, "EV: " + Main.companies[i].name, Toast.LENGTH_LONG).show();
        
        Intent iListComp = new Intent(this, ListCompaniesResults.class);
        this.startActivity(iListComp);
        
        
    }
	
	
	
	private HttpResponse search(String name) {
		
		//String IP = "http://172.29.145.85:8080";
		String url = Main.IP + "/companies";
		
		try {
            final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 5000);
            
            String target = url+"?oper=searchcompany&q=" + name;
            
            //substituir todos os espaços
            target = target.replaceAll(" ", "%20");
              
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
					Toast.makeText(this, "No results found!", Toast.LENGTH_SHORT).show();
					break;
				default:
					Toast.makeText(this, "Search error: " + status_code, Toast.LENGTH_SHORT).show();
					break;
			}
				
			return null;
		}
		
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            Log.d("MENU BUTTON", "MENU pressed");
            Intent IntentMain = new Intent(this, Main.class);
            this.startActivity(IntentMain);
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
