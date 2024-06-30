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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// Fragment voor het weergeven van de contacten opgehaald uit de telefoon
public class ContactsFragment extends Fragment implements OnContactClickListener, GroupsAdapter.OnGroupsClickListener {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private RecyclerView recyclerViewContacts;
    private RecyclerView recyclerViewGroups;
    private ContactsAdapter contactsAdapter;
    private GroupsAdapter groupsAdapter;
    private FirebaseFirestore db;
    private List<User> registeredUsers;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        recyclerViewContacts = rootView.findViewById(R.id.recycler_view_contacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        contactsAdapter = new ContactsAdapter(this);
        recyclerViewContacts.setAdapter(contactsAdapter);

        recyclerViewGroups = rootView.findViewById(R.id.recycler_view_groups);
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        groupsAdapter = new GroupsAdapter(this);
        recyclerViewGroups.setAdapter(groupsAdapter);

        db = FirebaseFirestore.getInstance();
        loadRegisteredUsers();
        requestContactsPermission();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Contacts");
        }

        return rootView;
    }

    // Methode voor wanneer er op de naam van een groepchat wordt geklikt
    @Override
    public void onGroupsClick(String groupName) {
        Bundle bundle = new Bundle();
        bundle.putString("groupName", groupName);
        bundle.putString("currentUserPhoneNumber", getCurrentUserPhoneNumber());
        Navigation.findNavController(requireView()).navigate(R.id.action_contacts_to_groupchatroom, bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        requestContactsPermission();
    }
    // Methode om toestemming te vragen de gegevens uit de telefoon op te halen
    private void requestContactsPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            Log.e("ContactsPermission", "No permission to read contacts");
        }
    }

    // Methode om de contacten op te halen die in de telefoon zitten en de app gebruiken
    private void loadContacts() {
        // Lijst met contacten in de telefoon
        List<Contact> contacts = fetchContactsFromDevice();

        // Lijst met gebruikers van de app
        List<String> registeredPhoneNumbers = registeredUsers.stream()
                .map(User::getPhoneNumber)
                .map(this::standardizePhoneNumber)
                .collect(Collectors.toList());

        // Lijst van gebruikers die ook in de telefoon staan
        List<Contact> registeredContacts = contacts.stream()
                .filter(contact -> registeredPhoneNumbers.contains(standardizePhoneNumber(contact.getPhoneNumber())))
                .collect(Collectors.toList());

        // Log die het resultaat van het filteren gaat tonen, welke gebruikers gebruiken de app en staan in het telefoon geheugen?
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Users in de telefoon die ook de app gebruiken: ");
        for (Contact contact : registeredContacts) {
            stringBuilder.append("\n")
                    .append(contact.getFirstName())
                    .append(" ")
                    .append(contact.getLastName())
                    .append(", Phone: ")
                    .append(contact.getPhoneNumber());
        }
        Log.d("FilteredContacts", stringBuilder.toString());

        String currentUserPhoneNumber = getCurrentUserPhoneNumber();
        String standardizedCurrentUserPhoneNumber = standardizePhoneNumber(currentUserPhoneNumber);
        CollectionReference userContactsRef = db.collection("users").document(standardizedCurrentUserPhoneNumber).collection("contacts");

        // Toevoegen in de database, in de collectie contacten bij de ingelogde user
        for (Contact contact : registeredContacts) {
            String contactPhoneNumber = standardizePhoneNumber(contact.getPhoneNumber());
            if (registeredPhoneNumbers.contains(contactPhoneNumber)) {
                userContactsRef.document(contactPhoneNumber).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("ContactExists", "Contact already exists: " + contactPhoneNumber);
                        } else {
                            userContactsRef.document(contactPhoneNumber).set(contact)
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Contact added for user: " + currentUserPhoneNumber))
                                    .addOnFailureListener(e -> Log.e("Firestore", "Error adding contact for user: " + currentUserPhoneNumber, e));
                        }
                    } else {
                        Log.e("Firestore", "Error checking contact existence for user: " + currentUserPhoneNumber, task.getException());
                    }
                });
            }
        }

        // Weergave op het scherm
        userContactsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Contact> allContacts = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    Contact contact = document.toObject(Contact.class);
                    if (contact.isChecked()) {
                        fetchOnlineStatus(contact, () -> {
                            allContacts.add(contact);
                            contactsAdapter.setContacts(allContacts);
                        });
                    }
                }
            } else {
                Log.e("Firestore", "Error getting contacts", task.getException());
            }
        });
    }

    // Methode om te gaan kijken of de gebruiker online/offline is
    private void fetchOnlineStatus(Contact contact, Runnable callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String contactPhoneNumber = standardizePhoneNumber(contact.getPhoneNumber());
        db.collection("users").document(contactPhoneNumber).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    boolean isOnline = document.getBoolean("online");
                    contact.setOnline(isOnline);
                    callback.run();
                } else {
                    Log.d("Firestore", "Contact document does not exist for phone number: " + contactPhoneNumber);
                }
            } else {
                Log.e("Firestore", "Error fetching online status for contact: " + contactPhoneNumber, task.getException());
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

        Log.d("PhoneContacts", "Contacten in telefoon geheugen:");
        for (Contact contact : contacts) {
            Log.d("PhoneContacts", contact.getFirstName() + " " + contact.getLastName() + ", Phone: " + contact.getPhoneNumber());
        }

        return contacts;
    }

    // Methode om de users die de app gebruiken op te halen uit de database
    private void loadRegisteredUsers() {
        registeredUsers = new ArrayList<>();
        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        User user = documentSnapshot.toObject(User.class);
                        registeredUsers.add(user);

                        Log.d("ContactsFragment", "Gebruikers van de app: " + user.getFirstName() + " " + user.getLastName() + ", Phone: " + user.getPhoneNumber());
                    }
                    loadContacts();
                    loadGroupNames();
                })
                .addOnFailureListener(e -> Log.e("ContactsFragment", "Error fetching registered users", e));
    }

    // Lijst met groepchats ophalen
    private void loadGroupNames() {
        String currentUserPhoneNumber = getCurrentUserPhoneNumber();
        if (currentUserPhoneNumber != null) {
            db.collection("groupchats").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<String> groupNames = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String groupName = documentSnapshot.getString("name");
                            if (groupName != null) {
                                String groupChatId = documentSnapshot.getId();
                                currentUserIsMemberOfGroupChat(currentUserPhoneNumber, groupChatId, isMember -> {
                                    if (isMember) {
                                        groupNames.add(groupName);
                                        groupsAdapter.setGroupNames(groupNames);
                                    }
                                });
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("ContactsFragment", "Error fetching group names", e));
        } else {
            Log.e("ContactsFragment", "Current user phone number is null");
        }
    }

    // Methode om na te gaan of de huidige gebruiker deel uitmaakt van een groepchat
    private void currentUserIsMemberOfGroupChat(String currentUserPhoneNumber, String groupChatId, OnCheckMemberListener listener) {
        db.collection("groupchats")
                .document(groupChatId)
                .collection("users")
                .document(currentUserPhoneNumber)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isMember = documentSnapshot.exists();
                    listener.onCheckMember(isMember);
                })
                .addOnFailureListener(e -> {
                    Log.e("ContactsFragment", "Error checking if current user is a member of group chat", e);
                    listener.onCheckMember(false); // Assume not a member on failure
                });
    }

    interface OnCheckMemberListener {
        void onCheckMember(boolean isMember);
    }

    // Toestemming vragen om de contacten uit de telefoon uit te lezen
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

    // Wanneer er op een contact geklikt wordt, wordt de gebruiker doorgestuurd naar de chatroom
    private void navigateToChatFragment(String roomId, Contact user) {
        Bundle bundle = new Bundle();
        bundle.putString("firstName", user.getFirstName());
        bundle.putString("lastName", user.getLastName());
        bundle.putString("roomId", roomId);
        bundle.putString("currentUserPhoneNumber", getCurrentUserPhoneNumber()); // Add currentUserPhoneNumber to the bundle
        Navigation.findNavController(requireView()).navigate(R.id.action_contacts_to_chat, bundle);
    }

    // Wanneer er op een contact geklikt wordt, gaat er gekeken worden of er al een room bestaat
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

    // Methode om een nieuwe chatroom aan te maken en op te slaan in firebase
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

    // Telefoonnummer van de huidige gebruiker ophalen
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

    // Methode om de spaties uit het telefoonnr te halen
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

