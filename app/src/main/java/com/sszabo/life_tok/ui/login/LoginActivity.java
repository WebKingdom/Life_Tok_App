package com.sszabo.life_tok.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.util.FirebaseUtil;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private String email;
    private String password;

    private EditText txtEmail;
    private EditText txtPassword;
    private CheckBox chkShowPassword;
    private AppCompatButton btnLogin;
    private AppCompatButton btnSignUp;
    private ProgressBar progressBarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtEmail = findViewById(R.id.txt_email);
        txtPassword = findViewById(R.id.txt_password);
        chkShowPassword = findViewById(R.id.checkbox_show_pass);

        btnLogin = findViewById(R.id.btn_login);
        btnSignUp = findViewById(R.id.btn_sign_up);
        progressBarLogin = findViewById(R.id.progress_bar);
        progressBarLogin.setVisibility(View.INVISIBLE);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!setAndVerifyFields()) {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_LONG).show();
                    return;
                }

                // Perform authentication login
                progressBarLogin.setVisibility(View.VISIBLE);

                FirebaseUtil.getAuth().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBarLogin.setVisibility(View.INVISIBLE);

                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Logging in",
                                            Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "onComplete: Login success");
                                    setResult(RESULT_OK);
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Invalid credentials",
                                            Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "onComplete: Login failed");
                                }
                            }
                        });
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Starting Register Activity.");

                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
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
     * Verifies all User registration fields
     *
     * @return
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
