package com.jackson.jike.ui.mine;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.jackson.jike.model.Feed;
import com.jackson.jike.ui.AbsViewModel;
import com.jackson.jike.ui.login.UserManager;
import com.jackson.network.ApiResponse;
import com.jackson.network.ApiService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (C), 2015-2020
 * FileName: ProfileListViewModel
 * Author: Luo
 * Date: 2020/6/3 16:01
 * Description:
 */
public class ProfileListViewModel extends AbsViewModel<Feed> {

    private String profileType;

    public void setProfileType(String tabType) {
        this.profileType = tabType;
    }

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    private class DataSource extends ItemKeyedDataSource<Integer, Feed> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            loadData(0, callback);
        }

        private void loadData(Integer key, ItemKeyedDataSource.LoadCallback<Feed> callback) {
            ApiResponse<List<Feed>> response = ApiService.get("/feeds/queryProfileFeeds")
                    .addParam("feedId", key)
                    .addParam("userId", UserManager.get().getUserId())
                    .addParam("pageCount", 10)
                    .addParam("profileType", profileType)
                    .responseType(new TypeReference<ArrayList<Feed>>() {
                    }.getType())
                    .execute();

            List<Feed> result = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(result);

            if (key > 0) {
                //告知UI层 本次分页是否有更多数据被加载回来了,也方便UI层关闭上拉加载的动画
                ((MutableLiveData) getBoundaryPageData()).postValue(result.size() > 0);
            }

        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id;
        }
    }
}
