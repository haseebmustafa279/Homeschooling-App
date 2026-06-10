package com.example.homeschooling.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.homeschooling.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ParentDashboardActivity extends AppCompatActivity {

    private static final int MAP_PICKER_REQUEST = 105;
    MaterialCardView cardLogout, cardProfile, cardSearchTutor, cardPostTuition, cardActiveTuitions, cardEscrow, cardMessages, cardPickLocation;
    DatabaseReference userRef;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        cardPostTuition = findViewById(R.id.cardPostTuition);
        cardSearchTutor = findViewById(R.id.cardSearchTutor);
        cardPickLocation = findViewById(R.id.cardPickLocation);
        cardActiveTuitions = findViewById(R.id.cardActiveTuitions);
        cardEscrow = findViewById(R.id.cardEscrow);
        cardMessages = findViewById(R.id.cardMessages);
        cardProfile = findViewById(R.id.cardProfile);
        cardLogout = findViewById(R.id.cardLogout);

        cardPostTuition.setOnClickListener(v ->
                startActivity(new Intent(this, PostTuitionActivity.class)));

        cardSearchTutor.setOnClickListener(v ->
                startActivity(new Intent(this, SearchTutorActivity.class)));

        cardPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivityForResult(intent, MAP_PICKER_REQUEST);
        });

        cardActiveTuitions.setOnClickListener(v -> {
            startActivity(new Intent(this, ParentActiveTuitionsActivity.class));
        });

        cardEscrow.setOnClickListener(v -> {
            startActivity(new Intent(this, EscrowParentActivity.class));
        });

        cardMessages.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatListActivity.class));
        });

        cardProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ParentProfileActivity.class)));

        cardLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            double lat = data.getDoubleExtra("lat", 0);
            double lng = data.getDoubleExtra("lng", 0);
            
            userRef.child("latitude").setValue(lat);
            userRef.child("longitude").setValue(lng).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Home Location Updated Successfully!", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
