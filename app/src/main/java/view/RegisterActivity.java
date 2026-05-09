package view;

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

public class RegisterActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private EditText emailField, passwordField, nicknameField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        emailField = findViewById(R.id.editEmail);
        passwordField = findViewById(R.id.editPassword);
        nicknameField = findViewById(R.id.editNickname);
        Button btnRegisterAction = findViewById(R.id.btnRegisterAction);
        Button btnBackToLogin = findViewById(R.id.btnBackToLogin);

        authViewModel.getAuthStatus().observe(this, status -> {
            if ("REG_SUCCESS".equals(status)) {
                Toast.makeText(this, R.string.msg_registration_success, Toast.LENGTH_SHORT).show();
                finish();
            } else if ("ERR_EMAIL_IN_USE".equals(status)) {
                Toast.makeText(this, R.string.err_email_already_in_use, Toast.LENGTH_LONG).show();
            } else if ("ERR_WEAK_PASSWORD".equals(status)) {
                Toast.makeText(this, R.string.err_weak_password, Toast.LENGTH_LONG).show();
            } else if ("ERR_REG_FAILED".equals(status)) {
                Toast.makeText(this, R.string.err_registration_failed, Toast.LENGTH_LONG).show();
            } else if (status != null && !Objects.equals(status, "LOGIN_SUCCESS")) {
                Toast.makeText(this, status, Toast.LENGTH_LONG).show();
            }
        });

        btnRegisterAction.setOnClickListener(v -> {
            String email = emailField.getText().toString();
            String pass = passwordField.getText().toString();
            String nickname = nicknameField.getText().toString();

            if (!email.isEmpty() && !pass.isEmpty() && !nickname.isEmpty()) {
                authViewModel.register(email, pass, nickname);
            } else {
                Toast.makeText(this, R.string.err_empty_fields, Toast.LENGTH_SHORT).show();
            }
        });

        btnBackToLogin.setOnClickListener(v -> finish());
    }
}
