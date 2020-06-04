package com.jackson.jike.ui.find;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.jackson.jike.model.SofaTab;
import com.jackson.jike.ui.sofa.SofaFragment;
import com.jackson.jike.util.AppConfig;
import com.jackson.nav_annotation.FragmentDestination;

import org.jetbrains.annotations.NotNull;

/**
 * Copyright (C), 2015-2020
 * FileName: FindFragment
 * Author: Luo
 * Date: 2020/3/26 22:47
 * Description:发现页面
 */
@FragmentDestination(pageUrl = "main/tabs/find")
public class FindFragment extends SofaFragment {

    @NotNull
    @Override
    protected Fragment getTabFragment(int position) {
        return TagListFragment.newInstance(getTabConfig().tabs.get(position).tag);
    }

    @Override
    protected SofaTab getTabConfig() {
        return AppConfig.getFindTabConfig();
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        String tagType = childFragment.getArguments().getString(TagListFragment.KEY_TAG_TYPE);
        if (TextUtils.equals(tagType, "onlyFollow")) {
            ViewModelProviders.of(childFragment).get(TagListViewModel.class)
                    .getSwitchTabLiveData().observe(this, o ->
                    viewPager.setCurrentItem(1)
            );
        }
    }
}