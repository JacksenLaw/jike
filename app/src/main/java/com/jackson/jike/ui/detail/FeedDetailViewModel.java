package com.jackson.jike.ui.detail;

import androidx.annotation.NonNull;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.jackson.jike.model.Comment;
import com.jackson.jike.ui.AbsViewModel;
import com.jackson.jike.ui.login.UserManager;
import com.jackson.network.ApiResponse;
import com.jackson.network.ApiService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (C), 2015-2020
 * FileName: FeedDetailViewModel
 * Author: Luo
 * Date: 2020/5/12 18:09
 * Description:
 */
public class FeedDetailViewModel extends AbsViewModel<Comment> {
    private long itemId;

    /**
     * 如果服务器接口需要根据列表当中最后一条item数据的相关信息来控制分页的话，用ItemKeyedDataSource
     * 如果服务器接口需要根据传进来的页码来控制分页的话，用PageKeyedDataSource
     *
     * @return DataSource
     */
    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    private class DataSource extends ItemKeyedDataSource<Integer, Comment> {
        /**
         * 加载初始化数据的,此方法在子线程中执行
         *
         * @param params
         * @param callback
         */
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Comment> callback) {
            loadData(0, params.requestedLoadSize, callback);
        }

        /**
         * 向后加载分页数据的,此方法在子线程中执行
         *
         * @param params
         * @param callback
         */
        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Comment> callback) {
            if (params.key > 0) {
                loadData(params.key, params.requestedLoadSize, callback);
            }
        }

        /**
         * 能够向前加载数据的,此方法在子线程中执行
         *
         * @param params
         * @param callback
         */
        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Comment> callback) {
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
        public Integer getKey(@NonNull Comment item) {
            return item.id;
        }
    }

    private void loadData(Integer key, int requestedLoadSize, ItemKeyedDataSource.LoadCallback<Comment> callback) {
        ApiResponse response = ApiService.get("/comment/queryFeedComments")
                .addParam("id", key)
                .addParam("itemId", itemId)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("pageCount", requestedLoadSize)
                .responseType(new TypeReference<ArrayList<Comment>>() {
                }.getType())
                .execute();

        List<Comment> list = response.body == null ? Collections.emptyList() : (List<Comment>) response.body;
        callback.onResult(list);
    }

}
