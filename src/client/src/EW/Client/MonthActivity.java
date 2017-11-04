package EW.Client;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import org.apache.http.HttpEntity;
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
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MonthActivity extends ListActivity {
	
	String[] names;
	String[] places;
	int[] images;
	int clicked = -1;

    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	// get the current date
        Calendar c = Calendar.getInstance();
        int Year = c.get(Calendar.YEAR);
        int Month = c.get(Calendar.MONTH);
        int Day = c.get(Calendar.DAY_OF_MONTH);
        
        c.add(Day, 30);
        
        String dinicio = Year + "-" + (Month + 1) + "-" + Day;
    	String dfim = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);;
    	
		// get month
    	Main.monthevents=SearchEvents(dinicio, dfim);
    	
        if (Main.monthevents==null) {
        	return;
        }
        int size=Main.monthevents.length;
        if (size==0) {
        	return;
        }
        
        names = new String[size];
        places = new String[size];
        images = new int[size];
        
        for (int i=0;i<size;i++) {
        	names[i] = Main.monthevents[i].Name;
        	places[i] = Main.monthevents[i].Place;
        	if (Main.checkedevents.contains(Main.monthevents[i].Ide)) {
        		images[i] = R.drawable.ok;
        		Main.monthevents[i].Check=true;
        	} else {
        		images[i] = 0;
        		Main.monthevents[i].Check=false;
        	}
        }
    	
    	this.setListAdapter(new MyArrayAdapter(this, names, places, images));
    	
    	final ListView lv = getListView();
    	lv.setTextFilterEnabled(true);

    	lv.setOnItemClickListener(new OnItemClickListener() {
    		Intent IntentEvent=new Intent(getApplicationContext(), EventActivity.class);

    		public void onItemClick(AdapterView<?> parent, View view,
    				int position, long id) {
    			clicked = position;
    			String ide = Main.monthevents[position].Ide;
    			boolean check = Main.monthevents[position].Check;
    			
    			Main.monthevents[position] = getEventDetail(ide);
    			Main.monthevents[position].Check=check;
    			
    			EventActivity.evt=Main.monthevents[position];
    			startActivityForResult(IntentEvent,0);
    	  }
    	});
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if (requestCode==0)
    	{
    		switch (resultCode) { 
	    		case 1:
	    			Toast.makeText(getApplicationContext(), "Event Added", Toast.LENGTH_SHORT).show();
	    			images[clicked]=R.drawable.ok;
	    			Main.monthevents[clicked].Check=true;
	    			if (!Main.checkedevents.contains(Main.monthevents[clicked].Ide)) {
	    				Main.checkedevents.add(Main.monthevents[clicked].Ide);
	    			}
	    			this.setListAdapter(new MyArrayAdapter(this, names, places, images));
	    			mark(Main.monthevents[clicked].Ide,"check");
	    			putCalendar(Main.monthevents[clicked]);
	    			try {
	    				FileOutputStream fos = openFileOutput("EWCche", Context.MODE_PRIVATE);
	    				for (int i=0;i<Main.checkedevents.size();i++) {
	    					String temp = Main.checkedevents.get(i) + "\n";
	    					String temp2 = Main.checkedeventscid.get(i) + "\n";
	    					fos.write(temp.getBytes());
	    					fos.write(temp2.getBytes());
	    				}
	    				fos.close();
	    				//Toast.makeText(getApplicationContext(), "ok", Toast.LENGTH_SHORT).show();
	    			} catch (Exception e2) {
	    				return;
	    			}
	    			break;
	    		case 2:
	    			Toast.makeText(getApplicationContext(), "Event Removed", Toast.LENGTH_SHORT).show();
	    			images[clicked]=0;
	    			Main.monthevents[clicked].Check=false;
	    			if (Main.checkedevents.contains(Main.monthevents[clicked].Ide)) {
	    				Main.checkedevents.remove(Main.monthevents[clicked].Ide);
	    			}
	    			this.setListAdapter(new MyArrayAdapter(this, names, places, images));
	    			mark(Main.monthevents[clicked].Ide,"uncheck");
	    			rmCalendar(Main.monthevents[clicked]);
	    			try {
	    				FileOutputStream fos = openFileOutput("EWCche", Context.MODE_PRIVATE);
	    				for (int i=0;i<Main.checkedevents.size();i++) {
	    					String temp = Main.checkedevents.get(i) + "\n";
	    					fos.write(temp.getBytes());
	    				}
	    				fos.close();
	    				//Toast.makeText(getApplicationContext(), "ok", Toast.LENGTH_SHORT).show();
	    			} catch (Exception e2) {
	    				return;
	    			}
	    			break;
	    		case RESULT_CANCELED:
	    			//Toast.makeText(getApplicationContext(), "cancelei", Toast.LENGTH_SHORT).show();
	    			break;
    		}
    	}
    }
    
    private void rmCalendar(Event event) {
		
	}

	private void putCalendar(Event event) {
		
	}

	private void mark(String ide, String check) {
        int statuscode=400;
		HttpResponse response=null;
		
    	// prepare the loading dialog box
        ProgressDialog dialog = new ProgressDialog(getParent());
        // make the progress bar cancelable
        dialog.setCancelable(true);
        // set a message text
        dialog.setMessage("Updating Server...");
        // show it
        dialog.show();

        String url = Main.IP + "/events";

    	try {
    		final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
            HttpPost httppost = new HttpPost(url);
            
            JSONObject jsonObj=new JSONObject();
    		jsonObj.put("ide", ide);
            jsonObj.put("oper", check);
            String POSTText = jsonObj.toString();
            
            StringEntity entity = new StringEntity(POSTText, "UTF-8");
            BasicHeader basicHeader = new BasicHeader(HTTP.CONTENT_TYPE, "application/json");
            httppost.getParams().setBooleanParameter("http.protocol.expect-continue", false);
            entity.setContentType(basicHeader);
            httppost.setEntity(entity);
            
            response = httpClient.execute(httppost);
    	} catch (Exception e) {
			 //Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
	       	Toast.makeText(getApplicationContext(), "Error on HTTP", Toast.LENGTH_SHORT).show();
	       	dialog.dismiss();
	       	return;
		}
    	
    	if (response==null) {
        	Toast.makeText(getApplicationContext(), "Server Error", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        	return;
        }
		
		// obtain status code 
        statuscode=response.getStatusLine().getStatusCode();
        
        // analyze if login is successful
        if (statuscode==200) {
            // success
        	//Toast.makeText(getApplicationContext(), "Best Events received", Toast.LENGTH_SHORT).show();
        } else {
        	// unknown code
        	Toast.makeText(getApplicationContext(), "Unknown Response Code", Toast.LENGTH_SHORT).show();
        	dialog.dismiss();
        	return;
        }
		
        dialog.dismiss();
		return;
	}
    
    public Event getEventDetail(String ide){
    	int statuscode=400;
		HttpResponse response=null;
		Event revent=null;
		
    	// prepare the loading dialog box
        ProgressDialog dialog = new ProgressDialog(this);
        // make the progress bar cancelable
        dialog.setCancelable(true);
        // set a message text
        dialog.setMessage("Fetching Event Detail...");
        // show it
        dialog.show();

        String url = Main.IP + "/events?oper=eventinfo&ide="+ide;

    	try {
            final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 5000);
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
        
        // analyze if login is successful
        if (statuscode==200) {
            // success
        	//Toast.makeText(getApplicationContext(), "Best Events received", Toast.LENGTH_SHORT).show();
        } else {
        	// unknown code
        	Toast.makeText(getApplicationContext(), "Unknown Response Code", Toast.LENGTH_SHORT).show();
        	dialog.dismiss();
        	return null;
        }
        
        try {
        	HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();
            String buffer=read(instream);
            
        	JSONObject jsonevent = new JSONObject(buffer);
        	
        	String id = jsonevent.getString("ide");
        	String idc = jsonevent.getString("idc");
        	String nome = jsonevent.getString("nome");
        	String desc = jsonevent.getString("desc");
        	String onde = jsonevent.getString("onde");
        	String emp = jsonevent.getString("nome_empresa");
        	String di = jsonevent.getString("dinicio");
        	String df = jsonevent.getString("dfim");
        	String cont = jsonevent.getString("contador");

        	revent = new Event(id,idc,nome,desc,onde,emp,di,df,cont);
           
        } catch (Exception e) {
			//Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
        	Toast.makeText(getApplicationContext(), "Error loading", Toast.LENGTH_SHORT).show();
        	dialog.dismiss();
        	return null;
		}
		
        dialog.dismiss();
		return revent;
    }
    
    private Event[] SearchEvents(String dinicio, String dfim) {
		int statuscode=400;
		HttpResponse response=null;
		Event[] revents=null;
		
		//Toast.makeText(getApplicationContext(), dinicio + " " + dfim, Toast.LENGTH_SHORT).show();
		
    	// prepare the loading dialog box
        ProgressDialog dialog = new ProgressDialog(getParent());
        // make the progress bar cancelable
        dialog.setCancelable(true);
        // set a message text
        dialog.setMessage("Fetching Month's Events...");
        // show it
        dialog.show();
        
        String url = Main.IP + "/events?oper=searchevent&dinicio=" + dinicio + "&dfim=" + dfim;
        
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
            JSONArray events = new JSONArray(read(instream));
            revents = new Event[events.length()];
            
            for(int i=0;i<events.length();i++){
            	
            	JSONObject jsondetail = events.getJSONObject(i);
            	String ide = jsondetail.getString("id");
            	String idc = ""; //jsondetail.getString("idc");
            	String nome = jsondetail.getString("nome");
            	String desc = ""; //jsondetail.getString("desc");
            	String onde = jsondetail.getString("onde");
            	String emp = jsondetail.getString("nome_emp");
            	String di = jsondetail.getString("dinicio");
            	String df = ""; //jsondetail.getString("dfim");
            	String cont = ""; //jsondetail.getString("contador")
            	
            	Event ev = new Event(ide,idc,nome,desc,onde,emp,di,df,cont);
            	//Toast.makeText(getApplicationContext(), ide + nome + onde + emp + di, Toast.LENGTH_SHORT).show();
    			revents[i] = ev;
            	
            }
           
        } catch (Exception e) {
			//Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
        	Toast.makeText(getApplicationContext(), "Error loading", Toast.LENGTH_SHORT).show();
        	dialog.dismiss();
        	return null;
		}
		
        Toast.makeText(getApplicationContext(), "Month's Events loaded", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
		return revents;
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
            Intent IntentMenu = new Intent(this, MenuActivity.class);
            this.startActivity(IntentMenu);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}