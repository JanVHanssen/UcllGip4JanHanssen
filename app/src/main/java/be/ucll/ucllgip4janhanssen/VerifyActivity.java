package be.ucll.ucllgip4janhanssen;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

// Het scherm voor de verificatie code in te geven
public class VerifyActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private String verificationId;
    private TextView messageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        firebaseAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        if (intent != null) {
            verificationId = intent.getStringExtra("verificationId");
        }

        EditText verifyCode = findViewById(R.id.verify);
        Button verifyButton = findViewById(R.id.verifyButton);
        messageTextView = findViewById(R.id.message);

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = verifyCode.getText().toString().trim();
                if (!code.isEmpty()) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                    login(credential);
                } else {
                    messageTextView.setText("Please enter the verification code.");
                }
            }
        });
    }

    // Methode om de login te doen met firebase authenticatie
    private void login(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            updateUserOnlineStatus(true);
                            startActivity(i);
                        } else {
                            messageTextView.setText("Login failed.");
                        }
                    }
                });
    }

    // Methode om de gebruiker zijn status op online te zetten
    private void updateUserOnlineStatus(boolean online) {
        String phoneNumber = getCurrentUserPhoneNumber();
        Log.d("VerifyActivity", "Current user phone number: " + phoneNumber);
        if (phoneNumber != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference usersRef = db.collection("users");
            Query query = usersRef.whereEqualTo("phoneNumber", phoneNumber);
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String userId = document.getId();
                        Log.d("VerifyActivity", "User ID: " + userId);
                        DocumentReference userRef = db.collection("users").document(userId);
                        userRef
                                .update("online", true)
                                .addOnSuccessListener(aVoid -> Log.d("VerifyActivity", "User online status updated successfully"))
                                .addOnFailureListener(e -> Log.e("VerifyActivity", "Error updating online status", e));
                    }
                } else {
                    Log.e("VerifyActivity", "Error getting user document", task.getException());
                }
            });
        }
    }

    // Ophalen van het telefoonnummer van de huidige gebruiker
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