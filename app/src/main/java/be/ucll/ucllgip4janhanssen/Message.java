package be.ucll.ucllgip4janhanssen;

// Model om een bericht te beschrijven
public class Message {
    private String id;
    private String text;
    private String senderId;
    private String time;
    private String roomId;
    private String imageUrl;

    public Message() {
    }

    // Constructor

    public Message(String id, String text, String senderId, String time, String roomId, String imageUrl) {
        this.id = id;
        this.text = text;
        this.senderId = senderId;
        this.time = time;
        this.roomId = roomId;
        this.imageUrl = imageUrl;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
