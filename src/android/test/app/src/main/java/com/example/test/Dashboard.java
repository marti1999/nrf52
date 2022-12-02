package com.example.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.utils.HexUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class Dashboard extends AppCompatActivity {
    private TextView ResultsTemp, ResultsPrec, ResultsWind;
    private TextView LineResults;
    private Button btnScan, btnConnect;
    private static final int BLUETOOTH_CODE = 100;
    private static final String SERVICE_UUID = "ee910d6a61f948929f27c1b2fa7e1ebe";
    private static final String CHARACTERISTIC_UUID = "a89b4483df7f4539ab8ae6bfb4070640";
    private boolean isNRF52Found = false;
    private boolean isNRF52Connected = false;
    // creating a variable for our Firebase Database.
    FirebaseDatabase firebaseDatabase;

    // creating a variable for our Database reference for Firebase.
    DatabaseReference databaseReference;

    // Variables used in the activity
    String lastKey = "";
    ArrayList<Entry> allEntries = new ArrayList<Entry>();
    ValueEventListener asyncListenerAll, asyncListenerLast;
    BleDevice nrf52;

    Handler handler = new Handler();
    Runnable handler_runnable;
    int handler_delay = 5000;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BleManager.getInstance().isConnected(nrf52)) {
            BleManager.getInstance().disconnect(nrf52);
        }
        BleManager.getInstance().destroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        getSupportActionBar().hide();
        // creating firebase instance
        firebaseDatabase = FirebaseDatabase.getInstance();
        // Creating a reference to our collection
        databaseReference = firebaseDatabase.getReference("Entries");
        //Initializing and mapping GUI components
        ResultsTemp = findViewById(R.id.temp_value);
        ResultsPrec = findViewById(R.id.precipitation_value);
        ResultsWind = findViewById(R.id.wind_value);
        btnScan = findViewById(R.id.idBtnScan);
        btnConnect = findViewById(R.id.idBtnConnect);
        LineResults = findViewById(R.id.ScanResult);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ble_checkpermission();
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ble_connect();
            }
        });

        fetchLastEntryAsync();
    }

    @Override
    protected void onResume(){
        handler.postDelayed(handler_runnable = new Runnable() {
            public void run() {
//                Log.d("HANDLER", "handler executed");
                if (isNRF52Connected){
                    ble_read();
                }
                handler.postDelayed(this, handler_delay);
            }
        }, handler_delay);
        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(handler_runnable); //stop handler when activity not visible super.onPause();
        super.onPause();

    }

    // fetches last entry by using the key previously saved.
    private void fetchLastEntryAsync() {
        // notice that child(lastKey) is called before get(). Now it only gets the element inside the list, instead of the whole list.
        // Alternatively it could be done by lastKey variable.
        asyncListenerLast = databaseReference.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Entry en = new Entry();

                for (DataSnapshot entrySnap : snapshot.getChildren()) {
                    en = entrySnap.getValue(Entry.class);
                }
                ResultsTemp.setText(en.getTemperature() + " ºC");
                ResultsPrec.setText(en.getPrecipitation() + " mL");
                ResultsWind.setText(en.getWind() + " km/h");

                Log.e("JÚLIA", en.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void fetchAllEntriesAsync() {
        // this variable needs to be outside the function because it is needed in another.
        asyncListenerAll = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allEntries.clear();

                for (DataSnapshot entrySnap : snapshot.getChildren()) {
                    Entry en = entrySnap.getValue(Entry.class);
                    allEntries.add(en);
                }
                //Results.setText(String.valueOf(allEntries));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("firebase", "Error getting data", error.toException().getCause());
            }
        });
    }

    private void ble_checkpermission() {
        // https://developer.android.com/guide/topics/connectivity/bluetooth/permissions
        // https://www.geeksforgeeks.org/android-how-to-request-permissions-in-android-application/
        // https://developer.android.com/training/permissions/requesting#request-permission
        // https://github.com/Jasonchenlijian/FastBle
        // https://programming.vip/docs/android-bluetooth-library-simple-use-of-fastble.html

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "obrir bluetooth", Toast.LENGTH_LONG).show();
            return;
        }

        ActivityCompat.requestPermissions(Dashboard.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION}, BLUETOOTH_CODE);

//        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT)
//                != PackageManager.PERMISSION_GRANTED)
//        {
//            Log.e("ble", "NO TÉ PERMISOS");
//            Toast.makeText(this, "error permisos BLUETOOTH_CONNECT", Toast.LENGTH_SHORT).show();
//            return;
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == BLUETOOTH_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(MainActivity.this, "BLUETOOTH_CONNECT_CODE Permission Granted", Toast.LENGTH_SHORT) .show();
                ble_setScanRule();
                ble_startScan();
            } else {
                Toast.makeText(Dashboard.this, "permisos denegats", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void ble_setScanRule() {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(null)
                .setDeviceName(true, null)
                .setDeviceMac("F0:09:D9:4C:E9:18")
                .setAutoConnect(false)
                .setScanTimeOut(10000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    private void ble_startScan() {

        if (isNRF52Connected || isNRF52Found) {
            Toast.makeText(this, "Already paired", Toast.LENGTH_SHORT).show();
        }

        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                Log.d("BLE", "scan finished");

            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanStarted(boolean success) {
                Log.d("BLE", "scan started");

            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                Log.d("BLE", "device found: " + bleDevice.getName());
                nrf52 = bleDevice;
                LineResults.setText("Device found: \n" + nrf52.getName() + "\n" + nrf52.getMac());
                isNRF52Found = true;
            }
        });

    }

    private void ble_connect() {
        if (!isNRF52Found) {
            Toast.makeText(this, "NRF52 was not discovered. Scan again", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!BleManager.getInstance().isConnected(nrf52)) {
            BleManager.getInstance().cancelScan();
            BleManager.getInstance().connect(nrf52, new BleGattCallback() {

                @Override
                public void onStartConnect() {
                    Log.d("BLE", "Connecting... ");
                }

                @Override
                public void onConnectFail(BleDevice bleDevice, BleException exception) {
                    Log.d("BLE", "connexion failed");
                }

                @Override
                public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                    Log.d("BLE", "connected!! ");
                    isNRF52Connected = true;
                    Toast.makeText(Dashboard.this, "Connected", Toast.LENGTH_SHORT).show();
                    LineResults.setText("");

                    // TODO mostrar per pantalla els serveis i característiques

//                    List<BluetoothGattService> services = BleManager.getInstance().getBluetoothGattServices(nrf52);
//                    services.

                }

                @Override
                public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                    Log.d("BLE", "Disconnected ");

                }
            });
        } else {
            Toast.makeText(this, "Already connected", Toast.LENGTH_SHORT).show();
        }
    }

    private void ble_read() {

        if (!isNRF52Connected) {
            return;
        }

        String uuid_service = formatAsUUID(SERVICE_UUID);
        String uuid_characteristic = formatAsUUID(CHARACTERISTIC_UUID);

        BleManager.getInstance().read(nrf52, uuid_service, uuid_characteristic, new BleReadCallback() {
            @Override
            public void onReadSuccess(byte[] data) {

                String big_endian = HexUtil.formatHexString(data, false);
                String little_endian = swapEndianString(big_endian);
                String temperature = little_endian.substring(6, 8);
                String precipitation = little_endian.substring(4, 6);
                String wind = little_endian.substring(2, 4);

                byte temp_byte = data[0];
                byte precipitation_byte = data[1];
                byte wind_byte = data[2];
                String text = "temperature: " + temp_byte + "\nprecipitation: " + precipitation_byte + "\nwind: " + wind_byte;
                //multiLineResults.setText(text);
                Log.d("BLE", "Received: " + big_endian);

                addEntryToFirebase(String.valueOf(temp_byte), String.valueOf(wind_byte), String.valueOf(precipitation_byte));

            }

            @Override
            public void onReadFailure(BleException exception) {
                Log.d("BLE", "error read: " + exception.getDescription());
            }
        });

    }

    private String formatAsUUID(String raw) {
        return java.util.UUID.fromString(
                raw.replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
                )
        ).toString();
    }

    private String swapEndianString(String in) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < in.length(); i += 2) {
            String s = in.substring(i, i + 2);
            sb.insert(0, s);
        }
        return sb.toString();
    }

    private void addEntryToFirebase(String temperature, String wind, String precipitation) {
        Entry entry = new Entry();
        entry.setTemperature(temperature);
        entry.setWind(wind);
        entry.setPrecipitation(precipitation);

        // NOW IT IS PUTTING A NEW ENTRY WITH AN AUTOMATIC KEY https://firebase.google.com/docs/database/android/lists-of-data#append_to_a_list_of_data
        // push() is creating a random key, so there's no need to call child(myKey)
        // if the random key needs to be accessed, the reference returned by push must be kept in a variable
        DatabaseReference pushedRef = databaseReference.push();

        pushedRef.setValue(entry).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //clearTextBoxes();
                lastKey = pushedRef.getKey();
            }
        });


    }
}