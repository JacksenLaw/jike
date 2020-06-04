package com.jackson.network.cache;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.jackson.common.AppGlobals;

/**
 * Copyright (C), 2015-2020
 * FileName: CacheDatabase
 * Author: Luo
 * Date: 2020/3/18 10:23
 * Description: 缓存数据库
 */
//exportSchema = true 会导出一份json文件，文件中包含了数据库在升级、创建表时所作的一些操作
@Database(entities = {Cache.class}, version = 1, exportSchema = true)
public abstract class CacheDatabase extends RoomDatabase {

    private static final CacheDatabase database;

    static {
        //创建一个内存数据库
        //但是这种数据库的数据只存在于内存中，也就是说，进程被杀之后，数据随之丢失
//        Room.inMemoryDatabaseBuilder();
        database = Room.databaseBuilder(AppGlobals.getApplication(), CacheDatabase.class, "ppjoke_cache")
                //允许在主线程进行查询，默认为false
                .allowMainThreadQueries()
                //数据库创建和打开后的回调
//                .addCallback()
                //设置查询的线程池
//                .setQueryExecutor()
//                .openHelperFactory()
                //room的日志模式
//                .setJournalMode()
                //数据库升级异常之后的回滚，会删除所有数据，并重建数据库
//                .fallbackToDestructiveMigration()
                //数据库升级异常之后根据指定版本进行回滚
//                .fallbackToDestructiveMigrationFrom()
//                .addMigrations(CacheDatabase.mMigration)
                .build();

    }

    public abstract CacheDao getCacheDao();

    public static CacheDatabase getDatabase() {
        return database;
    }

    static Migration mMigration = new Migration(1, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("alter table teacher rename to teacher1");
            database.execSQL("alter table teacher add column teagher_age INTEGER not null default 0");
        }
    };

}
