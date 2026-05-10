package view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progettocomputazione.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseUser;

import model.Group;
import viewmodel.AuthViewModel;
import viewmodel.GroupViewModel;

/**
 * Activity che visualizza la lista dei gruppi di cui l'utente fa parte.
 * Permette anche la creazione di nuovi gruppi e l'accesso al profilo.
 */
public class GroupListActivity extends AppCompatActivity {

    private GroupViewModel groupViewModel;
    private AuthViewModel authViewModel;
    private RecyclerView recyclerView;
    private FirestoreRecyclerAdapter<Group, GroupViewHolder> adapter;
    private TextView txtNoGroups;
    private TextView txtWelcome;
    private String latestNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        recyclerView = findViewById(R.id.recyclerGroups);
        txtNoGroups = findViewById(R.id.txtNoGroups);
        txtWelcome = findViewById(R.id.txtWelcome);
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        ExtendedFloatingActionButton fabCreateGroup = findViewById(R.id.fabCreateGroup);

        // Monitora il nickname per il messaggio di benvenuto
        authViewModel.getNickname().observe(this, name -> {
            latestNickname = name;
            txtWelcome.setText(getString(R.string.welcome_user, name));
        });

        FirebaseUser user = authViewModel.getCurrentUser();
        if (user != null) {
            authViewModel.fetchNickname(user.getUid());
        }

        btnMenu.setOnClickListener(this::showPopupMenu);

        // Osserva gli esiti delle azioni (es. creazione gruppo)
        groupViewModel.getActionStatus().observe(this, status -> {
            if ("SUCCESS".equals(status)) {
                Toast.makeText(this, R.string.msg_group_created, Toast.LENGTH_SHORT).show();
            } else if ("ERR_GROUP_NAME_EXISTS".equals(status)) {
                Toast.makeText(this, R.string.err_group_name_exists, Toast.LENGTH_LONG).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    // Previene il crash "Inconsistency detected" dovuto a bug interni di RecyclerView
                }
            }

            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });
        setupAdapter();
        
        fabCreateGroup.setOnClickListener(v -> showCreateGroupDialog());
    }

    /**
     * Mostra il menu a comparsa per Profilo e Logout.
     */
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.group_list_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.action_logout) {
                logout();
                return true;
            }
            return false;
        });
        popup.show();
    }

    /**
     * Esegue il logout e torna alla schermata di login.
     */
    private void logout() {
        authViewModel.logout();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Configura l'adapter per visualizzare i gruppi dell'utente in tempo reale.
     */
    private void setupAdapter() {
        FirebaseUser user = authViewModel.getCurrentUser();
        if (user == null) return;

        FirestoreRecyclerOptions<Group> options = new FirestoreRecyclerOptions.Builder<Group>()
                .setQuery(groupViewModel.getGroupsQuery(user.getUid()), Group.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Group, GroupViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull GroupViewHolder holder, int position, @NonNull Group model) {
                holder.name.setText(model.getName());
                holder.owner.setText(getString(R.string.proprietario_label, model.getOwnerNickname()));

                holder.itemView.setOnClickListener(v -> {
                    // Apre la chat del gruppo selezionato
                    Intent intent = new Intent(GroupListActivity.this, ChatActivity.class);
                    intent.putExtra("groupId", model.getName());
                    intent.putExtra("groupName", model.getName());
                    startActivity(intent);
                });
            }

            @NonNull
            @Override
            public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
                return new GroupViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                // Mostra/nasconde il messaggio "Nessun gruppo"
                txtNoGroups.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        };

        recyclerView.setAdapter(adapter);
    }

    /**
     * Mostra il dialogo per inserire il nome del nuovo gruppo da creare.
     */
    private void showCreateGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.create_group_dialog_title);
        final EditText input = new EditText(this);
        input.setHint(R.string.group_name_hint);
        builder.setView(input);
        builder.setPositiveButton(R.string.create, (dialog, which) -> {
            String groupName = input.getText().toString().trim();
            if (!groupName.isEmpty()) {
                FirebaseUser user = authViewModel.getCurrentUser();
                if (user != null) {
                    groupViewModel.createGroup(groupName, user.getUid(), latestNickname != null ? latestNickname : user.getEmail());
                }
            } else {
                Toast.makeText(this, R.string.err_empty_fields, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView name, owner;
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtGroupName);
            owner = itemView.findViewById(R.id.txtGroupOwner);
        }
    }
}
