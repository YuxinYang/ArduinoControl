package arduinocontroller.arduinocontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


public class ledControl extends AppCompatActivity {

    ToggleButton btn_b, btn_g, btn_r;
    Button btn_dis;
    TextView txt_view;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    BluetoothDevice dispositivo = null;
    private boolean isBtConnected = false;
    ConnectThread connectThread = null;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    InputStream inputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);
        Intent newint = getIntent();
        //receive the address of the bluetooth device
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);

        //view of the ledControl
        setContentView(R.layout.activity_led_control);

        //call the widgtes
        btn_b = (ToggleButton)findViewById(R.id.btn_blue);
        btn_g = (ToggleButton)findViewById(R.id.btn_green);
        btn_r = (ToggleButton)findViewById(R.id.btn_red);
        btn_dis = (Button)findViewById(R.id.btn_disconnect);
        txt_view = (TextView)findViewById(R.id.txt_view);

        //Call the class to connect
        new ConnectBT().execute();

        /*
        if (btSocket == null || !isBtConnected) {
            myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
            BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
            connectThread = new ConnectThread(dispositivo, myBluetooth);
        }else{
            finish();
        }
        connectThread.run();*/
        if(btSocket != null){
            try {
                inputStream = btSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            txt_view.setText(inputStream.toString());
        }



        //commands to be sent to bluetooth
        btn_b.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                if (isChecked) {
                    turnOnLed(4);      //method to turn on
                } else {
                    turnOnLed(4);      //method to turn on
                }
            }
        });

        btn_r.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                if (isChecked) {
                    turnOnLed(2);      //method to turn on
                } else {
                    turnOnLed(2);      //method to turn on
                }
            }
        });

        btn_g.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                if (isChecked) {
                    turnOnLed(3);      //method to turn on
                } else {
                    turnOnLed(3);      //method to turn on
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

    private void disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try {
                btSocket.close(); //close connection
            } catch (IOException e) {
                msg("Error");
            }
        }
        finish(); //return to the first layout

    }

    private void turnOffLed(int n)
    {
        if (btSocket!=null)
        {
            String outStream = String.valueOf(n) + "_OFF";
            try {
                btSocket.getOutputStream().write(outStream.getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void turnOnLed(int n)
    {
        if (btSocket!=null) {
            String outStream = String.valueOf(n) + "_ON";
            try {
                btSocket.getOutputStream().write(outStream.getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private  boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show(ledControl.this, "Connecting with " + address + "...", "Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) { //while the progress dialog is shown, the connection is done in background
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    if(dispositivo == null){
                        //msg("Device not found.  Unable to connect.");
                        finish();
                    }
                    Log.i("Device",dispositivo.getAddress());

                    try{
                        //btSocket = createBluetoothSocket(dispositivo);
                        btSocket = dispositivo.createRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.e("Error","creating socket");
                    }

                    if(myBluetooth.cancelDiscovery()) {
                        //myBluetooth.ACTION_DISCOVERY_FINISHED;
                        Log.i("ACTION", myBluetooth.ACTION_DISCOVERY_FINISHED);
                        try {
                            btSocket.connect();
                            Log.e("Action", "Connected");
                        } catch (IOException io_e) {
                            io_e.printStackTrace();
                            //Log.e("",io_e.getMessage());
                            try {
                                Log.e("Connect ERROR", "trying fallback...");
                                btSocket = (BluetoothSocket) dispositivo.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class}).invoke(dispositivo, 2);
                                btSocket.connect();
                                Log.e("Action", "Connected");
                            } catch (Exception e2) {
                                e2.printStackTrace();
                                Log.e("", "Couldn't establish Bluetooth connection!");
                                Log.e("Connect ERROR", "stop...");
                                try {
                                    btSocket.close();
                                } catch (IOException io_e2) {
                                    Log.e("Fatal Error", "In onResume() and unable to close socket during connection failure" + io_e2.getMessage() + ".");
                                }
                                ConnectSuccess = false;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                ConnectSuccess = false;
                e.printStackTrace();
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
