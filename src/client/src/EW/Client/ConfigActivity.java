package EW.Client;

import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ConfigActivity extends Activity {
	
	EditText ECName;
	EditText ECPass;
	EditText ECUser;
	EditText ECIP;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);
        ECName = (EditText) findViewById(R.id.editTextCalendar);
        ECUser = (EditText) findViewById(R.id.editTextCuser);
    	ECPass = (EditText) findViewById(R.id.editTextCPassword);
    	ECIP = (EditText) findViewById(R.id.editTextIP);
    	
    	ECName.setText(Main.gcalendar);
    	ECUser.setText(Main.gusername);
    	ECPass.setText(Main.gpassword);
    	ECIP.setText(Main.IP);
    }
    
    public void saveAction(View view) throws Exception {
    	Main.gcalendar = ECName.getText().toString();
    	Main.gusername = ECUser.getText().toString();
    	Main.gpassword = ECPass.getText().toString();
    	Main.IP = ECIP.getText().toString();
    	
    	if (
			//(Main.gcalendar.compareToIgnoreCase("")==0) ||
			//(Main.gpassword.compareToIgnoreCase("")==0) ||
			(Main.IP.compareToIgnoreCase("")==0)
    	) {
    		Toast.makeText(getApplicationContext(), "Information Missing", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
		// Create file 
		FileOutputStream fos = openFileOutput("EWC", Context.MODE_PRIVATE);
		fos.write((Main.IP + "\n").getBytes());
		fos.write((Main.gcalendar + "\n").getBytes());
		fos.write((Main.gusername + "\n").getBytes());
		fos.write((Main.gpassword + "\n").getBytes());
		fos.close();
		
		Intent main = new Intent(this, Main.class);
		startActivity(main);
		
    	//finish();		

    }
    
    public void cancelAction(View view) throws Exception {
    	ECName.setText("");
    	ECPass.setText("");
    	//finish();
    }
    
    public void infoAction(View view) throws Exception {
    	Toast.makeText(getApplicationContext(), "EventWide 1.0", Toast.LENGTH_SHORT).show();
    	Toast.makeText(getApplicationContext(), "Specify Google Calendar", Toast.LENGTH_LONG).show();
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
