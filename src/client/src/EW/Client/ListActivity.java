package EW.Client;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class ListActivity extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        
        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab
        
        intent = new Intent().setClass(this, TodayActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec = tabHost.newTabSpec("today").setIndicator("Today",
                          res.getDrawable(R.drawable.today))
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, WeekActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec = tabHost.newTabSpec("week").setIndicator("This Week",
                          res.getDrawable(R.drawable.thisweek))
                      .setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, MonthActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec = tabHost.newTabSpec("month").setIndicator("This Month",
                          res.getDrawable(R.drawable.thismonth))
                      .setContent(intent);
        tabHost.addTab(spec);
        
        /*intent = new Intent().setClass(this, MenuActivity.class);
        spec = tabHost.newTabSpec("menu").setIndicator("Menu",
                          res.getDrawable(R.drawable.menu))
                      .setContent(intent);
        tabHost.addTab(spec);*/

        tabHost.setCurrentTab(0);

        
    }
}
