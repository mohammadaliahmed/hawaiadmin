package com.appsinventiv.hawaiadmin.Activites.UserManagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.appsinventiv.hawaiadmin.Activites.MainActivity;
import com.appsinventiv.hawaiadmin.Models.AdminModel;
import com.appsinventiv.hawaiadmin.R;
import com.appsinventiv.hawaiadmin.Utils.CommonUtils;
import com.appsinventiv.hawaiadmin.Utils.SharedPrefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {


    TextView signup;
    Button login;
    EditText phone, password;
    DatabaseReference mDatabase;
    HashMap<String, AdminModel> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signup = findViewById(R.id.signup);
        login = findViewById(R.id.login);
        phone = findViewById(R.id.phone);
        password = findViewById(R.id.password);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        getDataFromServer();
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));

            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String us = phone.getText().toString();
                String pas = password.getText().toString();
                if (phone.getText().length() == 0) {
                    phone.setError("Enter phone");
                } else if (password.getText().length() == 0) {
                    password.setError("Enter password");
                } else {
                    checkLogin();
                }
            }
        });


    }

    private void checkLogin() {
        if (map.containsKey(phone.getText().toString())) {
            if (map.get(phone.getText().toString()).getPassword().equalsIgnoreCase(password.getText().toString())) {
                CommonUtils.showToast("Successfully login");
                SharedPrefs.setUserModel(map.get(phone.getText().toString()));
                startActivity(new Intent(Login.this, MainActivity.class));
                finish();
            } else {
                CommonUtils.showToast("Wrong password");
            }
        } else {
            CommonUtils.showToast("User does not exists\nPlease signup");
        }
    }

    private void getDataFromServer() {
        mDatabase.child("Admins").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        AdminModel model = snapshot.getValue(AdminModel.class);
                        if (model != null) {
                            map.put(model.getPhone(), model);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
