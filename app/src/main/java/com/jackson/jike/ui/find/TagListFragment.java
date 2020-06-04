package com.jackson.jike.ui.find;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.jackson.jike.R;
import com.jackson.jike.model.TagList;
import com.jackson.jike.ui.AbsListFragment;
import com.jackson.jike.ui.MutableItemKeyedDataSource;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.List;

/**
 * Copyright (C), 2015-2020
 * FileName: TagListFragment
 * Author: Luo
 * Date: 2020/6/1 16:04
 * Description:
 */
public class TagListFragment extends AbsListFragment<TagList, TagListViewModel> {

    public static final String KEY_TAG_TYPE = "tag_type";
    private String tagType;

    public static TagListFragment newInstance(String tagType) {

        Bundle args = new Bundle();
        args.putString(KEY_TAG_TYPE, tagType);
        TagListFragment fragment = new TagListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (TextUtils.equals(tagType, "onlyFollow")) {
            mEmptyView.setTitle(getString(R.string.tag_list_no_follow));
            mEmptyView.setButton(getString(R.string.tag_list_no_follow_button), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewModel.getSwitchTabLiveData().setValue(new Object());
                }
            });
        }
        mRecyclerView.removeItemDecorationAt(0);
        mViewModel.setTagType(tagType);
    }

    @Override
    public PagedListAdapter getAdapter() {
        tagType = getArguments().getString(KEY_TAG_TYPE);
        return new TagListAdapter(getContext());
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        final PagedList<TagList> currentList = adapter.getCurrentList();
        long tagId = currentList == null ? 0 : currentList.get(currentList.size() - 1).tagId;
        mViewModel.loadData(tagId, new ItemKeyedDataSource.LoadCallback<TagList>() {
            @Override
            public void onResult(@NonNull List<TagList> data) {
                if (data.size() > 0) {
                    //这里 咱们手动接管 分页数据加载的时候 使用MutableItemKeyedDataSource也是可以的。
                    //由于当且仅当 paging不再帮我们分页的时候，我们才会接管。所以 就不需要ViewModel中创建的DataSource继续工作了，所以使用
                    //MutablePageKeyedDataSource也是可以的
                    MutableItemKeyedDataSource<Long, TagList> dataSource =
                            new MutableItemKeyedDataSource<Long, TagList>(((ItemKeyedDataSource) mViewModel.getDataSource())) {
                                @NonNull
                                @Override
                                public Long getKey(@NonNull TagList item) {
                                    return item.tagId;
                                }
                            };
                    dataSource.data.addAll(currentList);
                    dataSource.data.addAll(data);
                    PagedList pagedList = dataSource.buildNewPagedList(currentList.getConfig());
                    submitList(pagedList);
                }else{
                    finishRefresh(false);
                }
            }
        });
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mViewModel.getDataSource().invalidate();
    }
}
