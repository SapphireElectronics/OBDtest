package ca.sapphire.obdtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Created by apreston on 6/22/2015.
 *
 */
public class BTconnect extends AsyncTask<String, String, Integer> {
    private static final String TAG = "BTconnect";

    TextView statusText;

    BluetoothAdapter mBluetoothAdapter;
    public BluetoothSocket mmSocket;
    private static final UUID OBD_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final int BT_CONNECT_OK = 1;
    private static final int BT_NOT_SUPPORTED = 2;
    private static final int BT_NOT_ENABLED = 3;
    private static final int BT_MAC_NOT_MATCHED = 4;
    private static final int BT_NOT_CONNECTED = 5;


    public boolean connected = false;

    public BTconnect( TextView status, BluetoothSocket mmSocket )
    {
        super();
        this.mmSocket = mmSocket;
        statusText = status;
    }

    protected Integer doInBackground( String... MACs ) {
        onProgressUpdate( "\nIn BTconnect run()");

        // check if Bluetooth is supported
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            publishProgress("\nBluetooth not supported");
            return BT_NOT_SUPPORTED;
        }

        publishProgress("\nBluetooth supported");

        // cancel discovery, we don't need to do it.
        mBluetoothAdapter.cancelDiscovery();

        if (!mBluetoothAdapter.isEnabled()) {
            publishProgress("\nBluetooth not enabled.");
            return BT_NOT_ENABLED;
            //            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        publishProgress("\nBluetooth enabled");

        BluetoothDevice btDevice = null;
        String MACaddress = "00:04:3E:6A:9E:0F";

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                publishProgress("\n" + device.getName() + " : " + device.getAddress());
                if (device.getAddress().equals(MACaddress)) {
                    btDevice = device;
                }
            }
        }

        // TODO: Can also get the device this way:
        //        btDevice = mBluetoothAdapter.getDefaultAdapter().getRemoteDevice( MACaddress );

        if (btDevice == null) {
            publishProgress("\nMAC address not matched");
            return BT_MAC_NOT_MATCHED;
        }

        publishProgress("\nIdentified BT device by MAC address");

        BluetoothSocket tmp = null;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = btDevice.createRfcommSocketToServiceRecord(OBD_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmSocket = tmp;
        publishProgress("\nCreated RFcomm socked.");

        mBluetoothAdapter.cancelDiscovery();
        publishProgress("\nCancelled discovery");

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
                publishProgress("\nBluetooth device is not connected");
                return BT_NOT_CONNECTED;
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }

            // at this point, we have a connection to the actual BT device, so we can open in and out streams
        }
        publishProgress("\nConnected.");
        return BT_CONNECT_OK;
    }

    protected void onProgressUpdate(String... str) {
        Log.i(TAG, "Process:" + str[0]);
//        statusText.append(str[0]);
    }

    protected void onPostExecute(Integer status) {
        Log.i(TAG, "Finished ConnectBT: " + status);

        if( status == BT_CONNECT_OK ) {
            connected = true;
            statusText.append( "\nBT Connected and ready.");
        }
        else {
            connected = false;
            statusText.append( "\nBT not ready.");
        }
    }

    protected void onCancelled() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "BT Connect cancelled");
        statusText.append("\nBT Connect cancelled");

    }

    public BluetoothSocket getMmSocket() {
        return mmSocket;
    }
}
