package be.ucll.ucllgip4janhanssen;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
// Fragment waar de groepchatgesprekken in plaatsvinden
public class GroupChatRoomFragment extends Fragment {

    private RecyclerView recyclerView;
    private GroupMessageAdapter adapter;
    private FirebaseFirestore db;
    private EditText editTextMessage;
    private Button buttonSend;
    private String groupName;
    private String currentUserPhoneNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_chat_room, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            groupName = bundle.getString("groupName");
            currentUserPhoneNumber = bundle.getString("currentUserPhoneNumber");
        }

        recyclerView = rootView.findViewById(R.id.recycler_view_chat);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GroupMessageAdapter(new ArrayList<>(), currentUserPhoneNumber);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        editTextMessage = rootView.findViewById(R.id.edit_text_message);
        buttonSend = rootView.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(v -> sendMessage());

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the toolbar
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(groupName);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Fetch and display group chat messages
        fetchGroupChatMessages();
    }

    private void fetchGroupChatMessages() {
        db.collection("groupchats").document(groupName).collection("messages")
                .orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("GroupChatRoomFragment", "Error fetching group chat messages", e);
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        List<GroupMessage> groupMessages = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            GroupMessage groupMessage = document.toObject(GroupMessage.class);
                            if (groupMessage != null) {
                                groupMessages.add(groupMessage);
                            }
                        }
                        adapter.setGroupMessages(groupMessages);
                    }
                });
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String time = getCurrentTimestamp();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                String userId = user.getPhoneNumber();

                // Retrieve user's first name and last name from Firestore
                db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String firstName = documentSnapshot.getString("firstName");
                                String lastName = documentSnapshot.getString("lastName");

                                GroupMessage groupMessage = new GroupMessage(
                                        null, // Assuming the ID will be generated by Firestore
                                        messageText,
                                        userId,
                                        firstName,
                                        lastName,
                                        time,
                                        groupName
                                );

                                // Add the message to Firestore
                                db.collection("groupchats").document(groupName)
                                        .collection("messages")
                                        .add(groupMessage)
                                        .addOnSuccessListener(documentReference -> {
                                            Log.d("GroupChatRoomFragment", "Message sent successfully");
                                            editTextMessage.getText().clear();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("GroupChatRoomFragment", "Error sending message", e);
                                            // Handle failure, e.g., show error message to the user
                                        });
                            } else {
                                Log.e("GroupChatRoomFragment", "User document does not exist for ID: " + userId);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("GroupChatRoomFragment", "Error retrieving user document", e);
                        });
            } else {
                Log.e("GroupChatRoomFragment", "Current user is null");
                // Handle the case where the current user is null
            }
        }
    }
    private String getCurrentTimestamp() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }
}