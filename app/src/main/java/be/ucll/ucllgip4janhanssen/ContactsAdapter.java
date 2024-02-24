package be.ucll.ucllgip4janhanssen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// De adapter gaat ervoor zorgen dat de data mooi wordt weergegeven in een recyclerview, lijst
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private List<Contact> contacts = new ArrayList<>();
    private OnContactClickListener onContactClickListener;

    public ContactsAdapter(OnContactClickListener listener) {
        this.onContactClickListener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_item, parent, false);
        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.bind(contact);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        private TextView firstNameTextView;
        private TextView lastNameTextView;
        private View onlineIndicatorView;

        public ContactViewHolder(View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.contactFirstName);
            lastNameTextView = itemView.findViewById(R.id.contactLastName);
            onlineIndicatorView = itemView.findViewById(R.id.onlineIndicator);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onContactClickListener != null) {
                    onContactClickListener.onContactClick(contacts.get(position));
                }
            });
        }

        public void bind(Contact contact) {
            firstNameTextView.setText(contact.getFirstName());
            lastNameTextView.setText(contact.getLastName());
            if (contact.isOnline()) {
                onlineIndicatorView.setBackgroundResource(R.drawable.green_dot);
            } else {
                onlineIndicatorView.setBackgroundResource(R.drawable.red_dot);
            }
        }
    }
}