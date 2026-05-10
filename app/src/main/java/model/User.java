package model;

/**
 * Classe modello che rappresenta un utente dell'applicazione.
 */
public class User {
    private String uid;      // Identificativo univoco dell'utente fornito da Firebase Auth
    private String email;    // Indirizzo email dell'utente
    private String nickname; // Nickname scelto dall'utente per farsi riconoscere nelle chat

    /**
     * Costruttore vuoto obbligatorio per Firebase Firestore.
     */
    public User() {}

    /**
     * Costruttore per la creazione di un nuovo profilo utente.
     */
    public User(String uid, String email, String nickname) {
        this.uid = uid;
        this.email = email;
        this.nickname = nickname;
    }

    // Getter e Setter necessari per la deserializzazione di Firestore

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
