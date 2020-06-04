package com.jackson.network.cache;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

/**
 * Copyright (C), 2015-2020
 * FileName: CacheDao
 * Author: Luo
 * Date: 2020/3/18 10:57
 * Description: 操作数据库
 */
@Dao
public interface CacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long save(Cache cache);

    @Query("select * from cache where `key`=:key")
    Cache getCache(String key);

    @Delete
    int delete(Cache cache);

    @Update(onConflict = OnConflictStrategy.REPLACE)//插入数据发生冲时的策略
    int uodate(Cache cache);

    @Transaction
    @Query("select * from cache")
    List<Cache> loadAll();

}
