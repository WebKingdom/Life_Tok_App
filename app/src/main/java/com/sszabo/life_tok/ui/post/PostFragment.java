package com.sszabo.life_tok.ui.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.sszabo.life_tok.R;
import com.sszabo.life_tok.databinding.FragmentPostBinding;

public class PostFragment extends Fragment {
    private static final String TAG = PostFragment.class.getSimpleName();

    private FragmentPostBinding binding;
    private PostViewModel postViewModel;

    private Button btnPost;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        postViewModel = new PostViewModel();

        binding = FragmentPostBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnPost = binding.btnPost;

        setListeners();

        return root;
    }

    private void setListeners() {
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(PostFragment.this)
                        .navigate(R.id.action_navigation_post_to_navigation_create,
                                null,
                                new NavOptions.Builder()
                                        .setEnterAnim(android.R.animator.fade_in)
                                        .setExitAnim(android.R.animator.fade_out)
                                        .build());
            }
        });
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
