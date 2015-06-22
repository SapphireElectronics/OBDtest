package ca.sapphire.obdtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    TextView statusText;
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_CONNECT_BT = 2;

    public final static String EXTRA_DEVICE_ADDRESS = "device_address";
    BluetoothAdapter mBluetoothAdapter;
    private static final UUID OBD_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Handler timerHandler = new Handler();

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

        BluetoothDevice btDevice = null;
        String MACaddress = "00:04:3E:6A:9E:0F";

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
// If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                statusText.append( "\n" + device.getName() + " : " + device.getAddress() );
                if (device.getAddress().equals(MACaddress) ) {
                    btDevice = device;

                }
            }
        }

        if( btDevice == null ) {
            statusText.append( "\nMAC address not matched" );
            finish();
        }

        statusText.append( "\nIdentified BT device by MAC address" );


        // Create the result Intent and include the MAC address
//        Intent intent = new Intent();
//        intent.putExtra(EXTRA_DEVICE_ADDRESS, MACaddress);

        // Set result and finish this Activity
//        setResult(MainActivity.RESULT_OK, intent);

        /*
        View view = this.findViewById(android.R.id.content);
        Intent connectIntent = new Intent(view.getContext(), Connect.class);
        connectIntent.putExtra(EXTRA_DEVICE_ADDRESS, MACaddress);
        startActivityForResult(connectIntent, REQUEST_CONNECT_BT);
*/

        Thread connectThread = new ConnectThread( btDevice );
        connectThread.run();

    }

    public void manageConnectedSocket( BluetoothSocket sock ) {
        Toast.makeText(MainActivity.this, "Bluetooth connected.", Toast.LENGTH_SHORT).show();

        statusText.append("\nBluetooth connected");

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

    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;


        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(OBD_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                    Toast.makeText(MainActivity.this, "Quiting - Bluetooth is not connected.", Toast.LENGTH_LONG).show();

                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
