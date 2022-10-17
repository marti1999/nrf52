package com.example.test;


import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {

    // TOTA LA INFORMACIÓ ES POT TROBAR AQUÍ
    // https://firebase.google.com/docs/database/android/read-and-write
    // https://firebase.google.com/docs/database/android/lists-of-data
    // https://www.geeksforgeeks.org/how-to-save-data-to-the-firebase-realtime-database-in-android/


    // creating variables for EditText and Buttons.
    private EditText textTemp, textWind, textPrec;
    private TextView multiLineResults;
    private Button btnSendEntry, btnSendBulk, btnCleanAllEntries, btnFetchLast, btnFetchAll, btnGetStats;
    private ToggleButton togBtnAllAsync, togBtnLastAsync;


    // creating a variable for our Firebase Database.
    FirebaseDatabase firebaseDatabase;

    // creating a variable for our Database reference for Firebase.
    DatabaseReference databaseReference;

    // Variables used in the activity
    String lastKey = "";
    ArrayList<Entry> allEntries = new ArrayList<Entry>();
    ValueEventListener asyncListenerAll, asyncListenerLast;


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

        // creating firebase instance
        firebaseDatabase = FirebaseDatabase.getInstance();
        // Creating a reference to our collection
        databaseReference = firebaseDatabase.getReference("Entries");

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
                clearTextBoxes();
                lastKey = pushedRef.getKey();
            }
        });

        // THIS WOULD ADD A NEW ENTRY WITH A CUSTOM KEY, IMPORTANT IF IT NEEDS TO BE SORTED LATER BY KEY
/*        long key = new Date().getTime();
        databaseReference.child(String.valueOf(key)).setValue(entry).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(MainActivity.this, "Data sent", Toast.LENGTH_LONG).show();
            }
        });

        // THIS IS AN ASYNC TASK, NOT REALLY NEEDED TO USE, SO LET'S KEEP IT SIMPLE
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseReference.setValue(employeeInfo);
                long id = new Date().getTime();
                databaseReference.child(String.valueOf(id)).setValue(entry);
                Toast.makeText(MainActivity.this, "data added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Fail to add data " + error, Toast.LENGTH_SHORT).show();
            }
        });

         */
    }

    // simply adds 10 random entries by calling addEntry 10 times
    private void addEntriesBulk(){
        for (int i = 0; i<10; i++){
            int temp = ThreadLocalRandom.current().nextInt(-10, 40+1);
            int wind = ThreadLocalRandom.current().nextInt(0, 100+1);
            int precipitation = ThreadLocalRandom.current().nextInt(0, 50);

            addEntryToFirebase(String.valueOf(temp), String.valueOf(wind), String.valueOf(precipitation));
        }
    }

    // fetches all entries and saves them in an ArrayList
    // it may be saved as well in a map/dictionary
    private void fetchAllEntries() {
        databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {

                    //https://stackoverflow.com/questions/32886546/how-to-extract-a-list-of-objects-from-firebase-datasnapshot-on-android
                    // thesecond solution explains how to save in in a map/dictionary
                    // https://medium.com/firebase-developers/how-to-map-an-array-of-objects-from-realtime-database-to-a-list-of-objects-53f27b33c8f3
                    // this second link explains how to map snapshot data to whatever you need to.

                    // clearing past entries
                    allEntries.clear();

                    // iterating through all children from the result (the result is a list, each children is an Entry)
                    for (DataSnapshot entrySnap : task.getResult().getChildren()) {
                        Entry en = entrySnap.getValue(Entry.class);
                        allEntries.add(en);
                    }
                    // printing results
                    multiLineResults.setText(String.valueOf(allEntries));
                }
            }
        });
    }

    // enables an async listener for changed on the database.
    // everytime there is an update on the database, it gets the new data and updates the GUI
    private void fetchAllEntriesAsync(){
        // this variable needs to be outside the function because it is needed in another.
        asyncListenerAll = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allEntries.clear();

                for (DataSnapshot entrySnap : snapshot.getChildren()){
                    Entry en = entrySnap.getValue(Entry.class);
                    allEntries.add(en);
                }
                multiLineResults.setText(String.valueOf(allEntries));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("firebase", "Error getting data", error.toException().getCause());
            }
        });
    }

    // disables asyncListener so new values are not automatically fetched
    private void disableFetchAllEntriesAsync(){
        if (databaseReference != null && asyncListenerAll != null){
            databaseReference.removeEventListener(asyncListenerAll);
        }
    }

    private void disableFetchLastEntryAsync(){
        if (databaseReference != null && asyncListenerAll != null){
            databaseReference.removeEventListener(asyncListenerLast);
        }
    }

    // fetches last entry by using the key previously saved.
    private void fetchLastEntryAsync() {
        // notice that child(lastKey) is called before get(). Now it only gets the element inside the list, instead of the whole list.
        asyncListenerLast = databaseReference.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Entry en = new Entry();

                for (DataSnapshot entrySnap : snapshot.getChildren()){
                    en = entrySnap.getValue(Entry.class);
                }
                multiLineResults.setText(String.valueOf(en));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void fetchLastEntry(){
        databaseReference.child(lastKey).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Entry entry = task.getResult().getValue(Entry.class);
                    multiLineResults.setText(String.valueOf(entry));
                }
            }
        });

    }

    // clearing all the content from the reference (all entries in Firestore Realtime)
    private void clearAllEntries() {
        databaseReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(MainActivity.this, "All entries cleared", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // clers text from GUI textboxes
    private void clearTextBoxes() {
        textTemp.getText().clear();
        textPrec.getText().clear();
        textWind.getText().clear();
    }
}