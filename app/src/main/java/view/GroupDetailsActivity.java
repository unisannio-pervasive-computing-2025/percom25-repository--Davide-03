package view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progettocomputazione.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import model.Group;
import model.User;
import viewmodel.AuthViewModel;
import viewmodel.GroupViewModel;

/**
 * Activity che visualizza i dettagli di un gruppo, inclusa la lista dei membri.
 * Permette al proprietario di gestire i partecipanti (aggiunta, rimozione, blocco).
 */
public class GroupDetailsActivity extends AppCompatActivity {

    private GroupViewModel groupViewModel;
    private AuthViewModel authViewModel;
    private String groupId;
    private MemberAdapter adapter;
    private String ownerId;
    private List<String> blockedMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        // Recupero dei parametri passati dall'activity precedente (ChatActivity)
        groupId = getIntent().getStringExtra("groupId");
        
        groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Configurazione Toolbar con tasto "Indietro"
        Toolbar toolbar = findViewById(R.id.detailsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        EditText editEmail = findViewById(R.id.editMemberEmail);
        Button btnAdd = findViewById(R.id.btnAddMember);
        View layoutAddMember = findViewById(R.id.layoutAddMember);
        RecyclerView recyclerView = findViewById(R.id.recyclerMembers);

        // Configurazione RecyclerView per la lista dei membri
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    // Previene crash rari di inconsistenza del layout
                }
            }
        });
        adapter = new MemberAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        /**
         * Osserva lo stato delle azioni effettuate (aggiunta membri, eliminazione gruppo, ecc.)
         */
        groupViewModel.getActionStatus().observe(this, status -> {
            if ("SUCCESS".equals(status)) {
                Toast.makeText(this, R.string.msg_member_added, Toast.LENGTH_SHORT).show();
                editEmail.setText("");
            } else if ("ERR_EMPTY_EMAIL".equals(status)) {
                Toast.makeText(this, R.string.err_empty_email, Toast.LENGTH_SHORT).show();
            } else if ("ERR_MEMBER_ALREADY_PRESENT".equals(status)) {
                Toast.makeText(this, R.string.err_member_already_present, Toast.LENGTH_LONG).show();
            } else if ("ERR_USER_NOT_FOUND".equals(status)) {
                Toast.makeText(this, R.string.err_user_not_found, Toast.LENGTH_LONG).show();
            } else if ("DELETE_SUCCESS".equals(status)) {
                Toast.makeText(this, R.string.msg_group_deleted, Toast.LENGTH_SHORT).show();
                // Gruppo eliminato: torna alla lista gruppi e pulisce lo stack
                tornaAllaListaGruppi();
            } else if ("LEAVE_SUCCESS".equals(status)) {
                Toast.makeText(this, R.string.msg_leave_success, Toast.LENGTH_SHORT).show();
                // Abbandono successo: torna alla lista gruppi
                tornaAllaListaGruppi();
            } else if (status != null) {
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
            }
        });

        // Aggiorna la lista membri quando i dati cambiano
        groupViewModel.getGroupMembers().observe(this, members -> adapter.updateMembers(members));

        /**
         * Monitora i dati del gruppo per gestire i permessi di visualizzazione (es. form aggiunta membro)
         */
        groupViewModel.getCurrentGroup().observe(this, group -> {
            if (group != null) {
                ownerId = group.getOwnerId();
                blockedMembers = group.getBlockedMembers() != null ? group.getBlockedMembers() : new ArrayList<>();
                
                FirebaseUser currentUser = authViewModel.getCurrentUser();
                boolean isOwner = currentUser != null && currentUser.getUid().equals(ownerId);
                
                // Solo il proprietario vede il campo per aggiungere membri
                layoutAddMember.setVisibility(isOwner ? View.VISIBLE : View.GONE);

                adapter.notifyDataSetChanged();
                invalidateOptionsMenu(); // Ricarica il menu per mostrare/nascondere "Elimina gruppo"
            }
        });

        btnAdd.setOnClickListener(v -> groupViewModel.addMember(groupId, editEmail.getText().toString()));

        // Inizia il caricamento dei dettagli del gruppo
        groupViewModel.loadGroupDetails(groupId);
    }

    /**
     * Metodo di utilità per tornare alla lista gruppi pulendo lo stack delle Activity.
     */
    private void tornaAllaListaGruppi() {
        Intent intent = new Intent(this, GroupListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Simula la pressione del tasto back di sistema per tornare all'Activity precedente (ChatActivity)
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_details_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteItem = menu.findItem(R.id.action_delete_group);
        if (deleteItem != null) {
            FirebaseUser currentUser = authViewModel.getCurrentUser();
            boolean isOwner = currentUser != null && currentUser.getUid().equals(ownerId);
            // Solo il proprietario può vedere l'opzione per eliminare il gruppo
            deleteItem.setVisible(isOwner);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_leave_group) {
            FirebaseUser user = authViewModel.getCurrentUser();
            if (user != null) {
                groupViewModel.leaveGroup(groupId, user.getUid());
            }
            return true;
        } else if (item.getItemId() == R.id.action_delete_group) {
            Group group = groupViewModel.getCurrentGroup().getValue();
            if (group != null && group.getMembers() != null) {
                // Il gruppo può essere eliminato solo se è rimasto solo il proprietario (o è vuoto)
                if (group.getMembers().size() <= 1) {
                    groupViewModel.deleteGroup(groupId);
                } else {
                    Toast.makeText(this, R.string.err_group_not_empty, Toast.LENGTH_LONG).show();
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Mostra il menu contestuale per un singolo membro della lista.
     */
    private void showMemberMenu(View view, User user) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.member_menu, popup.getMenu());

        boolean isBlocked = blockedMembers.contains(user.getUid());
        popup.getMenu().findItem(R.id.action_block).setVisible(!isBlocked);
        popup.getMenu().findItem(R.id.action_unblock).setVisible(isBlocked);

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_block) {
                groupViewModel.blockUser(groupId, user.getUid(), true);
                return true;
            } else if (itemId == R.id.action_unblock) {
                groupViewModel.blockUser(groupId, user.getUid(), false);
                return true;
            } else if (itemId == R.id.action_remove) {
                groupViewModel.removeMember(groupId, user.getUid());
                return true;
            }
            return false;
        });
        popup.show();
    }

    /**
     * Adapter interno per gestire la visualizzazione dei membri nel RecyclerView.
     */
    private class MemberAdapter extends RecyclerView.Adapter<MemberViewHolder> {
        private final List<User> members;

        public MemberAdapter(List<User> members) {
            this.members = members;
        }

        public void updateMembers(List<User> newMembers) {
            this.members.clear();
            this.members.addAll(newMembers);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
            return new MemberViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
            User user = members.get(position);
            holder.nickname.setText(user.getNickname());
            holder.email.setText(user.getEmail());

            FirebaseUser currentUser = authViewModel.getCurrentUser();
            boolean isCurrentUserOwner = currentUser != null && currentUser.getUid().equals(ownerId);

            if (user.getUid().equals(ownerId)) {
                // Stile speciale per il proprietario
                holder.ownerBadge.setVisibility(View.VISIBLE);
                holder.ownerBadge.setText(R.string.owner_badge);
                holder.btnMenu.setVisibility(View.GONE);
                holder.card.setCardBackgroundColor(ContextCompat.getColor(GroupDetailsActivity.this, R.color.primary_modern));
                holder.nickname.setTextColor(ContextCompat.getColor(GroupDetailsActivity.this, R.color.white));
                holder.email.setTextColor(ContextCompat.getColor(GroupDetailsActivity.this, R.color.white));
            } else {
                holder.ownerBadge.setVisibility(View.GONE);
                // Il menu di gestione membro è visibile solo al proprietario del gruppo
                holder.btnMenu.setVisibility(isCurrentUserOwner ? View.VISIBLE : View.GONE);
                
                if (blockedMembers.contains(user.getUid())) {
                    // Stile per utenti bloccati
                    holder.card.setCardBackgroundColor(ContextCompat.getColor(GroupDetailsActivity.this, R.color.blocked_red));
                    holder.nickname.setTextColor(ContextCompat.getColor(GroupDetailsActivity.this, R.color.blocked_text));
                    holder.email.setTextColor(ContextCompat.getColor(GroupDetailsActivity.this, R.color.blocked_text));
                } else {
                    // Stile standard
                    holder.card.setCardBackgroundColor(ContextCompat.getColor(GroupDetailsActivity.this, R.color.card_bg));
                    holder.nickname.setTextColor(ContextCompat.getColor(GroupDetailsActivity.this, R.color.text_main));
                    holder.email.setTextColor(ContextCompat.getColor(GroupDetailsActivity.this, R.color.text_secondary));
                }

                if (isCurrentUserOwner) {
                    holder.btnMenu.setOnClickListener(v -> showMemberMenu(v, user));
                }
            }
        }

        @Override
        public int getItemCount() {
            return members.size();
        }
    }

    /**
     * ViewHolder per la gestione degli elementi UI del singolo membro.
     */
    private static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView nickname, email, ownerBadge;
        ImageButton btnMenu;
        MaterialCardView card;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            nickname = itemView.findViewById(R.id.txtMemberNickname);
            email = itemView.findViewById(R.id.txtMemberEmail);
            ownerBadge = itemView.findViewById(R.id.txtOwnerBadge);
            btnMenu = itemView.findViewById(R.id.btnMemberMenu);
            card = itemView.findViewById(R.id.cardMember);
        }
    }
}
