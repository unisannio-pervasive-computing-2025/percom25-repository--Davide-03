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

public class GroupViewModel extends ViewModel {
    private final GroupRepository repository = new GroupRepository();
    private final SingleLiveEvent<String> actionStatus = new SingleLiveEvent<>();
    private final MutableLiveData<List<User>> groupMembers = new MutableLiveData<>();
    private final MutableLiveData<Group> currentGroup = new MutableLiveData<>();
    private ListenerRegistration groupListener;
    private String currentGroupId;

    public LiveData<String> getActionStatus() { return actionStatus; }
    public LiveData<List<User>> getGroupMembers() { return groupMembers; }
    public LiveData<Group> getCurrentGroup() { return currentGroup; }

    public void createGroup(String name, String ownerId, String ownerNickname) {
        repository.createGroup(name, ownerId, ownerNickname, actionStatus::setValue);
    }

    public Query getGroupsQuery(String userId) {
        return repository.getGroupsForUserQuery(userId);
    }

    public void addMember(String groupId, String email) {
        if (email == null || email.trim().isEmpty()) {
            actionStatus.setValue("ERR_EMPTY_EMAIL");
            return;
        }
        repository.addMember(groupId, email.trim(), status -> {
            actionStatus.setValue(status);
        });
    }

    public void loadGroupDetails(String groupId) {
        if (groupId != null && groupId.equals(currentGroupId) && groupListener != null) {
            return;
        }

        if (groupListener != null) groupListener.remove();
        currentGroupId = groupId;
        
        groupListener = repository.listenToGroup(groupId, group -> {
            currentGroup.setValue(group);
            if (group != null && group.getMembers() != null) {
                repository.getUsersDetails(group.getMembers(), groupMembers::setValue);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (groupListener != null) groupListener.remove();
    }

    public void blockUser(String groupId, String userId, boolean block) {
        repository.blockUser(groupId, userId, block, status -> {
            if ("SUCCESS".equals(status)) {
                actionStatus.setValue(block ? "Membro bloccato" : "Membro sbloccato");
            } else {
                actionStatus.setValue(status);
            }
        });
    }

    public void removeMember(String groupId, String userId) {
        repository.removeMember(groupId, userId, status -> {
            if ("SUCCESS".equals(status)) {
                actionStatus.setValue("Membro rimosso");
            } else {
                actionStatus.setValue(status);
            }
        });
    }

    public void deleteGroup(String groupId) {
        repository.deleteGroup(groupId, actionStatus::setValue);
    }

    public void leaveGroup(String groupId, String userId) {
        repository.leaveGroup(groupId, userId, actionStatus::setValue);
    }
}
