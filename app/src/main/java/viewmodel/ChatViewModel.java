package viewmodel;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.Query;
import model.ChatMessage;
import repository.ChatRepository;

public class ChatViewModel extends AndroidViewModel {
    private final ChatRepository repository;
    private final MutableLiveData<String> uploadStatus = new MutableLiveData<>();

    public ChatViewModel(@NonNull Application application) {
        super(application);
        // Iniettiamo il ContentResolver nel Repository in modo pulito
        this.repository = new ChatRepository(application.getContentResolver());
    }

    public LiveData<String> getUploadStatus() { return uploadStatus; }

    public void sendMessage(String groupId, String senderId, String senderName, String text) {
        if (text == null || text.trim().isEmpty()) return;
        ChatMessage message = new ChatMessage(senderId, senderName, text.trim());
        repository.sendMessage(groupId, message);
    }

    public void sendImage(String groupId, String senderId, String senderName, Uri imageUri) {
        uploadStatus.setValue("UPLOADING");
        repository.uploadImage(imageUri, url -> {
            ChatMessage message = new ChatMessage(senderId, senderName, url, true);
            repository.sendMessage(groupId, message);
            uploadStatus.setValue("SUCCESS");
        }, e -> uploadStatus.setValue("ERROR: " + e));
    }

    public Query getMessagesQuery(String groupId) {
        return repository.getMessagesQuery(groupId);
    }
}
