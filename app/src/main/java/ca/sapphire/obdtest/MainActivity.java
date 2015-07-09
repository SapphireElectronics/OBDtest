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
    Button connectButton;

    Handler timerHandler = new Handler();
    Handler taskHandler = new Handler();
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
        connectButton = (Button) findViewById(R.id.connectButton);

        addListenerOnConnectButton(this);

        timerHandler.postDelayed(timerRunnable, 100);
        taskHandler.postDelayed(taskRunnable, 100);

        tryBTconnect();

//        btConnect = new BTconnect( statusText, mmSocket );
//        btConnect.execute( "" ).getStatus();

        // TODO: Remove this.
//        goofAroundWithGraphics();
    }

    public void tryBTconnect() {
//        statusText = (TextView) findViewById(R.id.statusText);
        statusText.setText( "" );
        btConnect = new BTconnect(statusText, mmSocket);
        btConnect.execute("").getStatus();
        timeout = 0;
        tryConnect = true;
    }

    public void addListenerOnConnectButton( final Activity mActivity ) {
        connectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tryBTconnect();
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

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if( isConnected) {
                writeBtIn(obdRead());
                switch (writeCommandSequence) {
                    case 0:
                        writeStat("\n");
                        obdWrite("AT WS");
                        writeCommandSequence++;
                        break;

                    case 1:
                        if( !prompt )
                            break;

                        prompt = false;
                        writeStat( "\n" );
                        obdWrite("AT I");
                        writeCommandSequence++;
                        break;

                    case 2:
                        if( !prompt )
                            break;

                        prompt = false;
                        writeStat( "\n" );
                        obdWrite("AT PPS");
                        writeCommandSequence++;
                        break;

                    case 3:
                        if( !prompt )
                            break;

                        prompt = false;
                        writeStat( "\n" );
                        obdWrite("AT RV");
                        writeCommandSequence++;
                        break;
                }
            }
            timerHandler.postDelayed(this, 200);
        }
    };

    public int timeout = 0;

    Runnable taskRunnable = new Runnable() {
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
            taskHandler.postDelayed(this, 100);
        }
    };

    public void writeStat( String str ) {
        if( str != null )
            statusText.append( str );
    }

    public void manageConnectedSocket( BluetoothSocket sock ) {
        Toast.makeText(MainActivity.this, "Bluetooth connected.", Toast.LENGTH_SHORT).show();

        statusText.append("\nBack in main thread.");

        Intent myIntent = new Intent(this, TerminalActivity.class);
//        myIntent.putExtra("key", value); //Optional parameters
        startActivity(myIntent);
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
        int clr = statusText.getCurrentTextColor();
        statusText.setTextColor(Color.GREEN );
        writeStat(str);
        statusText.setTextColor( clr );

    }

    public void writeBtOut( String str ) {
        int clr = statusText.getCurrentTextColor();
        statusText.setTextColor(Color.BLUE );
        writeStat(str + "\n");
        statusText.setTextColor(clr);

    }

}
