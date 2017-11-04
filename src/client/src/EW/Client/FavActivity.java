package EW.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class FavActivity extends ListActivity {
	
	String[] names;
	String[] places;
	int[] images;
	int clicked = -1;

    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	int size = Main.idcomps.size();
    	
    	names = new String[size];
        places = new String[size];
        images = new int[size];
    	
    	//names = Main.idcomps.toArray(new String[Main.idcomps.size()]);
    	//places = Main.ncomps.toArray(new String[Main.ncomps.size()]);
        names = Main.ncomps.toArray(new String[Main.ncomps.size()]);
    	//Arrays.fill(names, "");
    	Arrays.fill(places, "");
    	Arrays.fill(images, 0);
    	
    	this.setListAdapter(new MyArrayAdapter(this, names, places, images));
    	
    	final ListView lv = getListView();
    	lv.setTextFilterEnabled(true);

    	lv.setOnItemClickListener(new OnItemClickListener() {
    		Intent IntentCompany=new Intent(getApplicationContext(), CompanyActivity.class);

    		public void onItemClick(AdapterView<?> parent, View view,
    				int position, long id) {
    			clicked = position;
    			
    			String idc = Main.idcomps.get(position);

    			CompanyActivity.comp=getCompanyDetail(idc);
    			
    			startActivityForResult(IntentCompany,0);
    	  }
    	});
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if (requestCode==0)
    	{
    		switch (resultCode)
    		{ case RESULT_OK:
    			break;
    		case RESULT_CANCELED:
    			break;
    		}
    	}
    	
    }
    
    public Company getCompanyDetail(String idc){
    	int statuscode=400;
		HttpResponse response=null;
		Company rcomp=null;
		
    	// prepare the loading dialog box
        ProgressDialog dialog = new ProgressDialog(this);
        // make the progress bar cancelable
        dialog.setCancelable(true);
        // set a message text
        dialog.setMessage("Fetching Company Detail...");
        // show it
        dialog.show();
        
        String url = Main.IP + "/companies?oper=viewcompany&idc=" + idc;

    	try {
            final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
            HttpGet httpget = new HttpGet(url);
            response = httpClient.execute(httpget);
    	} catch (Exception e) {
			 //Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
	       	Toast.makeText(getApplicationContext(), "Error on HTTP", Toast.LENGTH_SHORT).show();
	       	dialog.dismiss();
	       	return null;
		}
    	
    	if (response==null) {
        	Toast.makeText(getApplicationContext(), "Server Error", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        	return null;
        }
		
		// obtain status code 
        statuscode=response.getStatusLine().getStatusCode();
        
        // analyze statuscode
        if (!status(statuscode)) {
        	dialog.dismiss();
        	return null;
        }
        
        try {
        	
        	InputStream instream = response.getEntity().getContent();
			JSONObject comp = new JSONObject(read(instream));
			
			rcomp = new Company();
        	rcomp.idc = idc;
        	rcomp.name = comp.getString("nome");	
        	rcomp.city = comp.getString("cidade");	
        	rcomp.address = comp.getString("morada");	
        	rcomp.desc = comp.getString("descricao");	

			JSONArray tels = comp.getJSONArray("telefones");
			
			int[] itels = new int[tels.length()];
			
			for(int i = 0; i < tels.length(); i++)
				itels[i] = tels.getInt(i);
			
			rcomp.tels = itels;
           
        } catch (Exception e) {
			//Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
        	Toast.makeText(getApplicationContext(), "Error loading", Toast.LENGTH_SHORT).show();
        	dialog.dismiss();
        	return null;
		}
		
        dialog.dismiss();
		return rcomp;
    }
    
    private static String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }
	
	public boolean status(int statuscode) {
        if (statuscode==200) {
        	return true;
        } else if (statuscode==204) {
        	Toast.makeText(getApplicationContext(), "No content", Toast.LENGTH_SHORT).show();
        } else if (statuscode==400) {
        	Toast.makeText(getApplicationContext(), "Bad Request", Toast.LENGTH_SHORT).show();
        } else if (statuscode==403) {
        	Toast.makeText(getApplicationContext(), "Access forbidden", Toast.LENGTH_SHORT).show();
        } else if (statuscode==404) {
        	Toast.makeText(getApplicationContext(), "Not Found", Toast.LENGTH_SHORT).show();
        } else if (statuscode==409) {
        	Toast.makeText(getApplicationContext(), "Conflict", Toast.LENGTH_SHORT).show();
        } else if (statuscode==500) {
        	Toast.makeText(getApplicationContext(), "Internal Server Error", Toast.LENGTH_SHORT).show();
        } else {
        	Toast.makeText(getApplicationContext(), "Unknown Response Code", Toast.LENGTH_SHORT).show();
        }
        return false;
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
}
