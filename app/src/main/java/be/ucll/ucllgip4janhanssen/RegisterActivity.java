package be.ucll.ucllgip4janhanssen;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText phoneNumberEditText, firstNameEditText, lastNameEditText;
    private CheckBox agreeCheckBox;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        phoneNumberEditText = findViewById(R.id.phonenumber);
        firstNameEditText = findViewById(R.id.firstname);
        lastNameEditText = findViewById(R.id.lastname);
        agreeCheckBox = findViewById(R.id.agree);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String phoneNumber = phoneNumberEditText.getText().toString().trim().replaceAll("\\s", ""); // Remove spaces from phone number
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        boolean agree = agreeCheckBox.isChecked();

        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!agree) {
            Toast.makeText(RegisterActivity.this, "Please agree to terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user object
        User user = new User();
        user.setId(phoneNumber);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);

        // Save user to Firebase Firestore
        db.collection("users").document(phoneNumber).set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Log.e(TAG, "Error registering user", task.getException());
                            Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}