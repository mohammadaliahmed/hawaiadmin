package com.appsinventiv.hawaiadmin.Activites.RidersManagement;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.appsinventiv.hawaiadmin.Activites.UserManagement.Login;
import com.appsinventiv.hawaiadmin.Models.RiderModel;
import com.appsinventiv.hawaiadmin.R;
import com.appsinventiv.hawaiadmin.TrackingManagement.MapsActivity;
import com.appsinventiv.hawaiadmin.Utils.CommonUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ListOfRiders extends AppCompatActivity {


    FloatingActionButton addRider;
    RecyclerView recyclerView;
    RiderListAdapter adapter;
    public static ArrayList<RiderModel> riderList = new ArrayList<>();
    DatabaseReference mDatabase;
    RelativeLayout wholeLayout;
    EditText search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_riders);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mDatabase = FirebaseDatabase.getInstance().getReference();
        this.setTitle("Riders");
        wholeLayout = findViewById(R.id.wholeLayout);
        search = findViewById(R.id.search);
        recyclerView = findViewById(R.id.recyclerView);
        addRider = findViewById(R.id.addRider);
        adapter = new RiderListAdapter(this, riderList, new RiderListAdapter.RiderAdapterCallbacks() {
            @Override
            public void onEditRider(RiderModel model) {
                Intent i = new Intent(ListOfRiders.this, AddRider.class);
                i.putExtra("riderId", model.getId());
                startActivity(i);
            }

            @Override
            public void onTrackRider(RiderModel model) {
//                CommonUtils.showToast("Track rider");
//                Intent i = new Intent(ListOfRiders.this, MapsActivity.class);
//                i.putExtra("listPosition", model.getId());
//                startActivity(i);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        getDataFromServer();


        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                adapter.filter(editable.toString());
            }
        });

        addRider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ListOfRiders.this, AddRider.class));
            }
        });
    }

    private void getDataFromServer() {
        wholeLayout.setVisibility(View.VISIBLE);
        mDatabase.child("Riders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    riderList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        RiderModel model = snapshot.getValue(RiderModel.class);
                        if (model != null && model.getName() != null) {
                            riderList.add(model);
                        }
                    }
                    wholeLayout.setVisibility(View.GONE);
                    adapter.updateList(riderList);
                    adapter.notifyDataSetChanged();
                } else {
                    riderList.clear();
                    wholeLayout.setVisibility(View.GONE);
                    CommonUtils.showToast("No Data");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
