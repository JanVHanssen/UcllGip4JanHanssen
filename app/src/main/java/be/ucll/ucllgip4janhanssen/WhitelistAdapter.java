package be.ucll.ucllgip4janhanssen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WhitelistAdapter extends RecyclerView.Adapter<WhitelistAdapter.ContactViewHolder> {

    private List<Contact> contacts;
    private OnCheckboxChangedListener onCheckboxChangedListener;

    public WhitelistAdapter(List<Contact> contacts, OnCheckboxChangedListener listener) {
        this.contacts = contacts;
        this.onCheckboxChangedListener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.whitelist_item, parent, false);
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

    class ContactViewHolder extends RecyclerView.ViewHolder {
        private TextView firstNameTextView;
        private TextView lastNameTextView;
        private CheckBox checkBox;

        public ContactViewHolder(View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.contactFirstName);
            lastNameTextView = itemView.findViewById(R.id.contactLastName);
            checkBox = itemView.findViewById(R.id.checkBox);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onCheckboxChangedListener != null) {
                    onCheckboxChangedListener.onCheckboxChanged(contacts.get(position), isChecked);
                }
            });
        }

        public void bind(Contact contact) {
            firstNameTextView.setText(contact.getFirstName());
            lastNameTextView.setText(contact.getLastName());
            checkBox.setChecked(contact.isChecked());
        }
    }
}