package com.example.test;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // TOTA LA INFORMACIÓ ES POT TROBAR AQUÍ
    // https://firebase.google.com/docs/database/android/read-and-write
    // https://firebase.google.com/docs/database/android/lists-of-data
    // https://www.geeksforgeeks.org/how-to-save-data-to-the-firebase-realtime-database-in-android/


    // creating variables for
    // EditText and buttons.
    private EditText textTemp, textHumidity, textPrec, multiLineResults;
    private Button btnSendEntry, btnSendBulk, btnCleanAllEntries, btnFetchLast, btnFetchAll;


    // creating a variable for our
    // Firebase Database.
    FirebaseDatabase firebaseDatabase;

    // creating a variable for our Database
    // Reference for Firebase.
    DatabaseReference databaseReference;

    // creating a variable for
    // our object class
    Entry entry;
    String lastKey = "";
    ArrayList<Entry> allEntries = new ArrayList<Entry>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing our edittext and button
        textTemp = findViewById(R.id.idTextTemperature);
        textHumidity = findViewById(R.id.idTextHumidity);
        textPrec = findViewById(R.id.idTextPrecipitation);
        multiLineResults = findViewById(R.id.idMultiText);

        // below line is used to get the
        // instance of our FIrebase database.
        firebaseDatabase = FirebaseDatabase.getInstance();

        // below line is used to get reference for our database.
        databaseReference = firebaseDatabase.getReference("Entries");

        // initializing our object
        // class variable.
        entry = new Entry();

        btnSendEntry = findViewById(R.id.idButtonManually);
        btnSendBulk = findViewById(R.id.idButtonBulk);
        btnFetchLast = findViewById(R.id.idButtonFetchLast);
        btnFetchAll = findViewById(R.id.idButtonFetchAll);
        btnCleanAllEntries = findViewById(R.id.idButtonClearAll);

        // adding on click listener for our button.
        btnSendEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // getting text from our edittext fields.
                String name = textTemp.getText().toString();
                String phone = textHumidity.getText().toString();
                String address = textPrec.getText().toString();

                // below line is for checking whether the
                // edittext fields are empty or not.
                if (TextUtils.isEmpty(name) && TextUtils.isEmpty(phone) && TextUtils.isEmpty(address)) {
                    // if the text fields are empty
                    // then show the below message.
                    Toast.makeText(MainActivity.this, "Please add some data.", Toast.LENGTH_SHORT).show();
                } else {
                    // else call the method to add
                    // data to our database.
                    addEntryToFirebase(name, phone, address);
                }
            }
        });


        btnSendBulk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
            }
        });
        btnCleanAllEntries.setOnClickListener(new View.OnClickListener(){

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
    }


    private void fetchAllEntries() {
        databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {

                    //https://stackoverflow.com/questions/32886546/how-to-extract-a-list-of-objects-from-firebase-datasnapshot-on-android
                    // la segona solució explica com guardar-ho en un map/diccionari
                    // https://medium.com/firebase-developers/how-to-map-an-array-of-objects-from-realtime-database-to-a-list-of-objects-53f27b33c8f3
                    // i aquest últim link explica en sí com funciona els snapshots i mapejar dades de format firebase al que volguem

                    allEntries.clear();

                    for (DataSnapshot entrySnap : task.getResult().getChildren()) {
                        Entry en = entrySnap.getValue(Entry.class);
                        allEntries.add(en);
                    }
                    multiLineResults.setText(String.valueOf(allEntries));
                }

            }
        });
    }

    private void fetchLastEntry() {

        databaseReference.child(lastKey).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Toast.makeText(MainActivity.this, String.valueOf(task.getResult().getValue()), Toast.LENGTH_LONG).show();
                    Entry en = task.getResult().getValue(Entry.class);
                    multiLineResults.setText(String.valueOf(en));
                }
            }
        });


/*        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Entry entry = snapshot.getValue(Entry.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }

    private void clearAllEntries() {
        databaseReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(MainActivity.this, "All entries cleared", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void addEntryToFirebase(String temperature, String humidity, String precipitation) {
        // below 3 lines of code is used to set
        // data in our object class.
        entry.setTemperature(temperature);
        entry.setHumidity(humidity);
        entry.setPrecipitation(precipitation);

        // AIXÒ ÉS PER POSAR UN NOU VALOR AMB KEY AUTOMATICA https://firebase.google.com/docs/database/android/lists-of-data#append_to_a_list_of_data
        DatabaseReference pushedRef = databaseReference.push();
        pushedRef.setValue(entry).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                clearTextBoxes();
                lastKey = pushedRef.getKey();
                Toast.makeText(MainActivity.this, "key " + lastKey, Toast.LENGTH_LONG).show();

            }
        });

        // AIXO ÉS PER DONAR UNA KEY MANUALMENT
/*        long key = new Date().getTime();
        databaseReference.child(String.valueOf(key)).setValue(entry).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(MainActivity.this, "Data sent", Toast.LENGTH_LONG).show();
            }
        });*/


        // AIXO ÉS PER FER-HO DE MANERA ASYNC, REALMENT NO CAL TOCAR-HO I MENYS LIOS
        // we are use add value event listener method
        // which is called with database reference.
        /*
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // inside the method of on Data change we are setting
                // our object class to our database reference.
                // data base reference will sends data to firebase.
//                databaseReference.setValue(employeeInfo);
                long id = new Date().getTime();
                databaseReference.child(String.valueOf(id)).setValue(entry);
                //databaseReference.setValue(employeeInfo);

                // after adding this data we are showing toast message.
                Toast.makeText(MainActivity.this, "data added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // if the data is not added or it is cancelled then
                // we are displaying a failure toast message.
                Toast.makeText(MainActivity.this, "Fail to add data " + error, Toast.LENGTH_SHORT).show();
            }
        });

         */
    }

    private void clearTextBoxes() {
        textTemp.getText().clear();
        textPrec.getText().clear();
        textHumidity.getText().clear();
    }
}