package com.hawai.hawaiadmin.Activites.RidersManagement;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hawai.hawaiadmin.Models.RiderModel;
import com.hawai.hawaiadmin.R;
import com.hawai.hawaiadmin.Utils.CommonUtils;
import com.hawai.hawaiadmin.Utils.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AddRider extends AppCompatActivity {

    EditText name, phone, password;
    Button save, delete;
    DatabaseReference mDatabase;
    String riderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rider);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        this.setTitle("Add Rider");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        riderId = getIntent().getStringExtra("riderId");
        if (riderId != null) {

            getdataFromServer();
        }
        mDatabase = FirebaseDatabase.getInstance().getReference();
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        password = findViewById(R.id.password);
        delete = findViewById(R.id.delete);
        save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().length() == 0) {
                    name.setError("Enter name");
                } else if (phone.getText().length() == 0) {
                    phone.setError("Enter phone");
                } else if (password.getText().length() == 0) {
                    password.setError("Enter password");
                } else {
                    if (riderId != null) {
                        updateDetals();
                    } else {
                        saveRiderDetails();
                    }
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlert();
            }
        });
    }

    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage("Do you want to delete this rider? ");

        // add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDatabase.child("Riders").child(riderId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        CommonUtils.showToast("Deleted");
                        finish();
                    }
                });

            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getdataFromServer() {
        mDatabase.child("Riders").child(riderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    RiderModel model = dataSnapshot.getValue(RiderModel.class);
                    if (model != null) {
                        name.setText(model.getName());
                        phone.setText(model.getPhone());
                        password.setText(model.getPassword());
                        delete.setVisibility(View.VISIBLE);
                        phone.setFocusable(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveRiderDetails() {
        riderId = phone.getText().toString();
        mDatabase.child("Riders").child(riderId).setValue(new RiderModel(riderId,
                name.getText().toString(),
                riderId, password.getText().toString(),
                Constants.PIC_URL
        )).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                CommonUtils.showToast("Saved");
                delete.setVisibility(View.VISIBLE);

            }
        });
    }

    private void updateDetals() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name.getText().toString());
        map.put("password", password.getText().toString());
        mDatabase.child("Riders").child(riderId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                CommonUtils.showToast("Updated");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {


            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}
