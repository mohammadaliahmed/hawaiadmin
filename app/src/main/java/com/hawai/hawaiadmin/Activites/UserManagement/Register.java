package com.hawai.hawaiadmin.Activites.UserManagement;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.hawai.hawaiadmin.Activites.MainActivity;
import com.hawai.hawaiadmin.Models.AdminModel;
import com.hawai.hawaiadmin.R;
import com.hawai.hawaiadmin.Utils.CommonUtils;
import com.hawai.hawaiadmin.Utils.CompressImage;
import com.hawai.hawaiadmin.Utils.Glide4Engine;
import com.hawai.hawaiadmin.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import de.hdodenhof.circleimageview.CircleImageView;

public class Register extends AppCompatActivity {


    TextView login;
    ImageView pick;
    CircleImageView image;
    EditText name, email, phone, password;
    Button register;
    private List<Uri> mSelected = new ArrayList<>();
    DatabaseReference mDatabase;
    private String imageUrl;
    RelativeLayout wholeLayout;
    HashMap<String, String> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        login = findViewById(R.id.login);
        image = findViewById(R.id.image);
        pick = findViewById(R.id.pick);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        phone = findViewById(R.id.phone);
        register = findViewById(R.id.register);
        wholeLayout = findViewById(R.id.wholeLayout);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        getPermissions();

        getDataFromServer();
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMatisse();
            }
        });

        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (map.size() > 0) {
                    if (map.containsKey(editable.toString())) {
                        CommonUtils.showToast("Phone number already taken");
                    }
                }
            }
        });


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().length() == 0) {
                    name.setError("Enter name");
                } else if (phone.getText().length() == 0) {
                    phone.setError("Enter phone");
                } else if (email.getText().length() == 0) {
                    email.setError("Enter email");
                } else if (password.getText().length() == 0) {
                    password.setError("Enter password");
                } else if (mSelected.size() == 0) {
                    CommonUtils.showToast("Please select picture");
                } else {
                    signup();
                }
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    private void getDataFromServer() {
        mDatabase.child("Admins").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        AdminModel model = snapshot.getValue(AdminModel.class);
                        if (model != null) {
                            map.put(model.getPhone(), model.getPhone());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void signup() {
        if (map.containsKey(phone.getText().toString())) {
            CommonUtils.showToast("Phone number already taken");
        } else {
            putPictures(imageUrl);
        }
    }

    private void initMatisse() {
        Matisse.from(this)
                .choose(MimeType.ofImage(), false)
                .countable(true)
                .maxSelectable(1)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new Glide4Engine())
                .forResult(23);
    }

    public void putPictures(String path) {
        wholeLayout.setVisibility(View.VISIBLE);
        CommonUtils.showToast("Uploading image");
        StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        String imgName = Long.toHexString(Double.doubleToLongBits(Math.random()));

        ;
        Uri file = Uri.fromFile(new File(path));


        StorageReference riversRef = mStorageRef.child("Photos").child(imgName);

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    @SuppressWarnings("VisibleForTests")
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
//                        Uri downloadUrl = taskSnapshot.getre;
                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful()) ;
                        CommonUtils.showToast("Registering");
                        Uri downloadUrl = urlTask.getResult();
                        final AdminModel model = new AdminModel(
                                phone.getText().toString(),
                                name.getText().toString(),
                                email.getText().toString(),
                                phone.getText().toString(),
                                password.getText().toString(),
                                downloadUrl + "",
                                "",
                                System.currentTimeMillis()

                        );
                        mDatabase.child("Admins").child(phone.getText().toString()).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                CommonUtils.showToast("Successfully registered");
                                SharedPrefs.setUserModel(model);
                                startActivity(new Intent(Register.this, MainActivity.class));
                                finish();
                            }
                        });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        CommonUtils.showToast(exception.getMessage());
                        wholeLayout.setVisibility(View.GONE);
                        // ...
                    }
                });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 23 && data != null) {

            mSelected = Matisse.obtainResult(data);
            Glide.with(Register.this).load(mSelected.get(0)).into(image);
            CompressImage compressImage = new CompressImage(this);
            imageUrl = compressImage.compressImage("" + mSelected.get(0));
        }
    }

    private void getPermissions() {


        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE

        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                } else {
                }
            }
        }
        return true;
    }

}
