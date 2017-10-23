package arduinocontroller.arduinocontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * Created by yuxin on 22.10.17.
 */

public class ConnectThread extends Thread {
    private BluetoothDevice mBTDevice;
    //private Handler mHandler;
    private BluetoothSocket mBTSocket;
    private BluetoothAdapter mBTAdapter;
    public static UUID STR_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public ConnectThread (BluetoothDevice device,BluetoothAdapter adapter){
        this.mBTDevice = device;
        this.mBTAdapter = adapter;
        BluetoothSocket socketTemp = null;
        try{
            socketTemp = device.createInsecureRfcommSocketToServiceRecord(STR_UUID);
        }catch (IOException ex){
            Log.e("ERROR","creating socket...");
            ex.printStackTrace();
        }

        Log.e("Action", "Connecting...");
        mBTSocket = socketTemp;
        //设为全局的socket
        //App.getInstance().mSocket = mBTSocket;
    }
    @Override
    public void run() {
        Log.e("Action","ConnectThread starts to run()...");

        try {
            mBTAdapter.cancelDiscovery();
            if (!mBTSocket.isConnected()) {
                mBTSocket.connect();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            Message message = new Message();
            message.what = 0x222;
            try{
                //使用反射进行连接
                try {
                    mBTSocket =(BluetoothSocket) mBTDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mBTDevice,2);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                mBTSocket.connect();
            }catch(IOException ex2){
                try {
                    mBTSocket.close();
                } catch (IOException ex1) {
                    ex1.printStackTrace();
                }

            }

            ex.printStackTrace();
        }
        //连接成功发送message
        Message message = new Message();
        message.what = 0x111;
        message.obj = mBTDevice;

        Log.e("eee","run() stop...");
    }

    //关闭socket
    public void close(){
        try{
            mBTSocket.close();
        }catch (IOException ex){
            //...
        }
    }
}