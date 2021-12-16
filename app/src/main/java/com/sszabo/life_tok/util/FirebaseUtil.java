package com.sszabo.life_tok.util;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Utility class for Firebase so the references can be easily accessed.
 */
public class FirebaseUtil {
    // if debugging use emulator
    private static final boolean debugEmulator = false;

    private static FirebaseFirestore mFirestore;
    private static FirebaseStorage mStorage;
    private static FirebaseAuth mAuth;

    private static FirebaseAuth.AuthStateListener mAuthListener;

    /**
     * Gets the Firebase Firestore instance
     *
     * @return the Firestore instance
     */
    public static FirebaseFirestore getFirestore() {
        if (mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();

            // Connect to the Cloud Firestore emulator when appropriate. The host '10.0.2.2' is a
            // special IP address to let the Android emulator connect to 'localhost'.
            if (debugEmulator) {
                mFirestore.useEmulator("10.0.2.2", 8080);
            }
        }

        return mFirestore;
    }

    /**
     * Gets the Firebase Authentication instance
     *
     * @return the Authentication instance
     */
    public static FirebaseAuth getAuth() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();

            // Connect to the Firebase Auth emulator when appropriate. The host '10.0.2.2' is a
            // special IP address to let the Android emulator connect to 'localhost'.
            if (debugEmulator) {
                mAuth.useEmulator("10.0.2.2", 9099);
            }
        }

        return mAuth;
    }

    /**
     * Gets the Firebase Storage instance
     *
     * @return the Storage instance
     */
    public static FirebaseStorage getStorage() {
        if (mStorage == null) {
            mStorage = FirebaseStorage.getInstance();

            if (debugEmulator) {
                mStorage.useEmulator("10.0.2.2", 8080);
            }
        }

        return mStorage;
    }

    /**
     * Gets the authentication state listener stored in this class
     *
     * @return the authentication state listener
     */
    public static FirebaseAuth.AuthStateListener getAuthListener() {
        return mAuthListener;
    }

    /**
     * Sets the authentication state listener for this class
     *
     * @param authListener the authentication stare listener to set for the class
     */
    public static void setAuthListener(FirebaseAuth.AuthStateListener authListener) {
        mAuthListener = authListener;
    }

    /**
     * Adds the authentication state listener to the Firebase authentication
     */
    public static void addAuthListener() {
        mAuth.addAuthStateListener(mAuthListener);
    }

    /**
     * Removes the authentication state listener from the Firebase authentication
     */
    public static void removeAuthListener() {
        if (mAuth != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
