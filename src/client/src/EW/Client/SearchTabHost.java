package EW.Client;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class SearchTabHost extends TabActivity {
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        
        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab
        
        intent = new Intent().setClass(this, SearchActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec = tabHost.newTabSpec("search_events").setIndicator("Events",
                          res.getDrawable(R.drawable.ic_tab_events))
                      .setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, CompaniesSearchActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec = tabHost.newTabSpec("search_companies").setIndicator("Companies",
                          res.getDrawable(R.drawable.ic_tab_companies))
                      .setContent(intent);
        tabHost.addTab(spec);
       

        tabHost.setCurrentTab(0);

        
    }
}
