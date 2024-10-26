package com.example.teachassist;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.checkerframework.checker.units.qual.C;

import java.util.HashMap;

public class UserDetailsActivity extends AppCompatActivity {

    EditText editName, editGrade, editSubject;
    Button submitButton; // Renamed button
    FirebaseAuth mAuth;
    FirebaseDatabase mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        // Handle window insets for full screen compatibility
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mDB = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        editName = findViewById(R.id.editTextName);
        editGrade = findViewById(R.id.editTextGrade);
        editSubject = findViewById(R.id.editTextSubject);
        submitButton = findViewById(R.id.SubmitButton); // Updated

        // Handle button click to push data into Firebase
        submitButton.setOnClickListener(view -> {
            DatabaseReference myRef = mDB.getReference("TeachAssist").child("UserData");
            String username = editName.getText().toString();
            String grade = editGrade.getText().toString();
            String subject = editSubject.getText().toString();
            String uid = mAuth.getUid();


            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                    .build();
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User profile updated.");
                            }
                        }
                    });

            // Push Values into HashMap
            HashMap<String, Object> UserData = new HashMap<>();
            UserData.put("UID", uid);
            UserData.put("Username", username);
            UserData.put("Grade Level", grade);
            UserData.put("Subject", subject);

            // Push HashMap values into Firebase Realtime Database
            myRef.push().setValue(UserData);

            ChatActivity chatActivity = new ChatActivity();
            //Create Thread
            APIrequests apIrequests = new APIrequests(this,chatActivity);

            apIrequests.createThread(uid);

            // Start HomeActivity after pushing data
            startActivity(new Intent(UserDetailsActivity.this, HomeActivity.class));
            finish();
        });
    }
}
