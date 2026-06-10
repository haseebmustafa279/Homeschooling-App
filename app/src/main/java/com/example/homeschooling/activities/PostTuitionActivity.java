package com.example.homeschooling.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeschooling.R;
import com.example.homeschooling.models.TuitionRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PostTuitionActivity extends AppCompatActivity {

    TextInputEditText etSubject, etClass, etFee;
    AutoCompleteTextView autoTiming;
    MaterialButton btnPost;

    DatabaseReference requestRef;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_tuition);

        etSubject = findViewById(R.id.etSubject);
        etClass = findViewById(R.id.etClass);
        autoTiming = findViewById(R.id.autoCompleteTiming);
        etFee = findViewById(R.id.etFee);
        btnPost = findViewById(R.id.btnPost);

        // Setup Timing Dropdown
        String[] items = {"Morning", "Evening"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        autoTiming.setAdapter(adapter);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        requestRef = FirebaseDatabase.getInstance().getReference("TuitionRequests");

        btnPost.setOnClickListener(v -> postTuition());
    }

    private void postTuition() {
        String subject = etSubject.getText().toString().trim();
        String classLevel = etClass.getText().toString().trim();
        String timing = autoTiming.getText().toString().trim();
        String fee = etFee.getText().toString().trim();

        if (TextUtils.isEmpty(subject) || TextUtils.isEmpty(classLevel)
                || TextUtils.isEmpty(timing) || TextUtils.isEmpty(fee)) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        String requestId = requestRef.push().getKey();
        TuitionRequest request = new TuitionRequest(
                requestId,
                userId,
                subject,
                classLevel,
                timing,
                fee,
                "Lahore", // later dynamic
                "open"
        );

        if (requestId != null) {
            requestRef.child(requestId).setValue(request)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Tuition Requirement Posted", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to post", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
