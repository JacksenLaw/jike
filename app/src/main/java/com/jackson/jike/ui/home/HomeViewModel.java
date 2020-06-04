package com.jackson.jike.ui.home;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.alibaba.fastjson.TypeReference;
import com.jackson.network.ApiResponse;
import com.jackson.network.ApiService;
import com.jackson.network.JsonCallback;
import com.jackson.network.Request;
import com.jackson.jike.model.Feed;
import com.jackson.jike.ui.AbsViewModel;
import com.jackson.jike.ui.MutablePageKeyedDataSource;
import com.jackson.jike.ui.login.UserManager;
import com.jackson.common.util.KLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeViewModel extends AbsViewModel<Feed> {

    private volatile boolean witchCache = true;
    private MutableLiveData<PagedList<Feed>> cacheLiveData = new MutableLiveData<>();
    //加载更多同步位标记
    private AtomicBoolean loadAfter = new AtomicBoolean(false);
    private String mFeedType;

    @Override
    public DataSource createDataSource() {
        return new FeedDataSource();
    }

    public MutableLiveData<PagedList<Feed>> getCacheLiveData() {
        return cacheLiveData;
    }

    public void setFeedType(String feedType) {
        mFeedType = feedType;
    }

    class FeedDataSource extends ItemKeyedDataSource<Integer, Feed> {
        /**
         * 加载初始化数据的,此方法在子线程中执行
         *
         * @param params
         * @param callback
         */
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            KLog.i("loadInitial: ");
            loadData(0, params.requestedLoadSize, callback);
            witchCache = false;
        }

        /**
         * 向后加载分页数据的,此方法在子线程中执行
         *
         * @param params
         * @param callback
         */
        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            KLog.i("loadAfter: ");
            loadData(params.key, params.requestedLoadSize, callback);
        }

        /**
         * 能够向前加载数据的,此方法在子线程中执行
         *
         * @param params
         * @param callback
         */
        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            callback.onResult(Collections.emptyList());
        }

        /**
         * 返回分页时的入参 第几页
         *
         * @param item
         * @return
         */
        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id;
        }
    }

    private void loadData(Integer key, int count, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        if (key > 0) {
            loadAfter.set(true);
        }
        //feeds/queryHotFeedsList
        Request request = ApiService.get("/feeds/queryHotFeedsList")
                .addParam("feedType", mFeedType)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("feedId", key)
                .addParam("pageCount", count)
                .responseType(new TypeReference<ArrayList<Feed>>() {
                }.getType());

        if (witchCache) {
            //请求本地缓存数据
            request.cacheStrategy(Request.CACHE_ONLY);
            request.execute(new JsonCallback<List<Feed>>() {
                @Override
                public void onCacheSuccess(ApiResponse<List<Feed>> response) {
                    KLog.i("onCacheSuccess: " + response.body.size());
                    MutablePageKeyedDataSource dataSource = new MutablePageKeyedDataSource<Feed>();
                    dataSource.data.addAll(response.body);

                    PagedList pagedList = dataSource.buildNewPagedList(config);
                    cacheLiveData.postValue(pagedList);

                    //下面的不可取，否则会报
                    // java.lang.IllegalStateException: callback.onResult already called, cannot call again.
                    //if (response.body != null) {
                    //  callback.onResult(response.body);
                    // }
                }
            });
        }

        try {
            Request netRequest = witchCache ? request.clone() : request;
            //下拉刷新时缓存策略为先访问网络，成功后缓存到本地、 上拉加载时缓存策略为只访问网络
            netRequest.cacheStrategy(key == 0 ? Request.NET_CACHE : Request.NET_ONLY);
            ApiResponse<List<Feed>> response = netRequest.execute();

            List<Feed> data = response.body == null ? Collections.emptyList() : response.body;

            //将数据回传给DataSource
            callback.onResult(data);

            if (key > 0) {
                //通过BoundaryPageData发送数据 告诉UI层 是否应该主动关闭上拉加载分页的动画
                ((MutableLiveData) getBoundaryPageData()).postValue(data.size() > 0);
                loadAfter.set(false);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        KLog.i("loadData: key:" + key);

    }

    public void loadAfter(int id, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        if (loadAfter.get()) {
            callback.onResult(Collections.emptyList());
            return;
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                loadData(id, config.pageSize, callback);
            }
        });
    }

}