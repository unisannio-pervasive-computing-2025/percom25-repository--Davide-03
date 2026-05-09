package viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.Query;
import java.util.List;
import model.Group;
import model.User;
import repository.GroupRepository;

public class GroupViewModel extends ViewModel {
    private final GroupRepository repository = new GroupRepository();
    private final MutableLiveData<String> actionStatus = new MutableLiveData<>();
    private final MutableLiveData<List<User>> groupMembers = new MutableLiveData<>();
    private final MutableLiveData<Group> currentGroup = new MutableLiveData<>();

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
            if ("SUCCESS".equals(status)) {
                loadGroupDetails(groupId);
            }
        });
    }

    public void loadGroupDetails(String groupId) {
        repository.getGroup(groupId, group -> {
            currentGroup.setValue(group);
            if (group != null && group.getMembers() != null) {
                repository.getUsersDetails(group.getMembers(), groupMembers::setValue);
            }
        });
    }

    public void blockUser(String groupId, String userId, boolean block) {
        repository.blockUser(groupId, userId, block, status -> {
            if ("SUCCESS".equals(status)) {
                actionStatus.setValue(block ? "Membro bloccato" : "Membro sbloccato");
                loadGroupDetails(groupId);
            } else {
                actionStatus.setValue(status);
            }
        });
    }

    public void removeMember(String groupId, String userId) {
        repository.removeMember(groupId, userId, status -> {
            if ("SUCCESS".equals(status)) {
                actionStatus.setValue("Membro rimosso");
                loadGroupDetails(groupId);
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
