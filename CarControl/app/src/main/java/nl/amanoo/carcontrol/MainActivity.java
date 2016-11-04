package nl.amanoo.carcontrol;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
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
    private static final long max_strength = 100;
  //  boolean isReconnecting = false;
    TextView isConnected;
    private static String nietVerbonden="Niet Verbonden";
    private static String verbonden="Verbonden";
    boolean gevonden=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        isConnected=(TextView)findViewById(R.id.tvVerbonden);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        /*
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }*/

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
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                address = result;
                IntentFilter filter = new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED");

                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

                registerReceiver(dcReceiver, filter);
            /*if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }*/


                setupbtSocket();
                createJoystickOnlistener();

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
            //verbinding weggevallen
            //errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");

            //verbindThread.run();
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        new Thread(new Runnable() {
            public void run() {
                // Establish the connection.  This will block until it connects.
                try {
                    btSocket.connect();
                    outStream = btSocket.getOutputStream();

                    try {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                isConnected.setText(verbonden);
                            }
                        });
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //            isConnected=true;
                } catch (IOException e) {
                    try {
                        btSocket.close();
                        gevonden=false;
                    } catch (IOException e2) {
                        errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
                    }
                }
         //       isReconnecting=false;

            }
        }).start();


    }

    private void createJoystickOnlistener() {
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {

                double u = Math.cos(Math.toRadians(angle)) * ((long) strength) / max_strength;
                double v = Math.sin(Math.toRadians(angle)) * ((long) strength) / max_strength;

                double x;
                double y;
                if (angle == 45 && strength == 100) {
                    x = 1;
                    y = 1;
                } else if (angle == 135 && strength == 100) {
                    x = -1;
                    y = 1;
                } else if (angle == 225 && strength == 100) {
                    x = -1;
                    y = -1;
                } else {
                    x = 0.5 * Math.sqrt(2 + (2 * u * Math.sqrt(2)) + (u * u) - (v * v)) - 0.5 * Math.sqrt(2 - (2 * u * Math.sqrt(2)) + (u * u) - (v * v));
                    y = 0.5 * Math.sqrt(2 + (2 * v * Math.sqrt(2)) - (u * u) + (v * v)) - 0.5 * Math.sqrt(2 - (2 * v * Math.sqrt(2)) - (u * u) + (v * v));
                }
                short[] data = {(short) (30000 * x), (short) (30000 * y)};

                //
                String test = "";
                ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 2);
                ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
                shortBuffer.put(data);
                byte[] array = byteBuffer.array();

                ((TextView) findViewById(R.id.rewhiq)).setText("x: " + data[0] + "\ny: " + data[1]);
                try {
                    outStream.write(array);
                } catch (IOException e) {
                    //verbinding weggevallen
                    //Toast.makeText(getBaseContext(), "outstream.write error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    try {
                        outStream.close();
                        btSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    //setupbtSocket();
   //                 isConnected=false;
  //                  reconnect();
                }
            }
        }, 10); //10
    }


    private final BroadcastReceiver dcReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Toast.makeText(getBaseContext(), "verbinding verloren", Toast.LENGTH_SHORT).show();
                isConnected.setText(nietVerbonden);
                gevonden=false;
                btAdapter.startDiscovery();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getAddress().equals(address)){
                    Toast.makeText(getBaseContext(), "terug gevonden", Toast.LENGTH_SHORT).show();
                    gevonden=true;
                    //btAdapter.cancelDiscovery();
                    setupbtSocket();
                }
        }else if (!gevonden&&BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if(isConnected.getText().equals(nietVerbonden)){
                    Toast.makeText(getBaseContext(), "start opnieuw met zoeken", Toast.LENGTH_SHORT).show();
                    btAdapter.startDiscovery();
                }
            }

        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, MY_UUID);
        } catch (Exception e) {
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    private void errorExit(String s, String s1) {
        Toast.makeText(getBaseContext(), s1, Toast.LENGTH_SHORT).show();
    }



    @Override
    protected void onDestroy(){
        unregisterReceiver(dcReceiver);
    }
}