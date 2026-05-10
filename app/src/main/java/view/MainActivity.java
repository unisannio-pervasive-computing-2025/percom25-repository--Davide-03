package view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.progettocomputazione.R;

import java.util.Objects;

import viewmodel.AuthViewModel;

/**
 * Activity principale che funge da schermata di login.
 * Gestisce l'accesso dell'utente e il reindirizzamento alla lista gruppi.
 */
public class MainActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private EditText emailField, passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Gestione dei margini per schermi con notch/edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        emailField = findViewById(R.id.editEmail);
        passwordField = findViewById(R.id.editPassword);
        Button btnLoginAction = findViewById(R.id.btnLoginAction);
        Button btnGoToRegister = findViewById(R.id.btnGoToRegister);

        /**
         * Osserva lo stato dell'autenticazione per gestire il passaggio alla schermata successiva.
         */
        authViewModel.getAuthStatus().observe(this, status -> {
            if ("LOGIN_SUCCESS".equals(status)) {
                Toast.makeText(this, R.string.msg_login_success, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, GroupListActivity.class);
                startActivity(intent);
                finish(); // Chiude la MainActivity per non tornare al login col tasto back
            } else if ("SESSION_EXPIRED".equals(status)) {
                Toast.makeText(this, R.string.msg_session_expired, Toast.LENGTH_LONG).show();
            } else if ("ERR_INVALID_CREDENTIALS".equals(status)) {
                Toast.makeText(this, R.string.err_invalid_credentials, Toast.LENGTH_LONG).show();
            } else if ("ERR_UNKNOWN".equals(status)) {
                Toast.makeText(this, R.string.err_unknown, Toast.LENGTH_LONG).show();
            } else if (status != null && !Objects.equals(status, "REG_SUCCESS")) {
                Toast.makeText(this, status, Toast.LENGTH_LONG).show();
            }
        });

        btnLoginAction.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String pass = passwordField.getText().toString();
            
            if(!email.isEmpty() && !pass.isEmpty()) {
                authViewModel.login(email, pass);
            } else {
                Toast.makeText(this, R.string.err_empty_fields, Toast.LENGTH_SHORT).show();
            }
        });

        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Verifica all'avvio se l'utente ha già una sessione valida salvata (Auto-login)
        authViewModel.checkUserSession();
    }
}
