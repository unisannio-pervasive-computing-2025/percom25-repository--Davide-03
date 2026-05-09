package model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String uid;
    private String email;
    private String nickname;
    private List<String> groupIds; // Lista degli ID dei gruppi di cui fa parte

    // Costruttore vuoto obbligatorio per Firebase
    public User() {
    }

    public User(String uid, String email, String nickname) {
        this.uid = uid;
        this.email = email;
        this.nickname = nickname;
        this.groupIds = new ArrayList<>();
    }

    // GETTER E SETTER (Necessari per Firestore)
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public List<String> getGroupIds() { return groupIds; }
    public void setGroupIds(List<String> groupIds) { this.groupIds = groupIds; }
}
