package com.example.myapplication11;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.healthhelper.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    FirebaseDatabase firebaseDatabase;




    DatabaseReference databaseReference;
    private EditText searchPhoneEdt;
    private Button searchBtn;
    private TextView gotoSignUp;
    FileInputStream fstream;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            fstream = openFileInput("emp_detail");
            StringBuffer sbuffer = new StringBuffer();
            int i;

            while ((i = fstream.read())!= -1)
            {
                sbuffer.append((char)i);
            }

            fstream.close();
            String details[] = sbuffer.toString().split("\n");

            if(details[2].equals("Doctor"))
            {
                intent = new Intent(LoginActivity.this,DoctorActivity.class);
            } else if (details[2].equals("Patient")) {
                intent = new Intent(LoginActivity.this,PatientActivity.class);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Employees");


        searchBtn = findViewById(R.id.button);
        searchPhoneEdt=findViewById(R.id.editTextPhone);
        gotoSignUp=findViewById(R.id.textView2);


        gotoSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = searchPhoneEdt.getText().toString();
                if (!TextUtils.isEmpty(phoneNumber)) {
                    searchUserByPhone(phoneNumber);
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter a phone number to search.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void searchUserByPhone(String phoneNumber) {
        Query query = databaseReference.orderByChild("employeeContactNumber").equalTo(phoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.e("inside if","user exsit");
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        EmployeeInfo employee = snapshot.getValue(EmployeeInfo.class);
                        if (employee != null) {
                            Log.e("sending msg",employee.toString());
                            Toast.makeText(LoginActivity.this, "User found: " + employee.getEmployeeName(), Toast.LENGTH_LONG).show();
                            // You can update UI or perform other actions here
                            Intent intent=new Intent(LoginActivity.this, VerifyOtpActivity.class);
                            intent.putExtra("phone_number",phoneNumber);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "No user found with this phone number.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Search failed: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}