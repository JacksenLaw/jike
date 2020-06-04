package com.jackson.jike.ui.mine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jackson.common.util.StatusBar;
import com.jackson.jike.R;
import com.jackson.jike.databinding.ActivityProfileBinding;
import com.jackson.jike.model.User;
import com.jackson.jike.ui.login.UserManager;

public class ProfileActivity extends AppCompatActivity {

    public static final String KEY_TAB_TYPE = "key_tab_type";

    public static final String TAB_TYPE_ALL = "tab_all";//动态tab
    public static final String TAB_TYPE_FEED = "tab_feed";//帖子tab
    public static final String TAB_TYPE_COMMENT = "tab_comment";//评论tab

    private ActivityProfileBinding mBinding;

    /**
     * @param tabType 需要跳转到哪个tab的标记
     */
    public static void startProfileActivity(Context context, String tabType) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(KEY_TAB_TYPE, tabType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        User user = UserManager.get().getUser();
        mBinding.setUser(user);
        mBinding.actionBack.setOnClickListener(v -> finish());

        String[] tabs = getResources().getStringArray(R.array.tabs_profile);
        TabLayout tabLayout = mBinding.tabLayout;
        ViewPager2 viewPager = mBinding.viewPager;

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return ProfileListFragment.newInstance(getTabTypeByPosition(position));
            }

            private String getTabTypeByPosition(int position) {
                switch (position) {
                    case 0:
                        return TAB_TYPE_ALL;
                    case 1:
                        return TAB_TYPE_FEED;
                    case 2:
                        return TAB_TYPE_COMMENT;
                }
                return TAB_TYPE_ALL;
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        });

        //autoRefresh:当我们调用viewpager的adapter#notifychangged方法的时候，要不要主动的把tablayout的选项卡给移除掉重新配置
        //要在给viewpager设置adapter之后调用
        new TabLayoutMediator(tabLayout, viewPager, false, (tab, position) ->
                tab.setText(tabs[position])
        ).attach();

        int initTabPosition = getInitTabPosition();
        if (initTabPosition != 0) {
            viewPager.post(() -> viewPager.setCurrentItem(initTabPosition, false));
        }

        mBinding.appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            /**
             *
             * @param verticalOffset 滑动距离  负值
             */
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                boolean expand = Math.abs(verticalOffset) < appBarLayout.getTotalScrollRange();//getTotalScrollRange：appBar的最大可滑动范围
                mBinding.setExpand(expand);
            }
        });

    }

    private int getInitTabPosition() {
        String initTab = getIntent().getStringExtra(KEY_TAB_TYPE);
        if (TextUtils.isEmpty(initTab)) initTab = "";

        switch (initTab) {
            case TAB_TYPE_ALL: {
                return 0;
            }
            case TAB_TYPE_FEED: {
                return 1;
            }
            case TAB_TYPE_COMMENT: {
                return 2;
            }
        }
        return 0;
    }
}
