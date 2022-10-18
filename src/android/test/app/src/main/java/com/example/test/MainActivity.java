package com.example.test;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.utils.HexUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {

    // TOTA LA INFORMACIÓ ES POT TROBAR AQUÍ
    // https://firebase.google.com/docs/database/android/read-and-write
    // https://firebase.google.com/docs/database/android/lists-of-data
    // https://www.geeksforgeeks.org/how-to-save-data-to-the-firebase-realtime-database-in-android/


    // creating variables for EditText and Buttons.
    private EditText textTemp, textWind, textPrec;
    private TextView multiLineResults;
    private Button btnSendEntry, btnSendBulk, btnCleanAllEntries, btnFetchLast, btnFetchAll,
            btnGetStats, btnScan, btnConnect, btnRead;
    private ToggleButton togBtnAllAsync, togBtnLastAsync;

    myReference ref;

    // Variables used in the activity
    String lastKey = "";

    ArrayList<Entry> allEntries = new ArrayList<Entry>();
    ValueEventListener asyncListenerAll, asyncListenerLast;

    BleDevice nrf52;

    private static final int BLUETOOTH_CODE = 100;
    private static final String SERVICE_UUID = "19b10000e8f2537e4f6cd104768a1214";
    private static final String CHARACTERISTIC_UUID = "19b10001e8f2537e4f6cd104768a1214";
    private boolean isNRF52Found = false;
    private boolean isNRF52Connected = false;


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
        setContentView(R.layout.activity_main);

        // initializing and mapping GUI components
        textTemp = findViewById(R.id.idTextTemperature);
        textWind = findViewById(R.id.idTextHumidity);
        textPrec = findViewById(R.id.idTextPrecipitation);
        multiLineResults = findViewById(R.id.idMultiText);
        btnSendEntry = findViewById(R.id.idButtonManually);
        btnSendBulk = findViewById(R.id.idButtonBulk);
        btnFetchLast = findViewById(R.id.idButtonFetchLast);
        btnFetchAll = findViewById(R.id.idButtonFetchAll);
        btnCleanAllEntries = findViewById(R.id.idButtonClearAll);
        togBtnAllAsync = findViewById(R.id.idToogleAllAsync);
        togBtnLastAsync = findViewById(R.id.idToogleLastAsync);
        btnGetStats = findViewById(R.id.idGetStats);
        btnScan = findViewById(R.id.idBtnScan);
        btnConnect = findViewById(R.id.idBtnConnect);
        btnRead = findViewById(R.id.idBtnRead);


        // initializing myReference class (cloud reference)
        ref = new myReference("Entries");
        // creating firebase instance
//        firebaseDatabase = FirebaseDatabase.getInstance();
        // Creating a reference to our collection
//        databaseReference = firebaseDatabase.getReference("Entries");

        // adding on click listener for each button.
        btnGetStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Dashboard.class);
                startActivityForResult(intent, 0);
            }
        });
        btnSendEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // getting text from our edittext fields.
                String name = textTemp.getText().toString();
                String phone = textWind.getText().toString();
                String address = textPrec.getText().toString();

                // shows Toast message if a text field is empty. Otherwise calls addEntryToFireabse()
                if (TextUtils.isEmpty(name) && TextUtils.isEmpty(phone) && TextUtils.isEmpty(address)) {
                    Toast.makeText(MainActivity.this, "Please add some data.", Toast.LENGTH_SHORT).show();
                } else {
                    addEntryToFirebase(name, phone, address);
                }
            }
        });

        btnSendBulk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEntriesBulk();
            }
        });

        btnCleanAllEntries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAllEntries();
            }
        });

        btnFetchLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchLastEntry();
            }
        });

        btnFetchAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchAllEntries();
            }
        });

        togBtnAllAsync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    fetchAllEntriesAsync();
                } else {
                    disableFetchAllEntriesAsync();
                }
            }
        });

        togBtnLastAsync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    fetchLastEntryAsync();
                } else {
                    disableFetchLastEntryAsync();
                }
            }
        });

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

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ble_read();
            }
        });

        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);


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

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION}, BLUETOOTH_CODE);

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
                Toast.makeText(MainActivity.this, "permisos denegats", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void ble_setScanRule() {
        // TODO fer que es connecti automàticament.
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
                multiLineResults.setText("Device found: \n" + nrf52.getName() + "\n" + nrf52.getMac());
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
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    multiLineResults.setText("");

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
        Log.d("BLE", "BLE_read: ");

        if (!isNRF52Connected) {
            return;
        }

        String uuid_service = formatAsUUID(SERVICE_UUID);
        String uuid_characteristic = formatAsUUID(CHARACTERISTIC_UUID);

        BleManager.getInstance().read(nrf52, uuid_service, uuid_characteristic, new BleReadCallback() {
            @Override
            public void onReadSuccess(byte[] data) {
//                ByteBuffer wrapped = ByteBuffer.wrap(data);
//                int num = wrapped.getInt();
                String big_endian = HexUtil.formatHexString(data, false);

//                ByteBuffer bbuf = ByteBuffer.allocate(4);
//                bbuf.order(ByteOrder.BIG_ENDIAN);
//                bbuf.putInt(Integer.parseInt(num));
//                bbuf.order(ByteOrder.LITTLE_ENDIAN);
//                int integer_little_endian = bbuf.getInt();

                String little_endian = swapEndianString(big_endian);
                multiLineResults.setText(little_endian);
                Log.d("BLE", "Received: " + big_endian);
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
        boolean result = ref.addEntryToFirebase(entry);
        if (!result) {
            Toast.makeText(this, "add entry failed", Toast.LENGTH_SHORT).show();
        }

    }

    // simply adds 10 random entries by calling addEntry 10 times
    private void addEntriesBulk() {
        for (int i = 0; i < 10; i++) {
            int temp = ThreadLocalRandom.current().nextInt(-10, 40 + 1);
            int wind = ThreadLocalRandom.current().nextInt(0, 100 + 1);
            int precipitation = ThreadLocalRandom.current().nextInt(0, 50);
            Entry entry = new Entry();
            entry.setTemperature(String.valueOf(temp));
            entry.setWind(String.valueOf(wind));
            entry.setPrecipitation(String.valueOf(precipitation));

            if (!ref.addEntryToFirebase(entry)) {
                Toast.makeText(this, "add entries failed", Toast.LENGTH_SHORT).show();
                break;
            }

        }
    }

    // fetches all entries and saves them in an ArrayList
    // it may be saved as well in a map/dictionary
    private void fetchAllEntries() {
        boolean result = ref.fetchAllEntries();

        if (result) {
            multiLineResults.setText(String.valueOf(ref.getAllEntries()));
        }
    }

    // enables an async listener for changed on the database.
    // everytime there is an update on the database, it gets the new data and updates the GUI
    private void fetchAllEntriesAsync() {
        boolean result = ref.fetchAllEntriesAsync();
        if (result) {
            multiLineResults.setText(String.valueOf(ref.getAllEntries()));
        }
    }

    // disables asyncListener so new values are not automatically fetched
    private void disableFetchAllEntriesAsync() {
        ref.disableFetchAllEntriesAsync();
    }

    private void disableFetchLastEntryAsync() {
        ref.disableFetchLastEntryAsync();
    }

    // fetches last entry by using the key previously saved.
    private void fetchLastEntryAsync() {
        boolean result = ref.fetchLastEntryAsync();
        if (result) {
            multiLineResults.setText(String.valueOf(ref.getCurrentEntry()));
        }
    }

    private void fetchLastEntry() {
        boolean result = ref.fetchLastEntry();
        if (result) {
            multiLineResults.setText(String.valueOf(ref.getCurrentEntry()));
        }


    }

    // clearing all the content from the reference (all entries in Firestore Realtime)
    private void clearAllEntries() {
        boolean result = ref.clearAllEntries();
        if(result){
            Toast.makeText(MainActivity.this, "All entries cleared", Toast.LENGTH_SHORT).show();
        }
    }


    // clers text from GUI textboxes
    private void clearTextBoxes() {
        textTemp.getText().clear();
        textPrec.getText().clear();
        textWind.getText().clear();
    }
}