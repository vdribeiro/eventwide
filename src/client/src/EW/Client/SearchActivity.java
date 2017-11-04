package EW.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity {
	//private TextView mDateDisplay;
	private TextView startDate;
	private TextView endDate;
    //private Button mPickDate;
    private int sYear, eYear;
    private int sMonth, eMonth;
    private int sDay, eDay;
    
    //indicam se o user especificou as datas    
    boolean startAltered = false, endAltered = false;

    static final int START_DATE_DIALOG_ID = 0;
    static final int END_DATE_DIALOG_ID = 1;
	    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        // capture our View elements
        startDate = (TextView) findViewById(R.id.startDate);
        endDate = (TextView) findViewById(R.id.endDate);

        //Toast.makeText(this,"" + (startDate == null), Toast.LENGTH_SHORT).show();
        
        // add a click listener to the text views
        startDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(START_DATE_DIALOG_ID);
            }
        });
        endDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(END_DATE_DIALOG_ID);
            }
        });

        // get the current date
        final Calendar c = Calendar.getInstance();
        sYear = c.get(Calendar.YEAR);
        sMonth = c.get(Calendar.MONTH);
        sDay = c.get(Calendar.DAY_OF_MONTH);
        eYear = c.get(Calendar.YEAR);
        eMonth = c.get(Calendar.MONTH);
        eDay = c.get(Calendar.DAY_OF_MONTH);
                
        // display the current date (this method is below)
        //updateDisplay(0);
    }
    
    // updates the date in the TextView
    private void updateDisplay(int mode) {
    	if(mode == 0) {
    		startDate.setText(
	            new StringBuilder()
	                    // Month is 0 based so add 1
	            		.append("Start Date: ")
	                    .append(sMonth + 1).append("-")
	                    .append(sDay).append("-")
	                    .append(sYear).append(" "));
    		startAltered = true;
    	}
    	else if(mode == 1) {
	        endDate.setText(
	                new StringBuilder()
	                        // Month is 0 based so add 1
	                		.append("End Date: ")
	                        .append(eMonth + 1).append("-")
	                        .append(eDay).append("-")
	                        .append(eYear).append(" "));
	        endAltered = true;
    	}
    }
    
    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener startDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                    sYear = year;
                    sMonth = monthOfYear;
                    sDay = dayOfMonth;
                    updateDisplay(0);
                }
            };
            
    private DatePickerDialog.OnDateSetListener endDateSetListener =
        new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, 
                                  int monthOfYear, int dayOfMonth) {
                eYear = year;
                eMonth = monthOfYear;
                eDay = dayOfMonth;
                updateDisplay(1);
            }
        };
            
        @Override
        protected Dialog onCreateDialog(int id) {
            switch (id) {
            case START_DATE_DIALOG_ID:
                return new DatePickerDialog(getParent(),
                            startDateSetListener,
                            sYear, sMonth, sDay);
            case END_DATE_DIALOG_ID:
                return new DatePickerDialog(getParent(),
                            endDateSetListener,
                            eYear, eMonth, eDay);
            }
            return null;
        }
    
    public void searchAction(View view) throws Exception {
    	// HTTP values 
    	HttpResponse result=null;
    	//int statuscode=400;
    	InputStream instream=null;
    	//String temp=null;
    	
    	String inicio = (startAltered)? sYear + "-" + (sMonth + 1) + "-" + sDay : "";
    	String fim = (endAltered)? eYear + "-" + (eMonth + 1) + "-" + eDay : "";

    	// Text Object
    	EditText EName = (EditText) findViewById(R.id.editTextEventName);
    	EditText EDesc = (EditText) findViewById(R.id.editTextEventDesc);
    	EditText EPlace = (EditText) findViewById(R.id.editTextEventPlace);
    	EditText EComp = (EditText) findViewById(R.id.editTextEventCompany);

    	
    	if ( 
    		(EName.getText().toString().compareTo("")==0) && 
    		(EDesc.getText().toString().compareTo("")==0) &&
    		(EPlace.getText().toString().compareTo("")==0) &&
    		(EComp.getText().toString().compareTo("")==0) &&
    		(inicio.equals("")) &&
    		(fim.equals(""))
            ) 
    	{
            Toast.makeText(this, "Fill at least one field.", Toast.LENGTH_SHORT).show();
        	return;
    	}
        
        // send search
        result=search(EName.getText().toString(),EDesc.getText().toString(),
        		EPlace.getText().toString(),EComp.getText().toString(),
        		inicio, fim);
        if (result==null) {
        	//close loading dialog
           // dialog.dismiss();
        	return;
        }
                
        instream = result.getEntity().getContent();
        JSONArray events = new JSONArray(read(instream));
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
        
        //for(int i = 0; i < Main.events.length; i++)
        	//Toast.makeText(this, "EV: " + Main.events[i].Name, Toast.LENGTH_LONG).show();
        
        
        // close loading dialog
        //dialog.dismiss();
        
        Intent iListEvent = new Intent(this, ListEventResults.class);
        this.startActivity(iListEvent);
    }

	private HttpResponse search(String name, String desc, String place,
			String company, String start, String end) {
		
		//String IP = "http://172.29.145.85:8080";
		String url = Main.IP + "/events";
		
		try {
            final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 5000);
            
            String target = url+"?oper=searchevent";
            
            if(!place.equals(""))
            	target += "&onde=" + place;
            if(!company.equals(""))
            	target += "&empresa=" + company;
            if(!name.equals(""))
            	target += "&nome=" + name;
            if(!desc.equals(""))
            	target += "&descricao=" + desc;
            if(!start.equals(""))
            	target += "&dinicio=" + start;
            if(!end.equals(""))
            	target += "&dfim=" + end;
            
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
