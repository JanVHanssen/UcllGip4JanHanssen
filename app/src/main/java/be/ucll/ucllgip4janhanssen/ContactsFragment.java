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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Fragment voor het weergeven van de contacten opgehaald uit de telefoon
public class ContactsFragment extends Fragment implements OnContactClickListener {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private RecyclerView recyclerView;
    private ContactsAdapter adapter;
    private FirebaseFirestore db;
    private List<User> registeredUsers;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        recyclerView = rootView.findViewById(R.id.recycler_view_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ContactsAdapter(this);
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
        loadRegisteredUsers();
        requestContactsPermission();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Contacts");
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        requestContactsPermission();
    }

    private void requestContactsPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            Log.e("ContactsPermission", "No permission to read contacts");
        }
    }

    private void loadContacts() {
        List<Contact> contacts = fetchContactsFromDevice();  // Contacts from the device

        // Get the phone numbers of registered users
        List<String> registeredPhoneNumbers = registeredUsers.stream()
                .map(User::getPhoneNumber)
                .map(this::standardizePhoneNumber)
                .collect(Collectors.toList());

        // Filter contacts that are both in the phone's contact list and registered users
        List<Contact> registeredContacts = contacts.stream()
                .filter(contact -> registeredPhoneNumbers.contains(standardizePhoneNumber(contact.getPhoneNumber())))
                .collect(Collectors.toList());

        // Get the current user's document reference
        String currentUserPhoneNumber = getCurrentUserPhoneNumber();
        String standardizedCurrentUserPhoneNumber = standardizePhoneNumber(currentUserPhoneNumber);
        CollectionReference userContactsRef = db.collection("users").document(standardizedCurrentUserPhoneNumber).collection("contacts");

        // Check and add new contacts to the database
        for (Contact contact : registeredContacts) {
            String contactPhoneNumber = standardizePhoneNumber(contact.getPhoneNumber());
            userContactsRef.document(contactPhoneNumber).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Contact already exists in the database, no need to add
                        Log.d("ContactExists", "Contact already exists: " + contactPhoneNumber);
                    } else {
                        // Contact doesn't exist in the database, add it
                        userContactsRef.document(contactPhoneNumber).set(contact)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Contact added for user: " + currentUserPhoneNumber))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error adding contact for user: " + currentUserPhoneNumber, e));
                    }
                } else {
                    Log.e("Firestore", "Error checking contact existence for user: " + currentUserPhoneNumber, task.getException());
                }
            });
        }
        Query query = userContactsRef.whereEqualTo("checked", true);

        // Fetch the contacts from the database based on the query
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Contact> filteredContacts = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    Contact contact = document.toObject(Contact.class);
                    filteredContacts.add(contact);
                }

                // Set the filtered contacts in the RecyclerView
                adapter.setContacts(filteredContacts);
            } else {
                Log.e("Firestore", "Error getting contacts", task.getException());
            }
        });
    }
    // Methode om de contacten uit de telefoon op te halen
    private List<Contact> fetchContactsFromDevice() {
        List<Contact> contacts = new ArrayList<>();
        Cursor cursor = getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

        if (cursor != null) {
            try {
                int idColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                int nameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                int hasPhoneNumberColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

                while (cursor.moveToNext()) {
                    String idString = cursor.getString(idColumnIndex);
                    long id = Long.parseLong(idString);
                    String name = cursor.getString(nameColumnIndex);
                    int hasPhoneNumber = cursor.getInt(hasPhoneNumberColumnIndex);

                    if (hasPhoneNumber > 0) {
                        Cursor phoneCursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{idString}, null);

                        if (phoneCursor != null) {
                            int phoneNumberColumnIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                            while (phoneCursor.moveToNext()) {
                                String phoneNumber = phoneCursor.getString(phoneNumberColumnIndex);
                                Contact contact = new Contact(id, name, "", phoneNumber, false, true);
                                contacts.add(contact);
                            }

                            phoneCursor.close();
                        }
                    }
                }
            } finally {
                cursor.close();
            }
        } else {
            Log.e("Cursor", "Cursor is null");
        }

        Log.d("PhoneContacts", "Users in phone contacts:");
        for (Contact contact : contacts) {
            Log.d("PhoneContacts", contact.getFirstName() + " " + contact.getLastName() + ", Phone: " + contact.getPhoneNumber());
        }

        return contacts;
    }

    // Methode om de users die de app gebruiken op te halen uit de database
    private void loadRegisteredUsers() {
        registeredUsers = new ArrayList<>();
        db.collection("users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            User user = documentSnapshot.toObject(User.class);
                            registeredUsers.add(user);
                        }
                        Log.d("RegisteredUsers", "Users in database:");
                        for (User user : registeredUsers) {
                            Log.d("RegisteredUsers", user.getFirstName() + " " + user.getLastName() + ", Phone: " + user.getPhoneNumber());
                        }
                        loadContacts();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ContactsFragment", "Error fetching registered users", e);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToChatFragment(String roomId, Contact user) {
        Bundle bundle = new Bundle();
        bundle.putString("firstName", user.getFirstName());
        bundle.putString("lastName", user.getLastName());
        bundle.putString("roomId", roomId);
        bundle.putString("currentUserPhoneNumber", getCurrentUserPhoneNumber()); // Add currentUserPhoneNumber to the bundle
        Navigation.findNavController(requireView()).navigate(R.id.action_contacts_to_chat, bundle);
    }

    @Override
    public void onContactClick(Contact user) {
        Log.d("Contact Clicked", "Name: " + user.getFirstName() + " " + user.getLastName() + " , Phone: " + user.getPhoneNumber());

        // Checken of het contact waar op geklikt wordt al een chatgesprek heeft
        String currentUserPhoneNumber = getCurrentUserPhoneNumber();
        String contactPhoneNumber = user.getPhoneNumber();

        if (currentUserPhoneNumber != null) {
            String standardizedCurrentUserPhoneNumber = standardizePhoneNumber(currentUserPhoneNumber);
            String standardizedContactPhoneNumber = standardizePhoneNumber(contactPhoneNumber);

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("rooms")
                    .whereArrayContains("users", standardizedCurrentUserPhoneNumber)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                boolean roomExists = false;
                                for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                    List<String> users = (List<String>) documentSnapshot.get("users");
                                    if (users != null && users.contains(standardizedContactPhoneNumber)) {
                                        // Als de room bestaat, navigeren we daar naartoe
                                        String roomId = documentSnapshot.getId();
                                        navigateToChatFragment(roomId, user);
                                        roomExists = true;
                                        break;
                                    }
                                }
                                if (!roomExists) {
                                    // Als de room niet bestaat, gaan we een nieuwe aanmaken
                                    createNewRoom(standardizedCurrentUserPhoneNumber, user);
                                }
                            } else {
                                createNewRoom(standardizedCurrentUserPhoneNumber, user);
                            }
                        } else {
                            Log.e("ContactsFragment", "Error checking room existence", task.getException());
                        }
                    });
        }
    }

    private void createNewRoom(String currentUserPhoneNumber, Contact contactUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> roomData = new HashMap<>();

        String standardizedCurrentUserPhoneNumber = standardizePhoneNumber(currentUserPhoneNumber);
        String standardizedContactPhoneNumber = standardizePhoneNumber(contactUser.getPhoneNumber());

        List<String> users = Arrays.asList(standardizedCurrentUserPhoneNumber, standardizedContactPhoneNumber);
        roomData.put("users", users);
        roomData.put("messages", Collections.emptyList());
        db.collection("rooms")
                .add(roomData)
                .addOnSuccessListener(documentReference -> {
                    String roomId = documentReference.getId();
                    Log.d("CreateNewRoom", "Room created with ID: " + roomId);
                    navigateToChatFragment(roomId, contactUser);
                })
                .addOnFailureListener(e -> {
                    Log.e("ContactsFragment", "Error creating new room", e);
                });
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

