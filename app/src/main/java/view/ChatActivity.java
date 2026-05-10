package view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.progettocomputazione.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Locale;

import model.ChatMessage;
import viewmodel.AuthViewModel;
import viewmodel.ChatViewModel;
import viewmodel.GroupViewModel;

public class ChatActivity extends AppCompatActivity {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private ChatViewModel chatViewModel;
    private AuthViewModel authViewModel;
    private GroupViewModel groupViewModel;
    private String groupId;
    private String groupOwnerId;
    private String latestNickname;
    private FirestoreRecyclerAdapter<ChatMessage, MessageViewHolder> adapter;
    private View layoutInput;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    showImagePreviewDialog(uri);
                }
            }
    );

    private void showImagePreviewDialog(Uri uri) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_preview, null);
        ImageView imgPreview = dialogView.findViewById(R.id.imgPreview);
        imgPreview.setImageURI(uri);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_send_image_title)
                .setView(dialogView)
                .setPositiveButton(R.string.btn_send_action, (dialog, which) -> {
                    FirebaseUser user = authViewModel.getCurrentUser();
                    if (user != null) {
                        String name = (latestNickname != null) ? latestNickname : user.getEmail();
                        chatViewModel.sendImage(groupId, user.getUid(), name, uri);
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        groupId = getIntent().getStringExtra("groupId");
        String groupName = getIntent().getStringExtra("groupName");

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);

        authViewModel.getNickname().observe(this, name -> latestNickname = name);

        FirebaseUser currentUser = authViewModel.getCurrentUser();
        if (currentUser != null) {
            authViewModel.fetchNickname(currentUser.getUid());
        }

        Toolbar toolbar = findViewById(R.id.chatToolbar);
        if (groupName != null) {
            toolbar.setTitle(groupName);
        }
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, GroupDetailsActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerMessages);
        EditText editMessage = findViewById(R.id.editMessage);
        View btnSend = findViewById(R.id.btnSendMessage);
        View btnAttach = findViewById(R.id.btnAttach);
        layoutInput = findViewById(R.id.layoutInput);
        
        groupViewModel.getCurrentGroup().observe(this, group -> {
            if (group == null) {
                Toast.makeText(this, R.string.msg_group_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            groupOwnerId = group.getOwnerId();
            FirebaseUser user = authViewModel.getCurrentUser();
            if (user != null) {
                boolean isMember = group.getMembers() != null && group.getMembers().contains(user.getUid());
                if (!isMember) {
                    Toast.makeText(this, R.string.msg_removed_from_group, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                boolean isBlocked = group.getBlockedMembers() != null && group.getBlockedMembers().contains(user.getUid());
                boolean isGroupLocked = group.isLocked();
                layoutInput.setVisibility((isBlocked || isGroupLocked) ? View.GONE : View.VISIBLE);
            }
            if (adapter != null) adapter.notifyDataSetChanged();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    // Previene il crash "Inconsistency detected"
                }
            }
        });
        setupAdapter(recyclerView);

        btnSend.setOnClickListener(v -> {
            FirebaseUser user = authViewModel.getCurrentUser();
            if (user != null) {
                String name = (latestNickname != null) ? latestNickname : user.getEmail();
                chatViewModel.sendMessage(groupId, user.getUid(), name, editMessage.getText().toString());
                editMessage.setText("");
            }
        });

        btnAttach.setOnClickListener(v -> mGetContent.launch("image/*"));

        chatViewModel.getUploadStatus().observe(this, status -> {
            if ("UPLOADING".equals(status)) {
                Toast.makeText(this, R.string.msg_uploading_image, Toast.LENGTH_SHORT).show();
            } else if ("SUCCESS".equals(status)) {
                Toast.makeText(this, R.string.msg_image_sent, Toast.LENGTH_SHORT).show();
            } else if (status != null && status.startsWith("ERROR")) {
                Toast.makeText(this, status, Toast.LENGTH_LONG).show();
            }
        });
        
        groupViewModel.loadGroupDetails(groupId);
    }

    private void setupAdapter(RecyclerView recyclerView) {
        FirestoreRecyclerOptions<ChatMessage> options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(chatViewModel.getMessagesQuery(groupId), ChatMessage.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ChatMessage, MessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull ChatMessage model) {
                boolean isSameAsPrevious = false;
                if (position > 0) {
                    ChatMessage previousMessage = getItem(position - 1);
                    if (previousMessage.getSenderId() != null && previousMessage.getSenderId().equals(model.getSenderId())) {
                        isSameAsPrevious = true;
                    }
                }

                if (isSameAsPrevious) {
                    holder.layoutSenderInfo.setVisibility(View.GONE);
                    holder.itemView.setPadding(holder.itemView.getPaddingLeft(), 0, holder.itemView.getPaddingRight(), holder.itemView.getPaddingBottom());
                } else {
                    holder.layoutSenderInfo.setVisibility(View.VISIBLE);
                    holder.txtSender.setText(model.getSenderName());
                    int topPadding = (int) (4 * getResources().getDisplayMetrics().density);
                    holder.itemView.setPadding(holder.itemView.getPaddingLeft(), topPadding, holder.itemView.getPaddingRight(), holder.itemView.getPaddingBottom());
                    holder.txtOwnerBadge.setVisibility((model.getSenderId() != null && model.getSenderId().equals(groupOwnerId)) ? View.VISIBLE : View.GONE);
                }
                
                if (ChatMessage.TYPE_IMAGE.equals(model.getType())) {
                    holder.txtMessage.setVisibility(View.GONE);
                    holder.imgPhoto.setVisibility(View.VISIBLE);
                    Glide.with(holder.imgPhoto.getContext()).load(model.getImageUrl()).into(holder.imgPhoto);
                } else {
                    holder.imgPhoto.setVisibility(View.GONE);
                    holder.txtMessage.setVisibility(View.VISIBLE);
                    holder.txtMessage.setText(model.getMessage());
                }

                if (model.getTimestamp() != null) {
                    holder.txtTime.setText(timeFormat.format(model.getTimestamp().toDate()));
                } else {
                    holder.txtTime.setText("");
                }
            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                int layoutRes = (viewType == VIEW_TYPE_SENT) ? R.layout.item_message_sent : R.layout.item_message_received;
                View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
                return new MessageViewHolder(view);
            }

            @Override
            public int getItemViewType(int position) {
                ChatMessage msg = getItem(position);
                FirebaseUser currentUser = authViewModel.getCurrentUser();
                return (currentUser != null && msg.getSenderId() != null && msg.getSenderId().equals(currentUser.getUid())) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
            }
            
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                recyclerView.smoothScrollToPosition(getItemCount() > 0 ? getItemCount() - 1 : 0);
            }
        };

        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
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

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtSender, txtOwnerBadge, txtTime;
        ImageView imgPhoto;
        View layoutSenderInfo;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMsgContent);
            txtSender = itemView.findViewById(R.id.txtMsgSenderName);
            txtOwnerBadge = itemView.findViewById(R.id.txtMsgOwnerBadge);
            txtTime = itemView.findViewById(R.id.txtMsgTime);
            imgPhoto = itemView.findViewById(R.id.imgMsgPhoto);
            layoutSenderInfo = itemView.findViewById(R.id.layoutSenderInfo);
        }
    }
}
