package nl.amanoo.carcontrol;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import java.util.ArrayList;

public class BluetoothActivity extends AppCompatActivity {

    private BluetoothAdapter BA;
    private ArrayList<BluetoothDevice> btDevices;
    ListView lv;
    Button btn;
    ProgressBar spinner;
    private static final int REQUEST_COURSE = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bluetooth);
        lv = (ListView)findViewById(R.id.bluetList);
        btn = (Button)findViewById(R.id.btnRefresh);
        spinner = (ProgressBar)findViewById(R.id.progressBar2);
        spinner.setVisibility(View.GONE);
        btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                searchDevices();
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //tv.setText(((BluetoothDevice) pairedDevices.toArray()[position]).getName()  +" " + ((BluetoothDevice) pairedDevices.toArray()[position]).getAddress() );
                // ((BluetoothDevice) pairedDevices.toArray()[position]).doeIets();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", btDevices.get(position).getAddress());
                setResult(Activity.RESULT_OK,returnIntent);
                unregisterReceiver(mReceiver);
                BA.cancelDiscovery();
                finish();
            }
        });



        BA = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
        //create a list of bonded devices
        searchDevices();
    }

    void searchDevices(){
        if (ContextCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COURSE);
        }else {
            //check if bluetooth is enabled, and if not, request turn on
            if (!BA.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 0);
            }
            BA.startDiscovery();
        }

    }

    void listDevices(){
        ArrayList list = new ArrayList();
        for(BluetoothDevice bt : btDevices)
            list.add(bt.getName() + "\n" + bt.getAddress());
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if(BA.isEnabled()) {
                    BA.startDiscovery();
                }
            }else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                spinner.setVisibility(View.VISIBLE);
                btDevices = new ArrayList<BluetoothDevice>();
                listDevices();
//                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                spinner.setVisibility(View.GONE);
 //               mProgressDlg.dismiss();
                listDevices();

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btDevices.add(device);

                listDevices();

            }

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_COURSE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                searchDevices();
            }else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(BluetoothActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //Show an explanation to the user *asynchronously*
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Deze permissie is belangrijk voor het zoeken van bluetooth apparaten.")
                            .setTitle("Belangrijke permissie nodig");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COURSE);
                        }
                    });
                    ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COURSE);
                }else{
                    //Never ask again and handle your app without permission.
                }
            }
        }
    }

}
