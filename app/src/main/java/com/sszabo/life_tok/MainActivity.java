package com.sszabo.life_tok;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
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
import com.google.firebase.firestore.DocumentReference;
import com.sszabo.life_tok.databinding.ActivityMainBinding;
import com.sszabo.life_tok.ui.login.LoginActivity;
import com.sszabo.life_tok.util.FirebaseUtil;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private MainViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_map, R.id.navigation_create, R.id.navigation_profile)
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

    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseUtil.getAuth().getCurrentUser() == null) {
            Log.d(TAG, "onStart: Starting login");

            launchLoginActivity();
            return;
        }

        // TODO? start listening to Firestore updates
        FirebaseUtil.addAuthListener();

        if (!mViewModel.addSnapshotListeners()) {
            Log.d(TAG, "onStart: Failed to add snapshotListener to 'Users'");
            launchLoginActivity();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LifeTokApplication app = (LifeTokApplication) getApplication();
        app.executorService.shutdown();
    }

    /**
     * Opens login activity clearing all activities before it
     */
    public void launchLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
