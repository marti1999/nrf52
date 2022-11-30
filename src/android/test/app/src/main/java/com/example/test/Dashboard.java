package com.example.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class Dashboard extends AppCompatActivity {
    private TextView ResultsTemp, ResultsPrec, ResultsWind;

    // creating a variable for our Firebase Database.
    FirebaseDatabase firebaseDatabase;

    // creating a variable for our Database reference for Firebase.
    DatabaseReference databaseReference;

    // Variables used in the activity
    String lastKey = "";
    ArrayList<Entry> allEntries = new ArrayList<Entry>();
    ValueEventListener asyncListenerAll,asyncListenerLast;

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

        //fetchAllEntriesAsync();
        fetchLastEntryAsync();
    }

    // fetches last entry by using the key previously saved.
    private void fetchLastEntryAsync() {
        // notice that child(lastKey) is called before get(). Now it only gets the element inside the list, instead of the whole list.
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
}