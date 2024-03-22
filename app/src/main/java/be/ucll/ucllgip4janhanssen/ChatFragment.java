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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

// Scherm waarin de chat wordt weergegeven
public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private FirebaseFirestore db;
    private String roomId;
    private LinearLayoutManager layoutManager;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            roomId = bundle.getString("roomId");
        }

        recyclerView = rootView.findViewById(R.id.recycler_view_chat);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MessageAdapter(new ArrayList<>(), bundle.getString("currentUserPhoneNumber"));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        recyclerView.post(() -> recyclerView.scrollToPosition(adapter.getItemCount() - 1));

        Button buttonUpload = rootView.findViewById(R.id.button_upload);
        buttonUpload.setOnClickListener(v -> openFileChooser());

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadMessages();
    }

    // Berichten ophalen uit de database, gesorteerd volgens tijdstip
    private void loadMessages() {
        db.collection("rooms").document(roomId)
                .collection("messages")
                .orderBy("time", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Message> messages = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Message message = document.toObject(Message.class);
                        if (message != null) {
                            messages.add(message);
                        }
                    }
                    adapter.setMessages(messages);
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatFragment", "Error fetching messages", e);
                });
    }

    // Bericht versturen
    private void sendMessage(String messageText, String senderId, String imageUrl) {
        if (senderId != null) {
            String time = getCurrentTimestamp();
            Message message = new Message(null, messageText, senderId, time, roomId, imageUrl);

            db.collection("rooms").document(roomId)
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("SendMessage", "Message sent successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SendMessage", "Error sending message", e);
                    });
        }
    }

    // Tijdstip ophalen
    private String getCurrentTimestamp() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }

    // Menu voor de foto te kiezen openen
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Foto opslaan in firebase en weergeven in de chat
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            String imageId = UUID.randomUUID().toString();

            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + imageId);

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            String phoneNumber = getCurrentUserPhoneNumber();
                            if (phoneNumber != null) {
                                Message newMessage = new Message(null, "", phoneNumber, getCurrentTimestamp(), roomId, imageUrl);
                                adapter.addMessage(newMessage);
                                adapter.notifyItemInserted(adapter.getItemCount() - 1);

                                if (adapter.getItemCount() > 0) {
                                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                                }

                                sendMessage("", phoneNumber, imageUrl); // Passing imageUrl to sendMessage
                                Toast.makeText(getContext(), "Image Uploaded: " + imageUrl, Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("UploadImage", "Unable to get current user's phone number");
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UploadImage", "Error uploading image", e);
                    });
        }
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
}