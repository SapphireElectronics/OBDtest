package ca.sapphire.obdtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "OBDTest";

    TextView statusText;

    public final static String EXTRA_DEVICE_ADDRESS = "device_address";
    BluetoothAdapter mBluetoothAdapter;
    private static final UUID OBD_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Handler timerHandler = new Handler();
    Handler taskHandler = new Handler();
    boolean isConnected = false;

    BTconnect btConnect;

    public InputStream btIn = null;
    public PrintStream btOut = null;

    private BluetoothSocket mmSocket = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = (TextView) findViewById(R.id.statusText);
        timerHandler.postDelayed(timerRunnable, 100);
        taskHandler.postDelayed(taskRunnable, 100);

        btConnect = new BTconnect( statusText, mmSocket );
        btConnect.execute( "" ).getStatus();
    }


    public void manageConnectedSocket( BluetoothSocket sock ) {
        Toast.makeText(MainActivity.this, "Bluetooth connected.", Toast.LENGTH_SHORT).show();

        statusText.append("\nBluetooth connected and ready to go.");
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


    public int writeCommandSequence = 0;

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if( isConnected) {
                writeStat(obdRead());
                switch (writeCommandSequence) {
                    case 0:
                        obdWrite("AT WS");
                        writeCommandSequence++;
                        break;

                    case 1:
                        obdWrite("AT I");
                        writeCommandSequence++;
                        break;

                    case 2:
                        obdWrite("AT PPS");
                        writeCommandSequence++;
                        break;

                    case 3:
                        obdWrite("AT RV");
                        writeCommandSequence++;
                        break;
                }
            }
            timerHandler.postDelayed(this, 100);
        }
    };

    public final int taskStart = 0;
    public final int taskBTConn = 1;
    public final int taskBTSocks = 2;

    public int taskMode = taskStart;


    Runnable taskRunnable = new Runnable() {
        // handles sequencing oof tasks

        @Override
        public void run() {
            switch( taskMode ) {
                case taskStart:
                    if (btConnect.getStatus() == AsyncTask.Status.FINISHED) {
                        taskMode = taskBTConn;
                    }
                    break;

                case taskBTConn:
                    try {
                        mmSocket = btConnect.getMmSocket();
                        btIn = mmSocket.getInputStream();
                        btOut = new PrintStream(mmSocket.getOutputStream());
                        writeStat("\nInput and Output streams are open.");

                        isConnected = true;

                        // Do work to manage the connection (in a separate thread)
                        manageConnectedSocket(mmSocket);

                    } catch (IOException e) {
                        writeStat("\nNot able to open Input and/or Output streams");
                        e.printStackTrace();
                    }
                    taskMode = taskBTSocks;
                    isConnected = true;
                    break;
                default:;
            }
            taskHandler.postDelayed(this, 100);
        }
    };

    public void writeStat( String str ) {
        statusText.setText( (statusText.getText() + str ) );
    }

    byte[] arrayOfByte = new byte[1023];
    int bytes;

    public String obdRead() {
        try {
            bytes = btIn.read(arrayOfByte);
            return( new String( arrayOfByte, 0, bytes ) );
        } catch( IOException e ) {
            e.printStackTrace();
        }
        return null;
    }

    public void obdWrite( String str ) {
        btOut.print( str + "\r" );
        btOut.flush();
    }

}
