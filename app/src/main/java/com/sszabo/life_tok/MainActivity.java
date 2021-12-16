package com.sszabo.life_tok;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sszabo.life_tok.databinding.ActivityMainBinding;
import com.sszabo.life_tok.ui.login.LoginActivity;
import com.sszabo.life_tok.util.FirebaseUtil;

import java.util.concurrent.TimeUnit;

/**
 * Activity class for the main activity which is the entire Life Tok app (except for login & register).
 * Contains the navigation and handling of authentication.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private MainViewModel mViewModel;

    /**
     * Creates the activity, called when the activity is starting. Sets up authentication, navigation, and binding.
     *
     * @param savedInstanceState saved state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseUtil.getAuth().getCurrentUser() == null) {
            Log.d(TAG, "onStart: Starting login");

            launchLoginActivity();
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_map, R.id.nav_create, R.id.nav_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Create authentication listener
        FirebaseUtil.setAuthListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser curUser = FirebaseUtil.getAuth().getCurrentUser();
                mViewModel.setCurrentFirebaseUser(curUser);

                if (curUser != null) {
                    // user is signed in
                    Log.d(TAG, "onAuthStateChanged: User ID: " + curUser.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged: user == null, launching login");
                    launchLoginActivity();
                }
            }
        });
    }

    /**
     * Starts the fragment, called when fragment is visible to the user.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // TODO? start listening to Firestore updates
        FirebaseUtil.addAuthListener();

        if (!mViewModel.addSnapshotListeners()) {
            Log.d(TAG, "onStart: Failed to add snapshotListener to 'Users'");
            launchLoginActivity();
        }

        // Initialize the executors and handlers because login/logout destroys main activity
        ((LifeTokApplication) getApplication()).initApp();
    }

    /**
     * Destroys the fragment, called when fragment is no longer in use.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Main Activity Destroyed.");
        LifeTokApplication app = (LifeTokApplication) getApplication();
        if (!app.executorService.isShutdown()) {
            app.executorService.shutdown();
            try {
                if (app.executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    app.executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                app.executorService.shutdownNow();
            }
        }
    }

    /**
     * Launches the login activity, clearing all activities before it
     */
    private void launchLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
