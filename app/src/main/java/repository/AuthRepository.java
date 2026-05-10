package repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;
import model.User;
import java.util.function.Consumer;

public class AuthRepository {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void login(String email, String password, Consumer<String> callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.accept("LOGIN_SUCCESS");
                    } else {
                        Exception e = task.getException();
                        if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ||
                            e instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                            callback.accept("ERR_INVALID_CREDENTIALS");
                        } else {
                            callback.accept(e != null ? e.getMessage() : "ERR_UNKNOWN");
                        }
                    }
                });
    }

    public void register(String email, String password, String nickname, Consumer<String> callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String uid = task.getResult().getUser().getUid();
                        User newUser = new User(uid, email, nickname);
                        db.collection("users").document(uid).set(newUser)
                                .addOnSuccessListener(aVoid -> callback.accept("REG_SUCCESS"))
                                .addOnFailureListener(e -> callback.accept(e.getMessage()));
                    } else {
                        Exception e = task.getException();
                        if (e instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                            callback.accept("ERR_EMAIL_IN_USE");
                        } else if (e instanceof com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
                            callback.accept("ERR_WEAK_PASSWORD");
                        } else {
                            callback.accept(e != null ? e.getMessage() : "ERR_REG_FAILED");
                        }
                    }
                });
    }

    public ListenerRegistration listenToUserNickname(String uid, Consumer<String> callback) {
        return db.collection("users").document(uid)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        callback.accept(documentSnapshot.getString("nickname"));
                    } else {
                        callback.accept("Sconosciuto");
                    }
                });
    }

    public void checkUserExists(String uid, Consumer<Boolean> callback) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> callback.accept(documentSnapshot.exists()))
                .addOnFailureListener(e -> callback.accept(false));
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void logout() {
        mAuth.signOut();
    }

    public void updateNickname(String uid, String newNickname, Consumer<String> callback) {
        db.collection("users").document(uid).update("nickname", newNickname)
                .addOnSuccessListener(aVoid -> {
                    db.collection("groups").whereEqualTo("ownerId", uid).get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (queryDocumentSnapshots.isEmpty()) {
                                    callback.accept("SUCCESS");
                                    return;
                                }
                                WriteBatch batch = db.batch();
                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    batch.update(doc.getReference(), "ownerNickname", newNickname);
                                }
                                batch.commit()
                                        .addOnSuccessListener(v -> callback.accept("SUCCESS"))
                                        .addOnFailureListener(e -> callback.accept(e.getMessage()));
                            });
                })
                .addOnFailureListener(e -> callback.accept(e.getMessage()));
    }
}
