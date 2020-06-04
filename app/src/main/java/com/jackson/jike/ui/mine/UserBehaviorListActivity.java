package com.jackson.jike.ui.mine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.jackson.common.util.StatusBar;
import com.jackson.jike.R;
import com.jackson.jike.databinding.ActivityUserBehaviorBinding;

public class UserBehaviorListActivity extends AppCompatActivity {
    public static final int BEHAVIOR_FAVORITE = 0;
    public static final int BEHAVIOR_HISTORY = 1;

    public static final String KEY_BEHAVIOR = "behavior";

    public static void startBehaviorListActivity(Context context, int behavior) {
        Intent intent = new Intent(context, UserBehaviorListActivity.class);
        intent.putExtra(KEY_BEHAVIOR, behavior);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        int extra = getIntent().getIntExtra(KEY_BEHAVIOR, BEHAVIOR_FAVORITE);
        ActivityUserBehaviorBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_user_behavior);

        binding.actionClose.setOnClickListener(v ->
                finish()
        );
        binding.title.setText(extra == 0 ? getString(R.string.favorite_list) : getString(R.string.history_list));

        UserBehaviorListFragment fragment = UserBehaviorListFragment.newInstance(extra);
        getSupportFragmentManager().beginTransaction().add(R.id.contanier, fragment, "userBehavior").commit();
    }
}
