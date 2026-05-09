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

public class ChatRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final ContentResolver contentResolver;

    public ChatRepository(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public void sendMessage(String groupId, ChatMessage message) {
        db.collection("groups")
                .document(groupId)
                .collection("messages")
                .add(message);
    }

    public void uploadImage(Uri imageUri, Consumer<String> onSuccess, Consumer<String> onFailure) {
        String fileName = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("chat_images/" + fileName);
        
        try {
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

    public Query getMessagesQuery(String groupId) {
        return db.collection("groups")
                .document(groupId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING);
    }
}
