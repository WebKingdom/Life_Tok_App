package com.sszabo.life_tok.ui.settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.sszabo.life_tok.MainViewModel;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.databinding.FragmentSettingsBinding;
import com.sszabo.life_tok.model.User;
import com.sszabo.life_tok.ui.login.RegisterActivity;
import com.sszabo.life_tok.util.FirebaseUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO add delete profile and edit profile picture
public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private FragmentSettingsBinding binding;

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
    private Button btnUpdateProfile;
    private ProgressBar progressBarSettings;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // required for top back button functionality
        setHasOptionsMenu(true);

        txtFirstName = binding.txtFirstNameSettings;
        txtLastName = binding.txtLastNameSettings;
        txtEmail = binding.txtEmailSettings;
        txtPhone = binding.txtPhoneSettings;
        txtAddress = binding.txtAddressSettings;
        txtUsername = binding.txtUsernameSettings;
        txtPassword = binding.txtPasswordSettings;;
        txtConfirmPassword = binding.txtConfirmPasswordSettings;
        btnUpdateProfile = binding.btnUpdateProfileSettings;
        progressBarSettings = binding.progressBarSettings;
        progressBarSettings.setVisibility(View.INVISIBLE);

        setUIListeners();

        getAndSetUserData();

        return root;
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
        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!setAndVerifyFields()) {
                    Toast.makeText(getContext(), "Invalid fields!", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBarSettings.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getAndSetUserData() {
        txtFirstName.setText(MainViewModel.getCurrentUser().getFirstName());
        txtLastName.setText(MainViewModel.getCurrentUser().getLastName());
        txtEmail.setText(MainViewModel.getCurrentUser().getEmail());
        txtPhone.setText(MainViewModel.getCurrentUser().getPhoneNo());
        txtAddress.setText(MainViewModel.getCurrentUser().getAddress());
        txtUsername.setText(MainViewModel.getCurrentUser().getUsername());
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
