package be.ucll.ucllgip4janhanssen;

import java.util.List;

// Model om een chatroom te beschrijven
public class Room {
    private String id;
    private List<String> users;
    private List<Message> messages;

    public Room() {
    }

    // Constructor
    public Room(String id, List<String> users, List<Message> messages) {
        this.id = id;
        this.users = users;
        this.messages = messages;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
