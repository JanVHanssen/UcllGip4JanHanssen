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
    private String roomId;
    private LinearLayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1); // Scroll to bottom after updating messages
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatFragment", "Error fetching messages", e);
                });
    }

    private void sendMessage(String messageText, String senderId) {
        if (!messageText.isEmpty() && senderId != null) {
            String time = getCurrentTimestamp();
            Message message = new Message(null, messageText, senderId, time, roomId);

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

    private String getCurrentTimestamp() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }
}
