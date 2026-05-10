package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe modello che rappresenta un gruppo di chat.
 * Contiene informazioni su membri, proprietario e stato del gruppo.
 */
public class Group {
    private String name;                // Nome univoco del gruppo (usato come ID documento)
    private String ownerId;             // UID dell'utente creatore/proprietario
    private String ownerNickname;       // Nickname aggiornato del proprietario
    private List<String> members;       // Lista degli UID di tutti i partecipanti
    private List<String> blockedMembers; // Lista degli UID degli utenti che possono solo leggere (non scrivere)
    private boolean isLocked;           // Stato di blocco globale del gruppo (funzionalità traccia)

    /**
     * Costruttore vuoto obbligatorio per Firebase Firestore.
     */
    public Group() {}

    /**
     * Costruttore per la creazione di un nuovo gruppo.
     * Inizializza le liste e imposta il creatore come primo membro.
     */
    public Group(String name, String ownerId, String ownerNickname) {
        this.name = name;
        this.ownerId = ownerId;
        this.ownerNickname = ownerNickname;
        this.members = new ArrayList<>();
        this.blockedMembers = new ArrayList<>();
        this.members.add(ownerId); // Il creatore è il primo membro del gruppo
        this.isLocked = false;
    }

    // Getter e Setter fondamentali per Firestore e per la logica dell'app

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerNickname() { return ownerNickname; }
    public void setOwnerNickname(String ownerNickname) { this.ownerNickname = ownerNickname; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public List<String> getBlockedMembers() { return blockedMembers; }
    public void setBlockedMembers(List<String> blockedMembers) { this.blockedMembers = blockedMembers; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }
}
