package com.jackson.jike.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

/**
 * Copyright (C), 2015-2020
 * FileName: AbsViewModel
 * Author: Luo
 * Date: 2020/3/26 22:23
 * Description:
 */
public abstract class AbsViewModel<T> extends ViewModel {

    protected PagedList.Config config;
    private DataSource dataSource;
    private LiveData<PagedList<T>> pageData;

    private MutableLiveData<Boolean> boundaryPageData = new MutableLiveData<>();

    public AbsViewModel() {

        config = new PagedList.Config.Builder()
                .setPageSize(10)//分页时需要加载的数量
                .setInitialLoadSizeHint(12)//初始化数据时加载的数量
//                 .setMaxSize(100)//列表数据数量，一般不设置
//                 .setEnablePlaceholders(false)//占位
//                 .setPrefetchDistance(2)//距离底部还有几个item的时候加载下一页数据，不设置即为 setPageSize() 大小
                .build();

        pageData = new LivePagedListBuilder(factory, config)
                .setInitialLoadKey(0)//加载初始化时需要传递的参数
//                .setFetchExecutor()//为pageList提供的异步线程池，有内置
                .setBoundaryCallback(callback)//可以监听到PageList数据加载的状态
                .build();
    }


    public LiveData<PagedList<T>> getPageData() {
        return pageData;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public LiveData<Boolean> getBoundaryPageData() {
        return boundaryPageData;
    }

    //PagedList数据被加载 情况的边界回调callback
    //但 不是每一次分页 都会回调这里，具体请看 ContiguousPagedList#mReceiver#onPageResult
    //deferBoundaryCallbacks
    PagedList.BoundaryCallback<T> callback = new PagedList.BoundaryCallback<T>() {
        @Override
        public void onZeroItemsLoaded() {
            //新提交的PagedList中没有数据
            boundaryPageData.postValue(false);
        }

        @Override
        public void onItemAtFrontLoaded(@NonNull T itemAtFront) {
            //新提交的PagedList中第一条数据被加载到列表上
            boundaryPageData.postValue(true);
        }

        @Override
        public void onItemAtEndLoaded(@NonNull T itemAtEnd) {
            //新提交的PagedList中最后一条数据被加载到列表上
        }
    };

    DataSource.Factory factory = new DataSource.Factory() {
        @NonNull
        @Override
        public DataSource create() {
            if (dataSource == null || dataSource.isInvalid()) {
                dataSource = createDataSource();
            }
            return dataSource;
        }
    };

    /**
     * 如果服务器接口需要根据列表当中最后一条item数据的相关信息来控制分页的话，用ItemKeyedDataSource
     * 如果服务器接口需要根据传进来的页码来控制分页的话，用PageKeyedDataSource
     * @return DataSource
     */
    public abstract DataSource createDataSource();


    //可以在这个方法里 做一些清理 的工作
    @Override
    protected void onCleared() {

    }

}
