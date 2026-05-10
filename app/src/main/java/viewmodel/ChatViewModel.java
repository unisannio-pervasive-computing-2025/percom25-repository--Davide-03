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
    private final SingleLiveEvent<String> uploadStatus = new SingleLiveEvent<>();

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.repository = ChatRepository.getInstance(application.getContentResolver());
    }

    public LiveData<String> getUploadStatus() { return uploadStatus; }

    public void sendMessage(String groupId, String senderId, String senderName, String text) {
        if (text == null || text.trim().isEmpty()) return;
        ChatMessage message = new ChatMessage(senderId, senderName, text.trim());
        repository.sendMessage(groupId, message, success -> {
            if (!success) uploadStatus.setValue("ERROR_SEND_FAILED");
        });
    }

    public void sendImage(String groupId, String senderId, String senderName, Uri imageUri) {
        uploadStatus.setValue("UPLOADING");
        repository.uploadImage(imageUri, url -> {
            ChatMessage message = new ChatMessage(senderId, senderName, url, true);
            repository.sendMessage(groupId, message, success -> {
                if (success) {
                    uploadStatus.setValue("SUCCESS");
                } else {
                    uploadStatus.setValue("ERROR_SEND_FAILED");
                }
            });
        }, e -> uploadStatus.setValue("ERROR: " + e));
    }

    public Query getMessagesQuery(String groupId) {
        return repository.getMessagesQuery(groupId);
    }
}
