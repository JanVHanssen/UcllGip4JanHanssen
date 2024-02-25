package be.ucll.ucllgip4janhanssen;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messages;
    private String currentUserPhoneNumber;

    // Constructor
    public MessageAdapter(List<Message> messages, String currentUserPhoneNumber) {
        this.messages = messages;
        this.currentUserPhoneNumber = currentUserPhoneNumber;
    }

    // ViewHolder for each message item
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_text_view);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) { // Sender message layout
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sender_message_item, parent, false);
        } else { // Receiver message layout
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receiver_message_item, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageTextView.setText(message.getText());

        // Adjust message alignment based on sender or receiver
        if (getItemViewType(position) == 0) {
            // Sender message: align to the right
            holder.messageTextView.setGravity(Gravity.END);
        } else {
            // Receiver message: align to the left
            holder.messageTextView.setGravity(Gravity.START);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // Override getItemViewType to distinguish sender and receiver messages
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSenderId().equals(currentUserPhoneNumber)) {
            return 0; // Sender message
        } else {
            return 1; // Receiver message
        }
    }

    // Method to add a single message
    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    // Method to set a list of messages
    public void setMessages(List<Message> messageList) {
        messages.clear();
        messages.addAll(messageList);
        notifyDataSetChanged();
    }
}