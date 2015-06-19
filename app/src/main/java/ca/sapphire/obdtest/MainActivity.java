package ca.sapphire.obdtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    TextView statusText;
    private final static int REQUEST_ENABLE_BT = 1;
    public final static String EXTRA_DEVICE_ADDRESS = "device_address";
    BluetoothAdapter mBluetoothAdapter;
    private static final UUID OBD_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = (TextView) findViewById(R.id.statusText);


//        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            statusText.setText("Bluetooth not supported");
            return;
        }

        statusText.setText("Bluetooth supported");

        // cancel discovery, we don't need to do it.
        mBluetoothAdapter.cancelDiscovery();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            continueConnect();
        }
    }

    public void continueConnect() {
        statusText.append("\nBluetooth enabled");
//        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item);
//        ArrayAdapter mArrayAdapter = new ArrayAdapter();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
// If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                statusText.append( "\n" + device.getName() + " : " + device.getAddress() );
            }
        }

        String MACaddress = "00:04:3E:6A:9E:0F";

        // Create the result Intent and include the MAC address
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS, MACaddress);

        // Set result and finish this Activity
        setResult(MainActivity.RESULT_OK, intent);

        View view = this.findViewById(android.R.id.content);

        // **add this 2 line code**
        Intent myIntent = new Intent(view.getContext(), Connect.class);
        myIntent.putExtra(EXTRA_DEVICE_ADDRESS, MACaddress);
        startActivityForResult(myIntent, 0);




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if( requestCode == REQUEST_ENABLE_BT )
        {
            if( resultCode == RESULT_OK ) {
                Toast.makeText(MainActivity.this, "Bluetooth enabled.", Toast.LENGTH_SHORT).show();
                continueConnect();
            }
            if( resultCode == RESULT_CANCELED ) {
                Toast.makeText(MainActivity.this, "Quiting - Bluetooth is disabled.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
