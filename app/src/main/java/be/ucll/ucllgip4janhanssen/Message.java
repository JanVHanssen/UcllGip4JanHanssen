package be.ucll.ucllgip4janhanssen;

public class Message {
    private String id;
    private String text;
    private String senderId;
    private String time;
    private String roomId;

    public Message() {
    }

    // Constructor
    public Message(String id, String text, String senderId, String time, String roomId) {
        this.id = id;
        this.text = text;
        this.senderId = senderId;
        this.time = time;
        this.roomId = roomId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
