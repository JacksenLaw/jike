package com.jackson.jike.ui.mine;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.jackson.common.util.StatusBar;
import com.jackson.jike.R;
import com.jackson.jike.databinding.FragmentMineBinding;
import com.jackson.jike.model.User;
import com.jackson.jike.ui.login.UserManager;
import com.jackson.nav_annotation.FragmentDestination;

@FragmentDestination(pageUrl = "main/tabs/mine", needLogin = true)
public class MineFragment extends Fragment {

    private FragmentMineBinding mBinding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentMineBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        User user = UserManager.get().getUser();
        mBinding.setUser(user);

        UserManager.get().refresh().observe(this, newUser -> {
            if (newUser != null) {
                mBinding.setUser(newUser);
            }
        });

        mBinding.actionLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.fragment_my_logout))
                    .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            UserManager.get().logout();
                            getActivity().onBackPressed();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null).create().show();
        });

        mBinding.goDetail.setOnClickListener(v ->
                ProfileActivity.startProfileActivity(getContext(), ProfileActivity.TAB_TYPE_ALL)
        );
        mBinding.userFeed.setOnClickListener(v ->
                ProfileActivity.startProfileActivity(getContext(), ProfileActivity.TAB_TYPE_FEED)
        );
        mBinding.userComment.setOnClickListener(v ->
                ProfileActivity.startProfileActivity(getContext(), ProfileActivity.TAB_TYPE_COMMENT)
        );
        mBinding.userFavorite.setOnClickListener(v ->
                UserBehaviorListActivity.startBehaviorListActivity(getContext(), UserBehaviorListActivity.BEHAVIOR_FAVORITE)
        );
        mBinding.userHistory.setOnClickListener(v ->
                UserBehaviorListActivity.startBehaviorListActivity(getContext(), UserBehaviorListActivity.BEHAVIOR_HISTORY)
        );
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBar.fitSystemBar(getActivity(), true, Color.TRANSPARENT);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        StatusBar.lightStatusBar(getActivity(), !hidden);
    }
}