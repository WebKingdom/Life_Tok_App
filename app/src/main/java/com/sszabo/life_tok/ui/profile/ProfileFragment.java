package com.sszabo.life_tok.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sszabo.life_tok.R;
import com.sszabo.life_tok.adapter.ProfileAdapter;
import com.sszabo.life_tok.databinding.FragmentProfileBinding;
import com.sszabo.life_tok.util.FirebaseUtil;

public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private ProfileViewModel profileViewModel;
    private FragmentProfileBinding binding;

    // TODO create adapter for recyclerview and
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setHasOptionsMenu(true);

        // look up recycler view in profile
        RecyclerView rvProfile = (RecyclerView) root.findViewById(R.id.recyclerViewProfile);
        // TODO pass in User argument when creating adapter?
        ProfileAdapter adapter = new ProfileAdapter();
        // attach adapter to recycler view to populate items
        rvProfile.setAdapter(adapter);

        rvProfile.setLayoutManager(new LinearLayoutManager(this.getContext()));

//        final TextView textView = rvProfile.findViewById(R.id.txt_username);
//        profileViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_nav_profile_to_nav_settings);
                return true;
            case R.id.action_logout:
                FirebaseUtil.getAuth().signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
