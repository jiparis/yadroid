package com.thegame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.Menu.Item;
import android.widget.Toast;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        setupGameLibrary();
    }
    
    protected void setupGameLibrary(){
    	
    }
   
    
    //Setup menus
    
    private final int MENU_GAMELOAD = 		Menu.FIRST;
    private final int MENU_GAMEDOWNLOAD = 	Menu.FIRST + 1;

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, MENU_GAMELOAD, getText(R.string.game_load));		
		menu.add(0, MENU_GAMEDOWNLOAD, getText(R.string.game_download));	
		
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, Item item) {
		super.onMenuItemSelected(featureId, item);
		Toast toast;
//		NotificationManager nm = 
//            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
		switch (item.getId()) {
		case MENU_GAMELOAD:
            toast = Toast.makeText(this, R.string.game_loading,
                    Toast.LENGTH_LONG);
            toast.show();
//			nm.notifyWithText(R.layout.main, getText(R.string.game_loading),
//                    NotificationManager.LENGTH_LONG, null);	
			startSubActivity(new Intent(this, GameActivity.class), MENU_GAMELOAD);
			
			break;
		case MENU_GAMEDOWNLOAD:
            toast = Toast.makeText(this, R.string.game_download,
                    Toast.LENGTH_LONG);
            toast.show();
//			nm.notifyWithText(R.layout.main, getText(R.string.game_download),
//                    NotificationManager.LENGTH_LONG, null);			
			break;
		default:
			break;
		}
		return true;
	}
    
    
}