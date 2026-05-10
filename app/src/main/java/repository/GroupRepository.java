package repository;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import model.Group;
import model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GroupRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void createGroup(String groupName, String ownerId, String ownerNickname, Consumer<String> callback) {
        db.collection("groups").document(groupName).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.accept("ERR_GROUP_NAME_EXISTS");
                    } else {
                        Group newGroup = new Group(groupName, ownerId, ownerNickname);
                        db.collection("groups")
                                .document(groupName)
                                .set(newGroup)
                                .addOnSuccessListener(aVoid -> callback.accept("SUCCESS"))
                                .addOnFailureListener(e -> callback.accept("ERR_CREATE_FAILED"));
                    }
                })
                .addOnFailureListener(e -> callback.accept("ERR_VERIFY_FAILED"));
    }

    public Query getGroupsForUserQuery(String userId) {
        return db.collection("groups").whereArrayContains("members", userId);
    }

    public void addMember(String groupId, String memberEmail, Consumer<String> callback) {
        db.collection("users").whereEqualTo("email", memberEmail).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.accept("ERR_USER_NOT_FOUND");
                        return;
                    }
                    String userId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    db.collection("groups").document(groupId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (!documentSnapshot.exists()) return;
                                Group group = documentSnapshot.toObject(Group.class);
                                if (group != null && group.getMembers() != null && group.getMembers().contains(userId)) {
                                    callback.accept("ERR_MEMBER_ALREADY_PRESENT");
                                } else {
                                    db.collection("groups").document(groupId)
                                            .update("members", FieldValue.arrayUnion(userId))
                                            .addOnSuccessListener(aVoid -> callback.accept("SUCCESS"))
                                            .addOnFailureListener(e -> callback.accept("ERR_UPDATE_FAILED"));
                                }
                            });
                })
                .addOnFailureListener(e -> callback.accept("ERR_SEARCH_FAILED"));
    }

    public ListenerRegistration listenToGroup(String groupId, Consumer<Group> callback) {
        return db.collection("groups").document(groupId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) return;
                    if (snapshot != null && snapshot.exists()) {
                        callback.accept(snapshot.toObject(Group.class));
                    } else {
                        callback.accept(null);
                    }
                });
    }

    public void getUsersDetails(List<String> userIds, Consumer<List<User>> callback) {
        if (userIds == null || userIds.isEmpty()) {
            callback.accept(new ArrayList<>());
            return;
        }
        db.collection("users").whereIn("uid", userIds).get()
                .addOnSuccessListener(queryDocumentSnapshots -> callback.accept(queryDocumentSnapshots.toObjects(User.class)));
    }

    public void blockUser(String groupId, String userId, boolean block, Consumer<String> callback) {
        db.collection("groups").document(groupId)
                .update("blockedMembers", block ? FieldValue.arrayUnion(userId) : FieldValue.arrayRemove(userId))
                .addOnSuccessListener(aVoid -> callback.accept("SUCCESS"))
                .addOnFailureListener(e -> callback.accept("Errore: " + e.getMessage()));
    }

    public void removeMember(String groupId, String userId, Consumer<String> callback) {
        db.collection("groups").document(groupId)
                .update("members", FieldValue.arrayRemove(userId), "blockedMembers", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(aVoid -> callback.accept("SUCCESS"))
                .addOnFailureListener(e -> callback.accept("Errore: " + e.getMessage()));
    }

    public void deleteGroup(String groupId, Consumer<String> callback) {
        db.collection("groups").document(groupId).delete()
                .addOnSuccessListener(aVoid -> callback.accept("DELETE_SUCCESS"))
                .addOnFailureListener(e -> callback.accept("Errore: " + e.getMessage()));
    }

    public void leaveGroup(String groupId, String userId, Consumer<String> callback) {
        db.collection("groups").document(groupId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    Group group = doc.toObject(Group.class);
                    if (group == null) return;

                    List<String> members = group.getMembers();
                    members.remove(userId);
                    if (group.getBlockedMembers() != null) {
                        group.getBlockedMembers().remove(userId);
                    }

                    if (members.isEmpty()) {
                        deleteGroup(groupId, status -> callback.accept("LEAVE_SUCCESS"));
                    } else if (userId.equals(group.getOwnerId())) {
                        // Il proprietario sta uscendo, scegliamo un nuovo proprietario a caso
                        String newOwnerId = members.get(new java.util.Random().nextInt(members.size()));
                        db.collection("users").document(newOwnerId).get()
                                .addOnSuccessListener(userDoc -> {
                                    User user = userDoc.toObject(User.class);
                                    String newNickname = (user != null) ? user.getNickname() : "Anonimo";
                                    
                                    db.collection("groups").document(groupId)
                                            .update("members", members,
                                                    "blockedMembers", group.getBlockedMembers(),
                                                    "ownerId", newOwnerId,
                                                    "ownerNickname", newNickname)
                                            .addOnSuccessListener(aVoid -> callback.accept("LEAVE_SUCCESS"))
                                            .addOnFailureListener(e -> callback.accept("Errore: " + e.getMessage()));
                                });
                    } else {
                        // Esce un membro normale
                        db.collection("groups").document(groupId)
                                .update("members", members, "blockedMembers", group.getBlockedMembers())
                                .addOnSuccessListener(aVoid -> callback.accept("LEAVE_SUCCESS"))
                                .addOnFailureListener(e -> callback.accept("Errore: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.accept("Errore: " + e.getMessage()));
    }
}
