package model;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String name;
    private String ownerId; // L'UID dell'utente che lo crea
    private String ownerNickname; // Nome visualizzato del proprietario
    private List<String> members; // Lista di email o UID dei partecipanti
    private List<String> blockedMembers; // Lista di UID degli utenti bloccati (possono solo leggere)
    private boolean isLocked; // Per la funzione di blocco chat della traccia

    // Costruttore vuoto obbligatorio per Firebase
    public Group() {}

    public Group(String name, String ownerId, String ownerNickname) {
        this.name = name;
        this.ownerId = ownerId;
        this.ownerNickname = ownerNickname;
        this.members = new ArrayList<>();
        this.blockedMembers = new ArrayList<>();
        this.members.add(ownerId); // L'owner è il primo membro
        this.isLocked = false;
    }

    // Aggiungi Getter e Setter per tutti i campi
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
