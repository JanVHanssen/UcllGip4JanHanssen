package be.ucll.ucllgip4janhanssen;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Fragment waarin de gebruiker andere gebruikers kan blokkeren
public class WhitelistFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private WhitelistAdapter adapter;
    private List<Contact> contactsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_whitelist, container, false);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Whitelist");
        }

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recycler_view_whitelist);
        contactsList = new ArrayList<>();
        adapter = new WhitelistAdapter(contactsList, new OnCheckboxChangedListener() {
            @Override
            public void onCheckboxChanged(Contact contact, boolean isChecked) {
                updateDatabase(contact, isChecked);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadContacts();

        return view;
    }

    // Lijst met gebruikers ophalen uit de database
    private void loadContacts() {
        String currentUserPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        db.collection("users").document(currentUserPhoneNumber).collection("contacts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Contact contact = documentSnapshot.toObject(Contact.class);
                        contactsList.add(contact);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                });
    }

    // Wijzigingen opslaan in de database, gebruikers die geblokkeerd zijn
    private void updateDatabase(Contact contact, boolean isChecked) {
        String currentUserPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        String standardizedCurrentUserPhoneNumber = standardizePhoneNumber(currentUserPhoneNumber);
        String contactPhoneNumber = contact.getPhoneNumber();
        String standardizedContactPhoneNumber = standardizePhoneNumber(contactPhoneNumber);

        db.collection("users").document(standardizedCurrentUserPhoneNumber)
                .collection("contacts").document(standardizedContactPhoneNumber)
                .update("checked", isChecked)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Checkbox state updated for contact: " + contactPhoneNumber))
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating checkbox state for contact: " + contactPhoneNumber, e));

    }

    // Telefoonnummer aanpassen, spaties eruit halen
    private String standardizePhoneNumber(String phoneNumber) {
        // Remove all non-numeric characters from the phone number
        String standardizedNumber = phoneNumber.replaceAll("[^0-9]", "");
        // Add country code if missing (assuming a country code of "+1" for example)
        if (!standardizedNumber.startsWith("+")) {
            standardizedNumber = "+" + standardizedNumber;
        }
        return standardizedNumber;
    }
}