package be.ucll.ucllgip4janhanssen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.widget.Toast;

// Het login scherm
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        // Check if someone is already logged in
        if (firebaseAuth.getCurrentUser() != null) {
            updateUserOnlineStatus(true);
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        Button login = findViewById(R.id.loginButton);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText phoneNumberEditText = findViewById(R.id.phoneNumber);
                String phoneNumber = phoneNumberEditText.getText().toString();

                if (!TextUtils.isEmpty(phoneNumber)) {
                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(LoginActivity.this)
                            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationFailed(@NonNull FirebaseException e) {
                                    Log.e("LoginActivity", "Verification failed: " + e.getMessage());
                                }

                                @Override
                                public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                    Log.d("LoginActivity", "Verification code sent successfully");
                                    Intent intent = new Intent(LoginActivity.this, VerifyActivity.class);
                                    intent.putExtra("verificationId", verificationId);
                                    intent.putExtra("token", token);
                                    startActivity(intent);
                                }

                                @Override
                                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                }
                            })
                            .build();

                    PhoneAuthProvider.verifyPhoneNumber(options);
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button register = findViewById(R.id.registerButton);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    // Methode voor het online zetten van de gebruiker die inlogt(online/offline status)
    private void updateUserOnlineStatus(boolean online) {
        String phoneNumber = getCurrentUserPhoneNumber();
        Log.d("LoginActivity", "Current user phone number: " + phoneNumber);
        if (phoneNumber != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference usersRef = db.collection("users");
            Query query = usersRef.whereEqualTo("phoneNumber", phoneNumber);
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String userId = document.getId();
                        Log.d("LoginActivity", "User ID: " + userId);
                        DocumentReference userRef = db.collection("users").document(userId);
                        userRef
                                .update("online", true)
                                .addOnSuccessListener(aVoid -> Log.d("LoginActivity", "User online status updated successfully"))
                                .addOnFailureListener(e -> Log.e("LoginActivity", "Error updating online status", e));
                    }
                } else {
                    Log.e("LoginActivity", "Error getting user document", task.getException());
                }
            });
        }
    }

    // Methode voor het telefoonnummer van de huidige gebruiker op te halen
    private String getCurrentUserPhoneNumber() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String phoneNumber = user.getPhoneNumber();
            Log.d("CurrentUserPhoneNumber", "Phone number: " + phoneNumber);
            return phoneNumber;
        } else {
            return null;
        }
    }
}