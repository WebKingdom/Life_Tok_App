package com.sszabo.life_tok;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.sszabo.life_tok.model.Event;
import com.sszabo.life_tok.model.User;
import com.sszabo.life_tok.util.FirebaseUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * View Model class for the Main Activity.
 */
public class MainViewModel extends ViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();

    // TODO create hash table map of temp files stored so don't always have to download media to temp file
    // TODO implement some form of cache throughout the app?
    private Hashtable<Integer, File> hashtable;

    private static User currentUser;

    private DocumentReference userDocRef;

    private FirebaseUser currentFirebaseUser;

    // Used for checking the outcome of events
    private boolean success;

    /**
     * Constructor for the main view model. Initializes all instance variables.
     */
    public MainViewModel() {
        currentUser = null;
        currentFirebaseUser = null;
        userDocRef = null;
        success = true;
    }

    /**
     * Adds snapshot listeners to a document reference. Specifically, is adds a snapshot listener
     * to the current user's document.
     * @return true if successfully added, false otherwise
     */
    public boolean addSnapshotListeners() {
        success = true;
        currentFirebaseUser = FirebaseUtil.getAuth().getCurrentUser();
        userDocRef = FirebaseUtil.getFirestore().collection("users").document(currentFirebaseUser.getUid());

        // listen to Firestore user data changes
        userDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "onEvent: Listen failed", error);
                    return;
                }

                String source = (snapshot != null && snapshot.getMetadata().hasPendingWrites()) ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "onEvent: " + source + " data: " + snapshot.getData());

                    try {
                        String username = snapshot.get("username").toString();
                        String email = snapshot.get("email").toString();

                        updateUserDataAuth(username, email);

                        // update user
                        currentUser = new User(snapshot.get("id").toString(),
                                snapshot.get("firstName").toString(),
                                snapshot.get("lastName").toString(),
                                username,
                                email,
                                snapshot.get("address").toString(),
                                snapshot.get("phoneNo").toString(),
                                snapshot.get("pictureUrl").toString(),
                                (List<String>) snapshot.get("publicEventIds"),
                                (List<String>) snapshot.get("following"),
                                (List<String>) snapshot.get("followers"));
                    } catch (NullPointerException e) {
                        Log.d(TAG, "onEvent: ");
                        e.printStackTrace();
                        success = false;
                    }
                } else {
                    Log.d(TAG, "onEvent: " + source + " data: null");
                }
            }
        });

        return success;
    }

    /**
     * Updates the data in the authentication repository if the data in Firestore changed
     * @param username of the user
     * @param email of the user
     */
    private void updateUserDataAuth(String username, String email) {
        FirebaseUser usr = FirebaseUtil.getAuth().getCurrentUser();

        if (!usr.getDisplayName().equals(username)) {
            // update username in authentication db
            UserProfileChangeRequest updates = new UserProfileChangeRequest
                    .Builder()
                    .setDisplayName(username)
                    .build();

            usr.updateProfile(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: Updated user profile username: " + username);
                    } else {
                        Log.d(TAG, "onComplete: Failed to update profile username: " + username);
                    }
                }
            });
        }

        if (!usr.getEmail().equals(email)) {
            usr.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: Updated user profile email: " + email);
                    } else {
                        Log.d(TAG, "onComplete: Failed to update profile email: " + email);
                    }
                }
            });
        }
    }

    /**
     * Gets the current User object
     * @return User object
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the current User object
     * @param user object to be set
     */
    public static void setCurrentUser(User user) {
        MainViewModel.currentUser = user;
    }

    /**
     * Sets the current Firebase user
     * @param user Firebase user to be set
     */
    public void setCurrentFirebaseUser(FirebaseUser user) {
        this.currentFirebaseUser = user;
    }
}
