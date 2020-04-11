package com.knowme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.tuyenmonkey.mkloader.MKLoader;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import database.SharedPref;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSetup extends AppCompatActivity {

    CardView btnSignUp;
    DatabaseReference mRef;
    StorageReference storageReference, thumb_ref;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    String id;
    ImageButton btnPick;
    CircleImageView imageView;
    ImageView imageBit;
    EditText editUser;
    Uri uri;
    private byte[] bytes;
    private HashMap<String, String> map;
    MKLoader loader;
    TextView textGo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);
        btnSignUp = findViewById(R.id.btn_sign_up);
        btnPick = findViewById(R.id.profile_setup_btn_pick_image);
        imageView = findViewById(R.id.profile_setup_image);
        editUser = findViewById(R.id.profile_setup_edit_username);
        loader = findViewById(R.id.profile_setup_loader);
        textGo = findViewById(R.id.profile_setup_text_go);

        imageBit = new ImageView(this);

        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setFixAspectRatio(true)
                        .start(ProfileSetup.this);
            }
        });

        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        mRef = firebaseDatabase.getReference().child("Users").child(id);
        storageReference = firebaseStorage.getReference().child("profile_pic").child(id);
        thumb_ref = firebaseStorage.getReference().child("thumb_pic").child(id);

        map = new HashMap<>();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(editUser.getText().toString())) {
                    editUser.setError("username is empty");
                    return;
                }
                if (uri == null) {
                    Toast.makeText(ProfileSetup.this, "please, choose a profile photo", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (editUser.getText().toString().contains(" ")) {
                    editUser.setError("space not allowed");
                    return;
                }
                upload();
            }
        });


    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result != null) {
                uri = result.getUri();
                imageBit.setImageURI(uri);
                Bitmap bitmap = ((BitmapDrawable) imageBit.getDrawable()).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                bytes = stream.toByteArray();
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));

            }
        }
    }

    private void upload() {
        textGo.setVisibility(View.INVISIBLE);
        loader.setVisibility(View.VISIBLE);
        btnPick.setEnabled(false);
        btnSignUp.setEnabled(false);
        thumb_ref.putBytes(bytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    thumb_ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {
                            map.put("username", editUser.getText().toString().trim());
                            map.put("thumb_nail", uri.toString());
                            map.put("display_name", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                            map.put("email_address", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                            firebaseDatabase.getReference().child("Users").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        SharedPref.getInstance(ProfileSetup.this).addUser(map.get("username"), map.get("email_address"),
                                                uri.toString(), id);
                                        startActivity(new Intent(ProfileSetup.this, UserActivity.class));
                                        finish();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }
}
