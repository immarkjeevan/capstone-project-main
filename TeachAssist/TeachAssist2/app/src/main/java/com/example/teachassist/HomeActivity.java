package com.example.teachassist;


import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;


public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mdb;
    BottomNavigationView btmNavView;
    HomeFragment homeFragment = new HomeFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    SettingsFragment settingsFragment = new SettingsFragment();
    ChatActivity chatActivity = new ChatActivity();
    APIrequests apIrequests = new APIrequests(this,chatActivity);



    DatabaseReference postRef;
    public void onStart() {
        super.onStart();
        mdb = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getUid().toString();
        postRef = FirebaseDatabase.getInstance().getReference().child("TeachAssist").child("UserData");
        postRef.orderByChild("UID").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Toast.makeText(HomeActivity.this, "Welcome to TeachAssist", Toast.LENGTH_SHORT).show();
                    apIrequests.getThreadIdByUid(mAuth.getCurrentUser().getUid().toString(), new APIrequests.ThreadIdCallback() {
                        @Override
                        public void onThreadIdRetrieved(String threadId) {
                            Toast.makeText(HomeActivity.this, threadId,Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(String errorMessage) {

                        }
                    });

                } else {
                    Intent intent = new Intent(HomeActivity.this, UserDetailsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btmNavView = findViewById(R.id.bottomNavigationView);
        btmNavView.setOnNavigationItemSelectedListener(this);
        btmNavView.setSelectedItemId(R.id.home);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView,homeFragment).commit();

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.home) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, homeFragment)
                    .commit();
            return true;
        } else if (itemId == R.id.settings) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, settingsFragment)
                    .commit();
            return true;
        } else if (itemId == R.id.profile) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, profileFragment)
                    .commit();
            return true;
        }
        return false;
    }
}

