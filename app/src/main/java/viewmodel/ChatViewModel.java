package viewmodel;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import model.ChatMessage;
import repository.ChatRepository;

/**
 * ViewModel che gestisce la logica della chat (invio messaggi e immagini).
 * Utilizza AndroidViewModel per avere accesso al ContentResolver tramite il contesto dell'applicazione.
 */
public class ChatViewModel extends AndroidViewModel {
    private final ChatRepository repository;
    
    // Stato dell'invio (es. UPLOADING, SUCCESS, ERROR)
    private final SingleLiveEvent<String> uploadStatus = new SingleLiveEvent<>();

    /**
     * Costruttore standard utilizzato dal ViewModelProvider.
     */
    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.repository = ChatRepository.getInstance(application.getContentResolver());
    }

    /**
     * Costruttore per Dependency Injection (utilizzato nei test).
     */
    public ChatViewModel(@NonNull Application application, ChatRepository repository) {
        super(application);
        this.repository = repository;
    }

    public LiveData<String> getUploadStatus() { return uploadStatus; }

    /**
     * Invia un messaggio di testo al gruppo specificato.
     */
    public void sendMessage(String groupId, String senderId, String senderName, String text) {
        if (text == null || text.trim().isEmpty()) return;
        
        ChatMessage message = ChatMessage.createTextMessage(senderId, senderName, text.trim());
        repository.sendMessage(groupId, message, success -> {
            if (!success) uploadStatus.setValue("ERROR_SEND_FAILED");
        });
    }

    /**
     * Carica un'immagine su Firebase Storage e poi invia il relativo messaggio nella chat.
     */
    public void sendImage(String groupId, String senderId, String senderName, Uri imageUri) {
        uploadStatus.setValue("UPLOADING");
        
        repository.uploadImage(imageUri, url -> {
            // Successo caricamento: creiamo il messaggio con l'URL dell'immagine
            ChatMessage message = ChatMessage.createImageMessage(senderId, senderName, url);
            repository.sendMessage(groupId, message, success -> {
                if (success) {
                    uploadStatus.setValue("SUCCESS");
                } else {
                    uploadStatus.setValue("ERROR_SEND_FAILED");
                }
            });
        }, e -> uploadStatus.setValue("ERROR: " + e));
    }

    /**
     * Restituisce la query per recuperare i messaggi del gruppo in tempo reale.
     */
    public com.google.firebase.firestore.Query getMessagesQuery(String groupId) {
        return repository.getMessagesQuery(groupId);
    }
}
