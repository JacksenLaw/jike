package com.jackson.jike.ui.sofa;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jackson.jike.databinding.FragmentSofaBinding;
import com.jackson.jike.model.SofaTab;
import com.jackson.jike.ui.home.HomeFragment;
import com.jackson.jike.util.AppConfig;
import com.jackson.common.util.KLog;
import com.jackson.nav_annotation.FragmentDestination;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C), 2015-2020
 * FileName: SofaFragment
 * Author: Luo
 * Date: 2020/3/26 22:47
 * Description:沙发页面
 */
@FragmentDestination(pageUrl = "main/tabs/sofa")
public class SofaFragment extends Fragment {

    private FragmentSofaBinding mBinding;
    protected ViewPager2 viewPager;
    private TabLayout tabLayout;
    protected SofaTab tabConfig;
    private List<SofaTab.TabsBean> tabs;

    private ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {

        @Override
        public void onPageSelected(int position) {
            int tabCount = tabLayout.getTabCount();
            for (int i = 0; i < tabCount; i++) {
                TabLayout.Tab tabAt = tabLayout.getTabAt(i);
                TextView customView = (TextView) tabAt.getCustomView();
                if (tabAt.getPosition() == position) {
                    customView.setTextSize(tabConfig.activeSize);
                    customView.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    customView.setTextSize(tabConfig.normalSize);
                    customView.setTypeface(Typeface.DEFAULT);
                }
            }

        }
    };
    private TabLayoutMediator mediator;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentSofaBinding.inflate(inflater, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewPager = mBinding.viewPager;
        tabLayout = mBinding.tabLayout;

        tabConfig = getTabConfig();
        tabs = new ArrayList<>();
        for (SofaTab.TabsBean tab : tabConfig.tabs) {
            if (tab.enable) {
                tabs.add(tab);
            }
        }

        //禁止预加载
        viewPager.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        //viewPager2默认只有一种类型的Adapter。FragmentStateAdapter
        //并且在页面切换的时候 不会调用子Fragment的setUserVisibleHint ，取而代之的是onPause(),onResume()、
        viewPager.setAdapter(new FragmentStateAdapter(getChildFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return getTabFragment(position);
            }

            @Override
            public int getItemCount() {
                return tabs.size();
            }
        });

        tabLayout.setTabGravity(tabConfig.tabGravity);

        //TabView与ViewPager联动
        //viewPager2 就不能和再用TabLayout.setUpWithViewPager()了
        //取而代之的是TabLayoutMediator。我们可以在onConfigureTab()方法的回调里面 做tab标签的配置

        //其中autoRefresh的意思是:如果viewPager2 中child的数量发生了变化，也即我们调用了adapter#notifyItemChanged()前后getItemCount不同。
        //要不要 重新刷野tabLayout的tab标签。视情况而定,像咱们sofaFragment的tab数量一旦固定了是不会变的，传true/false  都问题不大
        mediator = new TabLayoutMediator(tabLayout, viewPager, false, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                //在这里配置每个tab的行为属性
                tab.setCustomView(makeTabView(position));
            }

            private View makeTabView(int position) {
                TextView tabView = new TextView(getContext());
                int[][] states = new int[2][];
                states[0] = new int[]{android.R.attr.state_selected};
                states[1] = new int[]{};

                int[] colors = new int[]{Color.parseColor(tabConfig.activeColor), Color.parseColor(tabConfig.normalColor)};
                ColorStateList stateList = new ColorStateList(states, colors);
                tabView.setTextColor(stateList);
                tabView.setText(tabs.get(position).title);
                tabView.setTextSize(tabConfig.normalSize);
                return tabView;
            }
        });
        mediator.attach();

        viewPager.registerOnPageChangeCallback(onPageChangeCallback);
        //切换到默认选择项,那当然要等待初始化完成之后才有效
        viewPager.post(() -> viewPager.setCurrentItem(tabConfig.select));
    }

    @NotNull
    protected Fragment getTabFragment(int position) {
        return HomeFragment.newInstance(tabs.get(position).tag);
    }


    protected SofaTab getTabConfig() {
        return AppConfig.getSofaTabConfig();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment.isAdded() && fragment.isVisible()) {
                fragment.onHiddenChanged(hidden);
                break;
            }
        }
    }

    @Override
    public void onPause() {
        KLog.i("onPause");
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        KLog.i("onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
        mediator.detach();
    }
}