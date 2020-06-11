package com.jackson.jike;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jackson.common.util.CommonUtil;
import com.jackson.common.util.StatusBar;
import com.jackson.jike.model.Destination;
import com.jackson.jike.ui.home.HomeFragment;
import com.jackson.jike.ui.login.UserManager;
import com.jackson.jike.util.AppConfig;
import com.jackson.jike.util.NavGraphBuilder;
import com.jackson.jike.util.PlayerManager;
import com.jackson.jike.view.AppBottomBar;

import java.util.HashMap;
import java.util.Map;

/**
 * App 主页 入口
 * <p>
 * 1.底部导航栏 使用AppBottomBar 承载
 * 2.内容区域 使用WindowInsetsNavHostFragment 承载
 * <p>
 * 3.底部导航栏 和 内容区域的 切换联动 使用NavController驱动
 * 4.底部导航栏 按钮个数和 内容区域destination个数。由注解处理器NavProcessor来收集,生成assetsdestination.json。而后我们解析它。
 */
public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private NavController navController;
    private AppBottomBar navView;
    private int curMenuId;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
//                .build();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = NavHostFragment.findNavController(fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(navView, navController);

        NavGraphBuilder.build(navController, this, fragment.getId());

        navView.setOnNavigationItemSelectedListener(this);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        HashMap<String, Destination> destConfig = AppConfig.getDestConfig();
        for (Map.Entry<String, Destination> next : destConfig.entrySet()) {
            Destination value = next.getValue();
            if (value != null && !UserManager.get().isLogin() && value.needLogin && value.id == menuItem.getItemId()) {
                UserManager.get()
                        .login(this)
                        .observe(this, user ->
                                navView.setSelectedItemId(menuItem.getItemId())
                        );
                return false;
            }
        }
        if (curMenuId != menuItem.getItemId()) {
            PlayerManager.get().play();
            curMenuId = menuItem.getItemId();
        }

        NavDestination currentDestination = navController.getCurrentDestination();
        if (currentDestination instanceof FragmentNavigator.Destination) {
            FragmentNavigator.Destination destination = (FragmentNavigator.Destination) currentDestination;
            if (curMenuId == destination.getId()) {
//                InvokeUtils.invokeDeclaredMethod(destination.getClassName(), "refresh");
                Fragment currentFragment = getSupportFragmentManager().getPrimaryNavigationFragment();
                if (currentFragment instanceof HomeFragment) {
                    ((HomeFragment) currentFragment).refresh();
                }
                return false;
            }
        }

        navController.navigate(menuItem.getItemId());
        return !TextUtils.isEmpty(menuItem.getTitle());
    }

    @Override
    public void onBackPressed() {
//        boolean shouldIntercept = false;
//        int homeDestinationId = 0;
//        //获取当前正在显示的fragment
//        Fragment fragment = getSupportFragmentManager().getPrimaryNavigationFragment();
//        //获取当前显示的fragment 的destinationId （FixFragmentNavigator 中使用destinationId作为每个fragment 的tag使用）
//        String tag = fragment.getTag();
//        int currentPageDestId = Integer.parseInt(tag);
//
//        HashMap<String, Destination> config = AppConfig.getDestConfig();
//        Iterator<Map.Entry<String, Destination>> iterator = config.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, Destination> next = iterator.next();
//            Destination destination = next.getValue();
//            if (!destination.asStarter && destination.id == currentPageDestId) {
//                shouldIntercept = true;
//            }
//
//            if (destination.asStarter) {
//                homeDestinationId = destination.id;
//            }
//        }
//
//        if (shouldIntercept && homeDestinationId > 0) {
//            navView.setSelectedItemId(homeDestinationId);
//            return;
//        }
//        super.onBackPressed();

        //当前正在显示的页面destinationId
        int currentPageId = navController.getCurrentDestination().getId();

        //APP页面路导航结构图  首页的destinationId
        int homeDestId = navController.getGraph().getStartDestination();

        //如果当前正在显示的页面不是首页，而我们点击了返回键，则拦截。
        if (currentPageId != homeDestId) {
            navView.setSelectedItemId(homeDestId);
            return;
        }
        long endTime = System.currentTimeMillis();
        if (endTime - startTime > 2000) {
            CommonUtil.showToast("再按一次退出");
            startTime = endTime;
            return;
        }
        //否则 finish，此处不宜调用onBackPressed。因为navigation会操作回退栈,切换到之前显示的页面。
        PlayerManager.get().stop();
        finish();
    }
}
