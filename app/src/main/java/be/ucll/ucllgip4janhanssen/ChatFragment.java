package be.ucll.ucllgip4janhanssen;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private FirebaseFirestore db;
    private EditText editTextMessage;
    private Button buttonSend;
    private String roomId;
    private String currentUserPhoneNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            currentUserPhoneNumber = bundle.getString("currentUserPhoneNumber");
        }

        recyclerView = rootView.findViewById(R.id.recycler_view_chat);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MessageAdapter(new ArrayList<>(), currentUserPhoneNumber);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        roomId = getArguments().getString("roomId");

        editTextMessage = rootView.findViewById(R.id.edit_text_message);
        buttonSend = rootView.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Dit stuk gaat de naam van de chatpartner ophalen uit de bundel, meegestuurd door de vorige fragment, en dan weergeven in de toolbar
        Bundle bundle = getArguments();
        if (bundle != null) {
            currentUserPhoneNumber = bundle.getString("currentUserPhoneNumber");
            String firstName = bundle.getString("firstName");
            String lastName = bundle.getString("lastName");
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null && activity.getSupportActionBar() != null) {
                if (firstName != null && lastName != null) {
                    String fullName = firstName + " " + lastName;
                    activity.getSupportActionBar().setTitle(fullName);
                }
            }
        }

        // Dit stuk gaat een pijltje terug op de toolbar zetten
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        Log.d("ChatFragment", "Room ID: " + roomId);

        // Dit gaat alle berichten van deze chatroom ophalen en rangschikken volgens tijdstip
        db.collection("rooms").document(roomId)
                .collection("messages")
                .orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("ChatFragment", "Error fetching messages", e);
                        return;
                    }
                    if (queryDocumentSnapshots != null) {

                        List<Message> messages = new ArrayList<>(); // De berichten worden in een lijst gezet
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            // Elk document wordt omgezet naar een 'message' document en in de lijst gezet
                            Message message = document.toObject(Message.class);
                            if (message != null) {
                                messages.add(message);
                            }
                        }
                        adapter.setMessages(messages);
                    }
                });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Wanneer er op de terugknop van de toolbar wordt geklikt
        if (item.getItemId() == android.R.id.home) {
            Navigation.findNavController(requireView()).navigate(R.id.action_chat_to_contacts);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            if (currentUserPhoneNumber != null) {
                String senderId = currentUserPhoneNumber;
                String time = getCurrentTimestamp();

                // Nieuw message object aanmaken
                Message message = new Message(null, messageText, senderId, time, roomId);

                // Opslaan in de firestore
                db.collection("rooms").document(roomId)
                        .collection("messages")
                        .add(message)
                        .addOnSuccessListener(documentReference -> {
                            Log.d("SendMessage", "Message sent successfully");
                            // Tekstveld beneden leegmaken
                            editTextMessage.getText().clear();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("SendMessage", "Error sending message", e);
                            // Handle the error
                        });
            }
        }
    }
    private String getCurrentTimestamp() {
        // Get current timestamp
        return DateFormat.getDateTimeInstance().format(new Date());
    }
}