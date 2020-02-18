package com.hawai.hawaiadmin.TrackingManagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hawai.hawaiadmin.Models.MyMarker;
import com.hawai.hawaiadmin.Models.RiderModel;
import com.hawai.hawaiadmin.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hawai.hawaiadmin.Utils.CommonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ArrayList<MyMarker> mMyMarkersArray = new ArrayList<MyMarker>();
    private HashMap<Marker, MyMarker> mMarkersHashMap;
    private RelativeLayout pinView;
    String riderId;
    DatabaseReference mDatabase;
    private HashMap<String, Marker> myMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        pinView = findViewById(R.id.pinView);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        riderId = getIntent().getStringExtra("riderId");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void plotSingleMarker(final MyMarker myMarker) {
        // Create user marker with custom icon and other options
//
        Glide.with(MapsActivity.this).asBitmap().load(myMarker.getmIcon()).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                LatLng latLng = new LatLng(myMarker.getmLatitude(), myMarker.getmLongitude());
                Marker marr = myMap.get(myMarker.getPhone());
                if (marr != null) {
                    marr.remove();
                }
                Marker currentMarker = mMap.addMarker(new MarkerOptions()
                        .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
//                                .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(resource, 120, 120, false)))
                        .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(MapsActivity.this, resource)))
                        .position(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

                myMap.put(myMarker.getPhone(), currentMarker);
                mMarkersHashMap.put(currentMarker, myMarker);
                mMap.setInfoWindowAdapter(new MapsActivity.MarkerInfoWindowAdapter());

            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });


    }


    public Bitmap createCustomMarker(final Context context, Bitmap bitmap12) {

        final View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);

        final CircleImageView markerImage = marker.findViewById(R.id.user_dp);


        TextView txt_name = (TextView) marker.findViewById(R.id.name);


        markerImage.setImageBitmap(bitmap12);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(70, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }

    public Bitmap getBitmapFromURL(String strURL) {

        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the map.

        // Try to obtain the map from the SupportMapFragment.

        // Check if we were successful in obtaining the map.

        if (mMap != null) {
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {
                    marker.showInfoWindow();
                    return true;
                }
            });


        } else
            Toast.makeText(getApplicationContext(), "Unable to create Maps", Toast.LENGTH_SHORT).show();


    }

    public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        public MarkerInfoWindowAdapter() {
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            View v = getLayoutInflater().inflate(R.layout.infowindow_layout, null);
            final MyMarker myMarker = mMarkersHashMap.get(marker);
            CircleImageView markerIcon = v.findViewById(R.id.marker_icon);
            TextView markerLabel = (TextView) v.findViewById(R.id.marker_label);
            TextView anotherLabel = (TextView) v.findViewById(R.id.another_label);
            Glide.with(MapsActivity.this).load(myMarker.getmIcon()).into(markerIcon);
            markerLabel.setText(myMarker.getmLabel());
            anotherLabel.setText("Phone: " + myMarker.getPhone());
            return v;
        }
    }


    //    /**
//     * Manipulates the map once available.
//     * This callback is triggered when the map is ready to be used.
//     * This is where we can add markers or lines, add listeners or move the camera. In this case,
//     * we just add a marker near Sydney, Australia.
//     * If Google Play services is not installed on the device, the user will be prompted to install
//     * it inside the SupportMapFragment. This method will only be triggered once the user has
//     * installed Google Play services and returned to the app.
//     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        double lat = ListOfRiders.riderList.get(0).getLatitude();
//        double lon = ListOfRiders.riderList.get(0).getLongitude();
//        LatLng sydney = new LatLng(lat, lon);
//        mMap.addMarker(createMarker(this, sydney, 4));

//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16));

        getDataFromServer();

    }

    private void getDataFromServer() {
        mDatabase.child("Riders").child(riderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {

                    RiderModel model = dataSnapshot.getValue(RiderModel.class);
                    if (model != null) {
                        if(model.isActive()) {
                            mMyMarkersArray.clear();
                            mMarkersHashMap = new HashMap<Marker, MyMarker>();

                            MyMarker marker = new MyMarker(model.getName(),
                                    model.getPicUrl(), model.getLatitude(), model.getLongitude(), model.getPhone());


                            setUpMap();
                            plotSingleMarker(marker);
                        }else{
                            CommonUtils.showToast("Turned off tracking");
                            Marker marr = myMap.get(model.getPhone());
                            if(marr!=null) {
                                marr.remove();
                            }
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
