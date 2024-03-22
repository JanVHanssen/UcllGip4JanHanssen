package be.ucll.ucllgip4janhanssen;


// Model dat een bericht in de groepchat beschrijft
public class GroupMessage {
    private String id;
    private String text;
    private String senderId;
    private String senderFirstName;
    private String senderLastName;
    private String time;
    private String groupId;

    public GroupMessage() {
    }

    public GroupMessage(String id, String text, String senderId, String senderFirstName, String senderLastName, String time, String groupId) {
        this.id = id;
        this.text = text;
        this.senderId = senderId;
        this.senderFirstName = senderFirstName;
        this.senderLastName = senderLastName;
        this.time = time;
        this.groupId = groupId;
    }

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

    public String getSenderFirstName() {
        return senderFirstName;
    }

    public void setSenderFirstName(String senderFirstName) {
        this.senderFirstName = senderFirstName;
    }

    public String getSenderLastName() {
        return senderLastName;
    }

    public void setSenderLastName(String senderLastName) {
        this.senderLastName = senderLastName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
