package model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Classe modello che rappresenta un messaggio della chat.
 * Gestisce sia messaggi testuali che immagini.
 */
public class ChatMessage {
    // Costanti per definire la tipologia di messaggio
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_IMAGE = "IMAGE";

    private String senderId;    // UID del mittente
    private String senderName;  // Nome visualizzato del mittente
    private String message;     // Testo del messaggio (se di tipo TEXT)
    private String imageUrl;    // URL dell'immagine caricata (se di tipo IMAGE)
    private String type;        // Tipologia di messaggio (TEXT o IMAGE)

    /**
     * @ServerTimestamp
     * Utilizza il timestamp del server Firestore per garantire coerenza temporale
     * tra tutti i dispositivi, evitando problemi con orologi locali sfasati.
     */
    @ServerTimestamp
    private Timestamp timestamp;

    /**
     * Costruttore vuoto obbligatorio per Firebase Firestore.
     * Necessario per la deserializzazione automatica dei documenti.
     */
    public ChatMessage() {}

    /**
     * Costruttore privato per forzare l'uso dei Factory Methods.
     * Garantisce l'integrità dei dati alla creazione dell'oggetto.
     */
    private ChatMessage(String senderId, String senderName, String type) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.type = type;
    }

    /**
     * Metodo statico per creare correttamente un messaggio di testo.
     */
    public static ChatMessage createTextMessage(String senderId, String senderName, String text) {
        ChatMessage msg = new ChatMessage(senderId, senderName, TYPE_TEXT);
        msg.setMessage(text);
        return msg;
    }

    /**
     * Metodo statico per creare correttamente un messaggio contenente un'immagine.
     */
    public static ChatMessage createImageMessage(String senderId, String senderName, String imageUrl) {
        ChatMessage msg = new ChatMessage(senderId, senderName, TYPE_IMAGE);
        msg.setImageUrl(imageUrl);
        return msg;
    }

    /* Getter e Setter necessari per l'accesso ai campi e per Firestore */

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
