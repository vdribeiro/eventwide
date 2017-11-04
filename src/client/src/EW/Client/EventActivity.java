package EW.Client;

import java.io.FileOutputStream;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class EventActivity extends Activity {
	
	static Event evt;
	
	Button AR;
	Button FAV;
	TextView EName;
    TextView EDesc;
    TextView EPlac;
    TextView EComp;
    TextView ESDat;
    TextView EEDat;
    TextView EPA;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event);
        
        EName = (TextView) findViewById(R.id.TextViewName);
        EDesc = (TextView) findViewById(R.id.textViewDesc);
        EPlac = (TextView) findViewById(R.id.textViewPlace);
        EComp = (TextView) findViewById(R.id.textViewCompany);
        ESDat = (TextView) findViewById(R.id.textViewSD);
        EEDat = (TextView) findViewById(R.id.textViewED);
        EPA = (TextView) findViewById(R.id.textViewPA);
        AR = (Button) findViewById(R.id.buttonEditAddRm);
        FAV = (Button) findViewById(R.id.buttonFav);
        
        EName.setText(evt.Name);
        EDesc.setText(evt.Description);
        EPlac.setText("Place: " + evt.Place);
        EComp.setText("Company: " + evt.Company);
        ESDat.setText("Start Date: " + evt.Start);
        EEDat.setText("End Date: " + evt.End);
        EPA.setText(evt.Contador + " people will be attending this event.");
        
        if (evt.Check) {
        	AR.setText("Remove Event");
        } else {
        	AR.setText("Add Event");
        }
        
        // test if company is favorite
        if (Main.idcomps.contains(evt.Idc)) {
        	FAV.setText("Remove from Favorites");
    	} else {
    		FAV.setText("Add to Favorites");
    	}
        
    }
    
    public void cancelAction(View view) throws Exception {
    	Intent intent = new Intent();
    	setResult(RESULT_CANCELED, intent);
    	finish();
    }
    
    public void addrmAction(View view) throws Exception {
    	Intent intent = new Intent();
    	
        /*Bundle bundle = new Bundle();
        bundle.putString("ok", "ok");
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);*/
    	
    	if (evt.Check) {
    		setResult(2,intent);
        } else {
        	setResult(1, intent);
        }
        
    	finish();
    }
    
    public void favAction(View view) throws Exception {
    	
    	if (Main.idcomps.contains(evt.Idc)) { // remove    		
    		Main.idcomps.remove(evt.Idc);
    		Main.ncomps.remove(evt.Company);
    		FAV.setText("Add to Favorites");
    		Toast.makeText(getApplicationContext(), "Removed from Favorites", Toast.LENGTH_SHORT).show();
    	} else { // add
    		Main.idcomps.add(evt.Idc);
    		Main.ncomps.add(evt.Company);
    		FAV.setText("Remove from Favorites");
    		Toast.makeText(getApplicationContext(), "Added to Favorites", Toast.LENGTH_SHORT).show();
    	}
    	
    	try {
			FileOutputStream fos = openFileOutput("EWCfav", Context.MODE_PRIVATE);
			for (int i=0;i<Main.idcomps.size();i++) {
				String temp1 = Main.idcomps.get(i) + "\n";
				String temp2 = Main.ncomps.get(i) + "\n";
				fos.write(temp1.getBytes());
				fos.write(temp2.getBytes());
			}
			fos.close();
			//Toast.makeText(getApplicationContext(), "ok", Toast.LENGTH_SHORT).show();
		} catch (Exception e2) {
			return;
		}
    }
    
    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_BACK ) {
            Log.d("BACK BUTTON", "BACK pressed");
            Intent IntentBack = new Intent(this, Main.class);
            this.startActivity(IntentBack);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/

}
