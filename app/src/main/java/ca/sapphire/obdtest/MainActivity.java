package ca.sapphire.obdtest;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Random;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "OBDTest";

    TextView statusText;
    TextView txText;
    Button connectButton;
    Button monitorButton;
    Button clearButton;

    Handler isHandler = new Handler();
    Handler osHandler = new Handler();
    Handler connectHandler = new Handler();
    Handler mHandler;

    boolean isConnected = false;
    boolean tryConnect = false;

    BTconnect btConnect;

    public InputStreamReader btIn = null;
    public PrintStream btOut = null;

    private BluetoothSocket mmSocket = null;

    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = (TextView) findViewById(R.id.statusText);
        txText = (TextView) findViewById(R.id.TxText);
        statusText.setTextColor( 0xff008000 );  // Green
        txText.setTextColor( 0xff000080 );      // Blue

        connectButton = (Button) findViewById(R.id.connectButton);
        monitorButton = (Button) findViewById(R.id.monitorButton);
        clearButton = (Button) findViewById(R.id.clearButton);

        addListenerOnConnectButton(this);
        addListenerOnMonitorButton(this);
        addListenerOnClearButton(this);

//        isHandler.postDelayed(isRunnable, 100);
//        osHandler.postDelayed(osRunnable, 100);
        connectHandler.postDelayed(connectRunnable, 100);

        tryBTconnect();

//        btConnect = new BTconnect( statusText, mmSocket );
//        btConnect.execute( "" ).getStatus();

        // TODO: Remove this.
//        goofAroundWithGraphics();
    }

    public void tryBTconnect() {
        if( isConnected )
            return;

        statusText.setText("");
        btConnect = new BTconnect(statusText, mmSocket);
        btConnect.execute("").getStatus();
        timeout = 0;
        tryConnect = true;
    }

    public boolean monitor = false;

    public void toggleMonitor() {
        if( !isConnected ) return;

        monitor = !monitor;

        if( monitor ) {
            obdWrite( "AT MA" );
            monitorButton.setText( "Stop Monitor");
        }
        else {
            obdWrite( "." );
            monitorButton.setText("Start Monitor");
        }

    }

    public void addListenerOnConnectButton( final Activity mActivity ) {
        connectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tryBTconnect();
            }
        });
    }

    public void addListenerOnMonitorButton( final Activity mActivity ) {
        monitorButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                toggleMonitor();
            }
        });
    }

    public void addListenerOnClearButton( final Activity mActivity ) {
        clearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                statusText.setText( "" );
                txText.setText( "" );
            }
        });
    }


    public void goofAroundWithGraphics() {
        gridView = new GridView( this, 16, 16 );
        setContentView(gridView);
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

    Runnable osRunnable = new Runnable() {
        @Override
        public void run() {
            if( isConnected) {
//                writeBtIn(obdRead());
                switch (writeCommandSequence) {
                    case 0:
                        writeStat("\n");
                        obdWrite("AT WS");      // warm start
                        writeCommandSequence++;
                        break;

                    case 1:
                        if( !prompt )
                            break;

                        prompt = false;
                        writeStat( "\n" );
                        obdWrite("AT SP 2");        // set protocol 2: J1850 VPW
                        writeCommandSequence++;
                        break;

                    case 2:
                        if( !prompt )
                            break;

                        prompt = false;
                        writeStat( "\n" );
                        obdWrite("AT H1");          // show headers
                        writeCommandSequence++;
                        break;

                    case 3:
                        if( !prompt )
                            break;

                        prompt = false;
                        writeStat( "\n" );
                        obdWrite("AT I");          // show info
                        writeCommandSequence++;
                        break;

                }
            }
            osHandler.postDelayed(this, 200);
        }
    };

    String rxStr = null;

    Runnable isRunnable = new Runnable() {
        @Override
        public void run() {
            rxStr = obdRead();
            if( rxStr  != null ) {
                statusText.append( rxStr );
            }
            isHandler.postDelayed(this, 50);
        }
    };


    public int timeout = 0;

    Runnable connectRunnable = new Runnable() {
        // TODO: Stop running this thread when no longer needed, as in: we're connected or waiting on the Connect button click
        // handles sequencing oof tasks

        public int retries = 0;

        @Override
        public void run() {

//            Random r = new Random();
//
//            for (int i = 0; i < 32; i++) {
//                gridView.elementInc(r.nextInt(255));
//
//                if( ++passes > 255 ) {
//                    passes = 0;
//                    gridView.decAll();
//                }
//
//            }
//
//            gridView.invalidate();

            if (!isConnected && tryConnect ) {
                if (btConnect != null) {
                    if (btConnect.getStatus() == AsyncTask.Status.FINISHED) {
                        tryConnect = false;
                        try {
                            if (btConnect.connected) {
                                mmSocket = btConnect.getMmSocket();
                                btIn = new InputStreamReader(mmSocket.getInputStream());
                                btOut = new PrintStream(mmSocket.getOutputStream());
                                statusText.setText("Input and Output streams are open.");

                                isConnected = true;

                                // Do work to manage the connection (in a separate thread)
                                manageConnectedSocket(mmSocket);
                            }

                        } catch (IOException e) {
                            writeStat("\nNot able to open Input and/or Output streams");
                            e.printStackTrace();
                        }
                    } else {
                        if (++timeout > 75) {
                            tryConnect = false;
                            writeStat("\nTimeout on BT connect.");
                            if (!btConnect.isCancelled())
                                btConnect.cancel(true);

                            if (btConnect.getStatus() == AsyncTask.Status.FINISHED) {
                                writeStat("\nMain thread :: Not connected");
                                if (++retries < 3) {
                                    writeStat("\n\nRetry = " + retries);
                                }
                            }
                        }
                    }
                }
            }
            // only keep the connect handler running if we are not yet connected
            if( !isConnected )
                connectHandler.postDelayed(this, 100);
        }
    };

    public void writeStat( String str ) {
        if( str != null )
            statusText.append( str );
    }

    public void manageConnectedSocket( BluetoothSocket sock ) {
        Toast.makeText(MainActivity.this, "Bluetooth connected.", Toast.LENGTH_SHORT).show();

        statusText.append("\nBack in main thread.");

        //start input and output handlers
        isHandler.postDelayed(isRunnable, 10);
        osHandler.postDelayed(osRunnable, 10);

//        Intent myIntent = new Intent(this, TerminalActivity.class);
//        myIntent.putExtra( "is", mmSocket );
//        myIntent.putExtra("key", value); //Optional parameters
//        startActivity(myIntent);
    }

//    byte[] arrayOfByte = new byte[1023];
    char[] arrayOfChar = new char[256];
    int bytes;

    public boolean prompt = false;

    public String obdRead() {
        try {
//            bytes = btIn.read(arrayOfByte);
            if( btIn.ready() ) {
                bytes = btIn.read(arrayOfChar, 0, 256);
                if (bytes <= 0)
                    return null;
                //            return( new String( arrayOfByte, 0, bytes ) );
                String str = new String(arrayOfChar, 0, bytes);
                if( str.contains( ">" ) )
                    prompt = true;

                return(str);
            }
            else
                return null;
        } catch( IOException e ) {
            e.printStackTrace();
        }
        return null;
    }

    public void obdWrite( String str ) {
        btOut.print(str + "\r");
        btOut.flush();
        writeBtOut(str + "\r");
    }

    public void writeBtIn( String str ) {
        writeStat(str);
    }

    public void writeBtOut( String str ) {
        txText.append( str + "\n" );
    }

}
