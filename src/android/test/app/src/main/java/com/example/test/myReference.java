package com.example.test;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

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

public class myReference {
    // creating a variable for our Firebase Database.
    private FirebaseDatabase firebaseDatabase;
    // creating a variable for our Database reference for Firebase.
    private DatabaseReference databaseReference;
    private String lastKey = "";
    private ArrayList<Entry> allEntries = new ArrayList<Entry>();
    private Entry currentEntry;
    private ValueEventListener asyncListenerAll, asyncListenerLast;

    public Entry getCurrentEntry() {
        return currentEntry;
    }

    public void setCurrentEntry(Entry currentEntry) {
        this.currentEntry = currentEntry;
    }

    public FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }

    public void setFirebaseDatabase(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
    }

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }

    public void setDatabaseReference(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public String getLastKey() {
        return lastKey;
    }

    public void setLastKey(String lastKey) {
        this.lastKey = lastKey;
    }

    public ArrayList<Entry> getAllEntries() {
        return allEntries;
    }

    public void setAllEntries(ArrayList<Entry> allEntries) {
        this.allEntries = allEntries;
    }

    public ValueEventListener getAsyncListenerAll() {
        return asyncListenerAll;
    }

    public void setAsyncListenerAll(ValueEventListener asyncListenerAll) {
        this.asyncListenerAll = asyncListenerAll;
    }

    public ValueEventListener getAsyncListenerLast() {
        return asyncListenerLast;
    }

    public void setAsyncListenerLast(ValueEventListener asyncListenerLast) {
        this.asyncListenerLast = asyncListenerLast;
    }

    public myReference(String path) {
        // creating firebase instance
        firebaseDatabase = FirebaseDatabase.getInstance();
        // Creating a reference to our collection
        databaseReference = firebaseDatabase.getReference(path);

    }

    public boolean addEntryToFirebase(Entry entry) {
        final boolean[] isSuccess = {false};

        // NOW IT IS PUTTING A NEW ENTRY WITH AN AUTOMATIC KEY https://firebase.google.com/docs/database/android/lists-of-data#append_to_a_list_of_data
        // push() is creating a random key, so there's no need to call child(myKey)
        // if the random key needs to be accessed, the reference returned by push must be kept in a variable
        DatabaseReference pushedRef = databaseReference.push();

        pushedRef.setValue(entry).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                lastKey = pushedRef.getKey();
                isSuccess[0] = true;
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
         */
        // TODO arreglar aquesta merda que no fa await al listener i sempre retornarà fallse...
        // passa amb la resta de mètodes.
        return true;
    }

    // fetches all entries and saves them in an ArrayList
    // it may be saved as well in a map/dictionary
    public boolean fetchAllEntries() {
        final boolean[] isSuccess = {false};
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

                    isSuccess[0] = true;

                    // clearing past entries
                    allEntries.clear();

                    // iterating through all children from the result (the result is a list, each children is an Entry)
                    for (DataSnapshot entrySnap : task.getResult().getChildren()) {
                        Entry en = entrySnap.getValue(Entry.class);
                        allEntries.add(en);
                    }
                    // printing results

                }
            }
        });
        return true;
    }

    // enables an async listener for changed on the database.
    // everytime there is an update on the database, it gets the new data and updates the GUI
    public boolean fetchAllEntriesAsync() {
        final boolean[] isSuccess = {false};
        // this variable needs to be outside the function because it is needed later on .
        asyncListenerAll = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allEntries.clear();
                isSuccess[0] = true;

                for (DataSnapshot entrySnap : snapshot.getChildren()) {
                    Entry en = entrySnap.getValue(Entry.class);
                    allEntries.add(en);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("firebase", "Error getting data", error.toException().getCause());
            }
        });
        return true;
    }

    // fetches last entry by using the key previously saved.
    public boolean fetchLastEntryAsync() {
        final boolean[] isSuccess = {false};

        // notice that child(lastKey) is called before get(). Now it only gets the element inside the list, instead of the whole list.
        asyncListenerLast = databaseReference.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isSuccess[0] = true;
                for (DataSnapshot entrySnap : snapshot.getChildren()) {
                    currentEntry = entrySnap.getValue(Entry.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return true;

    }

    public boolean fetchLastEntry() {
        final boolean[] isSuccess = {false};
        databaseReference.child(lastKey).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    isSuccess[0] = true;
                    currentEntry = task.getResult().getValue(Entry.class);
                }
            }
        });
        return true;
    }

    // disables asyncListener so new values are not automatically fetched
    public void disableFetchAllEntriesAsync() {
        if (databaseReference != null && asyncListenerAll != null) {
            databaseReference.removeEventListener(asyncListenerAll);
        }
    }

    public void disableFetchLastEntryAsync() {
        if (databaseReference != null && asyncListenerAll != null) {
            databaseReference.removeEventListener(asyncListenerLast);
        }
    }

    public boolean clearAllEntries() {
        final boolean[] isSuccess = {false};
        databaseReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                isSuccess[0] = true;

            }
        });
        return true;
    }
}
