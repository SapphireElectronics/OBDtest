package ca.sapphire.obdtest;


import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;


public class TerminalActivity extends AppCompatActivity {

    TextView rxText, txText;
    InputStreamReader is;
    PrintStream os;

    Handler isHandler = new Handler();
    Handler osHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);
        rxText = (TextView) findViewById( R.id.RxText);
        txText = (TextView) findViewById(R.id.TxText);

        rxText.setText( "Rx data");
        txText.setText( "Tx data");

        isHandler.postDelayed(isRunnable, 10);
        osHandler.postDelayed(osRunnable, 10);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_terminal, menu);
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

    public void setIs( InputStreamReader is ) {
        this.is = is;
    }

    public void setOs( PrintStream os ) {
        this.os = os;
    }

    Runnable isRunnable = new Runnable() {
        @Override
        public void run() {
            obdRead();
            isHandler.postDelayed(this, 50);
        }
    };

    char[] arrayOfChar = new char[256];
    int bytes;
    boolean prompt = false;

    public String obdRead() {
        try {
//            bytes = btIn.read(arrayOfByte);
            if( is.ready() ) {
                bytes = is.read(arrayOfChar, 0, 256);
                if (bytes <= 0)
                    return null;
                //            return( new String( arrayOfByte, 0, bytes ) );
                String str = new String(arrayOfChar, 0, bytes);
                if( str.contains( ">" ) )
                    prompt = true;

                rxText.append( str );
                return(str);
            }
            else
                return null;
        } catch( IOException e ) {
            e.printStackTrace();
        }
        return null;
    }

    public int osState = 0;

    Runnable osRunnable = new Runnable() {
        @Override
        public void run() {
            switch( osState ) {
                case 0:
                    obdWrite("AT WS");
                    osHandler.postDelayed(this, 500);
                    osState++;
                    break;

                case 1:
                    obdWrite("AT I");
                    osHandler.postDelayed(this, 200);
                    osState++;
                    break;

                case 2:
                    obdWrite("AT RV");
                    osHandler.postDelayed(this, 200);
                    osState++;
                    break;
            }
            osHandler.postDelayed(this, 200);
        }
    };

    public void obdWrite( String str ) {
        os.print(str + "\r");
        os.flush();
        txText.append( str + "\r");
    }

}
