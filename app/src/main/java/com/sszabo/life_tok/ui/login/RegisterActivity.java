package com.sszabo.life_tok.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.model.User;
import com.sszabo.life_tok.util.FirebaseUtil;

import java.util.ArrayList;
import java.util.Objects;
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

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private Uri picUri;

    private ImageView profPic;
    private EditText txtFirstName;
    private EditText txtLastName;
    private EditText txtEmail;
    private EditText txtPhone;
    private EditText txtAddress;
    private EditText txtUsername;
    private EditText txtPassword;
    private EditText txtConfirmPassword;
    private Button btnCreateAccount;
    private ProgressBar progressBarRegister;

    private boolean isProfPicSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        isProfPicSet = false;
        profPic = findViewById(R.id.image_view_profile_register);
        txtFirstName = findViewById(R.id.txt_firstName);
        txtLastName = findViewById(R.id.txt_lastName);
        txtEmail = findViewById(R.id.txt_email);
        txtPhone = findViewById(R.id.txt_phone);
        txtAddress = findViewById(R.id.txt_address);
        txtUsername = findViewById(R.id.txt_username);
        txtPassword = findViewById(R.id.txt_password);
        txtConfirmPassword = findViewById(R.id.txt_confirm_password);
        btnCreateAccount = findViewById(R.id.btn_create_account);
        progressBarRegister = findViewById(R.id.progress_bar_register);
        progressBarRegister.setVisibility(View.INVISIBLE);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getData() != null) {
                    isProfPicSet = true;
                    picUri = result.getData().getData();
                    profPic.setImageURI(picUri);
                    profPic.setScaleX(1);
                    profPic.setScaleY(1);
                } else {
                    Log.d(TAG, "onActivityResult: Could not get profile picture Uri");
                }
            }
        });

        setUIListeners();
    }

    /**
     * Sets the listeners for the UI
     */
    private void setUIListeners() {
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!setAndVerifyFields()) {
                    Toast.makeText(RegisterActivity.this, "Invalid fields!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // create account on both databases (auth and Firestore)
                progressBarRegister.setVisibility(View.VISIBLE);
                registerUser();
            }
        });

        profPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // browse gallery for profile pic
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intent);
            }
        });
    }

    /**
     * Registers the user with email and password on Firebase authentication and Firestore DB
     */
    private void registerUser() {
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

                    // upload profile picture
                    String uploadPath = "userMedia/" + currentUser.getUid() + "/" + picUri.getLastPathSegment();
                    StorageReference stoRef = FirebaseUtil.getStorage().getReference(uploadPath);

                    stoRef.putFile(picUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                String pictureUrl = stoRef.toString();
                                Log.d(TAG, "onComplete: Uploaded profile picture");

                                User user = new User(
                                        currentUser.getUid(),
                                        firstName,
                                        lastName,
                                        username,
                                        email,
                                        address,
                                        phone,
                                        pictureUrl,
                                        new ArrayList<>(0),
                                        new ArrayList<>(0),
                                        new ArrayList<>(0)
                                );

                                // update username in authentication db
                                UserProfileChangeRequest updates = new UserProfileChangeRequest
                                        .Builder()
                                        .setDisplayName(username)
                                        .build();
                                currentUser.updateProfile(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "onComplete: Updated user profile username: " + username);
                                        } else {
                                            Log.d(TAG, "onComplete: Failed to update profile username: " + username);
                                        }
                                    }
                                });

                                // Add the user to Firestore db
                                FirebaseUtil.getFirestore().collection("users")
                                        .document(user.getId())
                                        .set(user)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressBarRegister.setVisibility(View.INVISIBLE);

                                                if (task.isSuccessful()) {
                                                    Toast.makeText(RegisterActivity.this, "Success",
                                                            Toast.LENGTH_SHORT).show();
                                                    Log.d(TAG, "onSuccess: User added with ID: " + user.getId() +
                                                            " result: " + task.getResult());
                                                    setResult(RESULT_OK);
                                                    finish();
                                                } else {
                                                    Toast.makeText(RegisterActivity.this,
                                                            "Failed to add user to Firestore",
                                                            Toast.LENGTH_SHORT).show();
                                                    Log.d(TAG, "onFailure: Failed to add user to Firestore with ID: "
                                                            + user.getId());
                                                    Objects.requireNonNull(task.getException()).printStackTrace();
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(RegisterActivity.this,
                                        "Could not upload profile picture", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    progressBarRegister.setVisibility(View.INVISIBLE);
                    Toast.makeText(RegisterActivity.this,
                            "Could not create authentication\n" + task.getException().getMessage() + "\nTry again.",
                            Toast.LENGTH_LONG).show();
                }
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

        if (!isProfPicSet) {
            valid = false;
            Toast.makeText(this, "Please set a profile picture", Toast.LENGTH_SHORT).show();
        }

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
