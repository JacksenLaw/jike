package com.jackson.network.cache;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Copyright (C), 2015-2020
 * FileName: Cache
 * Author: Luo
 * Date: 2020/3/18 10:45
 * Description: 缓存的表
 */
@Entity(tableName = "cache")
public class Cache implements Serializable {

    @PrimaryKey
    @NonNull
    public String key;
    //@ColumnInfo(name = "_data"),指定该字段在表中的列的名字
    public byte[] data;//将缓存的数据转换为二进制缓存到表中

}
