package com.jackson.jike.ui.find;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.jackson.jike.model.TagList;
import com.jackson.jike.ui.AbsViewModel;
import com.jackson.jike.ui.login.UserManager;
import com.jackson.network.ApiResponse;
import com.jackson.network.ApiService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TagListViewModel extends AbsViewModel<TagList> {

    private String tagType;
    private int offset;
    private AtomicBoolean loadAfter = new AtomicBoolean();//当前是否正在分页
    private MutableLiveData switchTabLiveData = new MutableLiveData();

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public MutableLiveData getSwitchTabLiveData() {
        return switchTabLiveData;
    }

    private class DataSource extends ItemKeyedDataSource<Long, TagList> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<TagList> callback) {
            loadData(0L, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<TagList> callback) {
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<TagList> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Long getKey(@NonNull TagList item) {
            return item.tagId;
        }

        private void loadData(Long key, ItemKeyedDataSource.LoadCallback<TagList> callback) {
            if (key > 0) {
                loadAfter.set(true);
            }
            ApiResponse<List<TagList>> response = ApiService.get("/tag/queryTagList")
                    .addParam("userId", UserManager.get().getUserId())
                    .addParam("tagId", key)
                    .addParam("tagType", tagType)
                    .addParam("pageCount", 10)
                    .addParam("offset", offset)//当前列表上已经展示的数据的数量
                    .responseType(new TypeReference<ArrayList<TagList>>() {
                    }.getType())
                    .execute();

            List<TagList> result = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(result);

            if (key > 0) {
                //分页请求
                loadAfter.set(false);
                offset += result.size();
                ((MutableLiveData) getBoundaryPageData()).postValue(result.size() > 0);
            } else {
                offset = result.size();
            }

        }

    }


    /**
     * @param tagId    分页id
     * @param callback
     */
    public void loadData(long tagId, ItemKeyedDataSource.LoadCallback callback) {
        if (tagId <= 0 || loadAfter.get()) {
            callback.onResult(Collections.emptyList());
            return;
        }

        ArchTaskExecutor.getIOThreadExecutor().execute(() ->
                ((DataSource) getDataSource()).loadData(tagId, callback)
        );

    }

}