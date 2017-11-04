package EW.Client;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

public class BestActivity extends ListActivity {
	
	String[] names;
	String[] places;
	int[] images;
	int clicked = -1;

    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        
        // get best
        Main.bestevents=getBestEvents();
        
        if (Main.bestevents==null) {
        	return;
        }
        int size=Main.bestevents.length;
        if (size==0) {
        	return;
        }
        
        names = new String[size];
        places = new String[size];
        images = new int[size];
        
        for (int i=0;i<size;i++) {
        	names[i] = Main.bestevents[i].Name;
        	places[i] = Main.bestevents[i].Place;
        	/*if (Main.bestevents[i].Check) {
        		images[i] = R.drawable.ok;
        	} else {
        		images[i] = 0;
        	}*/
        	if (Main.checkedevents.contains(Main.bestevents[i].Ide)) {
        		images[i] = R.drawable.ok;
        		Main.bestevents[i].Check=true;
        	} else {
        		images[i] = 0;
        		Main.bestevents[i].Check=false;
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
    			String ide = Main.bestevents[position].Ide;
    			boolean check = Main.bestevents[position].Check;
    			
    			Main.bestevents[position] = getEventDetail(ide);
    			Main.bestevents[position].Check=check;
    			
    			EventActivity.evt=Main.bestevents[position];
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
	    			Main.bestevents[clicked].Check=true;
	    			if (!Main.checkedevents.contains(Main.bestevents[clicked].Ide)) {
	    				Main.checkedevents.add(Main.bestevents[clicked].Ide);
	    			}
	    			this.setListAdapter(new MyArrayAdapter(this, names, places, images));
	    			mark(Main.bestevents[clicked].Ide,"check");
	    			putCalendar(Main.bestevents[clicked]);
	    			try {
	    				FileOutputStream fos = openFileOutput("EWCche", Context.MODE_PRIVATE);
	    				for (int i=0;i<Main.checkedevents.size();i++) {
	    					String temp = Main.checkedevents.get(i) + "\n";
	    					//String temp2 = Main.checkedeventscid.get(i) + "\n";
	    					fos.write(temp.getBytes());
	    					//fos.write(temp2.getBytes());
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
	    			Main.bestevents[clicked].Check=false;
	    			if (Main.checkedevents.contains(Main.bestevents[clicked].Ide)) {
	    				Main.checkedevents.remove(Main.bestevents[clicked].Ide);
	    			}
	    			this.setListAdapter(new MyArrayAdapter(this, names, places, images));
	    			mark(Main.bestevents[clicked].Ide,"uncheck");
	    			rmCalendar(Main.bestevents[clicked]);
	    			try {
	    				FileOutputStream fos = openFileOutput("EWCche", Context.MODE_PRIVATE);
	    				for (int i=0;i<Main.checkedevents.size();i++) {
	    					String temp = Main.checkedevents.get(i) + "\n";
	    					//String temp2 = Main.checkedeventscid.get(i) + "\n";
	    					fos.write(temp.getBytes());
	    					//fos.write(temp2.getBytes());
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
    	/*if (Main.gcalendar==null) return false;
		if (Main.gpassword==null) return false;
		if (Main.gusername==null) return false;
		if (Main.gcalendar.compareToIgnoreCase("")==0) return false;
		if (Main.gpassword.compareToIgnoreCase("")==0) return false;
		if (Main.gusername.compareToIgnoreCase("")==0) return false;
		
		URL feedUrl;
			try {
				feedUrl = new URL("https://www.google.com/calendar/feeds/default");
				 CalendarService myService = new CalendarService("exampleCo-exampleApp-1");
				 myService.setUserCredentials(Main.gusername, Main.gpassword);
				 
				 CalendarEventFeed myFeed = myService.getFeed(feedUrl, CalendarEventFeed.class);
				 //CalendarEventFeed resultFeed = myService.query(myQuery, CalendarEventFeed.class);
				 String idCalendario = "";
				 for(int i = 0; i<myFeed.getEntries().size(); i++){
					 //System.out.println(myFeed.getEntries().get(i).getTitle().getPlainText());
					 if(Main.gcalendar.toLowerCase().equals(myFeed.getEntries().get(i).getTitle().getPlainText().toLowerCase())){
						 //System.out.println("Encontrei.");
						 String temp[] = myFeed.getEntries().get(i).getId().toString().split("/");
						 idCalendario = temp[temp.length-1];
						 break;
					 }
				 }
				 //System.out.println(idCalendario);
				 //se nao encontrou o calendario entao cria-o...
				 if(idCalendario.equals("")){
					// Create the calendar
					 System.out.println("Calendario nao encontrado");
					 return false;					 
				 }
				 
				 
				URL postUrl = new URL("https://www.google.com/calendar/feeds/"+idCalendario+"/private/full");
				CalendarEventFeed eventFeed = myService.getFeed(postUrl, CalendarEventFeed.class);
				
				 for(int i = 0; i<eventFeed.getEntries().size(); i++){
					 String[] temp = eventFeed.getEntries().get(i).getId().toString().split("/");
					 String idEvnt = temp[(temp.length-1)];
					 System.out.println("Id do evento: "+idEvnt);
					 int index = Main.checkedevents.indexOf(event.Ide);
					 if (index<0) return false;
					 if(idEvnt.equals(Main.checkedeventscid.get(index))){
						 CalendarEventEntry cee = (CalendarEventEntry)eventFeed.getEntries().get(i);
						 cee.delete();
						 //System.out.println("Removido");
						 return true;
					 }
				 }

				//System.out.println("Não removido.");
				return false;
				 
			} catch (MalformedURLException e) {
				//System.out.println("URL mal formado.");
				return false;
				//e.printStackTrace();
			}
			// feedUrl = new URL("https://www.google.com/calendar/feeds/"+username+"/private/full");
			catch (AuthenticationException e) {
				//System.out.println("Erro na autenticacao.");
				return false;
				//e.printStackTrace();
			} catch (IOException e) {
				//System.out.println("IO Exception.");
				return false;
				//e.printStackTrace();
			} catch (ServiceException e) {
				//System.out.println("Service exception.");
				return false;
				//e.printStackTrace();
			}*/
	}

	private void putCalendar(Event event) {
		
		/*if (Main.gcalendar==null) return false;
		if (Main.gpassword==null) return false;
		if (Main.gusername==null) return false;
		if (Main.gcalendar.compareToIgnoreCase("")==0) return false;
		if (Main.gpassword.compareToIgnoreCase("")==0) return false;
		if (Main.gusername.compareToIgnoreCase("")==0) return false;
		
		Toast.makeText(getApplicationContext(),"aqui", Toast.LENGTH_SHORT).show();
		
		URL feedUrl;
		try {
			 feedUrl = new URL("https://www.google.com/calendar/feeds/default");
			 
			 //CalendarService myService = new CalendarService("exampleCo-exampleApp-1");
			 CalendarService myService = new CalendarService(getApplicationContext().toString());
			
			 myService.setUserCredentials(Main.gusername, Main.gpassword);
			 
			 CalendarEventFeed myFeed = myService.getFeed(feedUrl, CalendarEventFeed.class);
			 //CalendarEventFeed resultFeed = myService.query(myQuery, CalendarEventFeed.class);
			 String idCalendario = "";
			 
			 for(int i = 0; i<myFeed.getEntries().size(); i++){
				 //System.out.println(myFeed.getEntries().get(i).getTitle().getPlainText());
				 //Toast.makeText(getApplicationContext(), myFeed.getEntries().get(i).getTitle().getPlainText(), Toast.LENGTH_SHORT).show();
				 if(Main.gcalendar.toLowerCase().equals(myFeed.getEntries().get(i).getTitle().getPlainText().toLowerCase())){
					 //System.out.println("Entrei");
					 String temp[] = myFeed.getEntries().get(i).getId().toString().split("/");
					 idCalendario = temp[temp.length-1];
					 break;
				 }
			 }
			 //System.out.println(idCalendario);
			 //se nao encontrou o calendario entao cria-o...
			 if(idCalendario.equals("")){
				// Create the calendar
				 CalendarEntry calendar = new CalendarEntry();
				 calendar.setTitle(new PlainTextConstruct(Main.gcalendar));
				 calendar.setSummary(new PlainTextConstruct("Calendario dedicado ao EventWide."));
				 //calendar.setTimeZone(new TimeZoneProperty("America/Los_Angeles"));
				 calendar.setHidden(HiddenProperty.FALSE);
				 calendar.setColor(new ColorProperty("#2952A3"));
				 //calendar.addLocation(new Where("","","Oakland"));

				 // Insert the calendar
				 URL postUrl = new URL("https://www.google.com/calendar/feeds/default/owncalendars/full");
				 CalendarEntry returnedCalendar = myService.insert(postUrl, calendar);
				 String cid = returnedCalendar.getId();
				 if (!Main.checkedeventscid.contains(cid)) {
					 Main.checkedeventscid.add(cid);
				 }
				 //return adicionarEvento(Main.gcalendar, Main.gusername, Main.gpassword, event.Name, event.Description, event.Start, event.End, event.Place);
				 return putCalendar(event);
				 
			 }
			 
			 URL postUrl = new URL("https://www.google.com/calendar/feeds/"+idCalendario+"/private/full");
				CalendarEventEntry myEntry = new CalendarEventEntry();

				myEntry.setTitle(new PlainTextConstruct(event.Name));
				myEntry.setContent(new PlainTextConstruct(event.Description));
				
				String parseIni[] = event.Start.split(" ");
				String dataIni = parseIni[0]+"T"+parseIni[1]+"-00:00";
				DateTime startTime = DateTime.parseDateTime(dataIni);
				String parseFim[] = event.End.split(" ");
				String dataFim = parseFim[0]+"T"+parseFim[1]+"-00:00";
				DateTime endTime = DateTime.parseDateTime(dataFim);
				When eventTimes = new When();
				eventTimes.setStartTime(startTime);
				eventTimes.setEndTime(endTime);
				myEntry.addTime(eventTimes);
				
				
				Where local = new Where();
				local.setValueString(event.Place);
				myEntry.addLocation(local);


				CalendarEventEntry insertedEntry = myService.insert(postUrl, myEntry);
				//Toast.makeText(getApplicationContext(), "Added.", Toast.LENGTH_SHORT).show();
				//(System.out.println("Adicionado.");
				return true;
			 
		} catch (MalformedURLException e) {
			//System.out.println("URL mal formado.");
			return false;
			//e.printStackTrace();
		}
		// feedUrl = new URL("https://www.google.com/calendar/feeds/"+username+"/private/full");
		catch (AuthenticationException e) {
			//System.out.println("Erro na autenticacao.");
			return false;
			//e.printStackTrace();
		} catch (IOException e) {
			//System.out.println("IO Exception.");
			return false;
			//e.printStackTrace();
		} catch (ServiceException e) {
			//System.out.println("Service exception.");
			return false;
			//e.printStackTrace();
		} catch (Exception e) {
			return false;
		}*/
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
        
        // analyze statuscode
        if (!status(statuscode)) {
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
    
	private Event[] getBestEvents() {
		int statuscode=400;
		HttpResponse response=null;
		Event[] revents=null;
		
    	// prepare the loading dialog box
        ProgressDialog dialog = new ProgressDialog(this);
        // make the progress bar cancelable
        dialog.setCancelable(true);
        // set a message text
        dialog.setMessage("Fetching Best Events...");
        // show it
        dialog.show();
        
        String url = Main.IP + "/events?oper=7dias";
        
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
        	HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();
            String buffer=read(instream);
            
            JSONArray arrayReceive = new JSONArray(buffer);
            int size=arrayReceive.length();
            revents = new Event[size];
            
            for(int i=0;i<size;i++){
            	JSONObject jsondetail = arrayReceive.getJSONObject(i);
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
		
        Toast.makeText(getApplicationContext(), "Best Events loaded", Toast.LENGTH_SHORT).show();
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
