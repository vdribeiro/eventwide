package EW.Client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Main extends TabActivity {
	
	//user info
	static String IP="http://255.255.255.255:8080";
	static String token="";
	static String gcalendar="";
	static String gusername="";
	static String gpassword="";
	
	//favorite companies
	//static String[] idcomps;
	//static String[] ncomps;
	//static int favsize;
	static ArrayList<String> idcomps;
	static ArrayList<String> ncomps;
	
	//events array
	static Event[] events;
	static Event[] todayevents;
	static Event[] monthevents;
	static Event[] weekevents;
	static Event[] bestevents;
	static Event[] favevents;
	
	static Company[] companies;
	
	//static HashMap<String, Event> checkedevents;
	static ArrayList<String> checkedevents;
	static ArrayList<String> checkedeventscid;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        
        // setup files
        setup();
        
        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab
        
        // Create an Intent to launch an Activity for the tab (to be reused) and
        // Initialize a TabSpec for each tab and add it to the TabHost
        intent = new Intent().setClass(this, BestActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec = tabHost.newTabSpec("best").setIndicator("Best",
                          res.getDrawable(R.drawable.best))
                      .setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, FavEvActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec = tabHost.newTabSpec("fav").setIndicator("Favorites",
                          res.getDrawable(R.drawable.fave))
                      .setContent(intent);
        tabHost.addTab(spec);

        /*intent = new Intent().setClass(this, ListActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec = tabHost.newTabSpec("list").setIndicator("List",
                          res.getDrawable(R.drawable.config))
                      .setContent(intent);
        tabHost.addTab(spec);*/
        
        intent = new Intent().setClass(this, TodayActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec = tabHost.newTabSpec("today").setIndicator("Today",
                          res.getDrawable(R.drawable.today))
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, WeekActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec = tabHost.newTabSpec("week").setIndicator("Week",
                          res.getDrawable(R.drawable.thisweek))
                      .setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, MonthActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec = tabHost.newTabSpec("month").setIndicator("Month",
                          res.getDrawable(R.drawable.thismonth))
                      .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);

        
    }

	private void setup() {
		idcomps = new ArrayList<String>();
		ncomps = new ArrayList<String>();
		//checkedevents = new HashMap<String, Event>();
		checkedevents = new ArrayList<String>();
		checkedeventscid = new ArrayList<String>();
		
		try {
			FileInputStream fis;
			/*
			// Open favorites file
			fis = openFileInput("EWCfav");
			
			// if the file exists count the number of lines
			int count = 0;
			InputStream is = new BufferedInputStream(fis);
		    try {
		        byte[] c = new byte[1024];
		        int readChars = 0;
		        while ((readChars = is.read(c)) != -1) {
		            for (int i = 0; i < readChars; ++i) {
		                if (c[i] == '\n')
		                    ++count;
		            }
		        }
		    } finally {
		        is.close();
		    }
		    
		    fis.close();
		    Toast.makeText(getApplicationContext(), "Count " + count, Toast.LENGTH_SHORT).show();
		    
		    
		    // Now open to read data
		    fis = openFileInput("EWCfav");
		    
		    //vector size
			int size = count/2;
			Toast.makeText(getApplicationContext(), "Size " + size, Toast.LENGTH_SHORT).show();
			
			if (size>0) {
				// Get the object of DataInputStream
				DataInputStream in = new DataInputStream(fis);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				
				idcomps = new String[size];
				ncomps = new String[size];
				
				//Read File Line By Line
				int i=0;
				String strLine;
				while ((strLine = br.readLine()) != null)   {
					idcomps[i] = strLine;
					ncomps[i] = br.readLine();
					i++;
				}
				
			}
			fis.close();
			*/
			
			// Open favorites file
			fis = openFileInput("EWCfav");
			
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			//Toast.makeText(getApplicationContext(), "Size " + idcomps.size(), Toast.LENGTH_SHORT).show();
			
			//Read File Line By Line
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				idcomps.add(strLine);
				ncomps.add(br.readLine());
			}
			
			//Toast.makeText(getApplicationContext(), "Size " + idcomps.size(), Toast.LENGTH_SHORT).show();
			
			fis.close();
			
		} catch (Exception e) {
			// File does not exist
			try {
				// Create file 
				FileOutputStream fos = openFileOutput("EWCfav", Context.MODE_PRIVATE);
				//fos.write("0\n".getBytes());
				fos.close();
			} catch (Exception e2) {
				return;
			}
		}
		
		try {
			FileInputStream fis;
			
			// Open checked file
			fis = openFileInput("EWCche");
			
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			//Read File Line By Line
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				checkedevents.add(strLine);
				//checkedeventscid.add(br.readLine());
			}
			fis.close();
			
		} catch (Exception e) {
			// File does not exist
			try {
				// Create file 
				FileOutputStream fos = openFileOutput("EWCche", Context.MODE_PRIVATE);
				fos.close();
			} catch (Exception e2) {
				return;
			}
		}
        
		try {
			// Open Configuration file
			FileInputStream fis = openFileInput("EWC");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File
			strLine = br.readLine();
			IP = strLine;
			strLine = br.readLine();
			gcalendar=strLine;
			strLine = br.readLine();
			gusername=strLine;
			strLine = br.readLine();
			gpassword=strLine;
	        fis.close();
	        //Toast.makeText(getApplicationContext(), "Configurations Loaded", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Intent config = new Intent(this, ConfigActivity.class);
			startActivity(config);
			return;
		}
	}
}
