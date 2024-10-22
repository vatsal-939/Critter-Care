package com.example.myapplication11;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.healthhelper.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {




    private static final String TAG = "MainActivity";
    private EditText employeeNameEdt, employeePhoneEdt;
    private Button sendDatabtn;
    private RadioGroup radioGroup;
    private RadioButton r1, r2;
    int selectedId;
    FileOutputStream fstream;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Starting application");

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase", e);
            Toast.makeText(this, "Failed to initialize Firebase", Toast.LENGTH_LONG).show();
            return;
        }

        employeeNameEdt = findViewById(R.id.idEdtEmployeeName);
        employeePhoneEdt = findViewById(R.id.idEdtEmployeePhoneNumber);
        radioGroup = findViewById(R.id.radioGroupRole);
        r1 = findViewById(R.id.radioDoctor);
        r2 = findViewById(R.id.radioPatient);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Employees");

        Log.d(TAG, "Database Reference: " + databaseReference.toString());

        sendDatabtn = findViewById(R.id.idBtnSendData);

        // Check database connection
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                Log.d(TAG, "Firebase connection: " + (connected ? "Connected" : "Disconnected"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase connection listener cancelled", error.toException());
            }
        });

        sendDatabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }

                String name = employeeNameEdt.getText().toString() + '\n';
                String phone = employeePhoneEdt.getText().toString() + '\n';
                selectedId = radioGroup.getCheckedRadioButtonId();

                try {
                    fstream = openFileOutput("emp_detail", Context.MODE_PRIVATE);
                    fstream.write(name.getBytes());
                    fstream.write(phone.getBytes());
                    RadioButton selectedRadioButton = findViewById(selectedId);
                    String role = selectedRadioButton.getText().toString();
                    fstream.write(role.getBytes());
                    fstream.close();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Log.d(TAG, "Radio group: " + radioGroup.toString());
                Log.d(TAG, "Input data: Name=" + name + ", Phone=" + phone + ", SelectedId=" + selectedId);

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                    Toast.makeText(MainActivity.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                } else if (selectedId == -1) {
                    Toast.makeText(MainActivity.this, "Please select Doctor or Patient.", Toast.LENGTH_SHORT).show();
                } else {
                    RadioButton selectedRadioButton = findViewById(selectedId);
                    String role = selectedRadioButton.getText().toString();
                    Log.d(TAG,role);
                    addDatatoFirebase(name, phone, role);
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void addDatatoFirebase(String name, String phone, String role) {
        String employeeId = databaseReference.push().getKey();
        EmployeeInfo employeeInfo = new EmployeeInfo(name, phone, role);

        Log.d(TAG, "Attempting to add data: " + employeeInfo.toString());
        Log.d(TAG, "Employee ID: " + employeeId);

        databaseReference.child(employeeId).setValue(employeeInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, employeeInfo.toString());
                        Toast.makeText(MainActivity.this, "Data added successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        clearInputFields();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding data", e);
                        Toast.makeText(MainActivity.this, "Error adding data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void clearInputFields() {
        employeeNameEdt.setText("");
        employeePhoneEdt.setText("");
        radioGroup.clearCheck();
    }
}