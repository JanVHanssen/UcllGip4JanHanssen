package be.ucll.ucllgip4janhanssen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// De adapter voor de chats in de groupchat schermen
public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.GroupMessageViewHolder> {

    private List<GroupMessage> groupMessages;
    private String currentUserPhoneNumber;

    // Constructor
    public GroupMessageAdapter(List<GroupMessage> groupMessages, String currentUserPhoneNumber) {
        this.groupMessages = groupMessages;
        this.currentUserPhoneNumber = currentUserPhoneNumber;
    }

    // ViewHolder for each group message item
    public static class GroupMessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderNameTextView;
        TextView messageTextView;

        public GroupMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderNameTextView = itemView.findViewById(R.id.receiver_name_text_view);
            messageTextView = itemView.findViewById(R.id.message_text_view);
        }
    }

    @NonNull
    @Override
    public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) { // Sender message layout
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sender_message_item, parent, false);
        } else { // Receiver message layout
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receiver_groupmessage_item, parent, false);
        }
        return new GroupMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMessageViewHolder holder, int position) {
        GroupMessage groupMessage = groupMessages.get(position);

        holder.messageTextView.setText(groupMessage.getText());

        // Adjust message alignment based on sender or receiver
        if (getItemViewType(position) == 0) {
            holder.messageTextView.setGravity(Gravity.END);
        } else {
            holder.senderNameTextView.setText(groupMessage.getSenderFirstName() + " " + groupMessage.getSenderLastName());
            holder.messageTextView.setGravity(Gravity.START);
        }
    }

    @Override
    public int getItemCount() {
        return groupMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        GroupMessage groupMessage = groupMessages.get(position);
        if (groupMessage != null && groupMessage.getSenderId() != null && groupMessage.getSenderId().equals(currentUserPhoneNumber)) {
            return 0; // Sender message
        } else {
            return 1; // Receiver message
        }
    }

    // Method to add a single group message
    public void addGroupMessage(GroupMessage groupMessage) {
        groupMessages.add(groupMessage);
        notifyItemInserted(groupMessages.size() - 1);
    }

    // Method to set a list of group messages
    public void setGroupMessages(List<GroupMessage> groupMessageList) {
        groupMessages.clear();
        groupMessages.addAll(groupMessageList);
        notifyDataSetChanged();
    }
}
