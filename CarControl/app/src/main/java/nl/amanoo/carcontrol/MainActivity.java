package nl.amanoo.carcontrol;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.bluetooth.BluetoothSocket;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.UUID;
import java.io.IOException;
import android.widget.Toast;
import java.lang.reflect.Method;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address;
    JoystickView joystick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }

        joystick = (JoystickView) findViewById(R.id.joystickView);
           Intent intent = new Intent(this, BluetoothActivity.class);
           startActivityForResult(intent, 1);


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
            Intent intent = new Intent(this, BluetoothActivity.class);
            startActivityForResult(intent, 1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                address = result;

            /*if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }*/

                setupbtSocket();

                joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
                    @Override
                    public void onMove(int angle, int strength) {

                        double u = Math.cos(Math.toRadians(angle))*strength/100;
                        double v = Math.sin(Math.toRadians(angle))*strength/100;

                        double x;
                        double y;
                        if(angle==45&&strength==100){
                            x=1;
                            y=1;
                        }else if(angle==135&&strength==100){
                            x=-1;
                            y=1;
                        }else if(angle==225&&strength==100){
                            x=-1;
                            y=-1;
                        }else {
                            x = 0.5 * Math.sqrt(2 + (2 * u * Math.sqrt(2)) + (u * u) - (v * v)) - 0.5 * Math.sqrt(2 - (2 * u * Math.sqrt(2)) + (u * u) - (v * v));
                            y = 0.5 * Math.sqrt(2 + (2 * v * Math.sqrt(2)) - (u * u) + (v * v)) - 0.5 * Math.sqrt(2 - (2 * v * Math.sqrt(2)) - (u * u) + (v * v));
                        }
                        short[] data = { (short)(30000*x), (short)(30000*x), (short)(30000*x), (short)(30000*y), (short)(30000*y), (short)(30000*y) };

                        //
                        String test="";
                        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 2);
                        ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
                        shortBuffer.put(data);
                        byte[] array = byteBuffer.array();
                        /*for (int i=0; i < array.length; i++)
                        {
                            test=test+i + ": " + array[i]+"\n";
                        }*/


                                  ((TextView)findViewById(R.id.rewhiq)).setText("x: "+data[0]+"\ny: "+data[4]);
                        //((TextView)findViewById(R.id.rewhiq)).setText(test);
                        try {
                            outStream.write(array);
                        } catch (IOException e) {
                            Toast.makeText(getBaseContext(), "outstream.write error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            try {
                                outStream.close();
                                btSocket.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            setupbtSocket();
                        }
                    }
                }, 10);


            }
        }
    }

    private void setupbtSocket() {
        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e1) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }


        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, MY_UUID);
        } catch (Exception e) {
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    private void errorExit(String s, String s1) {
        Toast.makeText(getBaseContext(), s1, Toast.LENGTH_SHORT).show();
    }

    /*private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();


        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "outstream.write error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            try {
                outStream.close();
                btSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            setupbtSocket();
        }
    }*/



}