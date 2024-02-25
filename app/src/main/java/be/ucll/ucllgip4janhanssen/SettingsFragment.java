package be.ucll.ucllgip4janhanssen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends Fragment {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button registerRemoveButton;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Settings");
        }

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize Views
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        registerRemoveButton = view.findViewById(R.id.registerRemoveButton);

        // Check if the user has registered email and password
        if (currentUser != null && currentUser.getEmail() != null) {
            // User has registered email and password, set button text to "Remove"
            registerRemoveButton.setText("Remove");

            // Set email to EditText
            emailEditText.setText(currentUser.getEmail());
            // Set password to EditText (You can't get the password directly from Firebase)
            passwordEditText.setHint("**********"); // Show a hint instead of the actual password
            passwordEditText.setEnabled(false); // Disable editing of password field
        } else {
            // User has not registered email and password, set button text to "Register"
            registerRemoveButton.setText("Register");
        }

        // Set OnClickListener for register/remove button
        registerRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = registerRemoveButton.getText().toString();
                if (buttonText.equals("Register")) {
                    // User wants to register email and password
                    String email = emailEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();
                    registerEmailAndPassword(email, password);
                } else if (buttonText.equals("Remove")) {
                    // User wants to remove email and password
                    removeEmailAndPassword();
                }
            }
        });

        return view;
    }

    private void registerEmailAndPassword(String email, String password) {
        // Get the currently signed-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Link the email and password to the phone number-based account
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        user.linkWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Email and password successfully linked to the account
                            Toast.makeText(getContext(), "Email and password linked successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            // Failed to link email and password
                            Toast.makeText(getContext(), "Failed to link email and password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void removeEmailAndPassword() {
        // Get the currently signed-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Check if user is not null
        if (user != null) {
            // Unlink the email and password from the account
            user.unlink(EmailAuthProvider.PROVIDER_ID)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Email and password successfully removed from the account
                                Toast.makeText(getContext(), "Email and password removed successfully", Toast.LENGTH_SHORT).show();

                                // Clear email and password fields
                                emailEditText.setText("");
                                passwordEditText.setText("");
                            } else {
                                // Failed to remove email and password
                                Toast.makeText(getContext(), "Failed to remove email and password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            // User is null, handle accordingly (this should not happen if you properly handle authentication flow)
            Toast.makeText(getContext(), "User is not signed in", Toast.LENGTH_SHORT).show();
        }
    }
}