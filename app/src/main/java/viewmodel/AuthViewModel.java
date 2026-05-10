package viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import repository.AuthRepository;

/**
 * ViewModel responsabile per la gestione dell'autenticazione e del profilo utente.
 * Mantiene lo stato dell'utente e gestisce le operazioni di login, registrazione e aggiornamento nickname.
 */
public class AuthViewModel extends ViewModel {
    private final AuthRepository repository;
    
    // Stato dell'autenticazione (es. LOGIN_SUCCESS, ERR_INVALID_CREDENTIALS)
    private final SingleLiveEvent<String> authStatus = new SingleLiveEvent<>();
    
    // Nickname dell'utente corrente, aggiornato in tempo reale tramite listener
    private final MutableLiveData<String> nickname = new MutableLiveData<>();
    
    // Riferimento al listener di Firestore per poterlo rimuovere correttamente
    private ListenerRegistration nicknameListener;

    /**
     * Costruttore predefinito.
     */
    public AuthViewModel() {
        this.repository = new AuthRepository();
    }

    /**
     * Costruttore per Dependency Injection (utilizzato nei test)
     */
    public AuthViewModel(AuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<String> getAuthStatus() { return authStatus; }
    public LiveData<String> getNickname() { return nickname; }

    /**
     * Esegue il login tramite email e password.
     */
    public void login(String email, String password) {
        repository.login(email, password, authStatus::setValue);
    }

    /**
     * Registra un nuovo utente e crea il relativo profilo nel database
     */
    public void register(String email, String password, String nickname) {
        repository.register(email, password, nickname, authStatus::setValue);
    }

    /**
     * Verifica se esiste una sessione attiva e se l'utente è ancora valido nel database
     */
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

    /**
     * Attiva un listener in tempo reale per monitorare i cambiamenti del nickname dell'utente.
     */
    public void fetchNickname(String uid) {
        if (nicknameListener != null) nicknameListener.remove();
        nicknameListener = repository.listenToUserNickname(uid, nickname::setValue);
    }

    /**
     * Aggiorna il nickname dell'utente sia nel profilo che in tutti i gruppi di cui è proprietario.
     */
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

    /**
     * Effettua il logout dell'utente.
     */
    public void logout() {
        repository.logout();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Rimuove il listener per evitare memory leak quando il ViewModel viene distrutto
        if (nicknameListener != null) nicknameListener.remove();
    }
}
