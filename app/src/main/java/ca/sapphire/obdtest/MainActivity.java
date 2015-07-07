package ca.sapphire.obdtest;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
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

    BTconnect btConnect;

    public InputStream btIn = null;
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
        btConnect = new BTconnect(statusText, mmSocket);
        btConnect.execute("").getStatus();
        taskMode = taskConnect;
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
    public final int taskConnect = 4;
    public final int taskBTConn = 1;
    public final int taskBTSocks = 2;
    public final int taskCancel = 3;
    public final int taskHalt = 5;

    public int taskMode = taskConnect;


    Runnable taskRunnable = new Runnable() {
        // handles sequencing oof tasks

        public int timeout = 0;
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

            switch( taskMode ) {
                // start the Bluetooth connect process
                case taskConnect:
                    taskMode = taskStart;
                    timeout = 0;
                    break;

                case taskStart:
                    if( btConnect != null ) {
                        if (btConnect.getStatus() == AsyncTask.Status.FINISHED) {
                            taskMode = taskBTConn;
                            break;
                        }
                    }
                    if( ++timeout > 50 ) {
                        writeStat("\nTimeout on BT connect.");
                        btConnect.cancel(true);
                        taskMode = taskCancel;
                    }
                    break;

                case taskBTConn:
                    try {
                        if( !btConnect.connected )
                            break;

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

                case taskCancel:
                    if (btConnect.getStatus() == AsyncTask.Status.FINISHED) {
                        writeStat("\nMain thread :: Not connected");
                        if(  ++retries < 3 ) {
                            writeStat( "\n\nRetry = " + retries );
                        }



                        taskMode = taskHalt;
                    }
                    break;

                case taskHalt:
                    break;

                default:;
            }
            taskHandler.postDelayed(this, 100);
        }
    };

    public void writeStat( String str ) {
        statusText.append( str );
    }

    public void manageConnectedSocket( BluetoothSocket sock ) {
        Toast.makeText(MainActivity.this, "Bluetooth connected.", Toast.LENGTH_SHORT).show();

        statusText.append("\nBack in main thread.");
    }

    byte[] arrayOfByte = new byte[1023];
    int bytes;

    public String obdRead() {
        try {
            bytes = btIn.read(arrayOfByte);
            if( bytes <= 0 )
                return null;
            return( new String( arrayOfByte, 0, bytes ) );
        } catch( IOException e ) {
            e.printStackTrace();
        }
        return null;
    }

    public void obdWrite( String str ) {
        btOut.print( str + "\r" );
        btOut.flush();
        writeStat( str + "\r" );
    }

}
