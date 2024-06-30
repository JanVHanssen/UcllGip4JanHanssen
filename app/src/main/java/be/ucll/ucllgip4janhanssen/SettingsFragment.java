package be.ucll.ucllgip4janhanssen;

import android.os.Bundle;
import android.util.Log;
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

// Fragment waar de gebruiker een mail en paswoord kan toevoegen maar ook kan verwijderen
public class SettingsFragment extends Fragment {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button registerRemoveButton;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Settings");
        }

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        registerRemoveButton = view.findViewById(R.id.registerRemoveButton);

        if (currentUser != null) {
            if (currentUser.getEmail() == null || currentUser.getEmail().length() <= 3) {
                Log.d("SettingsFragment", "User phone number: " + currentUser.getPhoneNumber());
                Log.d("SettingsFragment", "User email: " + currentUser.getEmail());
                registerRemoveButton.setText("Remove");
                emailEditText.setText(currentUser.getEmail());
                passwordEditText.setHint("**********");
                passwordEditText.setEnabled(false);
            } else {
                Log.d("SettingsFragment", "User email is null");
                registerRemoveButton.setText("Register");
            }
        } else {
            Log.d("SettingsFragment", "Current user is null");
        }

        registerRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null && currentUser.getEmail() != null || !currentUser.getEmail().isEmpty()) {
                    removeEmailAndPassword();
                } else {
                    String email = emailEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();
                    registerEmailAndPassword(email, password);
                }
            }
        });

        return view;
    }

    // Toevoegen van een mail en paswoord aan de account
    private void registerEmailAndPassword(String email, String password) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        user.linkWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Email and password linked successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to link email and password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Methode voor het verwijderen van de mail en paswoord
    private void removeEmailAndPassword() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.unlink(EmailAuthProvider.PROVIDER_ID)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Email and password removed successfully", Toast.LENGTH_SHORT).show();

                                emailEditText.setText("");
                                passwordEditText.setText("");
                            } else {
                                Toast.makeText(getContext(), "Failed to remove email and password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(getContext(), "User is not signed in", Toast.LENGTH_SHORT).show();
        }
    }
}