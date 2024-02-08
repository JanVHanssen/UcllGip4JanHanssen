package be.ucll.ucllgip4janhanssen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
// Fragment voor het weergeven van de contacten opgehaald uit de telefoon
public class ContactsFragment extends Fragment implements OnContactClickListener{

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private RecyclerView recyclerView;
    private ContactsAdapter adapter;

// Methode die de weergave van de pagina gaat bepalen
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        recyclerView = rootView.findViewById(R.id.recycler_view_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ContactsAdapter(this);
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    // Methode die de request contacts permission methode gaat opstarten
    @Override
    public void onResume() {
        super.onResume();
        requestContactsPermission();
    }

    // Methode die permissie gaat aanvragen om de contacten in te lezen indien nodig
    private void requestContactsPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            loadContacts();
        }
    }

    // Methode om alle contacten op te halen uit het geheugen van de telefoon
    private void loadContacts() {
        List<Contact> contacts = new ArrayList<>();
        Cursor cursor = getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

        if (cursor != null) {
            int idColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            int nameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            int hasPhoneNumberColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

            if (idColumnIndex != -1 && nameColumnIndex != -1 && hasPhoneNumberColumnIndex != -1) {
                while (cursor.moveToNext()) {
                    String idString = cursor.getString(idColumnIndex);
                    long id = Long.parseLong(idString); // Convert String ID to long
                    String name = cursor.getString(nameColumnIndex);
                    int hasPhoneNumber = cursor.getInt(hasPhoneNumberColumnIndex);

                    if (hasPhoneNumber > 0) {
                        Cursor phoneCursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{idString}, null);

                        if (phoneCursor != null) {
                            int phoneNumberColumnIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                            if (phoneNumberColumnIndex != -1) {
                                while (phoneCursor.moveToNext()) {
                                    String phoneNumber = phoneCursor.getString(phoneNumberColumnIndex);
                                    Contact contact = new Contact(id, name, "", phoneNumber, false); // You might need to set online status appropriately
                                    contacts.add(contact);
                                }
                            } else {
                                Log.e("PhoneCursor", "Phone number column not found");
                            }

                            phoneCursor.close();
                        }
                    }
                }
            } else {
                Log.e("Cursor", "Required columns not found");
            }

            cursor.close();
        } else {
            Log.e("Cursor", "Cursor is null");
        }

        adapter.setContacts(contacts);
    }

    // Methode om te checken of de permissie voor de contacten te lezen in orde is
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Methode die bepaalt wat er gebeurt als je op een contact klikt
    @Override
    public void onContactClick(Contact contact) {
        Log.d("Contact Clicked", "Name: " + contact.getFirstName() + contact.getLastName() + " , Phone: " + contact.getPhoneNumber());
    }
    
}