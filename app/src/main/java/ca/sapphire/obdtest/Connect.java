package ca.sapphire.obdtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by Admin on 13/06/15.
 */
public class Connect extends Activity {
    private static final String TAG = "zeoconnect";
    private ByteBuffer localByteBuffer;
    private InputStream in;
    byte[] arrayOfByte = new byte[4096];
    int bytes;
    private static final UUID OBD_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Handler readHandler = new Handler();
    Handler writeHandler = new Handler();

    PrintStream ps;

    TextView statusText;


    public BluetoothDevice mDevice;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

//        statusText = (TextView) findViewById(R.id.statusText);

        try {
            setup();
        } catch (ZeoMessageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ZeoMessageParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public class ZeoMessageException extends Exception {
        public ZeoMessageException(String message) {
            super(message);
        }
    }

    public class ZeoMessageParseException extends Exception {
        public ZeoMessageParseException(String message) {
            super(message);
        }
    }


    private void setup() throws ZeoMessageException, ZeoMessageParseException  {
        // TODO Auto-generated method stub

        String MACaddress = getIntent().getStringExtra( "device_address" );

        Log.i( TAG, "MAC :" + MACaddress );


        getApplicationContext().registerReceiver(receiver,
                new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        getApplicationContext().registerReceiver(receiver,
                new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

        //TODO Replace with actual MAC address
        BluetoothDevice zee = BluetoothAdapter.getDefaultAdapter().
                getRemoteDevice("00:04:3E:6A:9E:0F");// add device mac adress

        setResult( RESULT_OK );

        try {
            sock = zee.createRfcommSocketToServiceRecord( OBD_UUID );
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            setResult(RESULT_CANCELED);
        }

        Log.d(TAG, "++++ Connecting");
        try {
            sock.connect();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            setResult(RESULT_CANCELED);
        }
        Log.d(TAG, "++++ Connected");


        try {
            in = sock.getInputStream();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            setResult( RESULT_CANCELED );
        }


        Log.d(TAG, "++++ Listening...");
/*
        OutputStream os = null;

        try {
            os = sock.getOutputStream();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        ps = new PrintStream( os );
        obdWrite("AT WS");

//        obdWrite("AT WS");

        readHandler.postDelayed(readRunnable, 0);
        writeHandler.postDelayed(writeRunnable, 0);

        for (int i = 0; i < 20 ; i++) {
            nap();
        }
*/

/*
        while( ! obdPrompt );
        obdWrite("AT I");

        while( ! obdPrompt );
        obdWrite("AT PPS");

        while( ! obdPrompt );
        obdWrite("AT RV");
*/
/*
        obdWrite("AT I");
        nap();
        Log.d(TAG, "[" + obdRead() + "]");
        nap();


        obdWrite("AT PPS");
        nap();
        Log.d(TAG, "[" + obdRead() + "]");
        nap();

        obdWrite("AT RV");
        nap();
        Log.d(TAG, "[" + obdRead() + "]");
        nap();
*/

/*
        while (true) {

            try {
                bytes = in.read(arrayOfByte);
                String str = new String(arrayOfByte,0,bytes);
                Log.d(TAG, "++++ Read "+ bytes +" bytes");
                Log.d(TAG, "[" + str + "]" );

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
*/
        Log.d(TAG, "++++ Done: test()");

        }

    public void nap() {
        try {
            Thread.sleep( 50 );
        } catch( InterruptedException e ) {
            e.printStackTrace();
        }
    }

    public String obdRead() {
        try {
            bytes = in.read(arrayOfByte);
            return( new String( arrayOfByte, 0, bytes ) );
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
//        return( new String( arrayOfByte, 0, bytes ) );
    }

    public void obdWrite( String str ) {
        ps.print( str + "\r" );
        ps.flush();
    }




    private static final LogBroadcastReceiver receiver = new LogBroadcastReceiver();
    public static class LogBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent) {
            Log.d("ZeoReceiver", paramAnonymousIntent.toString());
            Bundle extras = paramAnonymousIntent.getExtras();
            for (String k : extras.keySet()) {
                Log.d("ZeoReceiver", "    Extra: "+ extras.get(k).toString());
            }
        }


    };

    private BluetoothSocket sock;
    @Override
    public void onDestroy() {
        getApplicationContext().unregisterReceiver(receiver);
        if (sock != null) {
            try {
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    public boolean obdPrompt = false;

    Runnable readRunnable = new Runnable() {
        @Override
        public void run() {
            // read and display data from OBD device
            String str = obdRead();
            Log.d(TAG, "[" + str + "]");

//            if( str.contains( ">" ) )
                obdPrompt = true;

            if( str != null )
                writeStat( str );

            readHandler.postDelayed(this, 100);
        }

    };

    public int writeCommandSequence = 0;

    Runnable writeRunnable = new Runnable() {
        @Override
        public void run() {
            switch( writeCommandSequence ) {
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

            writeHandler.postDelayed(this, 100);
        }
    };

    public void writeStat( String str ) {
        statusText.setText((str + statusText.getText()).substring(0, 256));
    }
}
