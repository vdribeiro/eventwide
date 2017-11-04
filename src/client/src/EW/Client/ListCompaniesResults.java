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

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ListCompaniesResults extends ListActivity {

	String[] names = null;
	String[] cities = null;
	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
        if (Main.companies==null) {
         	return;
         }
         int size=Main.companies.length;
         if (size==0) {
         	return;
         }
         
         names = new String[size];
         cities = new String[size];
         //int images[] = new int[size];
         
         for (int i=0;i<size;i++) {
         	names[i] = Main.companies[i].name;
         	cities[i] = Main.companies[i].city;
         }
         
         this.setListAdapter(new CompaniesAdapter(this, names, cities));
         //this.setListAdapter(new MyArrayAdapter(this, names, cities, images));
     	
     	final ListView lv = getListView();
     	lv.setTextFilterEnabled(true);
     	

     	lv.setOnItemClickListener(new OnItemClickListener() {
     		Intent IntentEvent=new Intent(getApplicationContext(), CompanyActivity.class);

     		public void onItemClick(AdapterView<?> parent, View view,
     				int position, long id) {
     			CompanyActivity.comp=Main.companies[position];
     			
     			HttpResponse response = getCompanyDetails(Main.companies[position].idc);
     			if (response==null)
     	        	return;
     			
     			InputStream instream;
				try {
					instream = response.getEntity().getContent();
					JSONObject comp = new JSONObject(read(instream));
					
					Main.companies[position].name = comp.getString("nome");	
					Main.companies[position].city = comp.getString("cidade");	
					Main.companies[position].address = comp.getString("morada");	
					Main.companies[position].desc = comp.getString("descricao");	

					JSONArray tels = comp.getJSONArray("telefones");
					
					int[] itels = new int[tels.length()];
					
					for(int i = 0; i < tels.length(); i++)
						itels[i] = tels.getInt(i);
					
					Main.companies[position].tels = itels;
					
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
     			startActivityForResult(IntentEvent,0);
     	  }
     	});
    	
    }
    
    private HttpResponse getCompanyDetails(String idc) {
		
		//String IP = "http://172.29.145.85:8080";
		String url = Main.IP + "/companies";
		
		try {
            final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 5000);
            
            String target = url+"?oper=viewcompany&idc=" + idc;

            HttpGet httpget = new HttpGet(target);
            HttpResponse response = httpClient.execute(httpget);
            int statusResponse=response.getStatusLine().getStatusCode();
            if(statusResponse!=200)	
					throw new Exception(Integer.toString(statusResponse));
            //Toast.makeText(this, Integer.toString(statusResponse), Toast.LENGTH_SHORT).show();
            return response;
            
		} catch (Exception e) {
			
			int status_code = Integer.parseInt(e.getMessage());
			switch(status_code) {
				case HttpURLConnection.HTTP_NO_CONTENT:
					Toast.makeText(this, "Company not found!", Toast.LENGTH_SHORT).show();
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
