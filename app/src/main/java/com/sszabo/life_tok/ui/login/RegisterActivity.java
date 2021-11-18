package com.sszabo.life_tok.ui.login;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.model.User;
import com.sszabo.life_tok.util.FirebaseUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String username;
    private String password;

    private EditText txtFirstName;
    private EditText txtLastName;
    private EditText txtEmail;
    private EditText txtPhone;
    private EditText txtAddress;
    private EditText txtUsername;
    private EditText txtPassword;
    private EditText txtConfirmPassword;
    private AppCompatButton btnCreateAccount;
    private ProgressBar progressBarRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtFirstName = findViewById(R.id.txt_firstName);
        txtLastName = findViewById(R.id.txt_lastName);
        txtEmail = findViewById(R.id.txt_email);
        txtPhone = findViewById(R.id.txt_phone);
        txtAddress = findViewById(R.id.txt_address);
        txtUsername = findViewById(R.id.txt_username);
        txtPassword = findViewById(R.id.txt_password);
        txtConfirmPassword = findViewById(R.id.txt_confirm_password);
        btnCreateAccount = findViewById(R.id.btn_create_account);
        progressBarRegister = findViewById(R.id.progress_bar);
        progressBarRegister.setVisibility(View.INVISIBLE);

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!setAndVerifyFields()) {
                    Toast.makeText(RegisterActivity.this, "Invalid fields!", Toast.LENGTH_LONG).show();
                    return;
                }

                progressBarRegister.setVisibility(View.VISIBLE);

                // create account on both databases (auth and Firestore)
                FirebaseAuth fAuth = FirebaseUtil.getAuth();
                fAuth.createUserWithEmailAndPassword(
                        email,
                        password
                ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // check if user created successfully
                        if (task.isSuccessful()) {
                            // store additional user fields in Firebase
                            FirebaseUser currentUser = fAuth.getCurrentUser();
                            Log.d(TAG, "onComplete: User auth added with ID: " + currentUser.getUid());

                            User user = new User(
                                    currentUser.getUid(),
                                    firstName,
                                    lastName,
                                    username,
                                    email,
                                    address,
                                    phone,
                                    new ArrayList<>(0),
                                    new ArrayList<>(0)
                            );

                            // update username of in authentication db
                            UserProfileChangeRequest updates = new UserProfileChangeRequest
                                    .Builder()
                                    .setDisplayName(username)
                                    .build();
                            currentUser.updateProfile(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        Log.d(TAG, "onComplete: User profile username now: " + username);
                                }
                            });

                            // Add the user to Firestore db
                            FirebaseUtil.getFirestore().collection("users")
                                    .document(user.getId())
                                    .set(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "Success",
                                                        Toast.LENGTH_SHORT).show();
                                                Log.d(TAG, "onSuccess: User added with ID: " + user.getId() +
                                                        " result: " + task.getResult());
                                            } else {
                                                Toast.makeText(RegisterActivity.this,
                                                        "Failed to user add to Firestore",
                                                        Toast.LENGTH_SHORT).show();
                                                Log.d(TAG, "onFailure: Failed to add user to Firestore with ID: "
                                                        + user.getId());
                                                task.getException().printStackTrace();
                                            }
                                        }
                                    });

                            progressBarRegister.setVisibility(View.INVISIBLE);
                            finish();
                        } else {
                            progressBarRegister.setVisibility(View.INVISIBLE);
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    /**
     * Sets and verifies all user registration fields
     *
     * @return
     */
    private boolean setAndVerifyFields() {
        boolean valid = true;

        firstName = txtFirstName.getText().toString();
        lastName = txtLastName.getText().toString();
        email = txtEmail.getText().toString();
        phone = txtPhone.getText().toString();
        address = txtAddress.getText().toString();
        username = txtUsername.getText().toString();
        password = txtPassword.getText().toString();

        if (firstName.isEmpty()) {
            valid = false;
            txtFirstName.setError("Required field");
        }

        if (lastName.isEmpty()) {
            valid = false;
            txtLastName.setError("Required field");
        }

        if (email.isEmpty()) {
            valid = false;
            txtEmail.setError("Required field");
        } else if (!isValidEmail(email)) {
            valid = false;
            txtEmail.setError("Invalid Email format");
        }

        if (phone.isEmpty()) {
            valid = false;
            txtPhone.setError("Required field");
        }

        if (address.isEmpty()) {
            valid = false;
            txtAddress.setError("Required field");
        }

        if (username.isEmpty()) {
            valid = false;
            txtUsername.setError("Required field");
        } else if (!isValidUsername(username)) {
            valid = false;
            txtUsername.setError("Invalid username format");
        }

        if (password.isEmpty()) {
            valid = false;
            txtPassword.setError("Required field");
        } else if (!isValidPassword(password)) {
            valid = false;
            txtPassword.setError("Password length must be greater than 6 characters and not contain whitespace");
        }

        if (txtConfirmPassword.getText().toString().isEmpty()) {
            valid = false;
            txtConfirmPassword.setError("Required field");
        } else if (!txtConfirmPassword.getText().toString().equals(txtPassword.getText().toString())) {
            valid = false;
            txtConfirmPassword.setError("Passwords do not match");
        }

        return valid;
    }

    /**
     * Validates user email with regex
     *
     * @param target char sequence to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(CharSequence target) {
        final String EMAIL_PATTERN = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(target);

        return matcher.matches();
    }

    /**
     * Validates user password with regex
     *
     * @param target char sequence to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidPassword(CharSequence target) {
        // check regex (no spaces and 6-14 characters long
        final String PASSWORD_PATTERN = "^(?=\\S+$).{5,}$";

        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(target);

        return matcher.matches();
    }

    /**
     * Validates user username with regex
     *
     * @param target char sequence to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidUsername(CharSequence target) {
        final String USERNAME_PATTERN = "^[a-zA-Z0-9]{5,}$";

        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        Matcher matcher = pattern.matcher(target);

        return matcher.matches();
    }
}
