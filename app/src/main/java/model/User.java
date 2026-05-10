package model;

public class User {
    private String uid;
    private String email;
    private String nickname;

    // Costruttore vuoto obbligatorio per Firebase
    public User() {
    }

    public User(String uid, String email, String nickname) {
        this.uid = uid;
        this.email = email;
        this.nickname = nickname;
    }

    // GETTER E SETTER (Necessari per Firestore)
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
