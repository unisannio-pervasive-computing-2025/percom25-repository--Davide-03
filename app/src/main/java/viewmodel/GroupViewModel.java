package viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.List;
import model.Group;
import model.User;
import repository.GroupRepository;

/**
 * ViewModel che gestisce la logica dei gruppi e dei partecipanti.
 */
public class GroupViewModel extends ViewModel {
    private final GroupRepository repository;
    
    // Stato di esito delle operazioni (es. "Membro aggiunto", "SUCCESS")
    private final SingleLiveEvent<String> actionStatus = new SingleLiveEvent<>();
    
    // Lista dettagliata dei membri del gruppo corrente (per visualizzare nickname ed email)
    private final MutableLiveData<List<User>> groupMembers = new MutableLiveData<>();
    
    // Dati del gruppo corrente monitorati in tempo reale
    private final MutableLiveData<Group> currentGroup = new MutableLiveData<>();
    
    private ListenerRegistration groupListener;
    private String currentGroupId;

    public GroupViewModel() {
        this.repository = new GroupRepository();
    }

    public GroupViewModel(GroupRepository repository) {
        this.repository = repository;
    }

    public LiveData<String> getActionStatus() { return actionStatus; }
    public LiveData<List<User>> getGroupMembers() { return groupMembers; }
    public LiveData<Group> getCurrentGroup() { return currentGroup; }

    /**
     * Crea un nuovo gruppo su Firestore.
     */
    public void createGroup(String name, String ownerId, String ownerNickname) {
        repository.createGroup(name, ownerId, ownerNickname, actionStatus::setValue);
    }

    /**
     * Restituisce la query per visualizzare i gruppi di cui l'utente fa parte.
     */
    public Query getGroupsQuery(String userId) {
        return repository.getGroupsForUserQuery(userId);
    }

    /**
     * Aggiunge un nuovo membro al gruppo tramite la sua email.
     */
    public void addMember(String groupId, String email) {
        if (email == null || email.trim().isEmpty()) {
            actionStatus.setValue("ERR_EMPTY_EMAIL");
            return;
        }
        repository.addMember(groupId, email.trim(), actionStatus::setValue);
    }

    /**
     * Attiva l'ascolto in tempo reale dei dettagli di un gruppo.
     * Se il gruppo cambia (es. utente bloccato), l'UI viene aggiornata immediatamente.
     */
    public void loadGroupDetails(String groupId) {
        // Evita di riattivare il listener se stiamo già ascoltando lo stesso gruppo
        if (groupId != null && groupId.equals(currentGroupId) && groupListener != null) {
            return;
        }

        if (groupListener != null) groupListener.remove();
        currentGroupId = groupId;
        
        groupListener = repository.listenToGroup(groupId, group -> {
            currentGroup.setValue(group);
            if (group != null && group.getMembers() != null) {
                // Recupera i dettagli (nickname, email) di tutti i membri per la lista partecipanti
                repository.getUsersDetails(group.getMembers(), groupMembers::setValue);
            }
        });
    }

    /**
     * Blocca o sblocca un utente. Un utente bloccato può leggere ma non scrivere nel gruppo.
     */
    public void blockUser(String groupId, String userId, boolean block) {
        repository.blockUser(groupId, userId, block, status -> {
            if ("SUCCESS".equals(status)) {
                actionStatus.setValue(block ? "Membro bloccato" : "Membro sbloccato");
            } else {
                actionStatus.setValue(status);
            }
        });
    }

    /**
     * Rimuove definitivamente un membro dal gruppo.
     */
    public void removeMember(String groupId, String userId) {
        repository.removeMember(groupId, userId, status -> {
            if ("SUCCESS".equals(status)) {
                actionStatus.setValue("Membro rimosso");
            } else {
                actionStatus.setValue(status);
            }
        });
    }

    /**
     * Elimina l'intero gruppo (solo se l'utente è il proprietario).
     */
    public void deleteGroup(String groupId) {
        repository.deleteGroup(groupId, actionStatus::setValue);
    }

    /**
     * Permette a un utente di abbandonare il gruppo.
     */
    public void leaveGroup(String groupId, String userId) {
        repository.leaveGroup(groupId, userId, actionStatus::setValue);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (groupListener != null) groupListener.remove();
    }
}
