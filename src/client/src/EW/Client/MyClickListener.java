package EW.Client;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class MyClickListener implements OnItemClickListener{
	
	Intent IntentEvent;
	
	public MyClickListener() {}
	
	public MyClickListener(Context packageContext, Class<?> cls) {
		IntentEvent = new Intent().setClass(packageContext, cls);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,
  	      int position, long id) {
            //startActivity(IntentEvent);
  	  }

}
