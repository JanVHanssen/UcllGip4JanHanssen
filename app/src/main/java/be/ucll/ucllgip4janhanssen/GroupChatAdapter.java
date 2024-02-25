package be.ucll.ucllgip4janhanssen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.ContactViewHolder> {

    private List<Contact> contacts;
    private OnCheckboxChangedListener onCheckboxChangedListener;

    public GroupChatAdapter(List<Contact> contacts) {
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.groupchat_item, parent, false);
        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.bind(contact);
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (onCheckboxChangedListener != null) {
                onCheckboxChangedListener.onCheckboxChanged(contact, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        private TextView firstNameTextView;
        private TextView lastNameTextView;
        private CheckBox checkBox;

        public ContactViewHolder(View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.contactFirstName);
            lastNameTextView = itemView.findViewById(R.id.contactLastName);
            checkBox = itemView.findViewById(R.id.checkBox);
        }

        public void bind(Contact contact) {
            firstNameTextView.setText(contact.getFirstName());
            lastNameTextView.setText(contact.getLastName());
            checkBox.setChecked(false);
        }
    }
}
