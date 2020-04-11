package com.knowme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.gauravk.bubblenavigation.BubbleNavigationLinearView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

import database.SharedPref;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends AppCompatActivity {

    Button signOut;
    TextView textGreetings;
    CircleImageView imageView;
    FirebaseUser user;
    DatabaseReference userDatabase;
    String img, username, greetings;
    BubbleNavigationLinearView bubbleNavigationLinearView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
     //   signOut = findViewById(R.id.btn_sign_out);
        textGreetings = findViewById(R.id.activity_user_text_greetings);
        imageView = findViewById(R.id.activity_user_image_view);
        bubbleNavigationLinearView = findViewById(R.id.bottom_navigation_view_linear);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

        getSupportFragmentManager().beginTransaction().replace(R.id.activity_user_fragment_frame, new HomeFragment()).commit();

        /*findViewById(R.id.activity_user_fragment_frame).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                bubbleNavigationLinearView.setVisibility(View.INVISIBLE);
                else
                    bubbleNavigationLinearView.setVisibility(View.VISIBLE);
                return true;
            }
        });*/

        bubbleNavigationLinearView.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                if(position == 0)
                    getSupportFragmentManager().beginTransaction().replace(R.id.activity_user_fragment_frame, new HomeFragment()).commit();
                else
                    getSupportFragmentManager().beginTransaction().replace(R.id.activity_user_fragment_frame, new QuizFragment()).commit();
            }
        });

        username = SharedPref.getInstance(this).getUser();
        greetings = getGreetings() + username;
        textGreetings.setText(greetings);

        Glide.with(UserActivity.this)
                .asBitmap()
                .override(500, 500)
                .load(Uri.parse(SharedPref.getInstance(this)
                        .getImage()))
                .placeholder(R.drawable.person_white)
                .into(imageView);

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                img = dataSnapshot.child("thumb_nail").getValue(String.class);
                username = dataSnapshot.child("username").getValue(String.class);
                greetings = getGreetings() + username.toLowerCase();
                Glide.with(UserActivity.this).load(img).placeholder(R.drawable.person_white).into(imageView);
                textGreetings.setText(greetings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
/*
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut.setText("Signing Out...");
                signOut.setEnabled(false);
                AuthUI.getInstance().signOut(UserActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            SharedPref.getInstance(UserActivity.this).removeUser();
                            startActivity(new Intent(UserActivity.this, MainActivity.class));
                            finish();
                        } else {
                            signOut.setEnabled(true);
                            signOut.setText("Sign Out");
                        }
                    }
                });
            }
        });*/
    }


    private String getGreetings() {
        Calendar calendar = Calendar.getInstance();
        int time = calendar.get(Calendar.HOUR_OF_DAY);
        if (time < 12)
            return "Good morning, ";
        else if (time >= 12 && time < 16)
            return "Good afternoon, ";
        else if (time >= 16 && time < 24)
            return "Good evening, ";
        else
            return "Hi, ";
    }

}
