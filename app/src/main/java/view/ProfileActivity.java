package view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.progettocomputazione.R;
import com.google.firebase.auth.FirebaseUser;

import viewmodel.AuthViewModel;

public class ProfileActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        Toolbar toolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView txtEmail = findViewById(R.id.txtProfileEmail);
        EditText editNickname = findViewById(R.id.editProfileNickname);
        Button btnUpdate = findViewById(R.id.btnUpdateProfile);

        authViewModel.getNickname().observe(this, editNickname::setText);

        FirebaseUser user = authViewModel.getCurrentUser();
        if (user != null) {
            txtEmail.setText(user.getEmail());
            authViewModel.fetchNickname(user.getUid());
        }

        authViewModel.getAuthStatus().observe(this, status -> {
            if ("SUCCESS".equals(status)) {
                Toast.makeText(this, "Profilo aggiornato!", Toast.LENGTH_SHORT).show();
            } else if (status != null && !status.equals("LOGIN_SUCCESS") && !status.equals("REG_SUCCESS")) {
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdate.setOnClickListener(v -> authViewModel.updateNickname(editNickname.getText().toString()));
    }
}
