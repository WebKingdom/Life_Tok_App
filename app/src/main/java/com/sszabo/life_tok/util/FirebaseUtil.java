package com.sszabo.life_tok.util;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtil {
    // if debugging use emulator
    private static final boolean debugEmulator = false;

    private static FirebaseFirestore mFirestore;
    private static FirebaseAuth mAuth;
    private static AuthUI mAuthUI;

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

    public static AuthUI getAuthUI() {
        if (mAuthUI == null) {
            mAuthUI = AuthUI.getInstance();

            // Connect to the Firebase Auth emulator when appropriate. The host '10.0.2.2' is a
            // special IP address to let the Android emulator connect to 'localhost'.
            if (debugEmulator) {
                mAuthUI.useEmulator("10.0.2.2", 9099);
            }
        }

        return mAuthUI;
    }
}
