package com.jackson.jike.ui.home;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.jackson.common.util.KLog;
import com.jackson.jike.exoplayer.PageListPlayDetector;
import com.jackson.jike.exoplayer.PageListPlayManager;
import com.jackson.jike.model.Feed;
import com.jackson.jike.ui.AbsListFragment;
import com.jackson.jike.ui.MutablePageKeyedDataSource;
import com.jackson.nav_annotation.FragmentDestination;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.List;

/**
 * Copyright (C), 2015-2020
 * FileName: HomeFragment
 * Author: Luo
 * Date: 2020/3/26 22:47
 * Description:首页页面
 */
@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
public class HomeFragment extends AbsListFragment<Feed, HomeViewModel> {

    private String feedType;
    private PageListPlayDetector mPlayDetector;
    private boolean shouldPause = true;

    public static HomeFragment newInstance(String feedType) {
        Bundle args = new Bundle();
        args.putString("feedType", feedType);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void refresh() {
        KLog.i("refresh  feedType = " + feedType);
        mRefreshLayout.autoRefresh();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mViewModel.getCacheLiveData().observe(this, feeds -> submitList(feeds));
        mPlayDetector = new PageListPlayDetector(this, mRecyclerView);
        mViewModel.setFeedType(feedType);
    }


    @Override
    public PagedListAdapter getAdapter() {
        feedType = getArguments() == null ? "all" : getArguments().getString("feedType");
        return new FeedAdapter(getContext(), feedType) {
            @Override
            public void onViewAttachedToWindow2(@NonNull ViewHolder holder) {
                if (holder.isVideoItem()) {
                    mPlayDetector.addTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onViewDetachedFromWindow2(@NonNull ViewHolder holder) {
                mPlayDetector.removeTarget(holder.getListPlayerView());
            }

            @Override
            public void onStartFeedDetailActivity(Feed feed) {
                boolean isVideo = feed.itemType == Feed.TYPE_VIDEO;
                shouldPause = !isVideo;
            }

            @Override
            public void onCurrentListChanged(@Nullable PagedList<Feed> previousList, @Nullable PagedList<Feed> currentList) {
                //避免插入数据时新插入的数据在刷新成功后显示在屏幕外面
                //这个方法是在我们每提交一次 pagelist对象到adapter 就会触发一次
                //每调用一次 adpater.submitlist
                if (previousList != null && currentList != null) {
                    if (!currentList.containsAll(previousList)) {
                        mRecyclerView.scrollToPosition(0);
                    }
                }
            }
        };
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        final PagedList<Feed> currentList = adapter.getCurrentList();
        if (currentList == null || currentList.size() <= 0) {
            finishRefresh(false);
            return;
        }
        Feed feed = currentList.get(adapter.getItemCount() - 1);
        mViewModel.loadAfter(feed.id, new ItemKeyedDataSource.LoadCallback<Feed>() {
            @Override
            public void onResult(@NonNull List<Feed> data) {
                PagedList.Config config = adapter.getCurrentList().getConfig();
                if (data.size() > 0) {
                    //这里 咱们手动接管 分页数据加载的时候 使用MutableItemKeyedDataSource也是可以的。
                    //由于当且仅当 paging不再帮我们分页的时候，我们才会接管。所以 就不需要ViewModel中创建的DataSource继续工作了，所以使用
                    //MutablePageKeyedDataSource也是可以的
                    MutablePageKeyedDataSource dataSource = new MutablePageKeyedDataSource();
                    dataSource.data.addAll(currentList);
                    dataSource.data.addAll(data);
                    PagedList pagedList = dataSource.buildNewPagedList(config);
                    submitList(pagedList);
                }
            }
        });
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        //invalidate 之后Paging会重新创建一个DataSource 重新调用它的loadInitial方法加载初始化数据
        //详情见：LivePagedListBuilder#compute方法
        mViewModel.getDataSource().invalidate();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            mPlayDetector.onPause();
        } else {
            mPlayDetector.onResume();
        }
    }

    @Override
    public void onPause() {
        //如果是跳转到详情页,咱们就不需要 暂停视频播放了
        //如果是前后台切换 或者去别的页面了 都是需要暂停视频播放的
        if (shouldPause) {
            mPlayDetector.onPause();
        }
        KLog.i("onPause: feedType:" + feedType);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        shouldPause = true;
        //由于沙发Tab的几个子页面 复用了HomeFragment。
        //我们需要判断下 当前页面 它是否有ParentFragment.
        //当且仅当 它和它的ParentFragment均可见的时候，才能恢复视频播放
        if (getParentFragment() != null) {
            if (getParentFragment().isVisible() && isVisible()) {
                KLog.i("onResume: feedType:" + feedType);
                mPlayDetector.onResume();
            }
        } else {
            if (isVisible()) {
                KLog.i("onResume: feedType:" + feedType);
                mPlayDetector.onResume();
            }
        }
    }


    @Override
    public void onDestroy() {
        //记得销毁
        PageListPlayManager.release(feedType);
        super.onDestroy();
    }
}