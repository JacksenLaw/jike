package com.jackson.jike.util;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.jackson.common.AppGlobals;
import com.jackson.jike.model.BottomBar;
import com.jackson.jike.model.Destination;
import com.jackson.jike.model.SofaTab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;

/**
 * Copyright (C), 2015-2020
 * FileName: AppConfig
 * Author: Luo
 * Date: 2020/3/12 20:21
 * Description: json解析工具
 */
public class AppConfig {

    private static HashMap<String, Destination> mDestConfig;
    private static BottomBar mBottomBar;
    private static SofaTab mSofaTab;
    private static SofaTab mFindTab;

    public static HashMap<String, Destination> getDestConfig() {
        if (mDestConfig == null) {
            String content = parseFile("destination.json");

            mDestConfig = JSON.parseObject(content, new TypeReference<HashMap<String, Destination>>() {
            });

        }
        return mDestConfig;
    }

    public static BottomBar getBottomBarConfig() {
        if (mBottomBar == null) {
            String content = parseFile("main_tabs_config.json");

            mBottomBar = JSON.parseObject(content, BottomBar.class);
        }
        return mBottomBar;
    }

    public static SofaTab getSofaTabConfig() {
        if (mSofaTab == null) {
            String content = parseFile("sofa_tabs_config.json");

            mSofaTab = JSON.parseObject(content, SofaTab.class);
            Collections.sort(mSofaTab.tabs, (o1, o2) -> o1.index < o2.index ? -1 : 1);
        }
        return mSofaTab;
    }

    public static SofaTab getFindTabConfig() {
        if (mFindTab == null) {
            String content = parseFile("find_tabs_config.json");

            mFindTab = JSON.parseObject(content, SofaTab.class);
            Collections.sort(mFindTab.tabs, (o1, o2) -> o1.index < o2.index ? -1 : 1);
        }
        return mFindTab;
    }

    private static String parseFile(String fileName) {
        AssetManager assets = AppGlobals.getApplication().getAssets();
        InputStream is = null;
        BufferedReader br = null;
        StringBuilder builder = new StringBuilder();
        try {
            is = assets.open(fileName);
            br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return builder.toString();
    }


}
