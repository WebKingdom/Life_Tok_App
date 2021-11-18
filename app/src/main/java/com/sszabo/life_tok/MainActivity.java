package com.sszabo.life_tok;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
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

import java.util.Collections;
import java.util.concurrent.BlockingDeque;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private MainViewModel mViewModel;
    private ActivityResultLauncher<Intent> loginActivityLauncher;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_map, R.id.navigation_dashboard, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Create authentication listener
        FirebaseUtil.setAuthListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mCurrentUser = FirebaseUtil.getAuth().getCurrentUser();
                if (mCurrentUser != null) {
                    // user is signed in
                    Log.d(TAG, "onAuthStateChanged: User ID: " + mCurrentUser.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged: user == null, starting login for result");
                    // restart login activity
                    onRestart();
                }
            }
        });

        // login activity launcher
        loginActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Log.d(TAG, "onActivityResult: Getting login result");
                        
                        mViewModel.setIsLoggingIn(false);
                        Intent data = result.getData();
                        int res = result.getResultCode();

                        if (res != Activity.RESULT_OK && shouldStartLogin()) {
                            // start login activity again if not logged in (no request codes)
                            Log.d(TAG, "onActivityResult: Bad results code and current user == null");
                            onRestart();
                        }
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (shouldStartLogin()) {
            Log.d(TAG, "onStart: Starting login for result");

            openLoginActivityForResult();
            return;
        }

        // TODO start listening to Firestore updates
        FirebaseUtil.addAuthListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUtil.removeAuthListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUtil.removeAuthListener();
        FirebaseUtil.getAuth().signOut();
        FirebaseUtil.getFirestore().terminate();
    }

    private boolean shouldStartLogin() {
        return (!mViewModel.getIsLoggingIn() && FirebaseUtil.getAuth().getCurrentUser() == null);
    }

    public void openLoginActivityForResult() {
        mViewModel.setIsLoggingIn(true);
        Intent intent = new Intent(this, LoginActivity.class);
        loginActivityLauncher.launch(intent);
    }
}
