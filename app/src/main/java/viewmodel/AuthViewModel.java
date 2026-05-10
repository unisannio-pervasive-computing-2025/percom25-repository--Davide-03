package viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import repository.AuthRepository;

public class AuthViewModel extends ViewModel {
    private final AuthRepository repository = new AuthRepository();
    private final SingleLiveEvent<String> authStatus = new SingleLiveEvent<>();
    private final MutableLiveData<String> nickname = new MutableLiveData<>();
    private ListenerRegistration nicknameListener;

    public LiveData<String> getAuthStatus() { return authStatus; }
    public LiveData<String> getNickname() { return nickname; }

    public void login(String email, String password) {
        repository.login(email, password, authStatus::setValue);
    }

    public void register(String email, String password, String nickname) {
        repository.register(email, password, nickname, authStatus::setValue);
    }

    public void checkUserSession() {
        FirebaseUser user = repository.getCurrentUser();
        if (user != null) {
            repository.checkUserExists(user.getUid(), exists -> {
                if (exists) {
                    authStatus.setValue("LOGIN_SUCCESS");
                    fetchNickname(user.getUid());
                } else {
                    repository.logout();
                    authStatus.setValue("SESSION_EXPIRED");
                }
            });
        }
    }

    public void fetchNickname(String uid) {
        if (nicknameListener != null) nicknameListener.remove();
        nicknameListener = repository.listenToUserNickname(uid, nickname::setValue);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (nicknameListener != null) nicknameListener.remove();
    }

    public void updateNickname(String newNickname) {
        FirebaseUser user = repository.getCurrentUser();
        if (user != null && newNickname != null && !newNickname.trim().isEmpty()) {
            repository.updateNickname(user.getUid(), newNickname.trim(), status -> {
                authStatus.setValue(status);
                if ("SUCCESS".equals(status)) {
                    nickname.setValue(newNickname.trim());
                }
            });
        }
    }

    public FirebaseUser getCurrentUser() {
        return repository.getCurrentUser();
    }

    public void logout() {
        repository.logout();
    }
}
