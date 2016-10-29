package nl.amanoo.carcontrol;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {

    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    ListView lv;
    TextView tv;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        lv = (ListView)findViewById(R.id.bluetList);
        tv = (TextView)findViewById(R.id.textView);
        btn = (Button)findViewById(R.id.btnRefresh);
        btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                listPairedDevices();
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //tv.setText(((BluetoothDevice) pairedDevices.toArray()[position]).getName()  +" " + ((BluetoothDevice) pairedDevices.toArray()[position]).getAddress() );
                // ((BluetoothDevice) pairedDevices.toArray()[position]).doeIets();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result",((BluetoothDevice) pairedDevices.toArray()[position]).getAddress());
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });

        BA = BluetoothAdapter.getDefaultAdapter();

        //check if bluetooth is enabled, and if not, request turn on
        if (!BA.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }

        //create a list of bonded devices
        listPairedDevices();
    }

    void listPairedDevices(){
        //check if bluetooth is enabled, and if not, request turn on
        if (!BA.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }

        pairedDevices = BA.getBondedDevices();
        ArrayList list = new ArrayList();
        for(BluetoothDevice bt : pairedDevices)
            list.add(bt.getName() + "\n" + bt.getAddress());
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);
    }
}
