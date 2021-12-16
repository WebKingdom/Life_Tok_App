package com.sszabo.life_tok.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.sszabo.life_tok.MainActivity;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.util.FirebaseUtil;

/**
 * Activity class for the login. Contains all the information/interactions for logging in.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private String email;
    private String password;

    private EditText txtEmail;
    private EditText txtPassword;
    private CheckBox chkShowPassword;
    private Button btnLogin;
    private Button btnSignUp;
    private ProgressBar progressBarLogin;

    /**
     * Creates the activity, called when the activity is starting. Sets up bindings and listeners.
     *
     * @param savedInstanceState saved state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtEmail = findViewById(R.id.txt_email);
        txtPassword = findViewById(R.id.txt_password);
        chkShowPassword = findViewById(R.id.checkbox_show_pass);

        btnLogin = findViewById(R.id.btn_login);
        btnSignUp = findViewById(R.id.btn_sign_up);
        progressBarLogin = findViewById(R.id.progress_bar_login);
        progressBarLogin.setVisibility(View.INVISIBLE);

        setListeners();
    }

    /**
     * Sets listeners for the interactive UI elements.
     */
    private void setListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!setAndVerifyFields()) {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Perform authentication login
                progressBarLogin.setVisibility(View.VISIBLE);
                performSignIn();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Starting Register Activity.");
                launchRegisterActivity();
            }
        });

        chkShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show  or hide password
                if (chkShowPassword.isChecked()) {
                    txtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    txtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
    }

    /**
     * Tries to sign in with email and password, launching main activity if successful
     */
    private void performSignIn() {
        FirebaseUtil.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBarLogin.setVisibility(View.INVISIBLE);

                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Logging in",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onComplete: Login success");
                            launchMainActivity();
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid credentials",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onComplete: Login failed");
                        }
                    }
                });
    }

    /**
     * Launches the register activity
     */
    private void launchRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Launches the main activity with a clear activity stack
     */
    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Verifies and sets all user login fields
     *
     * @return true if all fields are valid, false otherwise
     */
    private boolean setAndVerifyFields() {
        boolean valid = true;

        email = txtEmail.getText().toString();
        password = txtPassword.getText().toString();

        if (email.isEmpty()) {
            valid = false;
            txtEmail.setError("Required field");
        }

        if (password.isEmpty()) {
            valid = false;
            txtPassword.setError("Required field");
        }

        return valid;
    }
}
