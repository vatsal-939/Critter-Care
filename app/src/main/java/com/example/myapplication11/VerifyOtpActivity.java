package com.example.myapplication11;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthhelper.R;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class VerifyOtpActivity extends AppCompatActivity {

    private static final String TAG = "VerifyOtp";
    private EditText e1,e2,e3,e4,e5,e6;
    private Button btnVerify;
    private String phoneNumber;
    private String receivedOtp,editTextOTP;
    Intent intent;
    FileInputStream fstream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        e1 = findViewById(R.id.otpDigit1);
        e2 = findViewById(R.id.otpDigit2);
        e3 = findViewById(R.id.otpDigit3);
        e4 = findViewById(R.id.otpDigit4);
        e5 = findViewById(R.id.otpDigit5);
        e6 = findViewById(R.id.otpDigit6);
        btnVerify = findViewById(R.id.button2);

        setOtpAutoMove(e1,e2,null);
        setOtpAutoMove(e2,e3,e1);
        setOtpAutoMove(e3,e4,e2);
        setOtpAutoMove(e4,e5,e3);
        setOtpAutoMove(e5,e6,e4);
        setOtpAutoMove(e6,null,e5);


        // Get phone number from the Intent
        phoneNumber = getIntent().getStringExtra("phone_number");

        // Make sure phone number includes country code
        if (phoneNumber != null && !phoneNumber.startsWith("+")) {
            phoneNumber = "+91" + phoneNumber;  // Assuming Indian phone numbers
        }

        // Send OTP via backend (Twilio)
        sendOtpToBackend();

        // Verify button click
        btnVerify.setOnClickListener(v -> verifyOTP());
    }

    private void setOtpAutoMove(final EditText currentField, final EditText nextField, final EditText previousField) {
        currentField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && nextField != null) {
                    nextField.requestFocus();  // Move focus to next EditText
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0 && previousField != null || currentField == null) {
                    previousField.requestFocus();
                }
            }
        });

        currentField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    // Move to the previous field if backspace is pressed and the current field is empty
                    if (((EditText) v).getText().toString().isEmpty() && previousField != null) {
                        previousField.requestFocus();  // Move focus to the previous EditText
                    }
                }
                return false;
            }
        });
    }

    private void sendOtpToBackend() {
        // Create a logging interceptor to log network requests and responses
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create an OkHttpClient with the logging interceptor
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:3000/")  // Use this for emulator or your local IP for real devices
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        // Create the service
        OtpService otpService = retrofit.create(OtpService.class);

        // Create request body
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("phoneNumber", phoneNumber);

        // Send OTP request
        otpService.sendOtp(requestBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject responseBody = response.body();
                    if (responseBody != null && responseBody.has("otp")) {
                        receivedOtp = responseBody.get("otp").getAsString();
                        Log.d(TAG, "OTP sent to " + phoneNumber);
                        Toast.makeText(VerifyOtpActivity.this, "OTP sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Unexpected response format: " + responseBody);
                        Toast.makeText(VerifyOtpActivity.this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Log.e(TAG, "Failed to send OTP. Response code: " + response.code());
                        Log.e(TAG, "Error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response body", e);
                    }
                }
            }


            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });
    }

    private void verifyOTP() {
        String otp1 = e1.getText().toString();
        String otp2 = e2.getText().toString();
        String otp3 = e3.getText().toString();
        String otp4 = e4.getText().toString();
        String otp5 = e5.getText().toString();
        String otp6 = e6.getText().toString();
        editTextOTP = otp1+otp2+otp3+otp4+otp5+otp6;

        String enteredOtp = editTextOTP;
        if (enteredOtp.equals(receivedOtp)) {

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
                    navigateToDoctor();
                } else if (details[2].equals("Patient")) {
                    navigateToPatient();
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Log.d(TAG, "OTP verified successfully!");

        } else {
            Log.e(TAG, "Invalid OTP entered.");
        }
    }

    private void navigateToDoctor() {
        // Navigate to the HomeActivity
        intent = new Intent(VerifyOtpActivity.this, DoctorActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToPatient() {
        // Navigate to the HomeActivity
        intent = new Intent(VerifyOtpActivity.this, PatientActivity.class);
        startActivity(intent);
        finish();
    }



    // Retrofit interface for sending OTP
    interface OtpService {
        @Headers("Content-Type: application/json")
        @POST("send-otp")
        Call<JsonObject> sendOtp(@Body HashMap<String, String> requestBody);
    }
}