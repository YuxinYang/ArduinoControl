package arduinocontroller.arduinocontroller;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.UUID;

public class ledControl extends AppCompatActivity {

    Button btn_b, btn_g, btn_r, btn_dis;
    TextView textView;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private boolean isBTNB_on = false;
    private boolean isBTNR_on = false;
    private boolean isBTNG_on = false;
    String outStream = null;

    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("16178937-b0fa-4cd6-b5a2-83ca123b7133");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        //receive the address of the bluetooth device
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);

        //view of the ledControl
        setContentView(R.layout.activity_led_control);

        //call the widgtes
        btn_b = (Button)findViewById(R.id.btn_blue);
        btn_g = (Button)findViewById(R.id.btn_green);
        btn_r = (Button)findViewById(R.id.btn_red);
        btn_dis = (Button)findViewById(R.id.btn_disconnect);
        textView = (TextView)findViewById(R.id.textView);

        //Call the class to connect
        new ConnectBT().execute();

        //commands to be sent to bluetooth
        btn_b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(!isBTNB_on){
                    turnOnLed(4);      //method to turn on
                    isBTNB_on = true;
                }else{
                    turnOffLed(4);
                    isBTNB_on = false;
                }
            }
        });

        btn_g.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(!isBTNG_on){
                    turnOnLed(6);      //method to turn on
                    isBTNG_on = true;
                }else{
                    turnOffLed(6);
                    isBTNG_on = false;
                }
            }
        });

        btn_r.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(!isBTNR_on){
                    turnOnLed(5);      //method to turn on
                    isBTNR_on = true;
                }else{
                    turnOffLed(5);
                    isBTNR_on = false;
                }
            }
        });

        btn_dis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                disconnect();
            }
        });

    }

    private void disconnect(){
        //If the btSocket is busy
        if(btSocket != null){
            try{
                btSocket.close();
            }catch(IOException io_e){
                msg("DISCONNECT ERROR");
            }
        }
        finish();
    }

    private void turnOnLed(int x){
        if(btSocket != null){
            try{
                outStream = String.valueOf(x)+"_TO";
                btSocket.getOutputStream().write(outStream.toString().getBytes());
            }catch(IOException io_e){
                msg("TURN ON " + String.valueOf(x) + " ERROR");
            }
        }
    }

    private void turnOffLed(int x){
        if(btSocket != null){
            try{
                outStream = String.valueOf(x)+"_TF";
                btSocket.getOutputStream().write(outStream.toString().getBytes());
            }catch(IOException io_e){
                msg("TURN OFF " + String.valueOf(x) + " ERROR");
            }
        }
    }

    //show Notification
    private void msg(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>{
        private  boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) { //while the progress dialog is shown, the connection is done in background
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException io_e) {
                ConnectSuccess = false;
                io_e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) { 
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Try again.");
                finish();
            }else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
