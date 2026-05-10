package model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class ChatMessage {
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_IMAGE = "IMAGE";

    private String senderId;
    private String senderName;
    private String message;
    private String imageUrl;
    private String type;
    @ServerTimestamp
    private Timestamp timestamp;

    public ChatMessage() {}

    // Costruttore per messaggi di testo
    public ChatMessage(String senderId, String senderName, String message) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.type = TYPE_TEXT;
    }

    // Costruttore per messaggi immagine
    public ChatMessage(String senderId, String senderName, String imageUrl, boolean isImage) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.imageUrl = imageUrl;
        this.type = TYPE_IMAGE;
    }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
