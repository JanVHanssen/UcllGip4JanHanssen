package be.ucll.ucllgip4janhanssen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

// Hoofdscherm waarin de verschillende fragmenten worden geladen
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        setSupportActionBar(findViewById(R.id.toolbar));

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);
        navController = navHostFragment.getNavController();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // Instellen van het menu rechtsboven
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            navController.navigate(R.id.action_main_to_settings);
        }
        if (item.getItemId() == R.id.action_contacts) {
            navController.navigate(R.id.action_main_to_contacts);
        }
        if (item.getItemId() == R.id.action_whitelist) {
            navController.navigate(R.id.action_main_to_whitelist);
        }
        if (item.getItemId() == R.id.action_groups) {
            navController.navigate(R.id.action_main_to_groupchat);
        }
        if (item.getItemId() == R.id.action_logout) {
            updateUserOnlineStatus(false);

            firebaseAuth.signOut();
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
        return true;
    }

    private void updateUserOnlineStatus(boolean online) {
        String phoneNumber = getCurrentUserPhoneNumber();
        Log.d("MainActivity", "Current user phone number: " + phoneNumber);
        if (phoneNumber != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference usersRef = db.collection("users");
            Query query = usersRef.whereEqualTo("phoneNumber", phoneNumber);
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String userId = document.getId();
                        Log.d("MainActivity", "User ID: " + userId);
                        DocumentReference userRef = db.collection("users").document(userId);
                        userRef
                                .update("online", false)
                                .addOnSuccessListener(aVoid -> Log.d("MainActivity", "User online status updated successfully"))
                                .addOnFailureListener(e -> Log.e("MainActivity", "Error updating online status", e));
                    }
                } else {
                    Log.e("MainActivity", "Error getting user document", task.getException());
                }
            });
        }
    }
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