package be.ucll.ucllgip4janhanssen;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

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

    private void login(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                        } else {
                            messageTextView.setText("Login failed.");
                        }
                    }
                });
    }
}