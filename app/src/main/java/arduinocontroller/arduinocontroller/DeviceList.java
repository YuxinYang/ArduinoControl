package arduinocontroller.arduinocontroller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.content.Intent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class DeviceList extends AppCompatActivity {

    public static final String TAG = "DEVICE_LIST";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_COARSE_LOCATION = 11;
    //widgets
    ToggleButton btnScan;
    ListView devicelist;
    //Bluetooth
    private BluetoothAdapter myBluetooth;
    private Set<BluetoothDevice> devices;
    public static String EXTRA_ADDRESS = "device_address";
    private ArrayAdapter<String> adapter;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Start...");

        setContentView(R.layout.activity_device_list);
        initViews();
        initBluetoothPermission();
        getPairedDevices();
        checkLocationPermission();
        initBEReceiver();
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReceiver);
        Log.i(TAG, "Unregister...");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // for now just request again
                    checkLocationPermission();
                }
                break;
            }
        }
    }

    private void initViews() {
        //Calling widgets
        btnScan = (ToggleButton) findViewById(R.id.btn_scan);
        devicelist = (ListView) findViewById(R.id.listView);

        btnScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    adapter.clear();
                    // start discovering devices only if location coarse permission get granted
                    myBluetooth.startDiscovery();
                } else {
                    //getActivity().unregisterReceiver(bReceiver);
                    myBluetooth.cancelDiscovery();
                }
            }
        });
    }

    private void initBluetoothPermission() {
        //if the device has bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if (myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish();
        } else if (!myBluetooth.isEnabled()) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, REQUEST_ENABLE_BT);
        }
    }

    private void getPairedDevices() {
        devices = myBluetooth.getBondedDevices();
        pairedDevicesList(devices);
    }

    private void initBEReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        registerReceiver(bReceiver, filter);
    }

    private void checkLocationPermission() {
        // Do something for lollipop and above versions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION);
        }
    }


    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("DEVICELIST", "Bluetooth device found\n");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceItem = device.getName() + "\n" + device.getAddress();
                Log.e("", String.valueOf(adapter.getPosition(deviceItem)));
                if (!(adapter.getPosition(deviceItem) > -1)) {
                    adapter.add(deviceItem);
                }
                devicelist.setAdapter(adapter);
            }
        }
    };

    private void pairedDevicesList(Set<BluetoothDevice> set) {
        ArrayList<String> list = new ArrayList<String>();

        if (devices.size() > 0) {
            for (BluetoothDevice bt : set) {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            myBluetooth.cancelDiscovery();
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make an intent to start next activity.
            Intent i = new Intent(DeviceList.this, ledControl.class);

            //Change the activity.
            i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
            startActivity(i);
        }
    };

}
