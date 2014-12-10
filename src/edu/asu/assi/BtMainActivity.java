package edu.asu.assi;

import com.example.mcassi.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class BtMainActivity extends ActionBarActivity {
	BluetoothAdapter btAdp = BluetoothAdapter.getDefaultAdapter();
    private ArrayAdapter<String> BTArrayAdapter;
    ListView myListView;
    OnItemClickListener exampleListener;
	//Discovered devices list
	final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override        
		public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                 // Get the BluetoothDevice object from the Intent
                 BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                 BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                 BTArrayAdapter.notifyDataSetChanged();
            }
	    }
	};
	
	public void find(View view) {
	       if (btAdp.isDiscovering()) {
	    	   btAdp.cancelDiscovery();
	       }
	       else {
	    	   btAdp.startDiscovery();
	           registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
	       }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bt_main);

		Button btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				find(v);
			}
		});
		
		myListView = (ListView)findViewById(R.id.listView1);
		
		// Create the arrayAdapter that contains the BTDevices, and set it to the ListView
		BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		myListView.setAdapter(BTArrayAdapter);
		
		exampleListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				btAdp.cancelDiscovery();

	            // Get the device MAC address, which is the last 17 chars in the View
	            String info = ((TextView) view).getText().toString();
	            String address = info.substring(info.length() - 17);
	            // Create the result Intent and include the MAC address
	            Intent intent = new Intent();
	            intent.putExtra("deviceaddr", address);
	            // Set result and finish this Activity
	            setResult(Activity.RESULT_OK, intent);
	            finish();
			}
		};
		
		myListView.setOnItemClickListener(exampleListener);		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bt_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater
					.inflate(R.layout.bt_main, container, false);
			return rootView;
		}
	}
	
	@Override
    public void onPause() {

        super.onPause();

        myListView.setOnItemClickListener(null);

    }

@Override
    public void onResume() {

        super.onResume();
            if(myListView != null){
            	myListView.setOnItemClickListener(exampleListener);
             }
    }
    
}
