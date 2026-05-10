package repository;

import android.content.ContentResolver;
import android.net.Uri;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import model.ChatMessage;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Repository che gestisce lo scambio di messaggi e il caricamento di immagini.
 * Implementa il pattern Singleton per centralizzare l'accesso alle risorse di chat.
 */
public class ChatRepository {
    private static ChatRepository instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final ContentResolver contentResolver;

    private ChatRepository(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    /**
     * Singleton Pattern: garantisce un unico punto di accesso per prevenire sprechi di memoria.
     */
    public static ChatRepository getInstance(ContentResolver contentResolver) {
        if (instance == null) {
            instance = new ChatRepository(contentResolver);
        }
        return instance;
    }

    /**
     * Aggiunge un nuovo messaggio alla collezione 'messages' di un gruppo specifico.
     */
    public void sendMessage(String groupId, ChatMessage message, Consumer<Boolean> callback) {
        db.collection("groups")
                .document(groupId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    if (callback != null) callback.accept(true);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.accept(false);
                });
    }

    /**
     * Carica un file immagine su Firebase Storage. 
     * In caso di successo, restituisce l'URL pubblico del file tramite callback.
     */
    public void uploadImage(Uri imageUri, Consumer<String> onSuccess, Consumer<String> onFailure) {
        String fileName = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("chat_images/" + fileName);
        
        try {
            // Utilizzo del ContentResolver per leggere dati da URI esterni (es. Galleria)
            InputStream stream = contentResolver.openInputStream(imageUri);
            if (stream == null) {
                onFailure.accept("Errore: impossibile leggere l'immagine");
                return;
            }
            ref.putStream(stream)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> onSuccess.accept(uri.toString())))
                    .addOnFailureListener(e -> onFailure.accept(e.getMessage()));
        } catch (FileNotFoundException e) {
            onFailure.accept("Errore: file non trovato");
        }
    }

    /**
     * Genera la query per recuperare i messaggi di un gruppo ordinati cronologicamente.
     */
    public Query getMessagesQuery(String groupId) {
        return db.collection("groups")
                .document(groupId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING);
    }
}
