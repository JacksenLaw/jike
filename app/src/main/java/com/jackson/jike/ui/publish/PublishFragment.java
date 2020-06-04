package com.jackson.jike.ui.publish;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.jackson.jike.R;
import com.jackson.common.util.KLog;

//@FragmentDestination(pageUrl = "main/tabs/publish")
public class PublishFragment extends Fragment {

    private PublishViewModel notificationsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        KLog.i();
        notificationsViewModel =
                ViewModelProviders.of(this).get(PublishViewModel.class);
        View root = inflater.inflate(R.layout.fragment_publish, container, false);
        final TextView textView = root.findViewById(R.id.text_publish);
        notificationsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}