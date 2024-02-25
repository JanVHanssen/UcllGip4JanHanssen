package be.ucll.ucllgip4janhanssen;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupchatFragment extends Fragment {

    private EditText editTextGroupName;
    private Button buttonCreate;
    private RecyclerView recyclerViewContacts;
    private GroupChatAdapter groupChatAdapter;
    private List<Contact> contactsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groupchat, container, false);

        // Initialize views
        editTextGroupName = view.findViewById(R.id.edit_text_group_name);
        buttonCreate = view.findViewById(R.id.button_create);
        recyclerViewContacts = view.findViewById(R.id.recycler_view_contacts);

        // Set up RecyclerView
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getActivity()));
        contactsList = new ArrayList<>();
        groupChatAdapter = new GroupChatAdapter(contactsList);
        recyclerViewContacts.setAdapter(groupChatAdapter);

        // Fetch contacts from Firestore and populate the list
        fetchContactsFromFirestore();

        // Set click listener for the Create button
        buttonCreate.setOnClickListener(v -> {
            String groupName = editTextGroupName.getText().toString().trim();
            if (!groupName.isEmpty()) {
                // Save group name to Firestore
                saveGroupNameToFirestore(groupName);
            } else {
                // Show error message or toast indicating that group name is empty
                Toast.makeText(getActivity(), "Group name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Set title
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Groups");
        }

        return view;
    }

    private void fetchContactsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserPhoneNumber = getCurrentUserPhoneNumber();

        db.collection("users")
                .document(currentUserPhoneNumber)
                .collection("contacts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Contact contact = documentSnapshot.toObject(Contact.class);
                        contactsList.add(contact);
                    }
                    groupChatAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                });
    }

    private void saveGroupNameToFirestore(String groupName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a reference to the group chat document under the "groupchats" collection
        DocumentReference groupChatRef = db.collection("groupchats").document(groupName);

        // Create a Map object to hold the group data
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("name", groupName);

        // Set the data for the new group chat
        groupChatRef.set(groupData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Group chat created successfully");

                    // Now, for each checked contact, add a document to the "users" subcollection
                    for (Contact contact : contactsList) {
                        if (contact.isChecked()) {
                            addContactToGroupChat(db, groupName, contact);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating group chat", e);
                    // Handle errors
                });
    }

    private void addContactToGroupChat(FirebaseFirestore db, String groupName, Contact contact) {
        String contactPhoneNumber = contact.getPhoneNumber();

        // Create a new document in the "users" subcollection with the contact information
        db.collection("groupchats").document(groupName).collection("users").document(contactPhoneNumber)
                .set(contact)
                .addOnSuccessListener(aVoid -> {
                    // Show a toast message indicating successful addition of the contact to the group chat
                    Toast.makeText(getActivity(), "Contact added to group chat", Toast.LENGTH_SHORT).show();

                    navigateToContactsFragment();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error adding contact to group chat", e));
    }
    private void navigateToContactsFragment() {
        NavHostFragment.findNavController(this).navigate(R.id.action_groupchat_to_contacts);
    }

    private String getCurrentUserPhoneNumber() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.getPhoneNumber();
        } else {
            return null;
        }
    }
}