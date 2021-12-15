package com.sszabo.life_tok.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sszabo.life_tok.MainViewModel;
import com.sszabo.life_tok.databinding.FragmentSettingsBinding;
import com.sszabo.life_tok.model.Event;
import com.sszabo.life_tok.model.User;
import com.sszabo.life_tok.util.FirebaseUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsFragment extends Fragment {

    private static final String TAG = SettingsViewModel.class.getSimpleName();

    private SettingsViewModel settingsViewModel;
    private FragmentSettingsBinding binding;

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private Uri picUri;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String username;
    private String password;
    private String pictureUrl;

    private ImageView profPic;
    private EditText txtFirstName;
    private EditText txtLastName;
    private EditText txtEmail;
    private EditText txtPhone;
    private EditText txtAddress;
    private EditText txtUsername;
    private EditText txtPassword;
    private EditText txtConfirmPassword;
    private Button btnUpdateProfile;
    private Button btnDeleteProfile;
    private ProgressBar progressBarSettings;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // required for top back button functionality
        setHasOptionsMenu(true);

        picUri = null;
        pictureUrl = "";

        profPic = binding.imageViewProfileSettings;
        txtFirstName = binding.txtFirstNameSettings;
        txtLastName = binding.txtLastNameSettings;
        txtEmail = binding.txtEmailSettings;
        txtPhone = binding.txtPhoneSettings;
        txtAddress = binding.txtAddressSettings;
        txtUsername = binding.txtUsernameSettings;
        txtPassword = binding.txtPasswordSettings;

        txtConfirmPassword = binding.txtConfirmPasswordSettings;
        btnUpdateProfile = binding.btnUpdateProfileSettings;
        btnDeleteProfile = binding.btnDeleteProfileSettings;
        progressBarSettings = binding.progressBarSettings;
        progressBarSettings.setVisibility(View.INVISIBLE);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getData() != null) {
                    settingsViewModel.setProfPicUpdated(true);
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

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        getAndSetUserData();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUIListeners() {
        User user = MainViewModel.getCurrentUser();
        String uid = user.getId();

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!setAndVerifyFields()) {
                    Toast.makeText(getContext(), "Invalid fields!", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBarSettings.setVisibility(View.VISIBLE);

                // user data update tasks list
                ArrayList<Task<Void>> updateTasks = new ArrayList<>();

                // delete and upload profile picture to storage
                if (settingsViewModel.isProfPicUpdated()) {
                    String url = user.getPictureUrl();
                    if (url != null && !url.isEmpty()) {
                        FirebaseUtil.getStorage()
                                .getReferenceFromUrl(url)
                                .delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "onComplete: Old profile picture deleted");
                                        } else {
                                            Log.d(TAG, "onComplete: Could not delete old profile picture");
                                        }
                                    }
                                });
                    }
                    // upload new profile picture
                    String uploadPath = "userMedia/" + uid + "/" + picUri.getLastPathSegment();
                    StorageReference stoRef = FirebaseUtil.getStorage().getReference(uploadPath);

                    stoRef.putFile(picUri)
                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        pictureUrl = stoRef.toString();

                                        updateTasks.add(FirebaseUtil.getFirestore()
                                                .collection("users")
                                                .document(uid)
                                                .update("pictureUrl", pictureUrl));
                                        Log.d(TAG, "onComplete: Uploaded profile picture");
                                    } else {
                                        Toast.makeText(getContext(), "Could not upload profile picture", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

                updateTasks.add(FirebaseUtil.getFirestore()
                        .collection("users")
                        .document(uid)
                        .update("firstName", firstName));

                updateTasks.add(FirebaseUtil.getFirestore()
                        .collection("users")
                        .document(uid)
                        .update("lastName", lastName));

                updateTasks.add(FirebaseUtil.getFirestore()
                        .collection("users")
                        .document(uid)
                        .update("username", username));

                updateTasks.add(FirebaseUtil.getFirestore()
                        .collection("users")
                        .document(uid)
                        .update("email", email));

                updateTasks.add(FirebaseUtil.getFirestore()
                        .collection("users")
                        .document(uid)
                        .update("address", address));

                updateTasks.add(FirebaseUtil.getFirestore()
                        .collection("users")
                        .document(uid)
                        .update("phoneNo", phone));

                updateTasks.add(FirebaseUtil.getAuth().getCurrentUser().updateEmail(email));
                updateTasks.add(FirebaseUtil.getAuth().getCurrentUser().updatePassword(password));

                UserProfileChangeRequest updates = new UserProfileChangeRequest
                        .Builder()
                        .setDisplayName(username)
                        .build();
                updateTasks.add(FirebaseUtil.getAuth().getCurrentUser().updateProfile(updates));

                Tasks.whenAllComplete(updateTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Updated profile", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Could not profile", Toast.LENGTH_SHORT).show();
                        }
                        progressBarSettings.setVisibility(View.INVISIBLE);
                        settingsViewModel.setProfPicUpdated(false);
                        getAndSetUserData();
                    }
                });
            }
        });

        btnDeleteProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete followers & following IDs from array of other users
                CollectionReference userRef = FirebaseUtil.getFirestore().collection("users");
                ArrayList<Task<QuerySnapshot>> deleteTasks = new ArrayList<>();
                deleteTasks.add(userRef.whereArrayContains("followers", uid).get());
                deleteTasks.add(userRef.whereArrayContains("following", uid).get());

                Tasks.whenAllComplete(deleteTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> task) {
                        if (task.isSuccessful()) {
                            for (Task<?> t : task.getResult()) {
                                for (QueryDocumentSnapshot snapshot : (QuerySnapshot) t.getResult()) {
                                    ArrayList<String> followers = (ArrayList<String>) snapshot.toObject(User.class).getFollowers();
                                    ArrayList<String> following = (ArrayList<String>) snapshot.toObject(User.class).getFollowing();
                                    followers.remove(uid);
                                    following.remove(uid);

                                    userRef.document(snapshot.getId()).update("followers", followers).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getContext(), "Could not delete follower IDs", Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        }
                                    });

                                    userRef.document(snapshot.getId()).update("following", following).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getContext(), "Could not delete following IDs", Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        }
                                    });
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), "Could not get follower/following IDs", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // delete public events
                CollectionReference publicEventRef = FirebaseUtil.getFirestore().collection("publicEvents");
                publicEventRef.whereEqualTo("userId", uid).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                        Event event = snapshot.toObject(Event.class);
                                        deleteSingleMedia(event.getMediaUrl());

                                        publicEventRef.document(snapshot.getId()).delete()
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(), "Could not delete public event", Toast.LENGTH_SHORT).show();
                                                        e.printStackTrace();
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Could not get public events", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                // delete private events of/within user (delete the "events" collection)
                CollectionReference privateEventRef = FirebaseUtil.getFirestore()
                        .collection("users")
                        .document(uid)
                        .collection("events");

                privateEventRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            Event event = snapshot.toObject(Event.class);
                            deleteSingleMedia(event.getMediaUrl());

                            privateEventRef.document(snapshot.getId()).delete()
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getContext(), "Could not delete private event", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });

                // delete user & profile pic
                deleteSingleMedia(user.getPictureUrl());
                FirebaseUtil.getFirestore()
                        .collection("users")
                        .document(uid)
                        .delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: Deleted user");
                                } else {
                                    Toast.makeText(getContext(), "Could not delete user", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                // delete auth
                FirebaseUtil.getAuth()
                        .getCurrentUser()
                        .delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: Deleted auth");
                                    FirebaseUtil.getAuth().signOut();
                                } else {
                                    Toast.makeText(getContext(), "Could not delete auth", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
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
     * Deletes a singe piece of media based from Firebase Storage at the provided URL.
     *
     * @param url the location to delete
     */
    private void deleteSingleMedia(String url) {
        if (url == null || url.isEmpty()) {
            Log.d(TAG, "deleteSingleMedia: media URL does not exist");
            return;
        }

        FirebaseUtil.getStorage()
                .getReferenceFromUrl(url)
                .delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Could not delete media", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Firebase Storage does not allow for folder to be deleted... Must be done 1 by 1
    private void deleteAllUserMedia(String uid) {
        FirebaseUtil.getStorage()
                .getReference("userMedia/" + uid)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Deleted media");
                        } else {
                            Toast.makeText(getContext(), "Could not delete media", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getAndSetUserData() {
        User user = MainViewModel.getCurrentUser();

        txtFirstName.setText(user.getFirstName());
        txtLastName.setText(user.getLastName());
        txtEmail.setText(user.getEmail());
        txtPhone.setText(user.getPhoneNo());
        txtAddress.setText(user.getAddress());
        txtUsername.setText(user.getUsername());

        // get profile picture
        String url = user.getPictureUrl();
        if (url == null || url.isEmpty() || settingsViewModel.isProfPicUpdated()) {
            return;
        }

        final long TEN_MEGABYTE = 10 * 1024 * 1024;
        StorageReference ref = FirebaseUtil.getStorage().getReferenceFromUrl(url);
        ref.getBytes(TEN_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                if (task.isSuccessful()) {
                    File temp = null;
                    try {
                        // write to temporary file
                        File outputDir = getContext().getCacheDir();
                        temp = File.createTempFile(user.getId(), ".jpg", outputDir);
                        FileOutputStream fos = new FileOutputStream(temp);
                        fos.write(task.getResult());
                        temp.deleteOnExit();
                    } catch (IOException | NullPointerException e) {
                        e.printStackTrace();
                    }

                    if (temp != null) {
                        // display picture
                        if (!settingsViewModel.isProfPicUpdated()) {
                            profPic.setImageURI(Uri.fromFile(temp));
                            profPic.setScaleX(1);
                            profPic.setScaleY(1);
                        }
                    } else {
                        Toast.makeText(getContext(), "Temporary profile picture save failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Could not get profile picture", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
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
