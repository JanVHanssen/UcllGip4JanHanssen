package be.ucll.ucllgip4janhanssen;

// Contact klasse die een contact beschrijft met de nodige variabelen
public class Contact {
    private long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private boolean online;
    private boolean isChecked;

    // Constructor
    public Contact() {
    }
    public Contact(long id, String firstName, String lastName, String phoneNumber, boolean online, boolean isChecked) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.online = online;
        this.isChecked = isChecked;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}