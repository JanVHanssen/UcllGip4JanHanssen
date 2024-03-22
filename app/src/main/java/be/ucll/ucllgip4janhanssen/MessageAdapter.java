package be.ucll.ucllgip4janhanssen;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

// Adapter voor het weergeven van de berichten en foto's in de chat
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages;
    private String currentUserPhoneNumber;

    // Constructor
    public MessageAdapter(List<Message> messages, String currentUserPhoneNumber) {
        this.messages = messages;
        this.currentUserPhoneNumber = currentUserPhoneNumber;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView imageView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        if (viewType == 0) {
            view = inflater.inflate(R.layout.sender_message_item, parent, false);
        } else {
            view = inflater.inflate(R.layout.receiver_message_item, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
            holder.messageTextView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            // Load image using Glide or Picasso library
            Glide.with(holder.itemView.getContext())
                    .load(message.getImageUrl())
                    .into(holder.imageView);
        } else {
            holder.imageView.setVisibility(View.GONE);
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.messageTextView.setText(message.getText());
        }

        // Adjust message alignment based on sender or receiver
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.messageTextView.getLayoutParams();
        if (getItemViewType(position) == 0) {
            // Sender message: align to the right
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            params.removeRule(RelativeLayout.ALIGN_PARENT_START);
        } else {
            // Receiver message: align to the left
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            params.removeRule(RelativeLayout.ALIGN_PARENT_END);
        }
        holder.messageTextView.setLayoutParams(params);
    }
    @Override
    public int getItemCount() {
        return messages.size();
    }

    // Override getItemViewType to distinguish sender and receiver messages
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.getSenderId().equals(currentUserPhoneNumber) ? 0 : 1;
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